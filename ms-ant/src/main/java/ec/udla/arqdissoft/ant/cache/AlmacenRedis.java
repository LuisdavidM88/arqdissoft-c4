package ec.udla.arqdissoft.ant.cache;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Optional;

@Component
@ConditionalOnProperty(prefix = "cache", name = "tipo", havingValue = "redis", matchIfMissing = true)
public class AlmacenRedis implements AlmacenCache {
    private final StringRedisTemplate redis;

    public AlmacenRedis(StringRedisTemplate redis) {
        this.redis = redis;
    }

    @Override
    public Optional<String> leer(String clave) {
        return Optional.ofNullable(redis.opsForValue().get(clave));
    }

    @Override
    public void guardar(String clave, String valor, Duration tiempoDeVida) {
        redis.opsForValue().set(clave, valor, tiempoDeVida);
    }
}
