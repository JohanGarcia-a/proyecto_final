package model;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class BaseDatos {
    private static final String URL = "jdbc:sqlite:pos_copia.db";
    private static Connection conn;

    static {
        try {
            inicializar();
            System.out.println("Base de datos pos_copia.db inicializada correctamente.");
        } catch (SQLException e) {
            System.err.println("[BaseDatos] Error al inicializar la base de datos: " + e.getMessage());
        }
    }

    public static Connection obtenerConexion() throws SQLException {
        if (conn == null || conn.isClosed()) {
            conn = DriverManager.getConnection(URL);
            System.out.println("Conectado a SQLite en pos_copia.db");
        }
        return conn;
    }

    public static void inicializar() throws SQLException {
        try (Connection tmp = DriverManager.getConnection(URL);
             Statement st = tmp.createStatement()) {

            // 1) Tabla usuario
            st.executeUpdate(
                    "CREATE TABLE IF NOT EXISTS usuario (" +
                            "  id INTEGER PRIMARY KEY AUTOINCREMENT," +
                            "  username TEXT UNIQUE NOT NULL," +
                            "  password TEXT NOT NULL," +
                            "  role TEXT NOT NULL" +
                            ");"
            );
            // Semilla de administrador por defecto
            st.executeUpdate(
                    "INSERT OR IGNORE INTO usuario(username,password,role) VALUES('admin','1234','ADMINISTRATOR');"
            );

            // 2) Tabla product
            st.executeUpdate(
                    "CREATE TABLE IF NOT EXISTS product (" +
                            "  id       INTEGER PRIMARY KEY AUTOINCREMENT," +
                            "  name     TEXT    NOT NULL," +
                            "  price    REAL    NOT NULL," +
                            "  stock    INTEGER NOT NULL," +
                            "  category TEXT    NOT NULL DEFAULT 'Sin categoría'," +
                            "  brand    TEXT    DEFAULT ''," +
                            "  size     TEXT    DEFAULT ''," +
                            "  image    TEXT    DEFAULT ''" +
                            ");"
            );

            // 3) Tabla sale
            st.executeUpdate(
                    "CREATE TABLE IF NOT EXISTS sale (" +
                            "  id    INTEGER PRIMARY KEY AUTOINCREMENT," +
                            "  date  TEXT    NOT NULL," +
                            "  total REAL    NOT NULL" +
                            ");"
            );
            // Agregar columnas folio, pago, cambio si no existen
            try {
                st.executeUpdate(
                        "ALTER TABLE sale ADD COLUMN folio TEXT NOT NULL DEFAULT '';"
                );
            } catch (SQLException ignore) {
            }
            try {
                st.executeUpdate(
                        "ALTER TABLE sale ADD COLUMN pago REAL NOT NULL DEFAULT 0;"
                );
            } catch (SQLException ignore) {
            }
            try {
                st.executeUpdate(
                        "ALTER TABLE sale ADD COLUMN cambio REAL NOT NULL DEFAULT 0;"
                );
            } catch (SQLException ignore) {
            }

            // 4) Tabla sale_item
            st.executeUpdate(
                    "CREATE TABLE IF NOT EXISTS sale_item (" +
                            "  id         INTEGER PRIMARY KEY AUTOINCREMENT," +
                            "  sale_id    INTEGER NOT NULL," +
                            "  product_id INTEGER NOT NULL," +
                            "  quantity   INTEGER NOT NULL," +
                            "  price      REAL    NOT NULL," +
                            "  FOREIGN KEY (sale_id)    REFERENCES sale(id)," +
                            "  FOREIGN KEY (product_id) REFERENCES product(id)" +
                            ");"
            );

            // 5) Índice para búsqueda de productos por nombre
            st.executeUpdate(
                    "CREATE INDEX IF NOT EXISTS idx_product_name ON product(name COLLATE NOCASE);"
            );

            // 6) Tabla TicketDiario (contador diario de folios)
            st.executeUpdate(
                    "CREATE TABLE IF NOT EXISTS TicketDiario (" +
                            "  fecha          TEXT PRIMARY KEY," +
                            "  ultimo_ticket  INTEGER NOT NULL" +
                            ");"
            );
        }
    }
}