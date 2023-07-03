package com.ozguryazilim.raf.ui.utils;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.primefaces.PrimeFaces;
import org.primefaces.util.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.faces.application.ConfigurableNavigationHandler;
import javax.faces.bean.SessionScoped;
import javax.faces.context.FacesContext;
import java.util.Map;
import java.util.UUID;

@SessionScoped
public class DocumentViewDialogUtils {
    private static final Logger LOG = LoggerFactory.getLogger(DocumentViewDialogUtils.class);
    public static void openDialog(String dialogName) {
        FacesContext facesContext = FacesContext.getCurrentInstance();

        ConfigurableNavigationHandler navigationHandler = (ConfigurableNavigationHandler) facesContext.getApplication().getNavigationHandler();
        String toViewId = navigationHandler.getNavigationCase(facesContext, "", dialogName).getToViewId(facesContext);
        String url = facesContext.getApplication().getViewHandler().getBookmarkableURL(facesContext, toViewId, null, true);
        String pfdlgcid = UUID.randomUUID().toString();

        try {
            JSONObject openDialogParams = new JSONObject();
            openDialogParams.put("url", url);
            openDialogParams.put("pfdlgcid", pfdlgcid);
            openDialogParams.put("sourceComponentId", "documentViewDialog_" + pfdlgcid);
            openDialogParams.put("options", new JSONObject());
            
            String openDialogScript = "window.top.RafFaces.openDocumentViewDialog(" + openDialogParams + ")";
            PrimeFaces.current().executeScript(openDialogScript);
        } catch (JSONException e) {
            LOG.error("Error while opening documentViewDialog.", e);
        }

    }

}
