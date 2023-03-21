package com.ozguryazilim.raf.imports.email;

import com.ozguryazilim.raf.imports.RafCommandPages;
import com.ozguryazilim.telve.messagebus.command.ui.CommandEditor;
import com.ozguryazilim.telve.messagebus.command.ui.CommandEditorBase;

/**
 *
 * @author oyas
 */
@CommandEditor(command = EMailFetchCommand.class, page = RafCommandPages.EMailFetchCommandEditor.class)
public class EMailFetchCommandEditor extends CommandEditorBase<EMailFetchCommand> {

    @Override
    public EMailFetchCommand createNewCommand() {
        String defaultCode = ""
                + "var emailExtension = \"text/html\".equals(message.getMimeType()) ? \".html\" : \".txt\";\n"
                + "var rafTemporaryEmailFolder = \"/RAF/Email/Temp\";\n"
                + "var rafEmailFolder = \"/RAF/Email\";\n"
                + "var fileName = message.getSubject().concat(emailExtension);\n"
                + "var tags = \"Email,Imported Email\";\n"
                + "\n"
                + "/*\n"
                + " Favourite email notifications for the users added relevant directories to favourite\n"
                + " is disabled by default on this command. To activate, you can call the "
                + " method with 'sendFavoriteEmail' parameter instead.\n"
                + " Method:\n"
                + " uploadEmailRecord(EMailMessage message, String temporaryFolderPath, String rafEmailFolder, String fileName, String tags, boolean sendFavoriteEmail)\n"
                + "*/\n"
                + "\n"
                + "importer.uploadEmailRecord(message, rafTemporaryEmailFolder, rafEmailFolder, fileName, tags);";
        return new EMailFetchCommand("imap", "", 993, "", "", Boolean.TRUE, "INBOX", "archive", "", "telve", defaultCode);
    }

}
