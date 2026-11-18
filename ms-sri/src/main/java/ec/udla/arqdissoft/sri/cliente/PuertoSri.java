package ec.udla.arqdissoft.sri.cliente;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.Optional;

public interface PuertoSri {
    boolean existeContribuyente(String ruc);

    Optional<JsonNode> obtenerContribuyente(String ruc);

    Optional<JsonNode> obtenerVehiculo(String placa);
}
