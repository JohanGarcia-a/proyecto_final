package vista;

import model.ROLE;
import model.Usuario;
import model.UsuarioDAO;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.List;

/**
 * Panel para gestión de usuarios con diseño optimizado.
 */
public class UserManagementPanel extends JPanel {
    private JTable tblUsers;
    private JTextField txtUser;
    private JPasswordField txtPass;
    private JComboBox<ROLE> cbRole;
    private UsuarioDAO dao = new UsuarioDAO();

    public UserManagementPanel() {
        setLayout(new BorderLayout(10, 10));

        // Tabla de usuarios
        DefaultTableModel model = new DefaultTableModel(new String[]{"Usuario", "Rol"}, 0);
        tblUsers = new JTable(model);
        tblUsers.setBackground(new Color(245, 245, 220));
        refreshTable();
        add(new JScrollPane(tblUsers), BorderLayout.CENTER);

        // Formulario de entrada
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(new Color(250, 235, 215));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;

        // Etiqueta y campo Usuario
        gbc.gridx = 0;
        gbc.gridy = 0;
        formPanel.add(new JLabel("Usuario:"), gbc);
        txtUser = new JTextField(15);
        gbc.gridx = 1;
        formPanel.add(txtUser, gbc);

        // Etiqueta y campo Contraseña
        gbc.gridx = 0;
        gbc.gridy = 1;
        formPanel.add(new JLabel("Contraseña:"), gbc);
        txtPass = new JPasswordField(15);
        gbc.gridx = 1;
        formPanel.add(txtPass, gbc);

        // Etiqueta y combo de Rol
        gbc.gridx = 0;
        gbc.gridy = 2;
        formPanel.add(new JLabel("Rol:"), gbc);
        cbRole = new JComboBox<>(ROLE.values());
        gbc.gridx = 1;
        formPanel.add(cbRole, gbc);

        // Botones de acción
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        buttonPanel.setBackground(new Color(250, 235, 215));
        JButton btnAdd = new JButton("Agregar");
        btnAdd.setBackground(Color.GREEN);
        btnAdd.addActionListener(this::onAdd);
        JButton btnUpdate = new JButton("Actualizar");
        btnUpdate.setBackground(Color.ORANGE);
        btnUpdate.addActionListener(this::onUpdate);
        JButton btnDelete = new JButton("Eliminar");
        btnDelete.setBackground(Color.RED);
        btnDelete.addActionListener(this::onDelete);
        JButton btnClear = new JButton("Limpiar");
        btnClear.setBackground(new Color(173, 216, 230));
        btnClear.addActionListener(e -> clearForm());
        buttonPanel.add(btnAdd);
        buttonPanel.add(btnUpdate);
        buttonPanel.add(btnDelete);
        buttonPanel.add(btnClear);

        // Panel inferior contenedor de formulario y botones
        JPanel southPanel = new JPanel(new BorderLayout());
        southPanel.add(formPanel, BorderLayout.CENTER);
        southPanel.add(buttonPanel, BorderLayout.SOUTH);

        add(southPanel, BorderLayout.SOUTH);
    }

    public void refreshTable() {
        DefaultTableModel m = (DefaultTableModel) tblUsers.getModel();
        m.setRowCount(0);
        try {
            List<Usuario> list = dao.encontrarTodos();
            for (Usuario u : list) {
                m.addRow(new Object[]{u.getUsername(), u.getRole()});
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error al cargar usuarios:\n" + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void onAdd(ActionEvent e) {
        try {
            dao.insertar(new Usuario(0,
                    txtUser.getText().trim(),
                    new String(txtPass.getPassword()),
                    (ROLE) cbRole.getSelectedItem()));
            refreshTable();
            clearForm();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error al agregar usuario:\n" + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void onUpdate(ActionEvent e) {
        try {
            dao.actualizar(new Usuario(0,
                    txtUser.getText().trim(),
                    new String(txtPass.getPassword()),
                    (ROLE) cbRole.getSelectedItem()));
            refreshTable();
            clearForm();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error al actualizar usuario:\n" + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void onDelete(ActionEvent e) {
        try {
            dao.eliminar(txtUser.getText().trim());
            refreshTable();
            clearForm();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error al eliminar usuario:\n" + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void clearForm() {
        txtUser.setText("");
        txtPass.setText("");
        cbRole.setSelectedIndex(0);
    }
}