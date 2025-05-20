package controlador;

import model.Venta;
import model.VentaDAO;
import vista.PanelTicketsDiarios;
import javax.swing.*;
import javax.swing.text.html.HTMLEditorKit;
import java.awt.print.PrinterException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

public class ControladorTicketsDiarios {
    private final PanelTicketsDiarios vista;
    private final VentaDAO dao = new VentaDAO();
    private final DateTimeFormatter fmtFecha = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    public ControladorTicketsDiarios(PanelTicketsDiarios vista) {
        this.vista = vista;
        vincularEventos();
        cargarTickets();
    }

    private void vincularEventos() {
        vista.btnBuscarFecha.addActionListener(e -> buscarPorFecha());
        vista.btnBuscarFolio.addActionListener(e -> buscarPorFolio());
        vista.btnVerTodos.addActionListener(e -> cargarTickets());
        vista.btnMostrarTicket.addActionListener(e -> mostrarTicket());
        vista.btnImprimirTicket.addActionListener(e -> imprimirTicket());
    }

    public void cargarTickets() {
        try {
            List<Venta> lista = dao.obtenerTodasVentasConTotal();
            vista.setModeloTabla(lista);
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(vista,
                    "Error al cargar tickets:\n" + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void buscarPorFecha() {
        String txt = vista.txtFecha.getText().trim();
        if (txt.isEmpty()) {
            cargarTickets();
            return;
        }
        try {
            LocalDate fecha = LocalDate.parse(txt);
            List<Venta> lista = dao.buscarPorFecha(fecha);
            vista.setModeloTabla(lista);
        } catch (DateTimeParseException ex) {
            JOptionPane.showMessageDialog(vista,
                    "Formato de fecha inválido. Use YYYY-MM-DD.",
                    "Error de formato", JOptionPane.WARNING_MESSAGE);
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(vista,
                    "Error al buscar por fecha:\n" + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void buscarPorFolio() {
        String pref = vista.txtFolio.getText().trim();
        if (pref.isEmpty()) {
            cargarTickets();
            return;
        }
        try {
            List<Venta> lista = dao.buscarPorFolio(pref);
            vista.setModeloTabla(lista);
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(vista,
                    "Error al buscar por folio:\n" + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void mostrarTicket() {
        int vr = vista.tblTickets.getSelectedRow();
        if (vr < 0) {
            JOptionPane.showMessageDialog(vista,
                    "Selecciona primero un ticket.",
                    "Atención", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int mr = vista.tblTickets.convertRowIndexToModel(vr);
        int id = (Integer) vista.tblTickets.getModel().getValueAt(mr, 0);
        try {
            Venta v = dao.obtenerVentaConLineas(id);
            String html = generarHTML(v);
            JEditorPane pane = new JEditorPane();
            pane.setEditorKit(new HTMLEditorKit());
            pane.setText(html);
            pane.setEditable(false);
            JOptionPane.showMessageDialog(vista,
                    new JScrollPane(pane),
                    "Ticket " + v.getFolio(),
                    JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(vista,
                    "No se pudo mostrar el ticket:\n" + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void imprimirTicket() {
        int vr = vista.tblTickets.getSelectedRow();
        if (vr < 0) {
            JOptionPane.showMessageDialog(vista,
                    "Selecciona primero un ticket.",
                    "Atención", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int mr = vista.tblTickets.convertRowIndexToModel(vr);
        int id = (Integer) vista.tblTickets.getModel().getValueAt(mr, 0);
        try {
            Venta v = dao.obtenerVentaConLineas(id);
            String html = generarHTML(v);
            JEditorPane printer = new JEditorPane("text/html", html);
            printer.setSize(600, 800);
            boolean done = printer.print();
            if (!done) {
                JOptionPane.showMessageDialog(vista,
                        "Impresión cancelada.",
                        "Imprimir", JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (PrinterException pe) {
            JOptionPane.showMessageDialog(vista,
                    "Error de impresión:\n" + pe.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(vista,
                    "No se pudo imprimir el ticket:\n" + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private String generarHTML(Venta v) {
        StringBuilder sb = new StringBuilder("<html><body>");
        sb.append(String.format("<b>Folio:</b> %s<br>", v.getFolio()));
        sb.append(String.format("<b>Fecha:</b> %s<br><br>",
                v.getFecha().format(fmtFecha)));

        sb.append("<pre>");
        sb.append("-".repeat(60)).append("\n");

        // ancho de nombre
        int maxName = v.getLineas().stream()
                .mapToInt(l -> l.getProducto().getNombre().length())
                .max().orElse("Producto".length());
        int nameW = Math.max(maxName, "Producto".length()) + 2;

        String hdr = String.format("%%-%ds %%5s %%10s %%10s%n", nameW);
        String ln = String.format("%%-%ds %%5d %%10.2f %%10.2f%n", nameW);

        sb.append(String.format(hdr, "Producto", "Cant", "Precio", "Subtotal"));
        double subtotal = 0;
        for (var l : v.getLineas()) {
            double lt = l.getPrecio() * l.getCantidad();
            subtotal += lt;
            sb.append(String.format(ln,
                    l.getProducto().getNombre(),
                    l.getCantidad(), l.getPrecio(), lt));
        }
        sb.append("-".repeat(60)).append("\n");
        sb.append("</pre>");

        double iva = subtotal * 0.16;
        double total = subtotal + iva;

        // Totales + Pago + Cambio
        sb.append(String.format("<br><b>Subtotal:</b> $%.2f<br>", subtotal));
        sb.append(String.format("<b>IVA 16%%:</b>   $%.2f<br>", iva));
        sb.append(String.format("<b>Total:</b>    $%.2f<br><br>", total));
        sb.append(String.format("<b>Pago:</b>     $%.2f<br>", v.getPago()));
        sb.append(String.format("<b>Cambio:</b>   $%.2f<br>", v.getCambio()));

        sb.append("</body></html>");
        return sb.toString();
    }

}
