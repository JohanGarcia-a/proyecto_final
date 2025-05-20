package controlador;

import model.BaseDatos;
import model.ModeloTablaProductos;
import model.Producto;
import model.ProductoDAO;
import vista.PanelProductos;
import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.event.ListSelectionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

public class ControladorProductos {
    private final PanelProductos vista;
    private final ProductoDAO dao = new ProductoDAO();

    public ControladorProductos(PanelProductos vista) throws SQLException {
        this.vista = vista;
        BaseDatos.inicializar();
        cargarDatos();
        vincularAcciones();
    }

    private void cargarDatos() throws SQLException {
        // Carga categorías
        DefaultComboBoxModel<String> mc = new DefaultComboBoxModel<>();
        for (String c : dao.encontrarTodasCategorias()) mc.addElement(c);
        vista.cbCategoria.setModel(mc);
        vista.cbCategoria.setEditable(true);

        // Carga marcas
        DefaultComboBoxModel<String> mm = new DefaultComboBoxModel<>();
        for (String m : dao.encontrarTodasMarcas()) mm.addElement(m);
        vista.cbMarca.setModel(mm);
        vista.cbMarca.setEditable(true);

        // Carga productos en la tabla
        List<Producto> lista = dao.encontrarTodos();
        ((ModeloTablaProductos) vista.tblProductos.getModel()).setProductos(lista);
    }

    private void vincularAcciones() {
        // Seleccionar imagen
        vista.btnSeleccionarImagen.addActionListener(e -> {
            JFileChooser chooser = new JFileChooser();
            chooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter(
                    "Imágenes", "jpg", "jpeg", "png", "gif"));
            if (chooser.showOpenDialog(vista) == JFileChooser.APPROVE_OPTION) {
                File f = chooser.getSelectedFile();
                vista.txtImagen.setText(f.getAbsolutePath());
                try {
                    BufferedImage img = ImageIO.read(f);
                    vista.lblPreview.setIcon(new ImageIcon(
                            img.getScaledInstance(80, 80, BufferedImage.SCALE_SMOOTH)));
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        });

        // Agregar producto con validaciones
        vista.btnAgregar.addActionListener(e -> {
            try {
                if (camposVacios()) {
                    JOptionPane.showMessageDialog(vista,
                            "Por favor, rellena todos los campos.",
                            "Validación", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                Producto p = leerFormulario(0);
                if (existeEnTabla(p)) {
                    JOptionPane.showMessageDialog(vista,
                            "El producto ya existe.",
                            "Duplicado", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                dao.insertar(p);
                recargar();
                limpiarFormulario();
            } catch (Exception ex) {
                mostrarError(ex);
            }
        });

        // Actualizar producto
        vista.btnActualizar.addActionListener(e -> {
            int fila = vista.tblProductos.getSelectedRow();
            if (fila < 0) return;
            try {
                if (camposVacios()) {
                    JOptionPane.showMessageDialog(vista,
                            "Por favor, rellena todos los campos.",
                            "Validación", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                int id = Integer.parseInt(vista.txtId.getText());
                Producto p = leerFormulario(id);
                if (existeEnTabla(p)) {
                    JOptionPane.showMessageDialog(vista,
                            "Ya existe otro producto con esos datos.",
                            "Duplicado", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                dao.actualizar(p);
                recargar();
                limpiarFormulario();
            } catch (Exception ex) {
                mostrarError(ex);
            }
        });

        // Eliminar producto
        vista.btnEliminar.addActionListener(e -> {
            int fila = vista.tblProductos.getSelectedRow();
            if (fila < 0) return;
            try {
                int id = (Integer) vista.tblProductos.getValueAt(fila, 0);
                dao.eliminar(id);
                recargar();
                limpiarFormulario();
            } catch (Exception ex) {
                mostrarError(ex);
            }
        });

        // Limpiar formulario
        vista.btnLimpiar.addActionListener(e -> limpiarFormulario());

        // Selección de fila para editar
        vista.tblProductos.getSelectionModel().addListSelectionListener((ListSelectionListener) e -> {
            if (e.getValueIsAdjusting()) return;
            int fila = vista.tblProductos.getSelectedRow();
            if (fila < 0) return;
            Producto p = ((ModeloTablaProductos) vista.tblProductos.getModel()).getProductoEn(fila);
            llenarFormulario(p);
        });
    }

    private Producto leerFormulario(int id) {
        String nom = vista.txtNombre.getText().trim();
        String mar = ((String) vista.cbMarca.getEditor().getItem()).trim();
        String tam = vista.txtTamano.getText().trim();
        String cat = ((String) vista.cbCategoria.getEditor().getItem()).trim();
        double pre = (Double) vista.spnPrecio.getValue();
        int exi = (Integer) vista.spnExistencias.getValue();
        String img = vista.txtImagen.getText().trim();
        return new Producto(id, nom, pre, exi, cat, mar, tam, img);
    }

    private void llenarFormulario(Producto p) {
        vista.txtId.setText(String.valueOf(p.getId()));
        vista.txtNombre.setText(p.getNombre());
        vista.cbMarca.setSelectedItem(p.getMarca());
        vista.txtTamano.setText(p.getTamano());
        vista.spnPrecio.setValue(p.getPrecio());
        vista.spnExistencias.setValue(p.getExistencias());
        vista.cbCategoria.setSelectedItem(p.getCategoria());
        vista.txtImagen.setText(p.getRutaImagen());
        try {
            if (!p.getRutaImagen().isEmpty()) {
                BufferedImage img = ImageIO.read(new File(p.getRutaImagen()));
                vista.lblPreview.setIcon(new ImageIcon(
                        img.getScaledInstance(80, 80, BufferedImage.SCALE_SMOOTH)));
            } else {
                vista.lblPreview.setIcon(null);
            }
        } catch (IOException ex) {
            vista.lblPreview.setIcon(null);
        }
    }

    private void recargar() throws SQLException {
        cargarDatos();
    }

    private void limpiarFormulario() {
        vista.txtId.setText("");
        vista.txtNombre.setText("");
        vista.cbMarca.setSelectedIndex(-1);
        vista.txtTamano.setText("");
        vista.spnPrecio.setValue(0.0);
        vista.spnExistencias.setValue(0);
        vista.cbCategoria.setSelectedIndex(-1);
        vista.txtImagen.setText("");
        vista.lblPreview.setIcon(null);
        vista.tblProductos.clearSelection();
    }

    private boolean camposVacios() {
        return vista.txtNombre.getText().trim().isEmpty()
                || ((String) vista.cbMarca.getEditor().getItem()).trim().isEmpty()
                || vista.txtTamano.getText().trim().isEmpty()
                || ((String) vista.cbCategoria.getEditor().getItem()).trim().isEmpty()
                || vista.txtImagen.getText().trim().isEmpty();
    }

    private boolean existeEnTabla(Producto p) {
        ModeloTablaProductos model = (ModeloTablaProductos) vista.tblProductos.getModel();
        for (int i = 0; i < model.getRowCount(); i++) {
            Producto otro = model.getProductoEn(i);
            if (otro.getNombre().equalsIgnoreCase(p.getNombre())
                    && otro.getMarca().equalsIgnoreCase(p.getMarca())
                    && otro.getId() != p.getId()) {
                return true;
            }
        }
        return false;
    }

    private void mostrarError(Exception ex) {
        ex.printStackTrace();
        JOptionPane.showMessageDialog(vista, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
    }
}