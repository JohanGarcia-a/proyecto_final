package model;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ProductoDAO {
    private static final String TABLE = "product";


    public void insertar(Producto p) throws SQLException {
        String sql = "INSERT INTO " + TABLE + " (name, price, stock, category, brand, size, image) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = BaseDatos.obtenerConexion()
                .prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, p.getNombre());
            ps.setDouble(2, p.getPrecio());
            ps.setInt(3, p.getExistencias());
            ps.setString(4, p.getCategoria());
            ps.setString(5, p.getMarca());
            ps.setString(6, p.getTamano());
            ps.setString(7, p.getRutaImagen());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    p.setId(rs.getInt(1));
                }
            }
        }
    }

    public void actualizar(Producto p) throws SQLException {
        String sql = "UPDATE " + TABLE + " SET "
                + "name = ?, price = ?, stock = ?, category = ?, brand = ?, size = ?, image = ? "
                + "WHERE id = ?";
        try (PreparedStatement ps = BaseDatos.obtenerConexion().prepareStatement(sql)) {
            ps.setString(1, p.getNombre());
            ps.setDouble(2, p.getPrecio());
            ps.setInt(3, p.getExistencias());
            ps.setString(4, p.getCategoria());
            ps.setString(5, p.getMarca());
            ps.setString(6, p.getTamano());
            ps.setString(7, p.getRutaImagen());
            ps.setInt(8, p.getId());
            ps.executeUpdate();
        }
    }

    public void eliminar(int id) throws SQLException {
        String sql = "DELETE FROM " + TABLE + " WHERE id = ?";
        try (PreparedStatement ps = BaseDatos.obtenerConexion().prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    public List<Producto> encontrarTodos() throws SQLException {
        List<Producto> lista = new ArrayList<>();
        String sql = "SELECT id, name, price, stock, category, brand, size, image "
                + "FROM " + TABLE + " ORDER BY name";
        try (Statement st = BaseDatos.obtenerConexion().createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                lista.add(new Producto(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getDouble("price"),
                        rs.getInt("stock"),
                        rs.getString("category"),
                        rs.getString("brand"),
                        rs.getString("size"),
                        rs.getString("image")
                ));
            }
        }
        return lista;
    }

    public List<Producto> encontrarPorCategoria(String categoria) throws SQLException {
        List<Producto> lista = new ArrayList<>();
        String sql = "SELECT id, name, price, stock, category, brand, size, image "
                + "FROM " + TABLE + " WHERE category = ?";
        try (PreparedStatement ps = BaseDatos.obtenerConexion().prepareStatement(sql)) {
            ps.setString(1, categoria);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(new Producto(
                            rs.getInt("id"),
                            rs.getString("name"),
                            rs.getDouble("price"),
                            rs.getInt("stock"),
                            rs.getString("category"),
                            rs.getString("brand"),
                            rs.getString("size"),
                            rs.getString("image")
                    ));
                }
            }
        }
        return lista;
    }

    public Producto encontrarPorId(int id) throws SQLException {
        String sql = "SELECT id, name, price, stock, category, brand, size, image "
                + "FROM " + TABLE + " WHERE id = ?";
        try (PreparedStatement ps = BaseDatos.obtenerConexion().prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new Producto(
                            rs.getInt("id"),
                            rs.getString("name"),
                            rs.getDouble("price"),
                            rs.getInt("stock"),
                            rs.getString("category"),
                            rs.getString("brand"),
                            rs.getString("size"),
                            rs.getString("image")
                    );
                }
            }
        }
        return null;
    }

    public Producto encontrarPorNombre(String name) throws SQLException {
        String sql = "SELECT id, name, price, stock, category, brand, size, image "
                + "FROM " + TABLE + " WHERE name = ? COLLATE NOCASE LIMIT 1";
        try (PreparedStatement ps = BaseDatos.obtenerConexion().prepareStatement(sql)) {
            ps.setString(1, name);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new Producto(
                            rs.getInt("id"),
                            rs.getString("name"),
                            rs.getDouble("price"),
                            rs.getInt("stock"),
                            rs.getString("category"),
                            rs.getString("brand"),
                            rs.getString("size"),
                            rs.getString("image")
                    );
                }
            }
        }
        return null;
    }

    public List<String> nombresContienen(String texto) throws SQLException {
        List<String> nombres = new ArrayList<>();
        String sql = "SELECT DISTINCT name FROM " + TABLE
                + " WHERE name LIKE ? COLLATE NOCASE ORDER BY name LIMIT 20";
        try (PreparedStatement ps = BaseDatos.obtenerConexion().prepareStatement(sql)) {
            ps.setString(1, "%" + texto + "%");
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    nombres.add(rs.getString(1));
                }
            }
        }
        return nombres;
    }

    public List<String> encontrarTodasCategorias() throws SQLException {
        List<String> cats = new ArrayList<>();
        String sql = "SELECT DISTINCT category FROM " + TABLE + " ORDER BY category";
        try (Statement st = BaseDatos.obtenerConexion().createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                cats.add(rs.getString(1));
            }
        }
        return cats;
    }

    public List<String> encontrarTodasMarcas() throws SQLException {
        List<String> brands = new ArrayList<>();
        String sql = "SELECT DISTINCT brand FROM " + TABLE + " "
                + "WHERE brand <> '' ORDER BY brand";
        try (Statement st = BaseDatos.obtenerConexion().createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                brands.add(rs.getString(1));
            }
        }
        return brands;
    }

    public void reducirExistencias(int productoId, int cantidad) throws SQLException {
        String sql = "UPDATE " + TABLE + " SET stock = CASE "
                + "    WHEN stock >= ? THEN stock - ? "
                + "    ELSE stock "
                + "END WHERE id = ?";
        try (PreparedStatement ps = BaseDatos.obtenerConexion().prepareStatement(sql)) {
            ps.setInt(1, cantidad);
            ps.setInt(2, cantidad);
            ps.setInt(3, productoId);
            ps.executeUpdate();
        }
    }
}
