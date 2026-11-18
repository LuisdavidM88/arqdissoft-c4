package ec.udla.arqdissoft.sri.config;

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

@Configuration
public class HttpConfig implements WebMvcConfigurer {
    @Value("${app.cors.origenes}")
    private String[] origenesPermitidos;

    @Bean
    public RestClient clienteSri(@Value("${sri.timeout-ms}") int timeoutMs) {
        SimpleClientHttpRequestFactory fabrica = new SimpleClientHttpRequestFactory();
        fabrica.setConnectTimeout(timeoutMs);
        fabrica.setReadTimeout(timeoutMs);
        return RestClient.builder()
                .requestFactory(fabrica)
                .defaultHeader(HttpHeaders.USER_AGENT, "Mozilla/5.0 (ArqDisSoft-C4 ms-sri)")
                .defaultHeader(HttpHeaders.ACCEPT, "application/json, text/plain, */*")
                .build();
    }

    @Override
    public void addCorsMappings(CorsRegistry registro) {
        registro.addMapping("/api/**")
                .allowedOrigins(origenesPermitidos)
                .allowedMethods("GET", "OPTIONS");
    }

    @Bean
    public OpenAPI documentacion() {
        return new OpenAPI().info(new Info()
                .title("ms-sri · Consulta de contribuyentes y vehículos")
                .version("v1")
                .description("Verifica en el SRI que el RUC sea de un contribuyente persona natural y consulta los datos de su vehículo."));
    }
}
