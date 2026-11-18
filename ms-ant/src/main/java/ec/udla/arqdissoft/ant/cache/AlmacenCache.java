package ec.udla.arqdissoft.ant.cache;

import java.time.Duration;
import java.util.Optional;

public interface AlmacenCache {
    Optional<String> leer(String clave);

    void guardar(String clave, String valor, Duration tiempoDeVida);
}
