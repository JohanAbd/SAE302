// DbManager.java
import java.sql.*;
import java.util.List;

public class DbManager {
    private String url;
    private Connection conn;

    public DbManager(String fileName) {
        this.url = "jdbc:sqlite:" + fileName;
    }

    /**
     * Ouvre la connexion et crée la table si elle n'existe pas.
     */
    public void connect() throws SQLException {
        conn = DriverManager.getConnection(url);
        createTableIfNotExists();
    }

    private void createTableIfNotExists() throws SQLException {
        String sql = "CREATE TABLE IF NOT EXISTS vulnerabilities (" +
                "id TEXT PRIMARY KEY," +
                "ip TEXT NOT NULL," +
                "port TEXT," +
                "severity TEXT," +
                "description TEXT," +
                "scan_date DATETIME DEFAULT CURRENT_TIMESTAMP" +
                ");";
        try (Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
        }
    }

    /**
     * Insert une vulnérabilité (utilise INSERT OR REPLACE pour éviter doublons d'ID).
     */
    public void saveVulnerability(Vulnerability v) throws SQLException {
        String sql = "INSERT OR REPLACE INTO vulnerabilities(id, ip, port, severity, description) VALUES(?,?,?,?,?);";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, v.getId());
            ps.setString(2, v.getIpAddress());
            ps.setString(3, v.getPort());
            ps.setString(4, v.getSeverity());
            ps.setString(5, v.getDescription());
            ps.executeUpdate();
        }
    }

    /**
     * Sauvegarde une liste.
     */
    public void saveVulnerabilities(List<Vulnerability> list) throws SQLException {
        if (list == null || list.isEmpty()) return;
        conn.setAutoCommit(false);
        try {
            for (Vulnerability v : list) {
                saveVulnerability(v);
            }
            conn.commit();
        } catch (SQLException e) {
            conn.rollback();
            throw e;
        } finally {
            conn.setAutoCommit(true);
        }
    }

    /**
     * Récupère toutes les vulnérabilités (utile pour la partie site web / Android).
     */
    public ResultSet fetchAll() throws SQLException {
        String sql = "SELECT id, ip, port, severity, description, scan_date FROM vulnerabilities ORDER BY scan_date DESC;";
        Statement st = conn.createStatement();
        return st.executeQuery(sql); // caller doit fermer le ResultSet et le Statement après usage
    }

    public void close() {
        if (conn == null) return;
        try {
            conn.close();
            conn = null;
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
