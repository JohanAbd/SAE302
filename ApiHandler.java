import jakarta.servlet.http.*;
import jakarta.servlet.annotation.*;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.ResultSet;

@WebServlet(name = "ApiHandler", urlPatterns = {"/api/*"})
public class ApiHandler extends HttpServlet {

    // Initialisation du gestionnaire de base de données
    private DbManager db = new DbManager("bd.db");

    @Override
    public void init() {
        try {
            db.connect();
        } catch (Exception e) { e.printStackTrace(); }
    }

    private String clean(String s) {
    if (s == null) return "";
    return s.replace("\\", "\\\\")      // Échappe les anti-slashs
            .replace("\"", "\\\"")      // Échappe les guillemets
            .replace("\n", "\\n")      // TRANSFORMER le saut de ligne en texte "\n"
            .replace("\r", "")          // Supprime les retours chariot
            .replace("\t", " ");        // Remplace les tabulations par des espaces
}

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json; charset=UTF-8");
        String path = req.getPathInfo();
        if ("/vulns".equals(path)) {
            listVulns(resp);
        } else {
            resp.getWriter().println("{\"error\":\"Route inconnue\"}");
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String path = req.getPathInfo();
        resp.setContentType("application/json; charset=UTF-8");

        // --- 1. LOGIN ---
        if ("/login".equals(path)) {
            String user = req.getParameter("username");
            String pass = req.getParameter("password");

            if (db.authenticate(user, pass)) {
                resp.getWriter().println("{\"status\":\"success\", \"token\":\"access_granted_123\"}");
            } else {
                resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                resp.getWriter().println("{\"status\":\"error\", \"message\":\"Identifiants incorrects\"}");
            }
            return;
        }

        // --- 2. SCAN ---
        else if ("/scan".equals(path)) {
            String targetIp = req.getParameter("ip");
            if (targetIp == null || targetIp.isEmpty()) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                resp.getWriter().println("{\"error\":\"IP manquante\"}");
                return;
            }

            try {
                ScannerApp scanner = new ScannerApp();
                scanner.runScan(targetIp);
                db.saveVulnerabilities(scanner.getVulnerabilities());
                resp.getWriter().println("{\"status\":\"success\", \"count\":" + scanner.getVulnerabilities().size() + "}");
            } catch (Exception e) {
                e.printStackTrace();
                resp.setStatus(500);
                resp.getWriter().println("{\"error\":\"Erreur lors du scan\"}");
            }
        }
    }

    private void listVulns(HttpServletResponse resp) throws IOException {
        try (PrintWriter w = resp.getWriter()) {
            ResultSet rs = db.fetchAll();
            w.println("[");
            boolean first = true;
            while (rs.next()) {
                if (!first) w.println(",");
                first = false;
                w.print("  {");
                w.print("\"id\":\"" + clean(rs.getString("id")) + "\",");
                w.print("\"ip\":\"" + clean(rs.getString("ip")) + "\",");
                w.print("\"port\":\"" + clean(rs.getString("port")) + "\",");
                w.print("\"severity\":\"" + clean(rs.getString("severity")) + "\",");
                w.print("\"description\":\"" + clean(rs.getString("description")) + "\",");
                w.print("\"scan_date\":\"" + clean(rs.getString("scan_date")) + "\"");
                w.print("}");
            }
            w.println("\n]");
        } catch (Exception e) {
            e.printStackTrace();
            resp.setStatus(500);
        }
    }
}
