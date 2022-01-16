package com.ozguryazilim.raf.uploader;

import com.ozguryazilim.telve.uploader.ui.FileUploadDialog;
import com.ozguryazilim.telve.uploader.ui.FileUploadHandler;
import org.apache.deltaspike.core.api.config.ConfigResolver;
import org.apache.deltaspike.core.api.scope.WindowScoped;
import org.primefaces.PrimeFaces;

import javax.faces.context.FacesContext;
import javax.inject.Named;
import java.util.HashMap;
import java.util.Map;

@Named
@WindowScoped
public class RafFileUploadDialog extends FileUploadDialog {

    //zip dosyalari decompress edilsin mi?
    private boolean decompress = "true".equals(ConfigResolver.getPropertyValue("auto.extract.zip.files.default.value", "true"));

    public void openDialog(FileUploadHandler handler) {
        super.maxNumberOfFiles = null;
        openDialog(handler, "");
    }

    public void openDialog(FileUploadHandler handler, Integer maxNumberOfFiles) {
        super.maxNumberOfFiles = maxNumberOfFiles;
        openDialog(handler, "");
    }

    @Override
    public void openDialog(FileUploadHandler handler, String ownerKey) {
        super.handler = handler;
        super.ownerKey = ownerKey;
        Map<String, Object> options = new HashMap();
        options.put("modal", true);
        options.put("resizable", false);
        options.put("contentHeight", 480);
        PrimeFaces.current().dialog().openDynamic("/dialogs/rafFileUploadDialog", options, null);
    }

    @Override
    public void fileUploaded() {
        Map<String, String> params = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap();
        if (super.handler != null && super.handler instanceof RafFileUploadHandler) {
            String uriArrString = params.get("uriArr");
            // There is no file for upload
            if (uriArrString == null || uriArrString.equals("")) {
                return;
            }
            String[] uriArr = uriArrString.split(",");
            // Type casting
            RafFileUploadHandler handler = (RafFileUploadHandler) super.handler;
            if (handler.getRafCode().equals("CHECKIN") && uriArr.length == 1) {
                // If is version check-in have to single file
                String versionComment = params.get("versionComment");
                handler.handleFileUpload(uriArr[0], versionComment);
            } else if (uriArr.length > 0) {
                for (String uri : uriArr) {
                    handler.handleFileUpload(uri, decompress);
                }
            }
        }
    }

    public boolean isDecompress() {
        return decompress;
    }

    public void setDecompress(boolean decompress) {
        this.decompress = decompress;
    }
}
