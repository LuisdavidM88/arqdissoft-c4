package ec.udla.arqdissoft.ant.servicio;

import java.time.Duration;
import java.time.Instant;

public class CircuitoAnt {
    private final int fallosParaAbrir;
    private final Duration tiempoAbierto;
    private int fallosSeguidos;
    private Instant abiertoHasta;

    public CircuitoAnt(int fallosParaAbrir, Duration tiempoAbierto) {
        this.fallosParaAbrir = fallosParaAbrir;
        this.tiempoAbierto = tiempoAbierto;
    }

    public synchronized boolean permiteLlamar(Instant ahora) {
        return abiertoHasta == null || !ahora.isBefore(abiertoHasta);
    }

    public synchronized void registrarExito() {
        fallosSeguidos = 0;
        abiertoHasta = null;
    }

    public synchronized void registrarFallo(Instant ahora) {
        fallosSeguidos++;
        boolean estabaProbando = abiertoHasta != null;
        if (estabaProbando || fallosSeguidos >= fallosParaAbrir) {
            abiertoHasta = ahora.plus(tiempoAbierto);
            fallosSeguidos = 0;
        }
    }

    public synchronized Instant abiertoHasta() {
        return abiertoHasta;
    }

    public synchronized void reiniciar() {
        fallosSeguidos = 0;
        abiertoHasta = null;
    }
}
