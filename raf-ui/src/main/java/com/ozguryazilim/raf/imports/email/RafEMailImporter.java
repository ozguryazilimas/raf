package com.ozguryazilim.raf.imports.email;

import com.google.common.base.Charsets;
import com.ozguryazilim.raf.RafException;
import com.ozguryazilim.raf.RafService;
import com.ozguryazilim.raf.category.RafCategoryService;
import com.ozguryazilim.raf.encoder.RafEncoder;
import com.ozguryazilim.raf.encoder.RafEncoderFactory;
import com.ozguryazilim.raf.imports.email.parser.EMailParser;
import com.ozguryazilim.raf.models.RafDocument;
import com.ozguryazilim.raf.models.RafMetadata;
import com.ozguryazilim.raf.models.RafObject;
import com.ozguryazilim.raf.models.RafRecord;
import com.ozguryazilim.raf.models.email.EMailAttacment;
import com.ozguryazilim.raf.models.email.EMailMessage;
import com.ozguryazilim.raf.models.email.MeetingFile;
import com.ozguryazilim.raf.tag.TagSuggestionService;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.activation.MimeType;
import javax.activation.MimeTypeParseException;
import javax.mail.MessagingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author oyas
 */
public class RafEMailImporter implements Serializable {

    private RafService rafService;
    private RafCategoryService rafCategoryService;
    private TagSuggestionService tagSuggestionService;

    RafEncoder re;

    private static final Logger LOG = LoggerFactory.getLogger(RafEMailImporter.class);

    public RafEMailImporter(RafService rafService, RafCategoryService rafCategoryService, TagSuggestionService tagSuggestionService) {
        this.rafService = rafService;
        this.rafCategoryService = rafCategoryService;
        this.tagSuggestionService = tagSuggestionService;
        if (this.re == null) {
            this.re = RafEncoderFactory.getFileNameEncoder();
        }
    }

    private boolean checkRafFolder(String folder) {
        try {
            return rafService.getFolder(folder) != null;
        } catch (RafException ex) {
            return false;
        }
    }

    public boolean isFileExistsInRAF(String rafPath) {
        try {
            RafObject rafObject = rafService.getRafObjectByPath(rafPath);
            return rafObject != null;
        } catch (RafException ex) {
            LOG.error("Raf exception", ex.getMessage());
        }
        return false;
    }

    public void debug(String s) {
        LOG.debug(s);
    }

    public void debug(String s, Object o) {
        LOG.debug(s, o);
    }

    public void debug(String s, Throwable t) {
        LOG.debug(s, t);
    }

    public void info(String s, Object o) {
        LOG.info(s, o);
    }

    public String encodeFilePath(String path) {
        return re.encode(path);
    }

    public EMailMessage parseEmail(String eml) {
        try {
            EMailParser parser = new EMailParser();
            EMailMessage message = parser.parse(eml);
            return message;
        } catch (MessagingException | IOException ex) {
            LOG.error("Mail Import Error", ex);
            return null;
        }
    }

    /**
     * Email içerisinden RFC5545e uygun toplantı dosyasını ayıklayarak döner
     *
     * @param message
     * @return
     */
    public MeetingFile getMeetingFile(EMailMessage message) throws MimeTypeParseException {
        for (EMailAttacment attacment : message.getAttachments()) {
            MimeType mimeType = new MimeType(attacment.getMimeType());
            if ("calendar".equals(mimeType.getSubType()) || "ics".equals(mimeType.getSubType())) {
                return new MeetingFile(attacment);
            }
        }
        return null;
    }

    public RafDocument uploadEmail(EMailMessage message, String filePath) {
        LOG.debug("Email is importing to raf : ".concat(message.getMessageId()));
        filePath = encodeFilePath(filePath);
        InputStream targetStream = new ByteArrayInputStream(message.getContent().getBytes(Charsets.UTF_8));
        RafDocument doc = null;
        try {
            doc = rafService.uploadDocument(filePath, targetStream);
            targetStream.close();
        } catch (RafException | IOException ex) {
            debug("Error", ex);
        }
        return doc;
    }

    public RafRecord moveToRecord(EMailMessage eMailMessage, RafDocument doc, String recordFolderPath) {
        recordFolderPath = encodeFilePath(recordFolderPath);
        RafRecord record = new RafRecord();
        record.setName(doc.getName());
        record.setTitle(eMailMessage.getSubject());
        record.setInfo("İçeri aktarılan E-Posta.");
        record.setPath(recordFolderPath);
        record.setMainDocument(doc.getName());
        record.setProcessIntanceId(0L);
        record.setRecordType("emailDoc");
        record.setDocumentType("emailDoc");
        record.setElectronicDocument(true);
        try {
            record = rafService.createRecord(record);
            rafService.moveObject(doc, record);
        } catch (RafException ex) {
            debug("error", ex);
            record = null;
        }
        return record;
    }

    public void addEmailMetadataToRecord(RafRecord record, EMailMessage message) {
        List<RafMetadata> metaDatas = new ArrayList();
        RafMetadata m = new RafMetadata();
        m.setType("emailDoc:metadata");
        m.getAttributes().put("emailDoc:messageId", message.getMessageId());
        m.getAttributes().put("emailDoc:from", message.getFrom().getAddress());
        m.getAttributes().put("emailDoc:date", message.getDate());
        m.getAttributes().put("emailDoc:subject", message.getSubject());
        m.getAttributes().put("emailDoc:toList", message.getToList().toString());
        m.getAttributes().put("emailDoc:ccList", message.getCcList().toString());
        m.getAttributes().put("emailDoc:bccList", message.getBccList().toString());
        m.getAttributes().put("emailDoc:references", message.getReferences().toString());
        m.getAttributes().put("emailDoc:relatedReferenceId", message.getRelatedReferenceId());
        metaDatas.add(m);
        try {
            rafService.saveMetadatas(record.getId(), metaDatas);
        } catch (RafException ex) {
            debug("error", ex);
        }
    }

    public void uploadAttachmentsToRecord(EMailMessage message, String rafAttachmentFolder, RafRecord record) {
        try {
            rafAttachmentFolder = encodeFilePath(rafAttachmentFolder);
            if (message.getAttachments() != null && !message.getAttachments().isEmpty()) {
                if (!checkRafFolder(rafAttachmentFolder)) {
                    rafService.createFolder(rafAttachmentFolder);
                }
                for (EMailAttacment attachment : message.getAttachments()) {
                    try {
                        String filePath = re.encode(rafAttachmentFolder.concat("/").concat(attachment.getName()));
                        RafDocument document = rafService.uploadDocument(filePath, new ByteArrayInputStream(attachment.getContent()));
                        if (document != null) {
                            rafService.moveObject(document, record);
                        }
                    } catch (Exception ex) {
                        LOG.error("Attachment Upload Exception", ex);
                    }

                }
            }
        } catch (RafException ex) {
            LOG.error("Raf Exception", ex);
        }
    }

    public RafRecord uploadEmailRecord(EMailMessage message, String temporaryFolderPath, String rafEmailFolder, String fileName, String tags) {
        RafRecord record = null;
        try {
            temporaryFolderPath = encodeFilePath(temporaryFolderPath);
            String emailFilePath = encodeFilePath(temporaryFolderPath.concat("/").concat(fileName.replace("/", "_")));
            String emailRecordPath = encodeFilePath(rafEmailFolder);
            if (!checkRafFolder(temporaryFolderPath)) {
                rafService.createFolder(temporaryFolderPath);
            }
            if (!checkRafFolder(emailRecordPath)) {
                rafService.createFolder(emailRecordPath);
            }
            RafDocument emailDoc = uploadEmail(message, emailFilePath);
            if (emailDoc != null) {
                record = moveToRecord(message, emailDoc, emailRecordPath);
                if (record != null) {
                    addEmailMetadataToRecord(record, message);
                    uploadAttachmentsToRecord(message, temporaryFolderPath, record);
                    if (tags != null) {
                        String rafCode = temporaryFolderPath.split("/")[2];
                        List<String> tagList = Arrays.asList(tags.split(","));
                        for (String tag : tagList) {
                            if (!tagSuggestionService.getSuggestions(rafCode).contains(tag)) {
                                tagSuggestionService.saveSuggestion(rafCode, tag);
                            }
                        }
                        record.setTags(tagList);
                        rafService.saveProperties(record);
                    }
                }
            }
        } catch (RafException ex) {
            LOG.error("Raf Exception", ex);
        }
        return record;
    }

}
