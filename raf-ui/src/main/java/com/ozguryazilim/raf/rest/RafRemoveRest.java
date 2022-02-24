package com.ozguryazilim.raf.rest;

import com.ozguryazilim.raf.RafException;
import com.ozguryazilim.raf.RafService;
import com.ozguryazilim.raf.events.RafFolderDataChangeEvent;
import com.ozguryazilim.raf.events.RafObjectDeleteEvent;
import com.ozguryazilim.raf.models.RafFolder;
import com.ozguryazilim.raf.models.RafObject;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.event.Event;
import javax.inject.Inject;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;
import java.io.Serializable;

/**
 * REST üzerinden dosya silmek için yazılmıştır.
 */
@RequiresPermissions("admin")
@Path("/api/remove")
public class RafRemoveRest implements Serializable {

    private static final Logger LOG = LoggerFactory.getLogger(RafRemoveRest.class);

    @Inject
    private RafService rafService;

    @Inject
    private Event<RafObjectDeleteEvent> deleteEvent;

    @Inject
    private Event<RafFolderDataChangeEvent> folderChangeEvent;

    @POST
    @Path("/file")
    public Response downloadFileByPath(@FormParam("raf") String raf, @FormParam("path") String path) {
        try {
            RafObject ro = rafService.getRafObjectByPath("/RAF/" + raf + path);
            removeRafObject(ro);
            LOG.info(String.format("%s is removed.", ro.getPath()));
            return Response.ok().build();
        } catch (RafException ex) {
            return Response.status(Response.Status.NOT_FOUND).entity("Error while removing specified content").build();
        }
    }

    @GET
    @Path("/file/{id}")
    public Response downloadFile(@PathParam("id") String docID) {
        try {
            RafObject ro = rafService.getRafObject(docID);
            removeRafObject(ro);
            LOG.info(String.format("%s is removed.", ro.getPath()));
            return Response.ok().build();
        } catch (RafException ex) {
            return Response.status(Response.Status.NOT_FOUND).entity("Error while removing specified content").build();
        }
    }

    private void removeRafObject(RafObject ro) throws RafException {
        rafService.deleteObject(ro);
        deleteEvent.fire(new RafObjectDeleteEvent(ro));

        if (ro instanceof RafFolder) {
            folderChangeEvent.fire(new RafFolderDataChangeEvent());
        }
    }

}

