// Main.java
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

/**
 * Exemple d'utilisation :
 *   java -cp ".:sqlite-jdbc.jar" Main 192.168.1.10 vuln.db
 *
 * Si pas d'arguments : ip = 127.0.0.1, db = vulnerabilities.db
 */
public class Main {
    public static void main(String[] args) {
        String targetIp = args.length >= 1 ? args[0] : "127.0.0.1";
        String dbFile = args.length >= 2 ? args[1] : "vulnerabilities.db";

        ScannerApp scanner = new ScannerApp();
        scanner.runScan(targetIp);

        List<Vulnerability> found = scanner.getVulnerabilities();
        System.out.println("Nombre de vulnérabilités détectées : " + found.size());
        for (Vulnerability v : found) {
            System.out.println(v);
        }

        DbManager db = new DbManager(dbFile);
        try {
            db.connect();
            db.saveVulnerabilities(found);

            System.out.println("Vulns enregistrées dans la base : " + dbFile);
            // Exemple : lister les 10 dernières
            try (ResultSet rs = db.fetchAll()) {
                System.out.println("=== Contenu de la BDD (10 premiers) ===");
                int c = 0;
                while (rs.next() && c < 10) {
                    System.out.println(
                        rs.getString("scan_date") + " | " +
                        rs.getString("ip") + ":" + rs.getString("port") + " | " +
                        rs.getString("severity") + " | " +
                        rs.getString("description")
                    );
                    c++;
                }
            }
        } catch (SQLException e) {
            System.err.println("Erreur BD : " + e.getMessage());
            e.printStackTrace();
        } finally {
            db.close();
        }
    }
}
