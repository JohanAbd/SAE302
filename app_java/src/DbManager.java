import java.sql.*;
import java.util.List;

public class DbManager {
    private String url;
    private Connection conn;

    public DbManager(String fileName) {
        this.url = "jdbc:sqlite:" + fileName;
    }

    public void connect() {
        try {
            conn = DriverManager.getConnection(url);
            System.out.println("Connexion SQLite établie.");
        } catch (SQLException e) {
            System.out.println("Erreur de connexion : " + e.getMessage());
        }
    }

    public void createTableIfNotExists() {
        String sql = """
            CREATE TABLE IF NOT EXISTS vulnerabilities (
                id TEXT PRIMARY KEY,
                description TEXT NOT NULL,
                severity TEXT,
                ip TEXT,
                port TEXT
            )
            """;
        try (Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
        } catch (SQLException e) {
            System.out.println("Erreur création table : " + e.getMessage());
        }
    }

    public void saveVulnerabilities(List<Vulnerability> vulns) {
        String sql = "INSERT OR REPLACE INTO vulnerabilities(id, description, severity, ip, port) VALUES(?,?,?,?,?)";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            for (Vulnerability v : vulns) {
                pstmt.setString(1, v.getId());
                pstmt.setString(2, v.getDescription());
                pstmt.setString(3, v.getSeverity());
                pstmt.setString(4, v.getIpAddress());
                pstmt.setString(5, v.getPort());
                pstmt.executeUpdate();
            }
            System.out.println("Failles enregistrées en BDD.");
        } catch (SQLException e) {
            System.out.println("Erreur d'insertion : " + e.getMessage());
        }
    }

    public void close() {
        try {
            if (conn != null) conn.close();
        } catch (SQLException e) {
            System.out.println("Erreur fermeture connexion : " + e.getMessage());
        }
    }
}
