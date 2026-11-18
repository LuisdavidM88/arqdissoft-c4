package ec.udla.arqdissoft.ant.validacion;

import java.util.Locale;
import java.util.regex.Pattern;

public final class ValidadorIdentificacion {
    private static final Pattern DIEZ_DIGITOS = Pattern.compile("\\d{10}");
    private static final Pattern PLACA_AUTO = Pattern.compile("[A-Z]{3}\\d{3,4}");
    private static final Pattern PLACA_MOTO = Pattern.compile("[A-Z]{2}\\d{3}[A-Z]");

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
        int provincia = Integer.parseInt(cedula.substring(0, 2));
        if (!((provincia >= 1 && provincia <= 24) || provincia == 30)) {
            return Resultado.error("Los dos primeros dígitos no corresponden a una provincia del Ecuador");
        }
        if (cedula.charAt(2) - '0' >= 6) {
            return Resultado.error("El tercer dígito de una cédula debe ser menor que 6");
        }
        int suma = 0;
        for (int i = 0; i < 9; i++) {
            int producto = (cedula.charAt(i) - '0') * (i % 2 == 0 ? 2 : 1);
            suma += producto > 9 ? producto - 9 : producto;
        }
        int verificador = (10 - suma % 10) % 10;
        if (verificador != cedula.charAt(9) - '0') {
            return Resultado.error("El dígito verificador de la cédula no es correcto");
        }
        return Resultado.ok();
    }

    public static Resultado validarPlaca(String placa) {
        if (PLACA_AUTO.matcher(placa).matches() || PLACA_MOTO.matcher(placa).matches()) {
            return Resultado.ok();
        }
        return Resultado.error("La placa debe tener el formato ABC1234 (auto) o AB123C (moto)");
    }
}
