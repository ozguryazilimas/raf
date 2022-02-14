package com.ozguryazilim.raf.rest;

import com.ozguryazilim.telve.rest.ext.Logged;
import org.apache.deltaspike.core.api.config.ConfigResolver;
import org.apache.shiro.authz.annotation.RequiresPermissions;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.HashMap;
import java.util.Map;

@Logged
@RequiresPermissions("admin")
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
