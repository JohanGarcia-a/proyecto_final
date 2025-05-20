package vista;

import javax.swing.*;

import model.ModeloTablaReporteVentas;

import java.awt.*;
import java.util.Calendar;
import java.util.Date;

public class PanelReporteVentas extends JPanel {
    public final JTabbedPane tabTipos;

    // Componentes Diario
    public final JSpinner spnFecha;
    public final JComboBox<String> cbTurno;
    public final JButton btnCargarDiario;
    public final JButton btnExportarPDF = new JButton("Exportar a PDF");

    // Componentes Mensual
    public final JSpinner spnMes;
    public final JButton btnCargarMensual;

    // Tabla y etiquetas
    public final JTable tblReporte;
    public final JLabel lblTotalSubtotal;
    public final JLabel lblTotalIva;
    public final JLabel lblIngresoTotal;


    public PanelReporteVentas() {
        setLayout(new BorderLayout(5, 5));

        tabTipos = new JTabbedPane();

        // Pestaña Diario
        JPanel pnlDiario = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));
        pnlDiario.add(new JLabel("Fecha:"));
        pnlDiario.setBackground(new Color(255, 222, 173));
        spnFecha = new JSpinner(new SpinnerDateModel(new Date(), null, null, Calendar.DAY_OF_MONTH));
        spnFecha.setEditor(new JSpinner.DateEditor(spnFecha, "yyyy-MM-dd"));
        pnlDiario.add(spnFecha);

        pnlDiario.add(new JLabel("Turno:"));
        cbTurno = new JComboBox<>(new String[]{"Todos", "Matutino", "Vespertino"});
        pnlDiario.add(cbTurno);

        btnCargarDiario = new JButton("Cargar Diario");
        btnCargarDiario.setBackground(new Color(0, 255, 0));
        pnlDiario.add(btnCargarDiario);
        tabTipos.addTab("Diario", pnlDiario);

        // Pestaña Mensual
        JPanel pnlMensual = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));
        pnlMensual.setBackground(new Color(255, 222, 173));
        pnlMensual.add(new JLabel("Mes/Año:"));
        spnMes = new JSpinner(new SpinnerDateModel(new Date(), null, null, Calendar.MONTH));
        spnMes.setEditor(new JSpinner.DateEditor(spnMes, "yyyy-MM"));
        pnlMensual.add(spnMes);

        btnCargarMensual = new JButton("Cargar Mensual");
        btnCargarMensual.setBackground(new Color(0, 255, 0));
        pnlMensual.add(btnCargarMensual);
        tabTipos.addTab("Mensual", pnlMensual);

        add(tabTipos, BorderLayout.NORTH);

        // Tabla de reporte
        tblReporte = new JTable(new ModeloTablaReporteVentas());
        tblReporte.setBackground(new Color(245, 245, 220));
        add(new JScrollPane(tblReporte), BorderLayout.CENTER);

        // --- Resumen de totales ---
        JPanel pnlResumen = new JPanel(new GridLayout(1, 4, 12, 8)); // Cambié a GridLayout
        pnlResumen.setBackground(new Color(255, 222, 173));
        lblTotalSubtotal = new JLabel("Total Subtotal: $0.00");
        lblTotalIva = new JLabel("Total IVA: $0.00");
        lblIngresoTotal = new JLabel("Ingreso Total: $0.00");


        pnlResumen.add(lblTotalSubtotal);
        pnlResumen.add(lblTotalIva);
        pnlResumen.add(lblIngresoTotal);
        btnExportarPDF.setBackground(new Color(10, 100, 200));
        pnlResumen.add(btnExportarPDF);
        add(pnlResumen, BorderLayout.SOUTH);


    }
}


