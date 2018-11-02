package com.ids.rest;

import com.ids.beans.IdsAlert;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.ArrayList;
import java.util.List;

@Path("ids")
public class IdsAlertResource {

    @GET
    @Path("alertxml")
    @Produces(MediaType.APPLICATION_XML)
    public IdsAlert getAllalert() {
        return new IdsAlert();
    }

    @GET
    @Path("alertjson")
    @Produces(MediaType.APPLICATION_JSON)
    public List<IdsAlert> getAllalerts() {
        List<IdsAlert> alerts = new ArrayList<>(3);
        alerts.add(new IdsAlert());
        alerts.add(new IdsAlert());
        alerts.add(new IdsAlert());
        return alerts;
    }

    @GET
    @Path("hello")
    @Produces(MediaType.TEXT_PLAIN)
    public String Hello() {
        return "Hello";
    }
}
