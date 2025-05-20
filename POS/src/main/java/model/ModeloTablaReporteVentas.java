package model;

import javax.swing.table.AbstractTableModel;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class ModeloTablaReporteVentas extends AbstractTableModel {
    private final String[] columnas = {"ID", "Fecha", "Subtotal", "IVA", "Total"};
    private List<Venta> datos = new ArrayList<>();
    private final DateTimeFormatter formateador = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    public void setVentas(List<Venta> ventas) {
        this.datos = ventas;
        fireTableDataChanged();
    }

    public List<Venta> getVentas() {
        return datos;
    }

    @Override
    public int getRowCount() {
        return datos.size();
    }

    @Override
    public int getColumnCount() {
        return columnas.length;
    }

    @Override
    public String getColumnName(int columna) {
        return columnas[columna];
    }

    @Override
    public Object getValueAt(int fila, int columna) {
        Venta v = datos.get(fila);
        double subtotal = v.getTotal();
        double iva = subtotal * 0.16;
        return switch (columna) {
            case 0 -> v.getId();
            case 1 -> v.getFecha().format(formateador);
            case 2 -> String.format("%.2f", subtotal);
            case 3 -> String.format("%.2f", iva);
            case 4 -> String.format("%.2f", subtotal + iva);
            default -> null;
        };
    }
}
