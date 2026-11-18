package ec.udla.arqdissoft.ant.config;

import ec.udla.arqdissoft.ant.servicio.CircuitoAnt;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.time.Clock;

@Configuration
public class AntConfig implements WebMvcConfigurer {
    @Value("${app.cors.origenes}")
    private String[] origenesPermitidos;

    @Bean
    public RestClient clienteAnt(AntProperties props) {
        SimpleClientHttpRequestFactory fabrica = new SimpleClientHttpRequestFactory();
        fabrica.setConnectTimeout(props.timeoutMs());
        fabrica.setReadTimeout(props.timeoutMs());
        return RestClient.builder()
                .requestFactory(fabrica)
                .defaultHeader(HttpHeaders.USER_AGENT, "Mozilla/5.0 (ArqDisSoft-C4 ms-ant)")
                .defaultHeader(HttpHeaders.ACCEPT, "text/html, application/json, */*")
                .build();
    }

    @Bean
    public CircuitoAnt circuitoAnt(AntProperties props) {
        return new CircuitoAnt(props.fallosParaAbrirCircuito(), props.circuitoAbierto());
    }

    @Bean
    public Clock reloj() {
        return Clock.systemUTC();
    }

    @Override
    public void addCorsMappings(CorsRegistry registro) {
        registro.addMapping("/api/**")
                .allowedOrigins(origenesPermitidos)
                .allowedMethods("GET", "PUT", "OPTIONS");
    }

    @Bean
    public OpenAPI documentacion() {
        return new OpenAPI().info(new Info()
                .title("ms-ant · Puntos de licencia con caché")
                .version("v1")
                .description("Consulta los puntos de licencia en la ANT. Como la ANT tiene baja disponibilidad, "
                        + "cada respuesta correcta se guarda en Redis (nube) y, si la ANT falla, se responde "
                        + "con el último dato bueno indicando que no es reciente."));
    }
}
