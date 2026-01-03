import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.security.MessageDigest;
import java.util.Base64;

public class DbManager {
    private String url = "jdbc:sqlite:bd.db";
    private Connection conn;

    // Constructeur qui accepte un nom de fichier (pour corriger l'erreur de ApiHandler et Main)
    public DbManager(String fileName) {
        this.url = "jdbc:sqlite:" + fileName;
    }

    // Constructeur sans argument (par défaut)
    public DbManager() {}

    public void connect() throws SQLException {
        conn = DriverManager.getConnection(url);
        
        // Création table USERS
        String sqlUsers = "CREATE TABLE IF NOT EXISTS users (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "username TEXT UNIQUE," +
                "password_hash TEXT," +
                "salt TEXT);";
        
        // Création table VULNERABILITIES
        String sqlVulns = "CREATE TABLE IF NOT EXISTS vulnerabilities (" +
                "id TEXT PRIMARY KEY," +
                "ip TEXT NOT NULL," +
                "port TEXT," +
                "severity TEXT," +
                "description TEXT," +
                "scan_date DATETIME DEFAULT CURRENT_TIMESTAMP);";
        
        try (Statement stmt = conn.createStatement()) {
            stmt.execute(sqlUsers);
            stmt.execute(sqlVulns);
        }
        
        // Créer l'admin par défaut si la table est vide
        createDefaultUser();
    }

    private void createDefaultUser() {
        try {
            String sql = "SELECT count(*) FROM users";
            Statement st = conn.createStatement();
            if (st.executeQuery(sql).getInt(1) == 0) {
                registerUser("admin", "admin123");
                System.out.println("[DB] Compte par défaut créé : admin / admin123");
            }
        } catch (Exception e) {}
    }

    public void registerUser(String user, String pass) throws Exception {
        String salt = "SAE302_SALT";
        String hash = hashPassword(pass, salt);
        String sql = "INSERT INTO users(username, password_hash, salt) VALUES(?,?,?);";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, user);
            ps.setString(2, hash);
            ps.setString(3, salt);
            ps.executeUpdate();
        }
    }

    public boolean authenticate(String user, String pass) {
        try {
            String sql = "SELECT password_hash, salt FROM users WHERE username = ?;";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, user);
                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    String storedHash = rs.getString("password_hash");
                    String salt = rs.getString("salt");
                    return storedHash.equals(hashPassword(pass, salt));
                }
            }
        } catch (Exception e) { e.printStackTrace(); }
        return false;
    }

    private String hashPassword(String pass, String salt) throws Exception {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        md.update(salt.getBytes());
        byte[] bytes = md.digest(pass.getBytes());
        return Base64.getEncoder().encodeToString(bytes);
    }

    public void saveVulnerabilities(List<Vulnerability> list) throws SQLException {
        if (list == null) return;
        String sql = "INSERT OR REPLACE INTO vulnerabilities(id, ip, port, severity, description) VALUES(?,?,?,?,?);";
        conn.setAutoCommit(false);
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            for (Vulnerability v : list) {
                ps.setString(1, v.getId());
                ps.setString(2, v.getIpAddress());
                ps.setString(3, v.getPort());
                ps.setString(4, v.getSeverity());
                ps.setString(5, v.getDescription());
                ps.addBatch();
            }
            ps.executeBatch();
            conn.commit();
        } catch (SQLException e) {
            conn.rollback();
            throw e;
        } finally {
            conn.setAutoCommit(true);
        }
    }

    public ResultSet fetchAll() throws SQLException {
        return conn.createStatement().executeQuery("SELECT * FROM vulnerabilities ORDER BY scan_date DESC");
    }

    public void close() {
        try {
            if (conn != null) conn.close();
        } catch (SQLException e) { e.printStackTrace(); }
    }
}
