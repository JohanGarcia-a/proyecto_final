// vista/MainFrame.java

package vista;

import controlador.ControladorProductos;
import controlador.ControladorProductosVentas;
import controlador.ControladorReporteVentas;
import controlador.ControladorTicketsDiarios;
import model.BaseDatos;
import model.ROLE;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import java.awt.*;
import java.sql.SQLException;
import java.util.List;

/**
 * Frame principal con pestañas de Inventario, Ventas, Reportes, Tickets, Usuarios y Configuración.
 */
public class MainFrame extends JFrame {
    private final JTabbedPane pestañas;
    private final PanelProductos panelInventario;
    private final PanelProductosVentas panelVentas;
    private final PanelReporteVentas panelReportes;
    private final PanelTicketsDiarios panelTickets;
    private final UserManagementPanel panelUsuarios;
    private final ConfigPanel panelConfiguracion;

    private ControladorProductosVentas controladorVentas;
    private ControladorReporteVentas controladorReportes;
    private ControladorTicketsDiarios controladorTickets;

    private ROLE currentRole;

    public MainFrame(ROLE role) {
        super("Mi punto de venta");
        this.currentRole = role;

        // Icono de la aplicación
        String ruta = "C:\\Users\\garci\\OneDrive\\Imágenes\\Tienda.png";
        setIconImage(new ImageIcon(ruta).getImage());

        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1000, 650);
        setLocationRelativeTo(null);

        // Inicializar base de datos
        try {
            BaseDatos.inicializar();
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(null,
                    "No se pudo inicializar la base de datos:\n" + ex.getMessage(),
                    "Error Crítico", JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }

        // Crear panels
        panelInventario = new PanelProductos();
        panelVentas = new PanelProductosVentas();
        panelReportes = new PanelReporteVentas();
        panelTickets = new PanelTicketsDiarios();
        panelUsuarios = new UserManagementPanel();
        panelConfiguracion = new ConfigPanel();

        // Listener original de “Guardar” (antes de persistir en BD):
        panelConfiguracion.addSaveListener(e -> {
            boolean manual = panelConfiguracion.isManualIdEnabled();
            int minStock2 = panelConfiguracion.getMinStock();
            int maxStock2 = panelConfiguracion.getMaxStock();
            double iva2 = panelConfiguracion.getIvaRate();
            double ieps2 = panelConfiguracion.getIepsRate();
            String path2 = panelConfiguracion.getExportPath();

            panelInventario.enableManualId(manual);
            panelInventario.setStockLimits(minStock2, maxStock2);
            controladorVentas.setTaxRates(iva2, ieps2);
            controladorReportes.setExportPath(path2);

            JOptionPane.showMessageDialog(this,
                    "Configuración aplicada correctamente.",
                    "Configuración", JOptionPane.INFORMATION_MESSAGE);
        });

        // Listener original de “Cerrar sesión”:
        panelConfiguracion.addLogoutListener(e -> {
            dispose();
            SwingUtilities.invokeLater(() -> MainFrame.main(null));
        });

        // Agregar pestañas
        pestañas = new JTabbedPane();
        pestañas.addTab("Inventario", panelInventario);
        pestañas.addTab("Ventas", panelVentas);
        pestañas.addTab("Reportes", panelReportes);
        pestañas.addTab("Tickets", panelTickets);
        pestañas.addTab("Usuarios", panelUsuarios);
        pestañas.addTab("Configuración", panelConfiguracion);

        // Estilo de pestañas (colores y opacidad)
        Color[] colores = {
                new Color(200, 230, 201),
                new Color(255, 224, 178),
                new Color(187, 222, 251),
                new Color(255, 205, 210),
                new Color(240, 240, 240),
                new Color(224, 224, 224)
        };
        for (int i = 0; i < pestañas.getTabCount(); i++) {
            pestañas.getComponentAt(i).setBackground(colores[i]);
            pestañas.setForegroundAt(i, Color.DARK_GRAY);
            ((JComponent) pestañas.getComponentAt(i)).setOpaque(true);
        }
        getContentPane().add(pestañas, BorderLayout.CENTER);

        // Listener para recargar datos al cambiar pestaña
        pestañas.addChangeListener((ChangeEvent e) -> {
            String title = pestañas.getTitleAt(pestañas.getSelectedIndex());
            try {
                switch (title) {
                    case "Ventas":
                        List<String> cats = new model.ProductoDAO().encontrarTodasCategorias();
                        panelVentas.mostrarCategorias(cats);
                        panelVentas.mostrarProductos(new model.ProductoDAO().encontrarTodos());
                        controladorVentas.refrescarMiniaturas();
                        break;
                    case "Tickets":
                        controladorTickets.cargarTickets();
                        break;
                    case "Usuarios":
                        panelUsuarios.refreshTable();
                        break;
                    case "Configuración":
                        panelConfiguracion.loadSettings();
                        break;
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this,
                        "Error al cargar pestaña " + title + ":\n" + ex.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        // Inicializar controladores en background
        new SwingWorker<Void, Void>() {
            protected Void doInBackground() throws Exception {
                new ControladorProductos(panelInventario);
                controladorVentas = new ControladorProductosVentas(panelVentas, panelInventario);
                controladorReportes = new ControladorReporteVentas(panelReportes);
                controladorTickets = new ControladorTicketsDiarios(panelTickets);
                return null;
            }

            protected void done() {
                try {
                    get();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }.execute();

        // Aplicar permisos según rol
        applyRolePermissions();
    }

    private void applyRolePermissions() {
        switch (currentRole) {
            case ADMINISTRATOR:
                enableAllTabs(true);
                break;
            case EMPLOYEE:
                pestañas.setEnabledAt(2, false);
                pestañas.setEnabledAt(4, false);
                pestañas.setEnabledAt(5, false);
                break;
            case GENERAL:
                for (int i = 1; i < pestañas.getTabCount(); i++)
                    pestañas.setEnabledAt(i, false);
                pestañas.setEnabledAt(0, true);
                break;
        }
    }

    private void enableAllTabs(boolean habilitar) {
        for (int i = 0; i < pestañas.getTabCount(); i++)
            pestañas.setEnabledAt(i, habilitar);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            LoginDialog login = new LoginDialog(null);
            login.setVisible(true);
            ROLE role = login.getSelectedRole();
            if (role == null) System.exit(0);
            MainFrame frame = new MainFrame(role);
            frame.setVisible(true);
        });
    }
}
