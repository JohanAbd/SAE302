import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ScannerApp {
    private List<Vulnerability> vulnerabilities;

    public ScannerApp() {
        vulnerabilities = new ArrayList<>();
    }

    @param targetIp
    public void runScan(String targetIp) {
        System.out.println("Début du scan Nmap sur " + targetIp + "...");
        
        // Construction de la commande : nmap -sV --script vuln <IP>
        // Note : Sur Windows, il faut souvent préciser le chemin complet vers nmap.exe si pas dans le PATH
        // ex: "C:\\Program Files (x86)\\Nmap\\nmap.exe"
        List<String> command = new ArrayList<>();
        command.add("nmap"); // ou "nmap.exe" sous Windows
        command.add("-sV");
        command.add("--script");
        command.add("vuln");
        command.add(targetIp);

        try {
            ProcessBuilder pb = new ProcessBuilder(command);
            pb.redirectErrorStream(true); // Fusionne la sortie erreur avec la sortie standard
            Process process = pb.start();

            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            String currentPort = "Unknown";

            while ((line = reader.readLine()) != null) {
                // Affichage optionnel pour voir ce qui se passe dans la console
                System.out.println("[NMAP] " + line);

                // 1. Détection du port (ex: "80/tcp open http")
                if (line.matches("^\\d+/tcp.*")) {
                    currentPort = line.split("/")[0]; // Récupère "80"
                }

                // 2. Détection d'une vulnérabilité
                // On cherche des mots clés comme "VULNERABLE", "CVE-" ou "Issue"
                if (line.contains("VULNERABLE") || line.contains("CVE-") || line.contains("| _")) {
                    
                    // Nettoyage de la ligne (enlève les | et espaces inutiles)
                    String cleanDesc = line.replace("|", "").trim();
                    
                    // Création de l'objet Vulnerability
                    // On génère un ID unique aléatoire car Nmap ne donne pas d'ID simple séquentiel
                    String uniqueId = UUID.randomUUID().toString();
                    
                    // Définition de la sévérité (Simplifiée ici, on met High par défaut si trouvé)
                    String severity = "High";
                    if(cleanDesc.toLowerCase().contains("info")) severity = "Low";

                    vulnerabilities.add(new Vulnerability(
                        uniqueId, 
                        cleanDesc, 
                        severity, 
                        targetIp, 
                        currentPort
                    ));
                }
            }
            
            int exitCode = process.waitFor();
            System.out.println("Scan terminé (Code de sortie : " + exitCode + ")");

        } catch (Exception e) {
            System.err.println("Erreur lors de l'exécution du scan : " + e.getMessage());
            e.printStackTrace();
        }
    }

    public List<Vulnerability> getVulnerabilities() {
        return vulnerabilities;
    }
}
