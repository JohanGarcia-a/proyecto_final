// vista/ConfigPanel.java

package vista;

import javax.swing.*;
import javax.swing.filechooser.FileSystemView;
import java.awt.*;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.prefs.Preferences;

/**
 * Panel de configuración optimizado para POS.
 */
public class ConfigPanel extends JPanel {
    private JCheckBox chkManualId;
    private JSpinner spnMinStock, spnMaxStock;
    private JSpinner spnIvaRate, spnIepsRate;
    private JTextField txtExportPath;
    private JButton btnBrowse, btnSave, btnLogout;
    private JComboBox<String> cbTheme;
    private final Preferences prefs = Preferences.userNodeForPackage(ConfigPanel.class);

    public ConfigPanel() {
        setLayout(new BorderLayout(10, 10));

        // Panel central con GridLayout 7 filas x 2 columnas
        JPanel center = new JPanel(new GridLayout(7, 2, 10, 10));
        // Fila 1: Manual ID
        center.add(new JLabel("Habilitar entrada manual de ID:"));
        chkManualId = new JCheckBox();
        center.add(chkManualId);
        // Fila 2: Stock mínimo
        center.add(new JLabel("Stock mínimo:"));
        spnMinStock = new JSpinner(new SpinnerNumberModel(0, 0, Integer.MAX_VALUE, 1));
        center.add(spnMinStock);
        // Fila 3: Stock máximo
        center.add(new JLabel("Stock máximo:"));
        spnMaxStock = new JSpinner(new SpinnerNumberModel(100, 0, Integer.MAX_VALUE, 1));
        center.add(spnMaxStock);
        // Fila 4: IVA
        center.add(new JLabel("IVA (%) :"));
        spnIvaRate = new JSpinner(new SpinnerNumberModel(16.0, 0.0, 100.0, 0.5));
        center.add(spnIvaRate);
        // Fila 5: IEPS
        center.add(new JLabel("IEPS (%) :"));
        spnIepsRate = new JSpinner(new SpinnerNumberModel(8.0, 0.0, 100.0, 0.5));
        center.add(spnIepsRate);
        // Fila 6: Ruta exportación
        center.add(new JLabel("Carpeta reportes:"));
        JPanel pathPanel = new JPanel(new BorderLayout(5, 0));
        txtExportPath = new JTextField();
        btnBrowse = new JButton("Examinar...");
        pathPanel.add(txtExportPath, BorderLayout.CENTER);
        pathPanel.add(btnBrowse, BorderLayout.EAST);
        center.add(pathPanel);
        // Fila 7: Tema
        center.add(new JLabel("Tema (Claro/Oscuro):"));
        cbTheme = new JComboBox<>(new String[]{"Claro", "Oscuro"});
        center.add(cbTheme);

        add(center, BorderLayout.CENTER);

        // Panel inferior con botones
        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        btnSave = new JButton("Guardar");
        btnLogout = new JButton("Cerrar sesión");
        bottom.add(btnSave);
        bottom.add(btnLogout);
        add(bottom, BorderLayout.SOUTH);

        // Listeners internos
        btnBrowse.addActionListener(e -> browseFolder());
        btnSave.addActionListener(e -> saveSettings());

        loadSettings();
    }

    /**
     * Carga la configuración desde Preferences.
     */
    public void loadSettings() {
        chkManualId.setSelected(prefs.getBoolean("manual_id", false));
        spnMinStock.setValue(prefs.getInt("stock_min", 0));
        spnMaxStock.setValue(prefs.getInt("stock_max", 100));
        spnIvaRate.setValue(prefs.getDouble("iva_rate", 16.0));
        spnIepsRate.setValue(prefs.getDouble("ieps_rate", 8.0));
        txtExportPath.setText(prefs.get("export_path",
                FileSystemView.getFileSystemView().getDefaultDirectory().getPath()));
        cbTheme.setSelectedItem(prefs.get("theme", "Claro"));
    }

    /**
     * Guarda la configuración en Preferences.
     */
    private void saveSettings() {
        prefs.putBoolean("manual_id", chkManualId.isSelected());
        prefs.putInt("stock_min", (Integer) spnMinStock.getValue());
        prefs.putInt("stock_max", (Integer) spnMaxStock.getValue());
        prefs.putDouble("iva_rate", (Double) spnIvaRate.getValue());
        prefs.putDouble("ieps_rate", (Double) spnIepsRate.getValue());
        prefs.put("export_path", txtExportPath.getText().trim());
        prefs.put("theme", (String) cbTheme.getSelectedItem());
        JOptionPane.showMessageDialog(this, "Configuración guardada.",
                "Configuración", JOptionPane.INFORMATION_MESSAGE);
    }

    /**
     * Abre un JFileChooser para seleccionar carpeta.
     */
    private void browseFolder() {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        chooser.setCurrentDirectory(new File(txtExportPath.getText()));
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            txtExportPath.setText(chooser.getSelectedFile().getAbsolutePath());
        }
    }

    /**
     * Permite a MainFrame registrar lógica de logout.
     */
    public void addLogoutListener(ActionListener listener) {
        btnLogout.addActionListener(listener);
    }

    /**
     * Permite a MainFrame registrar lógica adicional al guardar.
     */
    public void addSaveListener(ActionListener listener) {
        btnSave.addActionListener(listener);
    }

    // Getters para MainFrame
    public boolean isManualIdEnabled()  { return chkManualId.isSelected(); }
    public int getMinStock()            { return (Integer) spnMinStock.getValue(); }
    public int getMaxStock()            { return (Integer) spnMaxStock.getValue(); }
    public double getIvaRate()          { return (Double) spnIvaRate.getValue(); }
    public double getIepsRate()         { return (Double) spnIepsRate.getValue(); }
    public String getExportPath()       { return txtExportPath.getText().trim(); }
    public String getTheme()            { return (String) cbTheme.getSelectedItem(); }
}
