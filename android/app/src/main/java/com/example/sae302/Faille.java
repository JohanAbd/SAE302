package com.example.sae302;

public class Faille {
    private String id;
    private String ip;
    private String port;
    private String severity;
    private String description;
    private String scanDate;

    public Faille(String id, String ip, String port, String severity, String description, String scanDate) {
        this.id = id;
        this.ip = ip;
        this.port = port;
        this.severity = severity;
        this.description = description;
        this.scanDate = scanDate;
    }

    // Getters nécessaires pour l'affichage
    public String getIp() { return ip; }
    public String getPort() { return port; }
    public String getSeverity() { return severity; }
    public String getDescription() { return description; }
    public String getScanDate() { return scanDate; }
}