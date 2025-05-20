package model;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.List;

public class ModeloTablaProductos extends AbstractTableModel {
    private final String[] columnas = {"ID", "Nombre", "Marca", "Tamaño", "Precio", "Existencias", "Categoría",
            "Imagen"};
    private List<Producto> datos = new ArrayList<>();
    private List<Producto> lista;

    public ModeloTablaProductos(List<Producto> productos) {
        this.lista = productos;
    }

    public void setProductos(List<Producto> productos) {
        this.datos = productos;
        fireTableDataChanged();
    }

    public Producto getProductoEn(int fila) {
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
        Producto p = datos.get(fila);
        switch (col) {
            case 0:
                return p.getId();
            case 1:
                return p.getNombre();
            case 2:
                return p.getMarca();
            case 3:
                return p.getTamano();
            case 4:
                return String.format("%.2f", p.getPrecio());
            case 5:
                return p.getExistencias();
            case 6:
                return p.getCategoria();
            case 7:
                // devolvemos un ImageIcon si hay ruta, o null
                String ruta = p.getRutaImagen();
                if (ruta != null && !ruta.isEmpty()) {
                    ImageIcon ico = new ImageIcon(ruta);
                    // escalamos suavemente a 80×80
                    return new ImageIcon(ico.getImage().getScaledInstance(80, 80, java.awt.Image.SCALE_SMOOTH));
                } else {
                    return null;
                }
            default:
                return null;
        }
    }

    @Override
    public Class<?> getColumnClass(int col) {
        if (col == 7) {
            return ImageIcon.class;
        }
        return super.getColumnClass(col);
    }
}
