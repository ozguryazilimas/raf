package com.ozguryazilim.raf.rest;

import org.apache.deltaspike.core.api.config.ConfigResolver;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.HashMap;
import java.util.Map;

@Path("/api/version")
public class VersionRest {

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Map<String, String> getAppVersion() {
        Map<String, String> responseMap = new HashMap<>();
        responseMap.put("title", ConfigResolver.getPropertyValue("app.title"));
        responseMap.put("version", ConfigResolver.getPropertyValue("app.version"));
        return responseMap;
    }
}
