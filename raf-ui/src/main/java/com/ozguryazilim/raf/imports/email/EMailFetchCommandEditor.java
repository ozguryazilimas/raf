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
import javax.print.attribute.standard.Severity;

/**
 *
 * @author oyas
 */
@CommandEditor(command = EMailFetchCommand.class, page = RafCommandPages.EMailFetchCommandEditor.class)
public class EMailFetchCommandEditor extends CommandEditorBase<EMailFetchCommand> {

    @Inject
    private RafService rafService;

    @Override
    public EMailFetchCommand createNewCommand() {
        String defaultCode = ""
                + "var emailExtension = \"text/html\".equals(message.getMimeType()) ? \".html\" : \".txt\";\n"
                + "var fileName = message.getSubject().concat(emailExtension);\n"
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
                EMailFetchCommand.PostImportCommand.NONE,
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
            FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Path is not valid", "Path is not valid");
            facesContext.addMessage(uiComponent.getClientId(facesContext), message);
        }
    }

    public EMailFetchCommand.PostImportCommand[] getPostCommands() {
        return EMailFetchCommand.PostImportCommand.values();
    }

}
