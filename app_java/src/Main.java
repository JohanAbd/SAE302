// --- Main.java ---
public class Main {
    public static void main(String[] args) {
        ScannerApp scanner = new ScannerApp();
        scanner.runScan();

        DbManager db = new DbManager("vulnerabilities.db");
        db.connect();
        db.saveVulnerabilities(scanner.getVulnerabilities());
        db.close();
    }
}
