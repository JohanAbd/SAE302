import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        // Demander à l'utilisateur quelle IP scanner
        Scanner console = new Scanner(System.in);
        System.out.print("Entrez l'adresse IP cible (ex: 127.0.0.1) : ");
        String targetIp = console.nextLine();
        console.close();

        // 1. Initialiser le scanner
        ScannerApp scanner = new ScannerApp();
        
        // 2. Lancer le scan (cette méthode prend du temps !)
        scanner.runScan(targetIp);

        // 3. Sauvegarder dans la base de données SQLite
        DbManager db = new DbManager("sae3.02.db");
        db.connect();
        db.createTableIfNotExists(); // Important : créer la table si elle n'existe pas
        
        // On récupère la liste remplie par le scan
        db.saveVulnerabilities(scanner.getVulnerabilities());
        
        db.close();
    }
}
