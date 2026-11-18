package ec.udla.arqdissoft.ant.cache;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Component
@ConditionalOnProperty(prefix = "cache", name = "tipo", havingValue = "memoria")
public class AlmacenMemoria implements AlmacenCache {
    private final Map<String, String> datos = new ConcurrentHashMap<>();

    @Override
    public Optional<String> leer(String clave) {
        return Optional.ofNullable(datos.get(clave));
    }

    @Override
    public void guardar(String clave, String valor, Duration tiempoDeVida) {
        datos.put(clave, valor);
    }
}
