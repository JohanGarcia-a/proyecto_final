package model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;


public class Venta {
    private int id;
    private String folio;                    // <-- Nuevo
    private LocalDateTime fecha;
    private double total;
    private final List<LineaVenta> lineas = new ArrayList<>();
    private double pago;
    private double cambio;

    public Venta() {
        this.fecha = LocalDateTime.now();
        this.folio = "";
    }

    public void agregarLinea(LineaVenta linea) {
        lineas.add(linea);
        total += linea.getPrecio() * linea.getCantidad();
    }


    public void recalcularTotal() {
        total = 0;
        for (LineaVenta linea : lineas) {
            total += linea.getPrecio() * linea.getCantidad();
        }
    }


    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getFolio() {
        return folio;
    }

    public void setFolio(String folio) {
        this.folio = folio;
    }

    public LocalDateTime getFecha() {
        return fecha;
    }

    public void setFecha(LocalDateTime fecha) {
        this.fecha = fecha;
    }

    public double getTotal() {
        return total;
    }

    public void setTotal(double total) {
        this.total = total;
    }

    public List<LineaVenta> getLineas() {
        return lineas;
    }

    public double getPago() {
        return pago;
    }

    public void setPago(double pago) {
        this.pago = pago;
    }

    public double getCambio() {
        return cambio;
    }

    public void setCambio(double cambio) {
        this.cambio = cambio;
    }
}
