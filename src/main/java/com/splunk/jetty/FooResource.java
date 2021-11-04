package com.splunk.jetty;

import jakarta.servlet.http.PushBuilder;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import org.eclipse.jetty.server.Request;

public class FooResource {

    @GET
    @Path("/foo")
    public String foo(Request request){
        PushBuilder pushBuilder = request.newPushBuilder();
        pushBuilder.path("/bar");
        pushBuilder.push();
        return "foo";
    }


}
