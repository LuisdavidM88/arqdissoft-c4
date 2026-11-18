package ec.udla.arqdissoft.ant.cliente;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AnalizadorRespuestaAntTest {
    private final AnalizadorRespuestaAnt analizador = new AnalizadorRespuestaAnt(new ObjectMapper());

    @Test
    void leePuntosYCitacionesDeUnaTablaHtml() {
        String html = """
                <html><body>
                  <table><tr><td>Puntos:</td><td>26</td></tr></table>
                  <table>
                    <tr><th>Citación</th><th>Placa</th><th>Infracción</th></tr>
                    <tr><td>12345</td><td>PBA1234</td><td>Exceso de velocidad</td></tr>
                  </table>
                </body></html>
                """;

        ResultadoAnt r = analizador.analizar(html);

        assertThat(r.puntos()).isEqualTo(26);
        assertThat(r.citaciones()).anySatisfy(fila -> assertThat(fila).contains("PBA1234"));
        assertThat(r.origenDatos()).isEqualTo("ANT");
    }

    @Test
    void leeUnaGrillaJson() {
        String json = """
                {"page":1,"total":1,"records":2,"puntos":"28",
                 "rows":[{"id":"1","cell":["111","PBA1234","Multa"]},{"id":"2","cell":["222","PBA1234","Multa"]}]}
                """;

        ResultadoAnt r = analizador.analizar(json);

        assertThat(r.puntos()).isEqualTo(28);
        assertThat(r.totalCitaciones()).isEqualTo(2);
        assertThat(r.citaciones().get(0)).containsExactly("111", "PBA1234", "Multa");
    }

    @Test
    void mensajeSinCitacionesEsUnaRespuestaValida() {
        ResultadoAnt r = analizador.analizar("<html><body><p>No registra citaciones pendientes</p></body></html>");

        assertThat(r.totalCitaciones()).isZero();
    }

    @Test
    void paginaDeErrorNoSeGuardaEnCache() {
        assertThatThrownBy(() -> analizador.analizar("<html><body><h1>502 Bad Gateway</h1></body></html>"))
                .isInstanceOf(AntNoDisponibleException.class);
    }

    @Test
    void respuestaVaciaODesconocidaSeRechaza() {
        assertThatThrownBy(() -> analizador.analizar("   ")).isInstanceOf(AntNoDisponibleException.class);
        assertThatThrownBy(() -> analizador.analizar("<html><body>Bienvenido</body></html>"))
                .isInstanceOf(AntNoDisponibleException.class);
    }
}
