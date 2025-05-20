package controlador;

import model.*;
import vista.PanelProductos;
import vista.PanelProductosVentas;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Controlador para el flujo de ventas, con soporte de tasas de impuestos configurables.
 */
public class ControladorProductosVentas {
    private final PanelProductosVentas vista;
    private final PanelProductos panelInventario;
    private final ProductoDAO dao = new ProductoDAO();
    private final Venta venta = new Venta();
    private final DateTimeFormatter formatoFecha = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    private final TicketDiarioDAO ticketDao = new TicketDiarioDAO();

    private final DefaultListModel<String> listModel = new DefaultListModel<>();
    private final JList<String> suggestionList = new JList<>(listModel);
    private final JPopupMenu popup = new JPopupMenu();
    private Timer acTimer;

    // Tasas de impuestos (porcentaje)
    private double ivaRate = 16.0 / 100.0;
    private double iepsRate = 0.0;

    public ControladorProductosVentas(PanelProductosVentas vista, PanelProductos panelInventario) throws SQLException {
        this.vista = vista;
        this.panelInventario = panelInventario;
        inicializar();
        configurarAutocompletado();
        vincularEventos();
    }

    /**
     * Establece las tasas de IVA e IEPS (porcentaje, ej. 16.0 para 16%).
     */
    public void setTaxRates(double ivaPct, double iepsPct) {
        this.ivaRate = ivaPct / 100.0;
        this.iepsRate = iepsPct / 100.0;
    }

    private void inicializar() throws SQLException {
        List<Producto> todos = dao.encontrarTodos();
        vista.mostrarProductos(todos);
        vista.mostrarCategorias(dao.encontrarTodasCategorias());
        vista.setCategoriaListener(this::filtrarPorCategoria);
    }

    private void configurarAutocompletado() {
        JTextField editor = vista.txtBusqueda;
        suggestionList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        popup.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        popup.add(new JScrollPane(suggestionList));

        acTimer = new Timer(200, e -> {
            acTimer.stop();
            SwingUtilities.invokeLater(() -> mostrarSugerencias(editor.getText().trim()));
        });
        acTimer.setRepeats(false);

        editor.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) {
                schedule(editor.getText());
            }

            public void removeUpdate(DocumentEvent e) {
                schedule(editor.getText());
            }

            public void changedUpdate(DocumentEvent e) {
            }
        });

        editor.addKeyListener(new KeyAdapter() {
            public void keyPressed(KeyEvent e) {
                if (!popup.isVisible()) return;
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    elegirSugerencia(editor);
                    e.consume();
                }
            }
        });

        suggestionList.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                elegirSugerencia(editor);
            }
        });
    }

    private void schedule(String text) {
        if (text.isEmpty()) popup.setVisible(false);
        else acTimer.restart();
    }

    private void mostrarSugerencias(String text) {
        if (text.isEmpty()) {
            popup.setVisible(false);
            return;
        }
        List<String> nombres;
        try {
            nombres = dao.nombresContienen(text);
        } catch (SQLException ex) {
            nombres = Collections.emptyList();
        }
        listModel.clear();
        nombres.forEach(listModel::addElement);
        if (listModel.isEmpty()) popup.setVisible(false);
        else {
            suggestionList.setSelectedIndex(0);
            popup.show(vista.txtBusqueda, 0, vista.txtBusqueda.getHeight());
        }
    }

    private void elegirSugerencia(JTextField editor) {
        String sel = suggestionList.getSelectedValue();
        if (sel != null) {
            editor.setText(sel);
            buscar();
        }
        popup.setVisible(false);
    }

    private void vincularEventos() {
        refrescarMiniaturas();
        vista.btnBuscar.addActionListener(e -> buscar());
        vista.btnAgregar.addActionListener(e -> {
            Producto p = (Producto) vista.cbBuscador.getSelectedItem();
            if (p != null) agregarLinea(p, (Integer) vista.spnCantidad.getValue());
        });
        vista.btnMas.addActionListener(e -> cambiarCantidad(+1));
        vista.btnMenos.addActionListener(e -> cambiarCantidad(-1));
        vista.btnEliminar.addActionListener(e -> eliminarLinea());
        vista.btnFinalizar.addActionListener(e -> finalizarVenta());

        for (Component c : vista.panelCategorias.getComponents()) {
            if (c instanceof JButton)
                ((JButton) c).addActionListener(e -> filtrarPorCategoria(((JButton) e.getSource()).getText()));
        }
    }

    public void refrescarMiniaturas() {
        for (Component c : vista.panelMiniaturas.getComponents()) {
            if (c instanceof JButton) {
                ((JButton) c).addActionListener(e -> {
                    Producto p = (Producto) ((JButton) c).getClientProperty("producto");
                    agregarLinea(p, (Integer) vista.spnCantidad.getValue());
                });
            }
        }
    }

    private void buscar() {
        String txt = vista.txtBusqueda.getText().trim();
        if (txt.isEmpty()) return;
        List<Producto> res = new ArrayList<>();
        try {
            if (txt.matches("\\d+")) {
                Producto p = dao.encontrarPorId(Integer.parseInt(txt));
                if (p != null) res.add(p);
            }
            for (String nombre : dao.nombresContienen(txt)) {
                Producto p = dao.encontrarPorNombre(nombre);
                if (p != null && !res.contains(p)) res.add(p);
            }
        } catch (SQLException ex) {
            mostrarError(ex);
        }
        DefaultComboBoxModel<Producto> m = new DefaultComboBoxModel<>();
        res.forEach(m::addElement);
        vista.cbBuscador.setModel(m);
    }

    private void agregarLinea(Producto p, int cantidad) {
        for (LineaVenta l : venta.getLineas()) {
            if (l.getProducto().getId() == p.getId()) {
                int opt = JOptionPane.showConfirmDialog(vista,
                        "El producto ya está en la venta. ¿Sumar " + cantidad + " unidades?",
                        "Duplicado", JOptionPane.YES_NO_OPTION);
                if (opt == JOptionPane.YES_OPTION) {
                    l.fijarCantidad(l.getCantidad() + cantidad);
                    venta.recalcularTotal();
                    refrescarTablaYResumen();
                }
                return;
            }
        }
        venta.agregarLinea(new LineaVenta(p, cantidad));
        venta.recalcularTotal();
        refrescarTablaYResumen();
    }

    private void cambiarCantidad(int delta) {
        int fila = vista.tblVenta.getSelectedRow();
        if (fila < 0) return;
        ModeloTablaVenta m = (ModeloTablaVenta) vista.tblVenta.getModel();
        LineaVenta l = m.getLineaEn(fila);
        int n = l.getCantidad() + delta;
        if (n <= 0) venta.getLineas().remove(l);
        else l.fijarCantidad(n);
        venta.recalcularTotal();
        m.setLineas(venta.getLineas());
        refrescarTablaYResumen();
    }

    private void eliminarLinea() {
        int fila = vista.tblVenta.getSelectedRow();
        if (fila < 0) return;
        ModeloTablaVenta m = (ModeloTablaVenta) vista.tblVenta.getModel();
        LineaVenta l = m.getLineaEn(fila);
        venta.getLineas().remove(l);
        venta.recalcularTotal();
        m.setLineas(venta.getLineas());
        refrescarTablaYResumen();
    }

    public void finalizarVenta() {
        if (venta.getLineas().isEmpty()) {
            JOptionPane.showMessageDialog(vista, "No hay productos agregados a la venta.", "Atención", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Pago y cálculo de montos
        double sub = venta.getTotal();
        double ivaAmt = sub * ivaRate;
        double iepsAmt = sub * iepsRate;
        double total = sub + ivaAmt + iepsAmt;
        double pago;
        try {
            pago = Double.parseDouble(vista.txtPago.getText().trim());
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(vista, "Pago inválido. Introduce un número.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (pago < total) {
            JOptionPane.showMessageDialog(vista, String.format("El pago debe ser al menos $%.2f.", total), "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        double cambio = pago - total;
        vista.lblCambio.setText(String.format("Cambio: $%.2f", cambio));

        try {
            // Folio diario
            LocalDate hoy = LocalDate.now();
            int ultimo = ticketDao.obtenerUltimoTicketDelDia(hoy);
            int siguiente = ultimo + 1;
            ticketDao.guardarUltimoTicketDelDia(hoy, siguiente);
            String fechaStr = hoy.format(DateTimeFormatter.ofPattern("yyyyMMdd"));
            String folio = fechaStr + "-" + String.format("%04d", siguiente);

            // Guardar venta (incluye stock inside DAO)
            venta.setFolio(folio);
            venta.setFecha(LocalDateTime.now());
            new VentaDAO().guardar(venta);

            // Mostrar ticket HTML
            StringBuilder sb = new StringBuilder("<html><body>");
            sb.append(String.format("<b>Folio:</b> %s<br>", folio));
            sb.append(String.format("<b>Fecha:</b> %s<br><br>", venta.getFecha().format(formatoFecha)));
            sb.append("<pre>");
            sb.append("-".repeat(60)).append("\n");
            // Formatos y contenido
            int maxNameLen = venta.getLineas().stream().mapToInt(l -> l.getProducto().getNombre().length()).max().orElse(8);
            String headerFmt = String.format("%%-%ds %%5s %%10s %%10s%n", maxNameLen + 2);
            String lineFmt = String.format("%%-%ds %%5d %%10.2f %%10.2f%n", maxNameLen + 2);
            sb.append(String.format(headerFmt, "Producto", "Cant", "Precio", "Subtotal"));
            for (LineaVenta l : venta.getLineas()) {
                double linTot = l.getPrecio() * l.getCantidad();
                sb.append(String.format(lineFmt, l.getProducto().getNombre(), l.getCantidad(), l.getPrecio(), linTot));
            }
            sb.append("-".repeat(60)).append("\n</pre>");
            // Totales completos
            sb.append(String.format("<br><b>Subtotal:</b> $%.2f<br>", sub));
            sb.append(String.format("<b>IVA (%.0f%%):</b> $%.2f<br>", ivaRate * 100, ivaAmt));
            if (iepsRate > 0) sb.append(String.format("<b>IEPS (%.0f%%):</b> $%.2f<br>", iepsRate * 100, iepsAmt));
            sb.append(String.format("<b>Total:</b> $%.2f<br><br>", total));
            sb.append(String.format("<b>Pago:</b> $%.2f<br>", pago));
            sb.append(String.format("<b>Cambio:</b> $%.2f<br>", cambio));
            sb.append("</body></html>");

            JOptionPane.showMessageDialog(vista, sb.toString(), "Ticket " + folio, JOptionPane.INFORMATION_MESSAGE);

            // Limpiar venta y refrescar UI
            venta.getLineas().clear();
            venta.setTotal(0);
            refrescarTablaYResumen();
            List<Producto> all = dao.encontrarTodos();
            vista.mostrarProductos(all);
            refrescarMiniaturas();
            panelInventario.recargarTabla(all);
            vista.txtPago.setText("");

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(vista, "Error al registrar venta:\n" + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void refrescarTablaYResumen() {
        ModeloTablaVenta m = (ModeloTablaVenta) vista.tblVenta.getModel();
        m.setLineas(venta.getLineas());
        double sub = venta.getTotal();
        double iva = sub * ivaRate;
        double ieps = sub * iepsRate;
        vista.lblSubtotal.setText(String.format("Subtotal: $%.2f", sub));
        vista.lblIva.setText(String.format("IVA (%.0f%%): $%.2f", ivaRate * 100, iva));
        vista.lblTotal.setText(String.format("Total: $%.2f", sub + iva + ieps));
    }

    private void mostrarError(Exception ex) {
        ex.printStackTrace();
        JOptionPane.showMessageDialog(vista, "Error interno:\n" + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
    }

    public void filtrarPorCategoria(String categoria) {
        try {
            List<Producto> productos = "Todos".equalsIgnoreCase(categoria) ? dao.encontrarTodos() : dao.encontrarPorCategoria(categoria);
            vista.mostrarProductos(productos);
            refrescarMiniaturas();
        } catch (SQLException ex) {
            mostrarError(ex);
        }
    }
}
