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
        String uri = params.get("uri");
        if (super.handler != null && super.handler instanceof RafFileUploadHandler) {
            //type casting
            RafFileUploadHandler handler = (RafFileUploadHandler) super.handler;
            handler.handleFileUpload(uri, decompress);
        }
    }

    public boolean isDecompress() {
        return decompress;
    }

    public void setDecompress(boolean decompress) {
        this.decompress = decompress;
    }
}