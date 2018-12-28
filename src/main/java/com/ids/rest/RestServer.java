package com.ids.rest;

import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;

import java.net.URI;

public class RestServer {

    // Base URI the Grizzly HTTP server will listen on
    public static String BASE_URI = "http://172.16.39.21:8888";

    /**
     * Starts Grizzly HTTP server exposing JAX-RS resources defined in this application.
     * @return Grizzly HTTP server.
     */
    public static HttpServer startServer() {
        String packagePath = "com.ids.rest";
        if (ServerConfig.parse("server.xml")) {
            BASE_URI = ServerConfig.baseUrl!=null ? ServerConfig.baseUrl : BASE_URI;
            if (ServerConfig.baseUrl != null) {
                BASE_URI = ServerConfig.baseUrl;
            }
            if (ServerConfig.packagePath != null) {
                packagePath = ServerConfig.packagePath;
            }
        }

        // create a resource config that scans for JAX-RS resources and providers
        // in com.example package

        final ResourceConfig rc = new ResourceConfig().packages(packagePath);

        // create and start a new instance of grizzly http server
        // exposing the Jersey application at BASE_URI
        return GrizzlyHttpServerFactory.createHttpServer(URI.create(BASE_URI), rc);
    }
}
