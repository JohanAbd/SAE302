// ScannerApp.java
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Lance nmap --script vuln et récupère des objets Vulnerability.
 */
public class ScannerApp {
    private List<Vulnerability> vulnerabilities;
    // Si nécessaire, remplace "nmap" par le chemin complet vers nmap.exe sous Windows
    private static final String NMAP_COMMAND = "nmap";

    public ScannerApp() {
        this.vulnerabilities = new ArrayList<>();
    }

    /**
     * Lancer un scan nmap --script vuln sur targetIp.
     * Le parsing est basique : on détecte les lignes de port (ex: "80/tcp open http")
     * et les lignes indiquant des vulnérabilités (CVE-, VULNERABLE, ou lignes indentées issues des scripts).
     */
    public void runScan(String targetIp) {
        System.out.println("Début du scan Nmap sur " + targetIp + "...");
        List<String> command = new ArrayList<>();
        command.add(NMAP_COMMAND);
        command.add("-sV");
        command.add("--script");
        command.add("vuln");
        command.add(targetIp);

        try {
            ProcessBuilder pb = new ProcessBuilder(command);
            pb.redirectErrorStream(true);
            Process process = pb.start();

            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            String currentPort = "unknown";
            // Pattern pour détecter une ligne de port : ex "80/tcp open http"
            Pattern portPattern = Pattern.compile("^(\\d+)\\/tcp\\s+([a-zA-Z]+).*");
            // Pattern pour lignes indentées types des scripts (|   VULNERABLE: ...)
            Pattern indentedPattern = Pattern.compile("^\\s*\\|\\s*(.*)$");

            StringBuilder vulnBuffer = new StringBuilder(); // collecte lignes de description multi-lignes
            boolean insideVulnBlock = false;

            while ((line = reader.readLine()) != null) {
                System.out.println("[NMAP] " + line);

                // Détecte les lignes de port/service
                Matcher m = portPattern.matcher(line);
                if (m.find()) {
                    currentPort = m.group(1);
                    // fin d'un possible bloc vuln précédent -> flush si besoin
                    if (insideVulnBlock && vulnBuffer.length() > 0) {
                        addVulnFromBuffer(targetIp, currentPort, vulnBuffer.toString());
                        vulnBuffer.setLength(0);
                        insideVulnBlock = false;
                    }
                    continue;
                }

                // Lignes marquant explicitement vulnérabilité
                if (line.contains("VULNERABLE") || line.contains("CVE-") || line.toLowerCase().contains("vulnerab") ) {
                    // flush buffer si exist
                    if (vulnBuffer.length() > 0) {
                        vulnBuffer.append("\n");
                    }
                    vulnBuffer.append(line.trim());
                    insideVulnBlock = true;
                    // ajoute immédiatement (on veut au moins une ident)
                    addVulnFromBuffer(targetIp, currentPort, vulnBuffer.toString());
                    vulnBuffer.setLength(0);
                    insideVulnBlock = false;
                    continue;
                }

                // Lignes indentées produites par les scripts (commencent souvent par "|")
                Matcher mi = indentedPattern.matcher(line);
                if (mi.find()) {
                    String content = mi.group(1).trim();
                    // si c'est une ligne informative suite de vuln block -> accumuler
                    if (content.length() > 0) {
                        if (vulnBuffer.length() > 0) vulnBuffer.append("\n");
                        vulnBuffer.append(content);
                        insideVulnBlock = true;
                    }
                    // si on voit des marqueurs de CVE à l'intérieur
                    if (content.contains("CVE-") || content.toLowerCase().contains("vulnerab")) {
                        addVulnFromBuffer(targetIp, currentPort, vulnBuffer.toString());
                        vulnBuffer.setLength(0);
                        insideVulnBlock = false;
                    }
                } else {
                    // ligne non indentée : si on était dans un bloc vuln, on flush
                    if (insideVulnBlock && vulnBuffer.length() > 0) {
                        addVulnFromBuffer(targetIp, currentPort, vulnBuffer.toString());
                        vulnBuffer.setLength(0);
                        insideVulnBlock = false;
                    }
                }
            }

            // flush final
            if (insideVulnBlock && vulnBuffer.length() > 0) {
                addVulnFromBuffer(targetIp, currentPort, vulnBuffer.toString());
            }

            int exitCode = process.waitFor();
            System.out.println("Scan terminé (Code de sortie : " + exitCode + ")");
        } catch (Exception e) {
            System.err.println("Erreur lors de l'exécution du scan : " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Convertit le buffer en objet Vulnerability et l'ajoute à la liste.
     */
    private void addVulnFromBuffer(String targetIp, String port, String description) {
        if (description == null || description.trim().isEmpty()) return;
        String uniqueId = UUID.randomUUID().toString();
        String severity = inferSeverity(description);
        Vulnerability v = new Vulnerability(uniqueId, description.trim(), severity, targetIp, port);
        vulnerabilities.add(v);
        System.out.println("=> Vuln détectée : " + v);
    }

    /**
     * Règles heuristiques simples pour deviner la sévérité.
     */
    private String inferSeverity(String desc) {
        String d = desc.toLowerCase();
        if (d.contains("critical") || d.contains("remote code execution") || d.contains("authentication bypass") || d.contains("dos") )
            return "Critical";
        if (d.contains("high") || d.contains("unauthenticated") || d.contains("vulnerab") || d.contains("cve-"))
            return "High";
        if (d.contains("medium") || d.contains("exposure") )
            return "Medium";
        return "Low";
    }

    public List<Vulnerability> getVulnerabilities() {
        return vulnerabilities;
    }
}
