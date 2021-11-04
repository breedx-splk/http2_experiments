package com.splunk.jetty;

import jakarta.servlet.http.PushBuilder;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import org.eclipse.jetty.server.Request;

public class BarResource {

    @GET
    @Path("/bar")
    public String bar(Request request){
        return "bar";
    }
}
