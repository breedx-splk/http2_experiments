package com.splunk.jetty;

import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.PushBuilder;
import org.eclipse.jetty.server.Request;

import java.io.IOException;

public class FooServlet extends HttpServlet {

    @Override
    public void service(ServletRequest req, ServletResponse res) throws ServletException, IOException {
        Request baseRequest = Request.getBaseRequest(req);
        PushBuilder pushBuilder = baseRequest.newPushBuilder();
        res.getWriter().println("foo");
    }
}
