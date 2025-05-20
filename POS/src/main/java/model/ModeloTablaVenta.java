package model;

import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.List;

public class ModeloTablaVenta extends AbstractTableModel {
    private final String[] columnas = {"Nombre", "Marca", "Tamaño", "Cantidad", "Precio", "Subtotal"};
    private List<LineaVenta> datos = new ArrayList<>();

    public void setLineas(List<LineaVenta> lineas) {
        this.datos = lineas;
        fireTableDataChanged();
    }

    public LineaVenta getLineaEn(int fila) {
        return datos.get(fila);
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
    public String getColumnName(int col) {
        return columnas[col];
    }

    @Override
    public Object getValueAt(int fila, int col) {
        LineaVenta linea = datos.get(fila);
        switch (col) {
            case 0:
                return linea.getProducto().getNombre();
            case 1:
                return linea.getProducto().getMarca();
            case 2:
                return linea.getProducto().getTamano();
            case 3:
                return linea.getCantidad();
            case 4:
                return String.format("%.2f", linea.getPrecio());
            case 5:
                return String.format("%.2f", linea.getPrecio() * linea.getCantidad());
            default:
                return null;
        }
    }
}
