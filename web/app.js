// app.js - scan + affichage vulnérabilités

async function postScan(ip, onProgress){
  const body = new URLSearchParams();
  body.append("ip", ip);

  try{
    onProgress && onProgress("starting");
    const res = await fetch("/api/scan", {
      method: "POST",
      body: body.toString(),
      headers: {"Content-Type": "application/x-www-form-urlencoded"}
    });

    const text = await res.text();
    return {ok: res.ok, text};
  }catch(e){
    return {ok:false, text: e.message};
  }
}

async function fetchVulns(){
  try {
    const res = await fetch("/api/vulns");
    if(!res.ok) throw new Error("Erreur réseau");

    // JSON valide garanti maintenant
    return await res.json();

  } catch (e) {
    console.error("Erreur fetch JSON:", e);
    return [];
  }
}

/* index page logic */
if(document.getElementById("scanForm")){
  const form = document.getElementById("scanForm");
  const ipInput = document.getElementById("ip");
  const status = document.getElementById("status");
  const btn = document.getElementById("scanBtn");

  form.addEventListener("submit", async (ev)=>{
    ev.preventDefault();
    const ip = ipInput.value.trim();
    if(!ip){ status.textContent = "Saisis une IP"; return; }

    btn.disabled = true;
    const spinner = document.createElement("span");
    spinner.className = "spinner";
    btn.prepend(spinner);

    status.textContent = "Scan en cours…";

    try{
      const r = await postScan(ip);
      if(r.ok) status.textContent = "Scan terminé !";
      else status.textContent = "Erreur : " + r.text;
    }catch(e){
      status.textContent = "Erreur : " + e.message;
    }finally{
      btn.disabled = false;
      spinner.remove();
    }
  });
}

/* vuln page logic */
if(document.getElementById("vulnContainer")){
  const tbody = document.getElementById("vulnContainer");
  const loader = document.getElementById("vulnLoader");

  (async ()=>{
    loader.style.display = "block";
    const data = await fetchVulns();
    loader.style.display = "none";

    if(!data || data.length === 0){
      tbody.innerHTML = `<div class="card"><p class="small">Aucune vulnérabilité enregistrée.</p></div>`;
      return;
    }

    const list = document.createElement("div");
    list.className = "vuln-list";

    data.forEach(v => {
      const badgeClass = v.severity || "Low";
      const div = document.createElement("div");
      div.className = "vuln card";

      div.innerHTML = `
        <div class="badge ${badgeClass}">${badgeClass}</div>
        <div class="vmeta">
          <div class="row">
            <strong>${v.ip} ${v.port ? ":" + v.port : ""}</strong>
            <div class="small">${v.scan_date || ""} • ID: ${v.id || ""}</div>
          </div>
          <p class="small">Référence: ${v.id || "-"}</p>
          <div class="vdesc">${v.description.replace(/\\n/g,"<br>")}</div>
        </div>
      `;
      list.appendChild(div);
    });

    tbody.appendChild(list);
  })();
}
