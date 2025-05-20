package model;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;

public class VentaDAO {

    public void guardar(Venta venta) throws SQLException {
        Connection conexion = BaseDatos.obtenerConexion();
        conexion.setAutoCommit(false);
        try {
            // 1) Inserta la venta con folio, fecha, total, pago y cambio
            String sqlInsertVenta =
                    "INSERT INTO sale(folio, date, total, pago, cambio) VALUES(?, ?, ?, ?, ?)";
            try (PreparedStatement ps = conexion.prepareStatement(
                    sqlInsertVenta, Statement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, venta.getFolio());
                ps.setString(2, venta.getFecha().toString());
                ps.setDouble(3, venta.getTotal());
                ps.setDouble(4, venta.getPago());
                ps.setDouble(5, venta.getCambio());
                ps.executeUpdate();
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        venta.setId(rs.getInt(1));
                    }
                }
            }

            // 2) Inserta cada línea de venta y actualiza el stock
            String sqlInsertLinea =
                    "INSERT INTO sale_item(sale_id, product_id, quantity, price) VALUES(?,?,?,?)";
            String sqlActualizarStock =
                    "UPDATE product SET stock = stock - ? WHERE id = ?";
            for (LineaVenta linea : venta.getLineas()) {
                try (PreparedStatement psL = conexion.prepareStatement(sqlInsertLinea)) {
                    psL.setInt(1, venta.getId());
                    psL.setInt(2, linea.getProducto().getId());
                    psL.setInt(3, linea.getCantidad());
                    psL.setDouble(4, linea.getPrecio());
                    psL.executeUpdate();
                }
                try (PreparedStatement psS = conexion.prepareStatement(sqlActualizarStock)) {
                    psS.setInt(1, linea.getCantidad());
                    psS.setInt(2, linea.getProducto().getId());
                    psS.executeUpdate();
                }
            }

            conexion.commit();
        } catch (SQLException ex) {
            conexion.rollback();
            throw ex;
        } finally {
            conexion.setAutoCommit(true);
        }
    }


    public List<Venta> buscarPorFecha(LocalDate fecha) throws SQLException {
        String sql =
                "SELECT id, folio, date, total, pago, cambio " +
                        "FROM sale WHERE date LIKE ? ORDER BY date";
        try (PreparedStatement ps = BaseDatos.obtenerConexion().prepareStatement(sql)) {
            ps.setString(1, fecha.toString() + "%");
            try (ResultSet rs = ps.executeQuery()) {
                List<Venta> lista = new ArrayList<>();
                while (rs.next()) {
                    lista.add(mapearVentaBasica(rs));
                }
                return lista;
            }
        }
    }


    public List<Venta> buscarPorFechaYHorario(LocalDate fecha,
                                              String horaInicio,
                                              String horaFin) throws SQLException {
        String sql =
                "SELECT id, folio, date, total, pago, cambio " +
                        "FROM sale WHERE date >= ? AND date < ? ORDER BY date";
        try (PreparedStatement ps = BaseDatos.obtenerConexion().prepareStatement(sql)) {
            String base = fecha.toString(); // e.g. "2025-05-14"
            ps.setString(1, base + "T" + horaInicio + ":00");
            ps.setString(2, base + "T" + horaFin + ":00");
            try (ResultSet rs = ps.executeQuery()) {
                List<Venta> lista = new ArrayList<>();
                while (rs.next()) {
                    lista.add(mapearVentaBasica(rs));
                }
                return lista;
            }
        }
    }


    public List<Venta> buscarPorFolio(String prefijo) throws SQLException {
        String sql =
                "SELECT id, folio, date, total, pago, cambio " +
                        "FROM sale WHERE folio LIKE ? ORDER BY date";
        try (PreparedStatement ps = BaseDatos.obtenerConexion().prepareStatement(sql)) {
            ps.setString(1, prefijo + "%");
            try (ResultSet rs = ps.executeQuery()) {
                List<Venta> lista = new ArrayList<>();
                while (rs.next()) {
                    lista.add(mapearVentaBasica(rs));
                }
                return lista;
            }
        }
    }


    public List<Venta> buscarPorMes(YearMonth ym) throws SQLException {
        String patron = ym.toString() + "-%"; // e.g. "2025-05-%"
        String sql =
                "SELECT id, folio, date, total, pago, cambio " +
                        "FROM sale WHERE date LIKE ? ORDER BY date";
        try (PreparedStatement ps = BaseDatos.obtenerConexion().prepareStatement(sql)) {
            ps.setString(1, patron);
            try (ResultSet rs = ps.executeQuery()) {
                List<Venta> lista = new ArrayList<>();
                while (rs.next()) {
                    lista.add(mapearVentaBasica(rs));
                }
                return lista;
            }
        }
    }


    public Venta obtenerVentaConLineas(int ventaId) throws SQLException {
        Venta venta = null;

        // Cabecera
        String sqlV =
                "SELECT id, folio, date, total, pago, cambio FROM sale WHERE id = ?";
        try (PreparedStatement ps = BaseDatos.obtenerConexion().prepareStatement(sqlV)) {
            ps.setInt(1, ventaId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    venta = mapearVentaBasica(rs);
                }
            }
        }
        if (venta == null) {
            return null;
        }

        // Líneas
        String sqlL =
                "SELECT si.quantity, si.price, p.id, p.name, p.stock, p.category, p.brand, p.size, p.image " +
                        "FROM sale_item si " +
                        "JOIN product p ON p.id = si.product_id " +
                        "WHERE si.sale_id = ?";
        try (PreparedStatement ps = BaseDatos.obtenerConexion().prepareStatement(sqlL)) {
            ps.setInt(1, ventaId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Producto prod = new Producto(
                            rs.getInt("id"),
                            rs.getString("name"),
                            rs.getDouble("price"),
                            rs.getInt("stock"),
                            rs.getString("category"),
                            rs.getString("brand"),
                            rs.getString("size"),
                            rs.getString("image")
                    );
                    LineaVenta lv = new LineaVenta(prod, rs.getInt("quantity"));
                    venta.getLineas().add(lv);
                }
            }
        }

        return venta;
    }


    public List<Venta> obtenerTodasVentasConTotal() throws SQLException {
        List<Venta> lista = new ArrayList<>();
        String sql =
                "SELECT id, folio, date, total, pago, cambio " +
                        "FROM sale ORDER BY date DESC";
        try (Connection conn = BaseDatos.obtenerConexion();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                lista.add(mapearVentaBasica(rs));
            }
        }
        return lista;
    }


    private Venta mapearVentaBasica(ResultSet rs) throws SQLException {
        Venta v = new Venta();
        v.setId(rs.getInt("id"));
        v.setFolio(rs.getString("folio"));
        v.setFecha(LocalDateTime.parse(rs.getString("date")));
        v.setTotal(rs.getDouble("total"));
        v.setPago(rs.getDouble("pago"));
        v.setCambio(rs.getDouble("cambio"));
        return v;
    }
}
