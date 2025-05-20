package model;

import model.ROLE;
import model.Usuario;
import model.BaseDatos;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UsuarioDAO {
    public List<Usuario> encontrarTodos() throws SQLException {
        List<Usuario> lista = new ArrayList<>();
        String sql = "SELECT id, username, password, role FROM usuario";
        try (Connection conn = BaseDatos.obtenerConexion();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Usuario u = new Usuario(
                        rs.getInt("id"),
                        rs.getString("username"),
                        rs.getString("password"),
                        ROLE.valueOf(rs.getString("role"))
                );
                lista.add(u);
            }
        }
        return lista;
    }

    public Usuario encontrarPorUsername(String username) throws SQLException {
        String sql = "SELECT id, username, password, role FROM usuario WHERE username = ?";
        try (Connection conn = BaseDatos.obtenerConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new Usuario(
                            rs.getInt("id"),
                            rs.getString("username"),
                            rs.getString("password"),
                            ROLE.valueOf(rs.getString("role"))
                    );
                }
            }
        }
        return null;
    }

    public void insertar(Usuario u) throws SQLException {
        String sql = "INSERT INTO usuario(username, password, role) VALUES(?, ?, ?)";
        try (Connection conn = BaseDatos.obtenerConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, u.getUsername());
            ps.setString(2, u.getPassword());
            ps.setString(3, u.getRole().name());
            ps.executeUpdate();
        }
    }

    public void actualizar(Usuario u) throws SQLException {
        String sql = "UPDATE usuario SET password = ?, role = ? WHERE username = ?";
        try (Connection conn = BaseDatos.obtenerConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, u.getPassword());
            ps.setString(2, u.getRole().name());
            ps.setString(3, u.getUsername());
            ps.executeUpdate();
        }
    }

    public void eliminar(String username) throws SQLException {
        String sql = "DELETE FROM usuario WHERE username = ?";
        try (Connection conn = BaseDatos.obtenerConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            ps.executeUpdate();
        }
    }
}
