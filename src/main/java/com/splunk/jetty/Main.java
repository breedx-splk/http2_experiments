package com.splunk.jetty;

import org.eclipse.jetty.server.Server;
import org.glassfish.jersey.jetty.JettyHttpContainerFactory;
import org.glassfish.jersey.server.ResourceConfig;

import java.net.URI;

public class Main {

    public static final String BASE_URI = "http://localhost:8080/";

    public static void main(String[] args) throws Exception {
        Server server = startServer();
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                System.out.println("Shutting down the application...");
                server.stop();
                System.out.println("Done, exit.");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }));
        System.out.println("Server started.");
        Thread.currentThread().join();
    }

    public static Server startServer() {
        // scan packages
        ResourceConfig config = new ResourceConfig().packages("com.splunk.jetty");
        Server server =
                JettyHttpContainerFactory.createServer(URI.create(BASE_URI), config);

        return server;
    }


//    public static void main(String[] args) throws Exception {
//        QueuedThreadPool threadPool = new QueuedThreadPool();
//        threadPool.setName("server");
//        Server server = new Server(threadPool);
//        Connector connector = new ServerConnector(server);
//        server.addConnector(connector);
//
//        server.setHandler(new AbstractHandler() {
//            @Override
//            public void handle(String target, Request jettyRequest, HttpServletRequest request, HttpServletResponse response) {
//                // Mark the request as handled so that it
//                // will not be processed by other handlers.
//                jettyRequest.setHandled(true);
//            }
//        });
//        server.start();
//    }

}
