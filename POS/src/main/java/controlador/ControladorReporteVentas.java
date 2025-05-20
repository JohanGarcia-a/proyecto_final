package controlador;

import model.ModeloTablaReporteVentas;
import model.Venta;
import model.VentaDAO;
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import vista.PanelReporteVentas;

import javax.swing.*;
import java.io.File;
import java.io.InputStream;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Controlador para generación y exportación de reportes de ventas.
 */
public class ControladorReporteVentas {
    private final PanelReporteVentas vista;
    private final VentaDAO daoVentas;
    private final ModeloTablaReporteVentas modeloTabla;

    private String exportFolder;

    private static final DateTimeFormatter FMT_DIARIO =
            DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter FMT_MENSUAL =
            DateTimeFormatter.ofPattern("yyyy-MM");

    public ControladorReporteVentas(PanelReporteVentas vista) throws SQLException {
        this.vista = vista;
        this.daoVentas = new VentaDAO();
        this.modeloTabla = (ModeloTablaReporteVentas) vista.tblReporte.getModel();

        // Carpeta por defecto para exportar
        this.exportFolder = System.getProperty("user.dir") + File.separator + "reportes";

        // Listeners
        vista.btnCargarDiario.addActionListener(e -> cargarReporteDiario());
        vista.btnCargarMensual.addActionListener(e -> cargarReporteMensual());
        vista.btnExportarPDF.addActionListener(e -> exportarPDF());
    }

    /**
     * Permite cambiar la ruta de exportación de los PDF.
     */
    public void setExportPath(String folderPath) {
        this.exportFolder = folderPath;
    }

    private void cargarReporteDiario() {
        LocalDate ld = ((java.util.Date) vista.spnFecha.getValue())
                .toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        String turno = (String) vista.cbTurno.getSelectedItem();
        try {
            List<Venta> ventas;
            if ("Matutino (08:00–14:00)".equals(turno)) {
                ventas = daoVentas.buscarPorFechaYHorario(ld, "08:00", "14:00");
            } else if ("Vespertino (14:00–20:00)".equals(turno)) {
                ventas = daoVentas.buscarPorFechaYHorario(ld, "14:00", "20:00");
            } else {
                ventas = daoVentas.buscarPorFecha(ld);
            }
            modeloTabla.setVentas(ventas);
            actualizarResumen(ventas);
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(vista,
                    "Error al cargar reporte diario:\n" + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void cargarReporteMensual() {
        LocalDate ld = ((java.util.Date) vista.spnMes.getValue())
                .toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        YearMonth ym = YearMonth.of(ld.getYear(), ld.getMonth());
        try {
            List<Venta> ventas = daoVentas.buscarPorMes(ym);
            modeloTabla.setVentas(ventas);
            actualizarResumen(ventas);
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(vista,
                    "Error al cargar reporte mensual:\n" + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void actualizarResumen(List<Venta> ventas) {
        double totalSub = ventas.stream().mapToDouble(Venta::getTotal).sum();
        double totalIva = totalSub * 0.16;
        double totalGen = totalSub + totalIva;

        vista.lblTotalSubtotal.setText(String.format("Total Subtotal: $%.2f", totalSub));
        vista.lblTotalIva.setText(String.format("Total IVA: $%.2f", totalIva));
        vista.lblIngresoTotal.setText(String.format("Ingreso Total: $%.2f", totalGen));
    }

    private void exportarPDF() {
        try {
            // 1) Compilar JRXML
            InputStream jrxmlStream = Thread.currentThread()
                    .getContextClassLoader()
                    .getResourceAsStream("Reportes/ReporteVentas.jrxml");
            if (jrxmlStream == null) throw new JRException("No se encontró ReporteVentas.jrxml");
            JasperReport jasperReport = JasperCompileManager.compileReport(jrxmlStream);

            // 2) Parámetros
            Map<String, Object> params = new HashMap<>();
            String tipo, fechaForm, nombreArchivo;
            if (vista.tabTipos.getSelectedIndex() == 0) {
                tipo = "Diario";
                LocalDate d = ((java.util.Date) vista.spnFecha.getValue())
                        .toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
                fechaForm = d.format(FMT_DIARIO);
                nombreArchivo = "Ventas_Diario_" + fechaForm.replace("-", "");
            } else {
                tipo = "Mensual";
                LocalDate d = ((java.util.Date) vista.spnMes.getValue())
                        .toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
                YearMonth ym = YearMonth.of(d.getYear(), d.getMonth());
                fechaForm = ym.format(FMT_MENSUAL);
                nombreArchivo = "Ventas_Mensual_" + fechaForm.replace("-", "");
            }
            params.put("REPORT_TITLE", "Reporte " + tipo + " - " + fechaForm);

            // 3) Data source
            JRBeanCollectionDataSource ds =
                    new JRBeanCollectionDataSource(modeloTabla.getVentas());

            // 4) Llenar
            JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, params, ds);

            // 5) Exportar al folder configurado
            File d = new File(exportFolder);
            if (!d.exists()) d.mkdirs();
            String pdfPath = exportFolder + File.separator + nombreArchivo + ".pdf";
            JasperExportManager.exportReportToPdfFile(jasperPrint, pdfPath);

            // 6) Aviso
            JOptionPane.showMessageDialog(vista,
                    "PDF generado en:\n" + pdfPath,
                    "Exportación exitosa", JOptionPane.INFORMATION_MESSAGE);
        } catch (JRException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(vista,
                    "Error al exportar PDF:\n" + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
