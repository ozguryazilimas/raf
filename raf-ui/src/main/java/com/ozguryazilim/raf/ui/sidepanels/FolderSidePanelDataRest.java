package com.ozguryazilim.raf.ui.sidepanels;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.ozguryazilim.raf.RafContext;
import com.ozguryazilim.raf.RafException;
import com.ozguryazilim.raf.RafService;
import com.ozguryazilim.raf.definition.RafDefinitionService;
import com.ozguryazilim.raf.entities.RafDefinition;
import com.ozguryazilim.raf.models.RafFolder;
import com.ozguryazilim.raf.models.RafObject;
import org.apache.commons.lang3.StringUtils;
import org.omnifaces.util.Json;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 *
 * @author oyas
 */
@Path("folderData")
public class FolderSidePanelDataRest implements Serializable{
    
    @Inject
    private RafService rafService;
    
    @Inject
    private RafContext context;
    
    @Inject
    private RafDefinitionService rafDefinitionService;
    
    /**
     *
     * @param id
     * @return
     */
    @GET
    public Response folderList( @QueryParam("id") String id, @QueryParam("findUntilId") String findUntilId, @QueryParam("raf") String raf ,@QueryParam("init") Boolean initTree) throws RafException {

        List<RafFolder> folders;
        String selectedFolderId = "";

        if( "#".equals(id) && initTree) {
            if (StringUtils.isBlank(findUntilId)) {
                RafDefinition rafDefinition = rafDefinitionService.getRafDefinitionByCode(raf);
                folders = rafService.getRootFolders(rafDefinition.getNode().getPath());
                selectedFolderId = rafDefinition.getNodeId();

            } else {
                RafObject o = rafService.getRafObject(findUntilId);

                RafDefinition rafDefinition = rafDefinitionService.getRafDefinitionByCode(raf);
                List<RafFolder> foldersOfDepth = rafService.getChildFolderList(rafDefinition.getNode().getPath());

                folders = new ArrayList<>(foldersOfDepth);
                folders.add(rafService.getFolder(rafDefinition.getNode().getPath()));

                Optional<RafFolder> currentFolder = foldersOfDepth.stream()
                        .filter(f -> (o.getPath()).startsWith(f.getPath() + "/"))
                        .findAny();

                while (currentFolder.isPresent()) {
                    foldersOfDepth = rafService.getChildFolderList(currentFolder.get().getPath());
                    selectedFolderId = currentFolder.get().getId();
                    currentFolder = foldersOfDepth.stream()
                            .filter(f -> (o.getPath()).startsWith(f.getPath() + "/"))
                            .findAny();

                    folders.addAll(foldersOfDepth);
                }
            }
        } else {
            RafObject o = rafService.getRafObject(id);
            folders = rafService.getChildFolderList(o.getPath());
            selectedFolderId = o.getId();
        }

        if (StringUtils.isBlank(selectedFolderId)) {
            selectedFolderId = rafDefinitionService.getRafDefinitionByCode(raf).getNodeId();
        }

        String finalSelectedFolderId = selectedFolderId;

        JsonArray result = folders.stream()
                        .map(f -> getRafFolderJsonObject(folders, f, raf, f.getId().equals(finalSelectedFolderId)))
                        .collect(JsonArray::new, JsonArray::add, JsonArray::addAll);

        return Response.ok(result.toString(), MediaType.APPLICATION_JSON).build();
    }

    private JsonObject getRafFolderJsonObject(List<RafFolder> folders, RafFolder currentFolder, String raf, boolean selected) {
        JsonObject folder = new JsonObject();
        folder.addProperty("id", currentFolder.getId());
        folder.addProperty("parent", currentFolder.getParentId());
        folder.addProperty("text", currentFolder.getTitle());

        //state
        JsonObject state = new JsonObject();
        state.addProperty("selected", selected);

        if (folders.stream().noneMatch(o -> o.getParentId().equals(currentFolder.getId()))) {
            folder.addProperty("children", true);
        } else {
            state.addProperty("opened", true);
        }

        //a_attr
        JsonObject aAttr = new JsonObject();
        aAttr.addProperty("href", String.format("raf.jsf?id=%s&o=%s", raf, currentFolder.getId()));

        folder.add("a_attr", aAttr);
        folder.add("state", state);

        return folder;
    }


}
