import java.util.ArrayList;
import java.util.List;

public class ScannerApp {
    private List<Vulnerability> vulnerabilities;

    public ScannerApp() {
        vulnerabilities = new ArrayList<>();
    }

    public void runScan() {
        // TODO : analyser le réseau et détecter les failles
        vulnerabilities.add(new Vulnerability("1", "Port ouvert SSH", "High", "192.168.1.10", "22"));
        vulnerabilities.add(new Vulnerability("2", "Service HTTP non sécurisé", "Medium", "192.168.1.20", "80"));
    }

    public List<Vulnerability> getVulnerabilities() {
        return vulnerabilities;
    }
}
