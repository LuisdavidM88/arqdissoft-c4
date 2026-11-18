package ec.udla.arqdissoft.ant.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@ConfigurationProperties(prefix = "ant")
public record AntProperties(
        String modo,
        String url,
        int timeoutMs,
        Duration frescura,
        Duration vigencia,
        int fallosParaAbrirCircuito,
        Duration circuitoAbierto,
        boolean simularCaida) {
}
