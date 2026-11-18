package ec.udla.arqdissoft.ant.servicio;

import com.fasterxml.jackson.databind.ObjectMapper;
import ec.udla.arqdissoft.ant.cache.AlmacenMemoria;
import ec.udla.arqdissoft.ant.cliente.AntNoDisponibleException;
import ec.udla.arqdissoft.ant.cliente.ClienteAnt;
import ec.udla.arqdissoft.ant.cliente.ResultadoAnt;
import ec.udla.arqdissoft.ant.config.AntProperties;
import ec.udla.arqdissoft.ant.error.ApiException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PuntosLicenciaServiceTest {
    private static final String CEDULA = "1700000001";
    private static final String PLACA = "PBA1234";

    private static final class RelojMovible extends Clock {
        private Instant ahora = Instant.parse("2026-11-17T15:00:00Z");

        void adelantar(Duration d) {
            ahora = ahora.plus(d);
        }

        @Override
        public ZoneId getZone() {
            return ZoneOffset.UTC;
        }

        @Override
        public Clock withZone(ZoneId zone) {
            return this;
        }

        @Override
        public Instant instant() {
            return ahora;
        }
    }

    private static final class AntFalsa implements ClienteAnt {
        boolean caida = false;
        int llamadas = 0;

        @Override
        public ResultadoAnt consultar(String cedula, String placa) {
            llamadas++;
            if (caida) {
                throw new AntNoDisponibleException("La ANT respondió con error HTTP 502");
            }
            return new ResultadoAnt(26, 1, List.of(List.of("C-001", placa, "Exceso de velocidad")), ResultadoAnt.ORIGEN_REAL);
        }
    }

    private AntFalsa ant;
    private RelojMovible reloj;
    private SimulacionCaida simulacion;
    private PuntosLicenciaService servicio;

    @BeforeEach
    void preparar() {
        AntProperties props = new AntProperties("real", "url", 5000,
                Duration.ofHours(1), Duration.ofDays(7), 3, Duration.ofSeconds(60), false);
        ant = new AntFalsa();
        reloj = new RelojMovible();
        simulacion = new SimulacionCaida(props);
        servicio = new PuntosLicenciaService(ant, new AlmacenMemoria(), new ObjectMapper(), reloj, props,
                new CircuitoAnt(3, Duration.ofSeconds(60)), simulacion);
    }

    @Test
    void primeraConsultaVaALaAntYSeGuarda() {
        RespuestaPuntos r = servicio.consultar(CEDULA, PLACA);

        assertThat(r.fuente()).isEqualTo("ANT");
        assertThat(r.puntos()).isEqualTo(26);
        assertThat(r.esReciente()).isTrue();
        assertThat(r.aviso()).isNull();
        assertThat(ant.llamadas).isEqualTo(1);
    }

    @Test
    void dentroDeUnaHoraRespondeDesdeCacheSinLlamarALaAnt() {
        servicio.consultar(CEDULA, PLACA);
        reloj.adelantar(Duration.ofMinutes(30));

        RespuestaPuntos r = servicio.consultar(CEDULA, PLACA);

        assertThat(r.fuente()).isEqualTo("CACHE");
        assertThat(r.esReciente()).isTrue();
        assertThat(r.edadMinutos()).isEqualTo(30);
        assertThat(ant.llamadas).isEqualTo(1);
    }

    @Test
    void siLaAntFallaRespondeConElUltimoDatoBuenoYAvisa() {
        servicio.consultar(CEDULA, PLACA);
        reloj.adelantar(Duration.ofHours(3));
        ant.caida = true;

        RespuestaPuntos r = servicio.consultar(CEDULA, PLACA);

        assertThat(r.fuente()).isEqualTo("CACHE_RESPALDO");
        assertThat(r.puntos()).isEqualTo(26);
        assertThat(r.esReciente()).isFalse();
        assertThat(r.aviso()).contains("La ANT no responde").contains("17/11/2026 10:00");
        assertThat(r.edadMinutos()).isEqualTo(180);
    }

    @Test
    void siLaAntFallaYNoHayCacheResponde503() {
        ant.caida = true;

        assertThatThrownBy(() -> servicio.consultar(CEDULA, PLACA))
                .isInstanceOfSatisfying(ApiException.class, e -> {
                    assertThat(e.getStatus().value()).isEqualTo(503);
                    assertThat(e.getCodigo()).isEqualTo("ANT_NO_DISPONIBLE");
                });
    }

    @Test
    void unDatoDeMasDeSieteDiasYaNoSeUsa() {
        servicio.consultar(CEDULA, PLACA);
        reloj.adelantar(Duration.ofDays(8));
        ant.caida = true;

        assertThatThrownBy(() -> servicio.consultar(CEDULA, PLACA))
                .isInstanceOf(ApiException.class);
    }

    @Test
    void trasTresFallosSeguidosElCircuitoDejaDeLlamarALaAnt() {
        servicio.consultar(CEDULA, PLACA);
        reloj.adelantar(Duration.ofHours(2));
        ant.caida = true;
        int llamadasAntes = ant.llamadas;

        for (int i = 0; i < 5; i++) {
            servicio.consultar(CEDULA, PLACA);
        }

        assertThat(ant.llamadas - llamadasAntes).isEqualTo(3);
    }

    @Test
    void laCaidaSimuladaUsaElRespaldo() {
        servicio.consultar(CEDULA, PLACA);
        reloj.adelantar(Duration.ofHours(2));
        simulacion.cambiar(true);

        RespuestaPuntos r = servicio.consultar(CEDULA, PLACA);

        assertThat(r.fuente()).isEqualTo("CACHE_RESPALDO");
        assertThat(r.esReciente()).isFalse();
    }

    @Test
    void actualizarFuerzaLaConsultaYSiLaAntFallaUsaElRespaldo() {
        servicio.consultar(CEDULA, PLACA);
        reloj.adelantar(Duration.ofMinutes(10));
        ant.caida = true;

        RespuestaPuntos r = servicio.consultar(CEDULA, PLACA, true);

        assertThat(ant.llamadas).isEqualTo(2);
        assertThat(r.fuente()).isEqualTo("CACHE_RESPALDO");
        assertThat(r.esReciente()).isFalse();
    }

    @Test
    void cedulaInvalidaSeRechazaSinConsultarLaAnt() {
        assertThatThrownBy(() -> servicio.consultar("1234567890", PLACA))
                .isInstanceOfSatisfying(ApiException.class, e -> {
                    assertThat(e.getStatus().value()).isEqualTo(400);
                    assertThat(e.getCodigo()).isEqualTo("CEDULA_INVALIDA");
                });
        assertThat(ant.llamadas).isZero();
    }
}
