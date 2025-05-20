package vista;

import model.Venta;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class PanelTicketsDiarios extends JPanel {
    public JTextField txtFecha;
    public JButton btnBuscarFecha;
    public JTextField txtFolio;
    public JButton btnBuscarFolio;
    public JButton btnVerTodos;
    public JButton btnMostrarTicket;
    public JButton btnImprimirTicket;
    public JTable tblTickets;
    private DefaultTableModel modelo;
    private static final DateTimeFormatter FMT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    public PanelTicketsDiarios() {
        setLayout(new BorderLayout(10, 10));

        // --- Panel de búsqueda ---
        JPanel pBusq = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        pBusq.add(new JLabel("Buscar fecha (yyyy-MM-dd):"));
        txtFecha = new JTextField(10);
        pBusq.add(txtFecha);
        btnBuscarFecha = new JButton("Buscar Fecha");
        pBusq.add(btnBuscarFecha);

        pBusq.add(Box.createHorizontalStrut(20));
        pBusq.add(new JLabel("Buscar folio:"));
        txtFolio = new JTextField(10);
        pBusq.add(txtFolio);
        btnBuscarFolio = new JButton("Buscar Folio");
        pBusq.add(btnBuscarFolio);

        pBusq.add(Box.createHorizontalStrut(20));
        btnVerTodos = new JButton("Ver Todos");
        pBusq.add(btnVerTodos);

        add(pBusq, BorderLayout.NORTH);

        // --- Tabla de tickets (con columna oculta ID) ---
        modelo = new DefaultTableModel(
                new String[]{"ID", "Folio", "Fecha", "Total"}, 0
        ) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };
        tblTickets = new JTable(modelo);
        // ocultar la columna ID
        tblTickets.removeColumn(tblTickets.getColumnModel().getColumn(0));
        add(new JScrollPane(tblTickets), BorderLayout.CENTER);

        // --- Botones de acción ---
        JPanel pAcciones = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        btnMostrarTicket = new JButton("Mostrar Ticket");
        btnImprimirTicket = new JButton("Imprimir Ticket");
        pAcciones.add(btnMostrarTicket);
        pAcciones.add(btnImprimirTicket);
        add(pAcciones, BorderLayout.SOUTH);
    }

    public void setModeloTabla(List<Venta> lista) {
        modelo.setRowCount(0);
        for (Venta v : lista) {
            modelo.addRow(new Object[]{
                    v.getId(),
                    v.getFolio(),
                    v.getFecha().format(FMT),
                    String.format("%.2f", v.getTotal())
            });
        }
    }
}
