// WebServer.java modifié pour l'API uniquement
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;

public class WebServer {

    public static void main(String[] args) throws Exception {
        // On garde le port 8080 pour la communication interne avec Apache
        Server server = new Server(8080);

        ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
        context.setContextPath("/");
        server.setHandler(context);

        // === API REST UNIQUEMENT ===
        // Apache s'occupe du reste, Java ne gère plus le dossier ./web
        ServletHolder apiHolder = new ServletHolder(new ApiHandler());
        context.addServlet(apiHolder, "/api/*");

        server.start();
        System.out.println("Backend Java API démarré sur le port 8080");
        System.out.println("Le site est accessible via Apache : http://localhost");
        server.join();
    }
}
