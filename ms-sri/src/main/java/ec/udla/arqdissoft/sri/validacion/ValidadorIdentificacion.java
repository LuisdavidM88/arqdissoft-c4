package ec.udla.arqdissoft.sri.validacion;

import java.util.Locale;
import java.util.regex.Pattern;

public final class ValidadorIdentificacion {
    private static final Pattern DIEZ_DIGITOS = Pattern.compile("\\d{10}");
    private static final Pattern TRECE_DIGITOS = Pattern.compile("\\d{13}");
    private static final Pattern PLACA_AUTO = Pattern.compile("[A-Z]{3}\\d{3,4}");
    private static final Pattern PLACA_MOTO = Pattern.compile("[A-Z]{2}\\d{3}[A-Z]");

    private static final int[] COEF_SOCIEDAD_PRIVADA = {4, 3, 2, 7, 6, 5, 4, 3, 2};
    private static final int[] COEF_SOCIEDAD_PUBLICA = {3, 2, 7, 6, 5, 4, 3, 2};

    private ValidadorIdentificacion() {
    }

    public record Resultado(boolean valido, String motivo) {
        static Resultado ok() {
            return new Resultado(true, null);
        }

        static Resultado error(String motivo) {
            return new Resultado(false, motivo);
        }
    }

    public static String normalizarNumero(String valor) {
        return valor == null ? "" : valor.replaceAll("[\\s-]", "");
    }

    public static String normalizarPlaca(String valor) {
        return valor == null ? "" : valor.replaceAll("[\\s-]", "").toUpperCase(Locale.ROOT);
    }

    public static Resultado validarCedula(String cedula) {
        if (cedula == null || !DIEZ_DIGITOS.matcher(cedula).matches()) {
            return Resultado.error("La cédula debe tener 10 dígitos numéricos");
        }
        if (!provinciaValida(cedula)) {
            return Resultado.error("Los dos primeros dígitos no corresponden a una provincia del Ecuador");
        }
        if (digito(cedula, 2) >= 6) {
            return Resultado.error("El tercer dígito de una cédula debe ser menor que 6");
        }
        int suma = 0;
        for (int i = 0; i < 9; i++) {
            int producto = digito(cedula, i) * (i % 2 == 0 ? 2 : 1);
            suma += producto > 9 ? producto - 9 : producto;
        }
        int verificador = (10 - suma % 10) % 10;
        if (verificador != digito(cedula, 9)) {
            return Resultado.error("El dígito verificador de la cédula no es correcto");
        }
        return Resultado.ok();
    }

    public static Resultado validarRuc(String ruc) {
        if (ruc == null || !TRECE_DIGITOS.matcher(ruc).matches()) {
            return Resultado.error("El RUC debe tener 13 dígitos numéricos");
        }
        if (!provinciaValida(ruc)) {
            return Resultado.error("Los dos primeros dígitos no corresponden a una provincia del Ecuador");
        }
        int tercero = digito(ruc, 2);
        if (tercero < 6) {
            Resultado cedula = validarCedula(ruc.substring(0, 10));
            if (!cedula.valido()) {
                return Resultado.error("Los 10 primeros dígitos del RUC no forman una cédula válida: " + cedula.motivo());
            }
            return ruc.endsWith("000")
                    ? Resultado.error("Los tres últimos dígitos del RUC no pueden ser 000")
                    : Resultado.ok();
        }
        if (tercero == 6) {
            if (!modulo11Valido(ruc, COEF_SOCIEDAD_PUBLICA)) {
                return Resultado.error("El dígito verificador del RUC no es correcto");
            }
            return ruc.endsWith("0000")
                    ? Resultado.error("Los cuatro últimos dígitos del RUC no pueden ser 0000")
                    : Resultado.ok();
        }
        if (tercero == 9) {
            if (!modulo11Valido(ruc, COEF_SOCIEDAD_PRIVADA)) {
                return Resultado.error("El dígito verificador del RUC no es correcto");
            }
            return ruc.endsWith("000")
                    ? Resultado.error("Los tres últimos dígitos del RUC no pueden ser 000")
                    : Resultado.ok();
        }
        return Resultado.error("El tercer dígito del RUC no es válido");
    }

    public static Resultado validarPlaca(String placa) {
        if (placa == null || placa.isBlank()) {
            return Resultado.error("Ingrese la placa del vehículo");
        }
        if (PLACA_AUTO.matcher(placa).matches() || PLACA_MOTO.matcher(placa).matches()) {
            return Resultado.ok();
        }
        return Resultado.error("La placa debe tener el formato ABC1234 (auto) o AB123C (moto)");
    }

    private static boolean modulo11Valido(String numero, int[] coeficientes) {
        int suma = 0;
        for (int i = 0; i < coeficientes.length; i++) {
            suma += digito(numero, i) * coeficientes[i];
        }
        int residuo = suma % 11;
        int verificador = residuo == 0 ? 0 : 11 - residuo;
        return verificador < 10 && verificador == digito(numero, coeficientes.length);
    }

    private static boolean provinciaValida(String numero) {
        int provincia = Integer.parseInt(numero.substring(0, 2));
        return (provincia >= 1 && provincia <= 24) || provincia == 30;
    }

    private static int digito(String numero, int posicion) {
        return numero.charAt(posicion) - '0';
    }
}
