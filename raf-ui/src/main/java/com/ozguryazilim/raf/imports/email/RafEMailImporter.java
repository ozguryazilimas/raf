package com.ozguryazilim.raf.imports.email;

import com.google.common.base.Charsets;
import com.ozguryazilim.raf.RafException;
import com.ozguryazilim.raf.RafService;
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
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import javax.activation.MimeType;
import javax.activation.MimeTypeParseException;
import javax.enterprise.context.Dependent;
import javax.inject.Inject;
import javax.mail.MessagingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author oyas
 */
@Dependent
public class RafEMailImporter implements Serializable {

    @Inject
    RafService rafService;

    RafEncoder re;

    private static final Logger LOG = LoggerFactory.getLogger(RafEMailImporter.class);

    private boolean checkRafFolder(String folder) {
        try {
            return rafService.getFolder(folder) != null;
        } catch (RafException ex) {
            return false;
        }
    }

    private boolean isImportedBefore(String rafPath) {
        try {
            RafObject rafObject = rafService.getRafObjectByPath(rafPath);
            return rafObject != null;
        } catch (RafException ex) {
            LOG.error("Raf exception", ex.getMessage());
        }
        return false;
    }

    public void importMail(EMailImportCommand command) {
        try {
            EMailParser parser = new EMailParser();
            if (this.re == null) {
                this.re = RafEncoderFactory.getEncoder();
            }
            EMailMessage message = parser.parse(command.getEml());
            String rafAttachmentFilePath = re.encode(command.getRafPath().concat("/Attachments"));
            String rafBodyFilePath = re.encode(command.getRafPath().concat("/Attachments/").concat(message.getSubject()).concat(".txt"));
            String rafFilePath = re.encode(command.getRafPath().concat("/").concat(message.getSubject()).concat(".txt"));
            if (isImportedBefore(rafFilePath)) {
                LOG.info("Message with messageId (" + message.getMessageId() + ") already imported");
                return;
            }
            MeetingFile meetingFile = getMeetingFile(message);
            if (meetingFile == null) {
                createEmailRafDocument(rafAttachmentFilePath, rafBodyFilePath, command.getRafPath(), message);
            }//do nothing for meeting files.            
        } catch (MessagingException | IOException | MimeTypeParseException ex) {
            LOG.error("Mail Import Error", ex);
        }
    }

    /**
     * Email içerisinden RFC5545e uygun toplantı dosyasını ayıklayarak döner
     *
     * @param message
     * @return
     */
    private MeetingFile getMeetingFile(EMailMessage message) throws MimeTypeParseException {
        for (EMailAttacment attacment : message.getAttachments()) {
            MimeType mimeType = new MimeType(attacment.getMimeType());
            if ("calendar".equals(mimeType.getSubType()) || "ics".equals(mimeType.getSubType())) {
                return new MeetingFile(attacment);
            }
        }
        return null;
    }

    private void createEmailRafDocument(String rafAttachmentFolder, String rafBodyFilePath, String rafTargetPath, EMailMessage message) {
        try {
            LOG.debug("Email is importing to raf : ".concat(message.getMessageId()));
            InputStream targetStream = new ByteArrayInputStream(message.getContent().getBytes(Charsets.UTF_8));
            RafDocument bodyDocument = rafService.uploadDocument(rafBodyFilePath, targetStream);
            targetStream.close();
            RafRecord record = new RafRecord();
            record.setName(bodyDocument.getName());
            record.setTitle(bodyDocument.getName());
            record.setInfo("İçeri aktarılan E-Posta.");
            record.setPath(rafTargetPath);
            record.setMainDocument(bodyDocument.getName());
            record.setProcessIntanceId(0L);
            record.setRecordType("emailDoc");
            record.setDocumentType("emailDoc");
            record.setElectronicDocument(true);
            record = rafService.createRecord(record);

            rafService.moveObject(bodyDocument, record);

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
            rafService.saveMetadatas(record.getId(), metaDatas);

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
        } catch (IOException ex) {
            LOG.error("IO Exception", ex);
        }
    }

}
