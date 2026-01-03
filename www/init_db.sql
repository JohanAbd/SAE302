-- Initialisation de la base de données SAE302

-- 1. Table des utilisateurs pour l'authentification
CREATE TABLE IF NOT EXISTS utilisateurs (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    username TEXT NOT NULL UNIQUE,
    password TEXT NOT NULL
);

-- 2. Table des vulnérabilités (résultats des scans Nmap)
CREATE TABLE IF NOT EXISTS failles (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    ip TEXT NOT NULL,
    port TEXT,
    severity TEXT CHECK(severity IN ('Critical', 'High', 'Medium', 'Low')),
    description TEXT,
    scan_date DATETIME DEFAULT CURRENT_TIMESTAMP
);

-- 3. Insertion des données par défaut pour le premier lancement
-- Utilisateur par défaut : admin / admin123
INSERT INTO utilisateurs (username, password) 
VALUES ('admin', 'admin123');

-- Exemple de données de test 
INSERT INTO failles (ip, port, severity, description, scan_date) 
VALUES 
('192.168.1.110', '22', 'Medium', 'SSH version obsolète détectée.', '2026-01-03 14:00:00'),
('192.168.1.1', '80', 'High', 'Serveur web exposé avec vulnérabilité XSS.', '2026-01-03 14:15:00'),
('127.0.0.1', '3306', 'Low', 'Port MySQL ouvert localement.', '2026-01-03 14:30:00');