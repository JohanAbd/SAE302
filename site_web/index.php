<?php
header("Access-Control-Allow-Origin: *");
header("Access-Control-Allow-Headers: Content-Type");
header("Access-Control-Allow-Methods: GET, POST, OPTIONS");

header("Content-Type: application/json; charset=utf-8");

$DB_PATH = __DIR__ . "../". 'bdd.db';
$dir = dirname($DB_PATH);

$pdo = new PDO('sqlite:' . $DB_PATH, null, null, [
  PDO::ATTR_ERRMODE => PDO::ERRMODE_EXCEPTION,
  PDO::ATTR_DEFAULT_FETCH_MODE => PDO::FETCH_ASSOC,
]);

$pdo->exec('CREATE TABLE IF NOT EXISTS "users" (
  login TEXT NOT NULL PRIMARY KEY UNIQUE,
  type TEXT,
  password TEXT
);');

$pdo->exec('CREATE TABLE IF NOT EXISTS "vulnerabilities" (
  id TEXT NOT NULL PRIMARY KEY UNIQUE,
  description TEXT,
  severity TEXT,
  ip TEXT,
  port INTEGER
);');

if ($_SERVER["REQUEST_METHOD"] === "GET") {
    $stmt = $pdo->query("SELECT * FROM vulnerabilities");
    $vulns = $stmt->fetchAll();
    echo json_encode($vulns);
    exit;
}

if ($_SERVER["REQUEST_METHOD"] === "POST") {
    $data = json_decode(file_get_contents("php://input"), true) ?: [];


    $stmt = $pdo->prepare('INSERT OR REPLACE INTO vulnerabilities (id, description, severity, ip, port)
                           VALUES (:id, :description, :severity, :ip, :port)');
    $stmt->execute([
        ':id' => $data['id'],
        ':description' => $data['description'] ?? null,
        ':severity' => $data['severity'] ?? null,
        ':ip' => $data['ip'] ?? null,
        ':port' => isset($data['port']) ? (int)$data['port'] : null,
    ]);

    $stmt = $pdo->prepare('SELECT * FROM vulnerabilities WHERE id = :id');
    $stmt->execute([':id' => $data['id']]);
    $inserted = $stmt->fetch();

    echo json_encode(["status" => "OK", "record" => $inserted]);
    exit;
}