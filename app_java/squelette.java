import java.sql.*;
import java.util.List;

import java.util.ArrayList;
import java.util.List;


public class Vulnerability {
    private String id;
    private String description;
    private String severity;
    private String ipAddress;
    private String port;

    public Vulnerability(String id, String description, String severity, String ipAddress, String port) {
        this.id = id;
        this.description = description;
        this.severity = severity;
        this.ipAddress = ipAddress;
        this.port = port;
    }

    //getters and setters

}


public class ScannerApp {
    private List<Vulnerability> vulnerabilities;

    public ScannerApp() {
        vulnerabilities = new ArrayList<>();
    }

    public void runScan() {
        //Méthode permettant d'analyser une machine et détecter les failles
        vulnerabilities.add(new Vulnerability("1", "Port ouvert SSH", "High", "192.168.1.10", "22"));
        vulnerabilities.add(new Vulnerability("2", "Service HTTP non sécurisé", "Medium", "192.168.1.20", "80"));
    }

    //getters et setters

}

public class DbManager {
    private String url;
    private Connection conn;

    public DbManager(String fileName) {
        this.url = "jdbc:sqlite:" + fileName;
    }

    public void connect() {
        //Methode permettant la connexion avec la base de données
    }

    public void saveVulnerabilities(List<Vulnerability> vulns) {
        //Méthode permattant d'enregistrer une vulnérabilité dans la base de données SQLite
    }

    public void close() {
        //Pour fermer la connexion
    }
}