package model;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;

public class TicketDiarioDAO {

    public int obtenerUltimoTicketDelDia(LocalDate fecha) throws SQLException {
        String sql = "SELECT ultimo_ticket FROM TicketDiario WHERE fecha = ?";
        try (PreparedStatement ps = BaseDatos.obtenerConexion().prepareStatement(sql)) {
            ps.setString(1, fecha.toString());
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        }
    }

    public void guardarUltimoTicketDelDia(LocalDate fecha, int ultimoTicket) throws SQLException {
        String sql =
                "INSERT INTO TicketDiario(fecha, ultimo_ticket) VALUES(?, ?) " +
                        "ON CONFLICT(fecha) DO UPDATE SET ultimo_ticket = excluded.ultimo_ticket";
        try (PreparedStatement ps = BaseDatos.obtenerConexion().prepareStatement(sql)) {
            ps.setString(1, fecha.toString());
            ps.setInt(2, ultimoTicket);
            ps.executeUpdate();
        }
    }
}
