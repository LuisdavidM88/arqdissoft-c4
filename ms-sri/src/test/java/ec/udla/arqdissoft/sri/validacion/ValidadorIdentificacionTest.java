package ec.udla.arqdissoft.sri.validacion;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ValidadorIdentificacionTest {
    @Test
    void cedulaConDigitoVerificadorCorrectoEsValida() {
        assertThat(ValidadorIdentificacion.validarCedula("1700000001").valido()).isTrue();
        assertThat(ValidadorIdentificacion.validarCedula("0901234567").valido()).isTrue();
    }

    @Test
    void cedulaConErroresSeRechazaConMotivo() {
        assertThat(ValidadorIdentificacion.validarCedula("1710034066").motivo()).contains("verificador");
        assertThat(ValidadorIdentificacion.validarCedula("9910034065").motivo()).contains("provincia");
        assertThat(ValidadorIdentificacion.validarCedula("17100").motivo()).contains("10 dígitos");
        assertThat(ValidadorIdentificacion.validarCedula("1770034065").motivo()).contains("tercer dígito");
    }

    @Test
    void rucDePersonaNaturalEsCedulaMas001() {
        assertThat(ValidadorIdentificacion.validarRuc("1700000001001").valido()).isTrue();
        assertThat(ValidadorIdentificacion.validarRuc("1700000001000").valido()).isFalse();
        assertThat(ValidadorIdentificacion.validarRuc("1710034066001").valido()).isFalse();
    }

    @Test
    void rucDeSociedadesPasaLaEstructuraParaQueElSriConfirmeElTipo() {
        assertThat(ValidadorIdentificacion.validarRuc("1760013210001").valido()).isTrue();
        assertThat(ValidadorIdentificacion.validarRuc("1790000001001").valido()).isTrue();
        assertThat(ValidadorIdentificacion.validarRuc("1790000002001").valido()).isFalse();
    }

    @Test
    void rucConLongitudIncorrectaSeRechaza() {
        assertThat(ValidadorIdentificacion.validarRuc("170000000100").motivo()).contains("13 dígitos");
        assertThat(ValidadorIdentificacion.validarRuc(null).valido()).isFalse();
    }

    @Test
    void placaSeNormalizaYValida() {
        assertThat(ValidadorIdentificacion.normalizarPlaca(" pba-1234 ")).isEqualTo("PBA1234");
        assertThat(ValidadorIdentificacion.validarPlaca("PBA1234").valido()).isTrue();
        assertThat(ValidadorIdentificacion.validarPlaca("GSA123").valido()).isTrue();
        assertThat(ValidadorIdentificacion.validarPlaca("AB123C").valido()).isTrue();
        assertThat(ValidadorIdentificacion.validarPlaca("12ABC").valido()).isFalse();
        assertThat(ValidadorIdentificacion.validarPlaca("").valido()).isFalse();
    }
}
