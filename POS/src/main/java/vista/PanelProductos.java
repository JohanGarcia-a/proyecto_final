package vista;

import model.ModeloTablaProductos;
import model.Producto;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableColumn;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Panel de gestión de productos con validación de stock.
 */
public class PanelProductos extends JPanel {
    // Campos del formulario
    public final JTextField txtId = new JTextField(4);
    public final JTextField txtNombre = new JTextField(12);
    public final JComboBox<String> cbMarca = new JComboBox<>();
    public final JTextField txtTamano = new JTextField(6);
    public final JSpinner spnPrecio = new JSpinner(new SpinnerNumberModel(0.0, 0.0, 99999.0, 0.5));
    public final JSpinner spnExistencias = new JSpinner(new SpinnerNumberModel(0, 0, 10000, 1));
    public final JComboBox<String> cbCategoria = new JComboBox<>();
    public final JTextField txtImagen = new JTextField(20);
    public final JButton btnSeleccionarImagen = new JButton("Seleccionar...");
    public final JLabel lblPreview = new JLabel();
    public final JButton btnAgregar = new JButton("Agregar");
    public final JButton btnActualizar = new JButton("Actualizar");
    public final JButton btnEliminar = new JButton("Eliminar");
    public final JButton btnLimpiar = new JButton("Limpiar");
    public final JTable tblProductos;

    // Límites de stock configurables
    private int stockMinLimit = 0;
    private int stockMaxLimit = Integer.MAX_VALUE;

    public PanelProductos() {
        setLayout(new BorderLayout(5, 5));

        // ------------ FORMULARIO ------------
        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        form.setBackground(new Color(250, 235, 215));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 4, 4, 4);
        gbc.anchor = GridBagConstraints.WEST;
        int y = 0;

        // ID
        gbc.gridx = 0;
        gbc.gridy = y;
        form.add(new JLabel("ID:"), gbc);
        gbc.gridx = 1;
        txtId.setEnabled(false);
        form.add(txtId, gbc);

        // Nombre
        gbc.gridx = 0;
        gbc.gridy = ++y;
        form.add(new JLabel("Nombre:"), gbc);
        gbc.gridx = 1;
        form.add(txtNombre, gbc);

        // Marca
        gbc.gridx = 0;
        gbc.gridy = ++y;
        form.add(new JLabel("Marca:"), gbc);
        gbc.gridx = 1;
        cbMarca.setBackground(new Color(255, 222, 173));
        cbMarca.setEditable(true);
        form.add(cbMarca, gbc);

        // Tamaño
        gbc.gridx = 0;
        gbc.gridy = ++y;
        form.add(new JLabel("Tamaño:"), gbc);
        gbc.gridx = 1;
        form.add(txtTamano, gbc);

        // Precio
        gbc.gridx = 0;
        gbc.gridy = ++y;
        form.add(new JLabel("Precio:"), gbc);
        gbc.gridx = 1;
        form.add(spnPrecio, gbc);

        // Existencias
        gbc.gridx = 0;
        gbc.gridy = ++y;
        form.add(new JLabel("Existencias:"), gbc);
        gbc.gridx = 1;
        spnExistencias.setBackground(new Color(255, 222, 173));
        // Validación de límites
        spnExistencias.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                int val = (Integer) spnExistencias.getValue();
                if (val < stockMinLimit) spnExistencias.setValue(stockMinLimit);
                else if (val > stockMaxLimit) spnExistencias.setValue(stockMaxLimit);
            }
        });
        form.add(spnExistencias, gbc);

        // Categoría
        gbc.gridx = 0;
        gbc.gridy = ++y;
        form.add(new JLabel("Categoría:"), gbc);
        gbc.gridx = 1;
        cbCategoria.setBackground(new Color(255, 222, 173));
        cbCategoria.setEditable(true);
        form.add(cbCategoria, gbc);

        // Imagen
        gbc.gridx = 0;
        gbc.gridy = ++y;
        form.add(new JLabel("Imagen:"), gbc);
        gbc.gridx = 1;
        JPanel pnlImg = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        pnlImg.add(txtImagen);
        btnSeleccionarImagen.setBackground(new Color(255, 222, 173));
        pnlImg.add(btnSeleccionarImagen);
        form.add(pnlImg, gbc);

        // Preview
        gbc.gridx = 2;
        lblPreview.setPreferredSize(new Dimension(80, 80));
        lblPreview.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        form.add(lblPreview, gbc);

        // Botones de acción
        gbc.gridx = 0;
        gbc.gridy = ++y;
        gbc.gridwidth = 3;
        JPanel pnlBtns = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        btnAgregar.setBackground(Color.GREEN);
        pnlBtns.add(btnAgregar);
        btnActualizar.setBackground(Color.YELLOW);
        pnlBtns.add(btnActualizar);
        btnEliminar.setBackground(Color.RED);
        pnlBtns.add(btnEliminar);
        btnLimpiar.setBackground(new Color(173, 216, 230));
        pnlBtns.add(btnLimpiar);
        form.add(pnlBtns, gbc);

        add(form, BorderLayout.NORTH);

        // ------------ TABLA DE PRODUCTOS ------------
        tblProductos = new JTable(new ModeloTablaProductos(new ArrayList<>()));
        tblProductos.setRowHeight(80);
        tblProductos.setBackground(new Color(173, 216, 230));

        // Ajuste columna imagen
        TableColumn colImagen = tblProductos.getColumnModel().getColumn(7);
        colImagen.setPreferredWidth(100);
        colImagen.setMaxWidth(100);
        colImagen.setMinWidth(100);

        // Colorear columna existencias
        TableColumn colExist = tblProductos.getColumnModel().getColumn(5);
        colExist.setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                                                           boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                if (!isSelected) {
                    int exist;
                    try {
                        exist = Integer.parseInt(value.toString());
                    } catch (Exception ex) {
                        exist = 0;
                    }
                    if (exist <= stockMinLimit) c.setBackground(new Color(255, 105, 97));
                    else if (exist <= stockMaxLimit / 2) c.setBackground(new Color(253, 253, 150));
                    else c.setBackground(new Color(144, 238, 144));
                } else {
                    c.setBackground(table.getSelectionBackground());
                }
                return c;
            }
        });

        add(new JScrollPane(tblProductos), BorderLayout.CENTER);
    }

    /**
     * Recarga la tabla con los productos dados.
     */
    public void recargarTabla(List<Producto> productos) {
        ModeloTablaProductos model = (ModeloTablaProductos) tblProductos.getModel();
        model.setProductos(productos);
        tblProductos.revalidate();
        tblProductos.repaint();
    }

    /**
     * Habilita/deshabilita entrada manual de ID.
     */
    public void enableManualId(boolean enabled) {
        txtId.setEnabled(enabled);
    }

    /**
     * Ajusta límites de stock para el spinner y render.
     */
    public void setStockLimits(int min, int max) {
        this.stockMinLimit = min;
        this.stockMaxLimit = max;
        SpinnerNumberModel model = (SpinnerNumberModel) spnExistencias.getModel();
        model.setMinimum(min);
        model.setMaximum(max);
    }
}
