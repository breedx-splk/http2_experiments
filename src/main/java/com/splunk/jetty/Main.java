package com.splunk.jetty;

import jakarta.servlet.Servlet;
import org.eclipse.jetty.http.HttpField;
import org.eclipse.jetty.http.HttpFields;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.http.HttpURI;
import org.eclipse.jetty.http.HttpVersion;
import org.eclipse.jetty.http.MetaData;
import org.eclipse.jetty.http2.api.Session;
import org.eclipse.jetty.http2.api.Stream;
import org.eclipse.jetty.http2.api.server.ServerSessionListener;
import org.eclipse.jetty.http2.client.HTTP2Client;
import org.eclipse.jetty.http2.frames.DataFrame;
import org.eclipse.jetty.http2.frames.HeadersFrame;
import org.eclipse.jetty.http2.frames.PushPromiseFrame;
import org.eclipse.jetty.http2.server.HTTP2CServerConnectionFactory;
import org.eclipse.jetty.server.HttpConfiguration;
import org.eclipse.jetty.server.HttpConnectionFactory;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.servlet.ServletHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.util.Callback;
import org.eclipse.jetty.util.FuturePromise;

import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.Executors;
import java.util.concurrent.Phaser;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Main {
    final static HttpField USER_AGENT = new HttpField(HttpHeader.USER_AGENT, "http2 jetty experiment");
    final static int PORT = 8080;

    final static ScheduledExecutorService exec = Executors.newSingleThreadScheduledExecutor();
    static HTTP2Client client;

    public static void main(String[] args) throws Exception {
        Server server = startHttp2();
        System.out.println("Server started on port " + PORT);

        //start client
        client = new HTTP2Client();
        client.start();

        FuturePromise<Session> sessionPromise = new FuturePromise<>();
        client.connect(new InetSocketAddress("localhost", PORT), new ServerSessionListener.Adapter(), sessionPromise);
        Session session = sessionPromise.get(5, TimeUnit.SECONDS);

        exec.scheduleAtFixedRate(() -> safeDoRequest(session), 0, 2, TimeUnit.SECONDS);
    }

    private static Server startHttp2() throws Exception {
        final Server server = new Server();
        // Common HTTP configuration.
        final HttpConfiguration config = new HttpConfiguration();

        // HTTP/1.1 support.
        final HttpConnectionFactory http1 = new HttpConnectionFactory(config);

        // HTTP/2 cleartext support.
        final HTTP2CServerConnectionFactory http2c = new HTTP2CServerConnectionFactory(config);

        // Add the connector.
        final ServerConnector connector = new ServerConnector(server, http1, http2c);
        connector.setPort(PORT);
        server.addConnector(connector);

        // Add the servlet.
        final ServletHandler handler = new ServletHandler();

        Servlet fooServlet = new FooServlet();
        ServletHolder fooHolder = new ServletHolder(fooServlet);
        handler.addServletWithMapping(fooHolder, "/foo");
        server.setHandler(handler);

        // Start the server.
        server.start();
        return server;
    }

    private static void safeDoRequest(Session session) {
        try {
            doRequest(session);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void doRequest(Session session) throws Exception {

        // Prepare the HTTP request headers.
        HttpFields requestHeaders = HttpFields.from(USER_AGENT);

        // Prepare the HTTP request object.
        MetaData.Request request = new MetaData.Request("GET", HttpURI.from("http://localhost:8080/foo"), HttpVersion.HTTP_2, requestHeaders);
        // Create the HTTP/2 HEADERS frame representing the HTTP request.
        HeadersFrame headersFrame = new HeadersFrame(request, null, true);

        Phaser phaser = new Phaser(2);

        // Prepare the listener to receive the HTTP response frames.
        Stream.Listener responseListener =  new Stream.Listener.Adapter() {

            @Override
            public void onHeaders(Stream stream, HeadersFrame frame) {
                System.out.println(frame);
                if (frame.isEndStream()) {
                    phaser.arrive();
                }
            }

            @Override
            public void onData(Stream stream, DataFrame frame, Callback callback) {
                System.out.println(frame);
                String body = StandardCharsets.UTF_8.decode(frame.getData()).toString();
                System.out.print("Got response: " + body);
                callback.succeeded();
                if (frame.isEndStream()){
                    phaser.arrive();
                }
            }

            @Override
            public Stream.Listener onPush(Stream stream, PushPromiseFrame frame) {
                System.out.println(frame);
                phaser.register();
                return this;
            }
        };

        FuturePromise<Stream> streamPromise = new FuturePromise<>();
        session.newStream(headersFrame, streamPromise, responseListener);
        phaser.awaitAdvanceInterruptibly(phaser.arrive(), 1, TimeUnit.SECONDS);
    }

}
