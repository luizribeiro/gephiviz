package org.luizribeiro.gephiviz;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.webapp.WebAppContext;

public class Main {

    public static void main(String args[]) {
        String webPort = System.getenv("PORT");
        if (webPort == null || webPort.isEmpty()) {
            webPort = "8080";
        }

        Server server = new Server(Integer.parseInt(webPort));

        WebAppContext webapp = new WebAppContext();
        webapp.setWar("target/gephiviz.war");
        server.setHandler(webapp);
        try {
            server.start();
            server.join();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
