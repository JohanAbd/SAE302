-- Initialisation de la base de données compatible avec DbManager.java

-- 1. Table des utilisateurs avec SEL
CREATE TABLE IF NOT EXISTS users (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    username TEXT UNIQUE NOT NULL,
    password_hash TEXT NOT NULL, -- Stocke le résultat du SHA-256 + Base64
    salt TEXT NOT NULL           -- Stocke la clé "SAE302_SALT"
);

-- 2. Table des vulnérabilités
CREATE TABLE IF NOT EXISTS vulnerabilities (
    id TEXT PRIMARY KEY,         
    ip TEXT NOT NULL,
    port TEXT,
    severity TEXT,
    description TEXT,
    scan_date DATETIME DEFAULT CURRENT_TIMESTAMP
);

