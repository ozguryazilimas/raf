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
        String defaultCode = "var emailExtension = \"text/html\".equals(message.getMimeType()) ? \".html\" : \".txt\";\n"
                + "var rafTemporaryEmailFolder = \"/RAF/Email/Temp\";\n"
                + "var rafEmailFolder = \"/RAF/Email\";\n"
                + "var fileName = message.getSubject().concat(emailExtension);\n"
                + "importer.uploadEmailRecord(message, rafTemporaryEmailFolder, rafEmailFolder, fileName);";
        return new EMailFetchCommand("imap", "", 993, "", "", Boolean.TRUE, "INBOX", "archive", "", "telve", defaultCode);
    }

}
