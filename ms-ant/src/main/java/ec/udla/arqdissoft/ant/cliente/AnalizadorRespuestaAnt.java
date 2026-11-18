package ec.udla.arqdissoft.ant.cliente;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class AnalizadorRespuestaAnt {
    private static final Pattern PUNTOS = Pattern.compile(
            "(?i)puntos?(?:\\s+de\\s+(?:la\\s+)?licencia)?\\s*[:=]?\\s*(\\d{1,2})\\b");
    private static final Pattern NUMERO = Pattern.compile("\\d{1,2}");
    private static final Pattern SIN_REGISTROS = Pattern.compile(
            "(?i)no\\s+(registra|existen|tiene|se\\s+encontraron|hay)\\s+(citaciones|registros|infracciones|resultados|datos)");
    private static final Pattern PAGINA_ERROR = Pattern.compile(
            "(?i)(bad gateway|service unavailable|gateway time-?out|internal server error|error 50\\d)");
    private static final int MAXIMO_PUNTOS = 30;
    private static final int MAXIMO_FILAS = 50;

    private final ObjectMapper mapper;

    public AnalizadorRespuestaAnt(ObjectMapper mapper) {
        this.mapper = mapper;
    }

    public ResultadoAnt analizar(String cuerpo) {
        if (cuerpo == null || cuerpo.isBlank()) {
            throw new AntNoDisponibleException("La ANT devolvió una respuesta vacía");
        }
        String texto = cuerpo.trim();
        if (texto.startsWith("{") || texto.startsWith("[")) {
            return desdeJson(texto);
        }
        return desdeHtml(texto);
    }

    private ResultadoAnt desdeJson(String texto) {
        JsonNode raiz;
        try {
            raiz = mapper.readTree(texto);
        } catch (JsonProcessingException e) {
            throw new AntNoDisponibleException("La ANT devolvió un JSON no válido");
        }
        Integer puntos = buscarPuntos(raiz);
        JsonNode filasJson = raiz.isArray() ? raiz : raiz.path("rows");
        List<List<String>> filas = new ArrayList<>();
        if (filasJson.isArray()) {
            for (JsonNode fila : filasJson) {
                if (filas.size() >= MAXIMO_FILAS) {
                    break;
                }
                JsonNode celdas = fila.has("cell") ? fila.get("cell") : fila;
                List<String> valores = new ArrayList<>();
                if (celdas.isContainerNode()) {
                    celdas.forEach(celda -> valores.add(celda.asText()));
                } else {
                    valores.add(celdas.asText());
                }
                filas.add(valores);
            }
        }
        if (puntos == null && !filasJson.isArray()) {
            throw new AntNoDisponibleException("La ANT devolvió un JSON con un formato no reconocido");
        }
        int total = raiz.path("records").asInt(filasJson.isArray() ? filasJson.size() : 0);
        return new ResultadoAnt(puntos, total, filas, ResultadoAnt.ORIGEN_REAL);
    }

    private ResultadoAnt desdeHtml(String html) {
        Document documento = Jsoup.parse(html);
        String texto = documento.text();
        if (PAGINA_ERROR.matcher(texto).find()) {
            throw new AntNoDisponibleException("La ANT devolvió una página de error: " + resumir(texto));
        }

        Integer puntos = null;
        Matcher m = PUNTOS.matcher(texto);
        while (m.find() && puntos == null) {
            int valor = Integer.parseInt(m.group(1));
            if (valor <= MAXIMO_PUNTOS) {
                puntos = valor;
            }
        }

        List<List<String>> filas = new ArrayList<>();
        for (Element tr : documento.select("tr")) {
            agregarFila(filas, tr.children().stream()
                    .filter(celda -> celda.tagName().equalsIgnoreCase("td"))
                    .map(Element::text)
                    .toList());
        }
        for (Element row : documento.select("row")) {
            agregarFila(filas, row.select("cell").eachText());
        }

        boolean sinRegistros = SIN_REGISTROS.matcher(texto).find();
        if (puntos == null && filas.isEmpty() && !sinRegistros) {
            throw new AntNoDisponibleException("No se reconoció el formato de la respuesta de la ANT: " + resumir(texto));
        }
        int total = filas.size();
        return new ResultadoAnt(puntos, total, filas.subList(0, Math.min(total, MAXIMO_FILAS)), ResultadoAnt.ORIGEN_REAL);
    }

    private static void agregarFila(List<List<String>> filas, List<String> celdas) {
        if (!celdas.isEmpty() && celdas.stream().anyMatch(c -> !c.isBlank())) {
            filas.add(new ArrayList<>(celdas));
        }
    }

    private Integer buscarPuntos(JsonNode nodo) {
        if (nodo.isObject()) {
            for (Map.Entry<String, JsonNode> campo : nodo.properties()) {
                if (campo.getKey().toLowerCase(Locale.ROOT).contains("punto")) {
                    Integer valor = aEntero(campo.getValue());
                    if (valor != null) {
                        return valor;
                    }
                }
                Integer interno = buscarPuntos(campo.getValue());
                if (interno != null) {
                    return interno;
                }
            }
        } else if (nodo.isArray()) {
            for (JsonNode hijo : nodo) {
                Integer interno = buscarPuntos(hijo);
                if (interno != null) {
                    return interno;
                }
            }
        }
        return null;
    }

    private static Integer aEntero(JsonNode valor) {
        Integer numero = null;
        if (valor.isNumber()) {
            numero = valor.asInt();
        } else if (valor.isTextual()) {
            Matcher m = NUMERO.matcher(valor.asText());
            if (m.find()) {
                numero = Integer.parseInt(m.group());
            }
        }
        return numero != null && numero >= 0 && numero <= MAXIMO_PUNTOS ? numero : null;
    }

    private static String resumir(String texto) {
        return texto.length() > 120 ? texto.substring(0, 120) + "…" : texto;
    }
}
