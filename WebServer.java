// WebServer.java
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;

public class WebServer {

    public static void main(String[] args) throws Exception {

        Server server = new Server(8080);

        ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
        context.setContextPath("/");
        server.setHandler(context);

        // === API REST ===
        ServletHolder apiHolder = new ServletHolder(new ApiHandler());
        context.addServlet(apiHolder, "/api/*");

        // === Servir les fichiers statiques du dossier web/ ===
        ServletHolder staticHolder = new ServletHolder("default", new org.eclipse.jetty.servlet.DefaultServlet());
        staticHolder.setInitParameter("resourceBase", "./web");
        staticHolder.setInitParameter("dirAllowed", "true");
        context.addServlet(staticHolder, "/");

        server.start();
        System.out.println("Serveur web démarré : http://localhost:8080");
        server.join();
    }
}
