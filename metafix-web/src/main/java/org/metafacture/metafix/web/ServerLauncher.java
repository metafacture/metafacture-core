package org.metafacture.metafix.web;

import org.eclipse.jetty.annotations.AnnotationConfiguration;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.util.log.Slf4jLog;
import org.eclipse.jetty.webapp.Configuration;
import org.eclipse.jetty.webapp.MetaInfConfiguration;
import org.eclipse.jetty.webapp.WebAppContext;
import org.eclipse.jetty.webapp.WebInfConfiguration;
import org.eclipse.jetty.webapp.WebXmlConfiguration;
import org.eclipse.xtext.xbase.lib.Exceptions;

import java.net.InetSocketAddress;

/**
 * This program starts an HTTP server for testing the web integration of your DSL.
 * Just execute it and point a web browser to http://localhost:8080/
 */
public class ServerLauncher { // checkstyle-disable-line ClassDataAbstractionCoupling

    private ServerLauncher() {
        throw new IllegalAccessError("Utility class");
    }

    public static void main(final String[] args) {
        final WebAppContext context = new WebAppContext();
        context.setResourceBase("src/main/webapp");
        context.setWelcomeFiles(new String[] {"index.html"});
        context.setContextPath("/");
        context.setConfigurations(new Configuration[] {new AnnotationConfiguration(), new WebXmlConfiguration(), new WebInfConfiguration(), new MetaInfConfiguration()});
        context.setAttribute(WebInfConfiguration.CONTAINER_JAR_PATTERN, ".*/org\\.metafacture\\.fix\\.web/.*,.*\\.jar");
        context.setInitParameter("org.mortbay.jetty.servlet.Default.useFileMappedBuffer", "false");

        final Server server = new Server(new InetSocketAddress("0.0.0.0", 8080));
        server.setHandler(context);

        final Slf4jLog log = new Slf4jLog(ServerLauncher.class.getName());

        try {
            server.start();

            log.info("Server started " + server.getURI() + "...");

            new Thread(() -> {
                try {
                    log.info("Press enter to stop the server...");

                    if (System.in.read() != -1) {
                        server.stop();
                    }
                    else {
                        log.warn("Console input is not available. In order to stop the server, you need to cancel the process manually.");
                    }
                }
                catch (final Exception e) { // checkstyle-disable-line IllegalCatch
                    throw Exceptions.sneakyThrow(e);
                }
            }).start();

            server.join();
        }
        catch (final Throwable e) { // checkstyle-disable-line IllegalCatch
            if (e instanceof Exception) {
                log.warn(((Exception) e).getMessage());
                System.exit(1);
            }
            else {
                throw Exceptions.sneakyThrow(e);
            }
        }
    }

}
