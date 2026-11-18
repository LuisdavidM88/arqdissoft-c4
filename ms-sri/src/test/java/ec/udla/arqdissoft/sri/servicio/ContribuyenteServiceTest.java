package ec.udla.arqdissoft.sri.servicio;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import ec.udla.arqdissoft.sri.cliente.PuertoSri;
import ec.udla.arqdissoft.sri.dominio.Contribuyente;
import ec.udla.arqdissoft.sri.error.ApiException;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ContribuyenteServiceTest {
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private static final String PERSONA_NATURAL = """
            [{"numeroRuc":"1700000001001","razonSocial":"PEREZ LOPEZ JUAN CARLOS",
              "estadoContribuyenteRuc":"ACTIVO","actividadEconomicaPrincipal":"VENTA AL POR MENOR",
              "tipoContribuyente":"PERSONA NATURAL","regimen":"RIMPE","obligadoLlevarContabilidad":"NO",
              "informacionFechasContribuyente":{"fechaInicioActividades":"2015-03-10 00:00:00.0"}}]
            """;

    private static final String SOCIEDAD = """
            [{"numeroRuc":"1760013210001","razonSocial":"SERVICIO DE RENTAS INTERNAS",
              "estadoContribuyenteRuc":"ACTIVO","tipoContribuyente":"SOCIEDAD","regimen":"GENERAL"}]
            """;

    private static final class SriFalso implements PuertoSri {
        boolean existe = true;
        String cuerpo = PERSONA_NATURAL;
        int llamadas = 0;

        @Override
        public boolean existeContribuyente(String ruc) {
            llamadas++;
            return existe;
        }

        @Override
        public Optional<JsonNode> obtenerContribuyente(String ruc) {
            try {
                return Optional.of(MAPPER.readTree(cuerpo).get(0));
            } catch (Exception e) {
                throw new IllegalStateException(e);
            }
        }

        @Override
        public Optional<JsonNode> obtenerVehiculo(String placa) {
            return Optional.empty();
        }
    }

    @Test
    void personaNaturalDevuelveSusDatos() {
        ContribuyenteService servicio = new ContribuyenteService(new SriFalso());

        Contribuyente c = servicio.consultar("1700000001001");

        assertThat(c.nombre()).isEqualTo("PEREZ LOPEZ JUAN CARLOS");
        assertThat(c.cedula()).isEqualTo("1700000001");
        assertThat(c.estado()).isEqualTo("ACTIVO");
        assertThat(c.fechaInicioActividades()).isEqualTo("2015-03-10");
    }

    @Test
    void sociedadSeRechazaCon422() {
        SriFalso sri = new SriFalso();
        sri.cuerpo = SOCIEDAD;

        assertThatThrownBy(() -> new ContribuyenteService(sri).consultar("1760013210001"))
                .isInstanceOfSatisfying(ApiException.class, e -> {
                    assertThat(e.getStatus().value()).isEqualTo(422);
                    assertThat(e.getCodigo()).isEqualTo("NO_ES_PERSONA_NATURAL");
                    assertThat(e.getDetalle()).contains("SOCIEDAD");
                });
    }

    @Test
    void rucQueNoEsContribuyenteDevuelve404() {
        SriFalso sri = new SriFalso();
        sri.existe = false;

        assertThatThrownBy(() -> new ContribuyenteService(sri).consultar("1700000001001"))
                .isInstanceOfSatisfying(ApiException.class, e -> {
                    assertThat(e.getStatus().value()).isEqualTo(404);
                    assertThat(e.getCodigo()).isEqualTo("NO_ES_CONTRIBUYENTE");
                });
    }

    @Test
    void rucInvalidoNoLlegaAlSri() {
        SriFalso sri = new SriFalso();

        assertThatThrownBy(() -> new ContribuyenteService(sri).consultar("123"))
                .isInstanceOfSatisfying(ApiException.class, e -> {
                    assertThat(e.getStatus().value()).isEqualTo(400);
                    assertThat(e.getCodigo()).isEqualTo("RUC_INVALIDO");
                });
        assertThat(sri.llamadas).isZero();
    }
}
