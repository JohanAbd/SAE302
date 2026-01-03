// app.js - Gestion du Scan et de l'affichage des vulnérabilités

// 1. Vérification de session au chargement
if (!localStorage.getItem('isLogged') && !window.location.href.includes('login.html')) {
    window.location.href = 'login.html';
}

// 2. Fonction de déconnexion
function logout() {
    localStorage.removeItem('isLogged');
    window.location.href = 'login.html';
}

// 3. Fonction pour envoyer une requête de scan au serveur
async function postScan(ip) {
    const body = new URLSearchParams();
    body.append("ip", ip);

    try {
        const res = await fetch("/api/scan", {
            method: "POST",
            body: body.toString(),
            headers: { "Content-Type": "application/x-www-form-urlencoded" }
        });

        const text = await res.text();
        return { ok: res.ok, text };
    } catch (e) {
        return { ok: false, text: e.message };
    }
}

// 4. Fonction pour récupérer les vulnérabilités depuis l'API
async function fetchVulns() {
    try {
        const res = await fetch("/api/vulns");
        if (!res.ok) throw new Error("Erreur réseau lors de la récupération");
        return await res.json();
    } catch (e) {
        console.error("Erreur fetch JSON:", e);
        return null; // Retourne null en cas d'erreur pour différencier d'une liste vide
    }
}

/* --- LOGIQUE PAGE INDEX (SCAN) --- */
if (document.getElementById("scanForm")) {
    const form = document.getElementById("scanForm");
    const ipInput = document.getElementById("ip");
    const status = document.getElementById("status");
    const btn = document.getElementById("scanBtn");

    form.addEventListener("submit", async (ev) => {
        ev.preventDefault();
        const ip = ipInput.value.trim();
        if (!ip) { status.textContent = "Saisis une IP"; return; }

        btn.disabled = true;
        const spinner = document.createElement("span");
        spinner.className = "spinner";
        btn.prepend(spinner);

        status.innerHTML = "<em>Scan en cours (cela peut prendre 1 à 2 minutes)...</em>";

        try {
            const r = await postScan(ip);
            if (r.ok) {
                status.style.color = "#10b981";
                status.textContent = "Scan terminé avec succès !";
            } else {
                status.style.color = "#ef4444";
                status.textContent = "Erreur : " + r.text;
            }
        } catch (e) {
            status.textContent = "Erreur : " + e.message;
        } finally {
            btn.disabled = false;
            spinner.remove();
        }
    });
}

/* --- LOGIQUE PAGE VULNÉRABILITÉS (AFFICHAGE) --- */
if (document.getElementById("vulnContainer")) {
    const container = document.getElementById("vulnContainer");
    const loader = document.getElementById("vulnLoader");

    (async () => {
        try {
            // Affichage du loader
            if (loader) loader.style.display = "block";
            
            const data = await fetchVulns();
            
            // Cacher le loader
            if (loader) loader.style.display = "none";

            // Si erreur de fetch (data est null)
            if (data === null) {
                container.innerHTML = `<div class="card"><p style="color:var(--danger)">Impossible de contacter l'API. Vérifiez que le serveur Java tourne.</p></div>`;
                return;
            }

            // Si aucune donnée
            if (data.length === 0) {
                container.innerHTML = `<div class="card"><p>Aucune vulnérabilité trouvée dans la base de données.</p></div>`;
                return;
            }

            // Nettoyage du conteneur avant affichage
            container.innerHTML = "";

            // Génération des cartes
            data.forEach(v => {
                const card = document.createElement("div");
                
                // On ajoute la classe vuln-card ET la sévérité pour la bordure gauche
                card.className = `vuln-card severity-${v.severity}`;

                // Transformation des \n (texte) en vrais sauts de ligne pour la balise <code>
                const formattedDescription = v.description.split('\\n').join('\n');

                card.innerHTML = `
                    <div class="vuln-header">
                        <div class="vuln-target">
                            <strong>${v.ip}${v.port ? ':' + v.port : ''}</strong>
                        </div>
                        <span class="badge bg-${v.severity}">${v.severity}</span>
                    </div>
                    <div style="font-size: 12px; color: var(--muted); margin-bottom: 10px;">
                        Date du scan : ${v.scan_date}
                    </div>
                    <div class="vuln-body">
                        <code>${formattedDescription}</code>
                    </div>
                `;
                container.appendChild(card);
            });
        } catch (err) {
            console.error("Erreur d'affichage:", err);
            if (loader) loader.style.display = "none";
            container.innerHTML = `<p style="color:var(--danger)">Erreur interne du script d'affichage.</p>`;
        }
    })();
}
