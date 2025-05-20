package vista;

import model.UsuarioDAO;
import model.ROLE;
import model.Usuario;
import model.BaseDatos;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.sql.SQLException;

/**
 * Diálogo de inicio de sesión que valida contra la tabla usuario en SQLite.
 */
public class LoginDialog extends JDialog {
    private JTextField txtUser;
    private JPasswordField txtPass;
    private ROLE selectedRole;

    public LoginDialog(Frame owner) {
        super(owner, "Iniciar Sesión", true);
        try {
            BaseDatos.inicializar(); // Asegura creación de tablas y config
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(owner,
                    "Error al inicializar configuración:\n" + e.getMessage(),
                    "Error Crítico", JOptionPane.ERROR_MESSAGE);
        }

        txtUser = new JTextField();
        txtPass = new JPasswordField();

        JPanel panel = new JPanel(new GridLayout(2, 2, 5, 5));
        panel.add(new JLabel("Usuario:"));
        panel.add(txtUser);
        panel.add(new JLabel("Contraseña:"));
        panel.add(txtPass);

        JButton btnLogin = new JButton("Ingresar");
        btnLogin.addActionListener(this::onLogin);

        getContentPane().setLayout(new BorderLayout(5, 5));
        getContentPane().add(panel, BorderLayout.CENTER);
        getContentPane().add(btnLogin, BorderLayout.SOUTH);

        pack();
        setLocationRelativeTo(owner);
    }

    private void onLogin(ActionEvent e) {
        String username = txtUser.getText().trim();
        String password = new String(txtPass.getPassword());
        try {
            Usuario us = new UsuarioDAO().encontrarPorUsername(username);
            if (us != null && us.getPassword().equals(password)) {
                // Asume que Usuario.getRole() ya retorna un tipo ROLE
                selectedRole = us.getRole();
                dispose();
            } else {
                JOptionPane.showMessageDialog(this,
                        "Usuario o contraseña incorrectos.",
                        "Error de autenticación",
                        JOptionPane.ERROR_MESSAGE);
                txtPass.setText("");
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this,
                    "Error al conectar con la base de datos:\n" + ex.getMessage(),
                    "Error Crítico",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Retorna el rol seleccionado tras un inicio exitoso, o null si se canceló.
     */
    public ROLE getSelectedRole() {
        return selectedRole;
    }
}
