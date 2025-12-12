// ApiHandler.java
import jakarta.servlet.http.*;
import jakarta.servlet.annotation.*;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.ResultSet;

@WebServlet(name = "ApiHandler", urlPatterns = {"/api/*"})
public class ApiHandler extends HttpServlet {

    private DbManager db = new DbManager("vulnerabilities.db");

    @Override
    public void init() {
        try {
            db.connect();
        } catch (Exception e) { e.printStackTrace(); }
    }

    /** Nettoyage strict pour produire un JSON valide */
    private String clean(String s) {
    if (s == null) return "";

    return s
        .replace("\r", "")                      // Supprime retour chariot
        .replace("\t", " ")                     // Tab -> espace
        .replaceAll("(?m)^\\|\\s*", "\\\\| ")   // Corrige les lignes commençant par |
        .replace("\\", "\\\\")                  // Échappe backslash
        .replace("\"", "\\\"")                  // Échappe guillemets
        .replace("\n", "\\n");                  // Échappe sauts de ligne
}

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json; charset=UTF-8");

        String path = req.getPathInfo();
        if (path == null) path = "";

        switch (path) {
            case "/vulns":
                listVulns(resp);
                break;

            default:
                resp.getWriter().println("{\"error\": \"Endpoint inconnu\"}");
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json; charset=UTF-8");

        String path = req.getPathInfo();
        if (path == null) path = "";

        if (path.equals("/scan")) {
            String ip = req.getParameter("ip");
            if (ip == null || ip.isBlank()) {
                resp.getWriter().println("{\"error\":\"IP manquante\"}");
                return;
            }

            ScannerApp scanner = new ScannerApp();
            scanner.runScan(ip);

            try {
                db.saveVulnerabilities(scanner.getVulnerabilities());
            } catch (Exception e) {
                e.printStackTrace();
                resp.getWriter().println("{\"error\":\"Erreur BD\"}");
                return;
            }

            resp.getWriter().println("{\"status\":\"Scan terminé\"}");
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

                // Nettoyage obligatoire
                String desc = clean(rs.getString("description"));

                w.print("  {");
                w.print("\"id\":\"" + clean(rs.getString("id")) + "\",");
                w.print("\"ip\":\"" + clean(rs.getString("ip")) + "\",");
                w.print("\"port\":\"" + clean(rs.getString("port")) + "\",");
                w.print("\"severity\":\"" + clean(rs.getString("severity")) + "\",");
                w.print("\"description\":\"" + desc + "\",");
                w.print("\"scan_date\":\"" + clean(rs.getString("scan_date")) + "\"");
                w.print("}");
            }

            w.println("\n]");
        } catch (Exception e) {
            e.printStackTrace();
            resp.getWriter().println("{\"error\":\"Erreur lecture BD\"}");
        }
    }
}
