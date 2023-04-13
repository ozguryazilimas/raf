package com.ozguryazilim.raf.imports.email;

import com.ozguryazilim.raf.ApplicationContstants;
import com.ozguryazilim.raf.RafService;
import com.ozguryazilim.raf.imports.RafCommandPages;
import com.ozguryazilim.telve.messagebus.command.ui.CommandEditor;
import com.ozguryazilim.telve.messagebus.command.ui.CommandEditorBase;
import org.apache.commons.lang3.StringUtils;

import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.component.UIInput;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.mail.Folder;
import javax.mail.MessagingException;
import javax.mail.NoSuchProviderException;
import javax.mail.Session;
import javax.mail.Store;
import java.util.Properties;

/**
 *
 * @author oyas
 */
@CommandEditor(command = EMailFetchCommand.class, page = RafCommandPages.EMailFetchCommandEditor.class)
public class EMailFetchCommandEditor extends CommandEditorBase<EMailFetchCommand> {

    private static final String ARCHIVE_INPUT_ATTR_PROTOCOL = "protocol";
    private static final String ARCHIVE_INPUT_ATTR_HOST     = "host";
    private static final String ARCHIVE_INPUT_ATTR_PORT     = "port";
    private static final String ARCHIVE_INPUT_ATTR_DOMAIN   = "domain";
    private static final String ARCHIVE_INPUT_ATTR_USER     = "user";
    private static final String ARCHIVE_INPUT_ATTR_PASSWORD = "pass";
    private static final String ARCHIVE_INPUT_ATTR_SSL      = "ssl";

    private static final String MESSAGES_CLIENTID_EMAIL_SETTINGS = "ceForm:emailConnection";

    @Inject
    private RafService rafService;

    @Override
    public EMailFetchCommand createNewCommand() {
        String defaultCode = ""
                + "var emailExtension = \"text/html\".equals(mail.getMimeType()) ? \".html\" : \".txt\";\n"
                + "var fileName = mail.getSubject().concat(emailExtension);\n"
                + "var tags = \"Email,Imported Email\";\n"
                + "\n"
                + "/*\n"
                + " Favourite email notifications for the users added relevant directories to favourite\n"
                + " is disabled by default on this command. To activate, you can call the "
                + " method with 'sendFavoriteEmail' parameter instead.\n"
                + "*/\n"
                + "\n"
                + "importer.uploadEmailRecord(message, command, fileName, tags);";

        return new EMailFetchCommand(
                "imap",
                "",
                993,
                "",
                "",
                Boolean.TRUE,
                EMailFetchCommand.PostImportCommand.ARCHIVE,
                "INBOX",
                "archive",
                "",
                "telve",
                "",
                "",
                defaultCode
        );

    }

    public void validateRafPath(FacesContext facesContext, UIComponent uiComponent, Object value) {
        String path = (String) value;
        if (StringUtils.isBlank(path) || ApplicationContstants.RAF_ROOT.equals(path) || !rafService.checkRafFolder(path)) {
            ((UIInput) uiComponent).setValid(false);
            FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Path is not valid.", "Path is not valid.");
            facesContext.addMessage(uiComponent.getClientId(facesContext), message);
        }
    }

    public void validateArchiveFolder(FacesContext facesContext, UIComponent uiComponent, Object value) {
        String folder = (String) value;
        try {
            if (!isEmailFolderPresent(uiComponent, folder)) {
                ((UIInput) uiComponent).setValid(false);
                FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Email folder does not exists.", "Email folder does not exists.");
                facesContext.addMessage(uiComponent.getClientId(), message);
            }
        } catch (MessagingException e) {
            ((UIInput) uiComponent).setValid(false);
            FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Email connection is not available. Could not check folders existence.", "Email connection is not available. Could not check folders existence.");
            facesContext.addMessage(uiComponent.getClientId(), message);
        }
    }

    public void validateEmailConnection(FacesContext facesContext, UIComponent uiComponent, Object value) {
        try {
            if (!isEmailConnectionPresent(uiComponent)) {
                ((UIInput) uiComponent).setValid(false);
                FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Email connection is not available. Please check your connection settings.", "Email connection is not available. Please check your connection settings.");
                facesContext.addMessage(MESSAGES_CLIENTID_EMAIL_SETTINGS, message);
            }
        } catch (MessagingException e) {
            ((UIInput) uiComponent).setValid(false);
            FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Email connection is not available. Please check your connection settings.", "Email connection is not available. Please check your connection settings.");
            facesContext.addMessage(MESSAGES_CLIENTID_EMAIL_SETTINGS, message);
        }
    }

    public EMailFetchCommand.PostImportCommand[] getPostCommands() {
        return EMailFetchCommand.PostImportCommand.values();
    }

    private boolean isEmailFolderPresent(UIComponent component, String folderName) throws MessagingException {
        String protocol = ((String) component.getAttributes().get(ARCHIVE_INPUT_ATTR_PROTOCOL));
        Boolean ssl = ((Boolean) component.getAttributes().get(ARCHIVE_INPUT_ATTR_SSL));
        String host = ((String) component.getAttributes().get(ARCHIVE_INPUT_ATTR_HOST));
        String user = ((String) component.getAttributes().get(ARCHIVE_INPUT_ATTR_USER));
        String pass = ((String) component.getAttributes().get(ARCHIVE_INPUT_ATTR_PASSWORD));
        int port = ((Integer) component.getAttributes().get(ARCHIVE_INPUT_ATTR_PORT));

        Properties properties = new Properties();
        properties.put("mail.store.protocol", protocol);
        properties.put("mail." + protocol + ".host", host);
        properties.put("mail." + protocol + ".port", port);
        properties.put("mail." + protocol + ".starttls.enable", ssl);
        properties.put("mail.imap.connectiontimeout", "1000");

        Session emailSession = Session.getDefaultInstance(properties);
        Store store = emailSession.getStore(protocol + "s");
        store.connect(host, port, user, pass);

        Folder emailFolder = store.getFolder(folderName);

        return emailFolder.exists();
    }

    private boolean isEmailConnectionPresent(UIComponent component) throws MessagingException {
        String protocol = ((String) component.getAttributes().get(ARCHIVE_INPUT_ATTR_PROTOCOL));
        Boolean ssl = ((Boolean) component.getAttributes().get(ARCHIVE_INPUT_ATTR_SSL));
        String host = ((String) component.getAttributes().get(ARCHIVE_INPUT_ATTR_HOST));
        String user = ((String) component.getAttributes().get(ARCHIVE_INPUT_ATTR_USER));
        String pass = ((String) component.getAttributes().get(ARCHIVE_INPUT_ATTR_PASSWORD));
        int port = ((Integer) component.getAttributes().get(ARCHIVE_INPUT_ATTR_PORT));

        Properties properties = new Properties();
        properties.put("mail.store.protocol", protocol);
        properties.put("mail." + protocol + ".host", host);
        properties.put("mail." + protocol + ".port", port);
        properties.put("mail." + protocol + ".starttls.enable", ssl);
        properties.put("mail.imap.connectiontimeout", "1000");

        Session emailSession = Session.getDefaultInstance(properties);
        Store store = emailSession.getStore(protocol + "s");
        store.connect(host, port, user, pass);
        return store.isConnected();
    }
}
