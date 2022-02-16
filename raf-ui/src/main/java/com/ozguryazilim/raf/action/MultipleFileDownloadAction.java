package com.ozguryazilim.raf.action;

import com.ozguryazilim.raf.RafException;
import com.ozguryazilim.raf.RafService;
import com.ozguryazilim.raf.models.RafDocument;
import com.ozguryazilim.raf.models.RafFolder;
import com.ozguryazilim.raf.models.RafMimeTypes;
import com.ozguryazilim.raf.models.RafObject;
import com.ozguryazilim.raf.models.RafRecord;
import com.ozguryazilim.raf.ui.base.AbstractAction;
import com.ozguryazilim.raf.ui.base.Action;
import com.ozguryazilim.raf.ui.base.ActionCapability;
import com.ozguryazilim.telve.auth.Identity;
import com.ozguryazilim.telve.messagebus.command.CommandSender;
import com.ozguryazilim.telve.messages.FacesMessages;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.io.IOUtils;
import org.apache.deltaspike.core.api.config.ConfigResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author oyas
 */
@Action(icon = "fa-cloud-download",
        capabilities = {ActionCapability.CollectionViews, ActionCapability.DetailViews, ActionCapability.MultiSelection, ActionCapability.NeedSelection})
public class MultipleFileDownloadAction extends AbstractAction {

    private static final Logger LOG = LoggerFactory.getLogger(MultipleFileDownloadAction.class);

    @Inject
    private FacesContext facesContext;

    @Inject
    private RafService rafService;

    @Inject
    private CommandSender commandSender;

    @Inject
    private Identity identity;

    private List<RafObject> fileList;
    private long maxZipFileLimit;
    private long sumOfFileLengths;

    private ZipOutputStream zipOS;

    @Override
    public boolean applicable(boolean forCollection) {
        try {
            boolean permission = "true".equals(ConfigResolver.getProjectStageAwarePropertyValue("raf.multiplefiledownload", "true"));
            return permission && super.applicable(forCollection);
        } catch (Exception ex) {
            LOG.error("Error", ex);
            return super.applicable(forCollection);
        }
    }

    @Override
    protected void initActionModel() {
        fileList = new ArrayList();
        String maxZipFileLimitStr = ConfigResolver.getProjectStageAwarePropertyValue("raf.multiplefiledownloadLimit", "104857600");//Default : 100MB
        maxZipFileLimit = Long.parseLong(maxZipFileLimitStr);
        LOG.info("File {} dowloaded", getContext().getSelectedObject().getPath());
    }

    @Override
    protected boolean finalizeAction() {
        /*
        RafObject doc = getContext().getSelectedObject();
        if (doc instanceof RafRecord) {
            RafRecord record = (RafRecord) doc;
            doc = record.getDocuments().isEmpty() ? doc : record.getDocuments().get(0);
        }
        downloadFile(doc);
        commandSender.sendCommand(EventLogCommandBuilder.forRaf("RAF")
                .eventType("DownloadDocument")
                .forRafObject(doc)
                .message("event.DownloadDocument$%&" + identity.getUserName() + "$%&" + doc.getTitle())
                .user(identity.getLoginName())
                .build());
         */

        try {
            fileList.clear();
            sumOfFileLengths = 0;

            if (getContext().getSeletedItems() != null && !getContext().getSeletedItems().isEmpty() && RafMimeTypes.RAF_NODE.equals(getContext().getSeletedItems().get(0).getMimeType())) {
                throw new Exception("Raf node can not download.");
            }

            sumOfFileLengths = getSumOfFiles(getContext().getSeletedItems());

            if (sumOfFileLengths > maxZipFileLimit) {
                String errorMessage = String.format("Max Zip file limit is over, Max Zip File Limit : %d, File Size : %d", maxZipFileLimit, sumOfFileLengths);
                throw new Exception(errorMessage);
            }
            downloadZipFile();
//            getChildFiles(getContext().getSeletedItems());
            return true;
        } catch (Exception e) {
            LOG.error("Error", e);
            LOG.error(e.getMessage());
            FacesMessages.error(e.getMessage());
            return false;
        }

    }

    void downloadZipFile() throws IOException, RafException {
        HttpServletResponse response = (HttpServletResponse) facesContext.getExternalContext().getResponse();
        response.setContentType("application/zip");
        String zipFileName = String.format("prepared_zip_file_%s.zip", new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss").format(new Date()));
        response.setHeader("Content-disposition", "attachment;filename=" + zipFileName);
        OutputStream out = response.getOutputStream();
        zipOS = new ZipOutputStream(out);
        for (RafObject file : getContext().getSeletedItems()) {
            rafService.zipFile(file, file.getName(), zipOS);
        }
        zipOS.close();
        out.close();
        facesContext.responseComplete();
    }

    long getSumOfFiles(List<RafObject> files) throws RafException {
        long result = 0;
        for (RafObject file : files) {
            if (file instanceof RafFolder) {
                result += rafService.getFolderSize(file.getPath(), maxZipFileLimit);
            } else if (file instanceof RafRecord) {
                for (RafDocument document : ((RafRecord) file).getDocuments()) {
                    result += document.getLength();
                }
            } else {
                result += file.getLength();
            }
        }

        return result;
    }

    void getChildFiles(List<RafObject> files) {
        for (RafObject file : files) {
            if (file instanceof RafFolder) {
                try {
                    getChildFiles(rafService.getCollection(file.getId()).getItems());
                } catch (RafException ex) {
                    LOG.error("Error", ex);
                }
            } else if (file instanceof RafRecord) {
                ((RafRecord) file).getDocuments().forEach(document -> {
                    fileList.add(document);
                    sumOfFileLengths += document.getLength();
                });
            } else {
                fileList.add(file);
                sumOfFileLengths += file.getLength();
            }
        }
    }

    public void downloadFile(RafObject doc) {

        //FIXME: Yetki kontrolü ve event fırlatılacak
        try {
            InputStream is = rafService.getDocumentContent(doc.getId());

            HttpServletResponse response = (HttpServletResponse) facesContext.getExternalContext().getResponse();
            response.setContentType(doc.getMimeType());

            response.setHeader("Content-disposition", "attachment;filename=" + doc.getName());
            //FIXME: RafObject içine en azından RafDocument içine boyut ve hash bilgisi yazmak lazım.
            //response.setContentLength((int) content.getProperty("jcr:data").getBinary().getSize());

            try (OutputStream out = response.getOutputStream()) {
                IOUtils.copy(is, out);
                out.flush();
            }

            facesContext.responseComplete();
        } catch (RafException | IOException ex) {
            //FIXME: i18n
            LOG.error("File cannot downloded", ex);
            FacesMessages.error("File cannot downloaded");
        }
    }
}
