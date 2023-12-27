package com.ozguryazilim.raf.imports.email;

import com.ozguryazilim.raf.RafService;
import com.ozguryazilim.raf.category.RafCategoryService;
import com.ozguryazilim.raf.models.RafRecord;
import com.ozguryazilim.raf.tag.TagSuggestionService;
import com.ozguryazilim.telve.messagebus.command.AbstractCommandExecuter;
import com.ozguryazilim.telve.messagebus.command.CommandExecutor;
import com.ozguryazilim.telve.messagebus.command.CommandSender;
import org.apache.commons.jexl3.JexlBuilder;
import org.apache.commons.jexl3.JexlContext;
import org.apache.commons.jexl3.JexlEngine;
import org.apache.commons.jexl3.JexlScript;
import org.apache.commons.jexl3.MapContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.mail.Flags;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Store;
import java.util.Properties;

/**
 * Bu Command ve Executor'u sadece e-posta bağlantısını yapıp okunmamış
 * e-postaları alarak ayrı bir kuyruğa ekler.
 *
 * @author oyas
 */
@CommandExecutor(command = EMailFetchCommand.class)
public class EMailFetchCommandExecutor extends AbstractCommandExecuter<EMailFetchCommand> {

    private static final Logger LOG = LoggerFactory.getLogger(EMailFetchCommandExecutor.class);

    private static final String IMAP = "imap";
    private static final String POP3 = "pop3";

    @Inject
    private CommandSender commandSender;

    @Inject
    private RafService rafService;

    @Inject
    private RafCategoryService rafCategoryService;

    @Inject
    private TagSuggestionService tagSuggestionService;

    @Override
    public void execute(EMailFetchCommand command) {

        if (command.getProtocol() == null) {
            LOG.error("EMail Configuration Does Not Complete");
            return;
        }

        LOG.debug("EMail Import Process Begin");
        try {
            String protocol = command.getProtocol();

            //Burada kahveden okuma yapılacak
            Store store = getSession(command).getStore(protocol + "s");

            //FIXME: parola kahvede hashli saklanacak
            String host = command.getHost();
            String user = command.getUser();
            String pass = command.getPass();
            int port = command.getPort();
            store.connect(host, port, user, pass);

            String folder = command.getFolder();
            Folder emailFolder = store.getFolder(folder);

            emailFolder.open(Folder.READ_WRITE);

            String archiveFolderName = command.getArchiveFolder();
            Folder archiveFolder = store.getFolder(archiveFolderName);
            if (command.getPostImportCommand() == EMailFetchCommand.PostImportCommand.ARCHIVE && archiveFolder.exists()) {
                archiveFolder.open(Folder.READ_WRITE);
            }

            // retrieve the messages from the folder in an array and print it
            Message[] messages = emailFolder.getMessages();
            LOG.info("messages.length : {}", messages.length);

            for (Message message : messages) {
                if (!message.isExpunged()) {

                    LOG.info("E-mail importer jexl command executing.");
                    JexlEngine jexl = new JexlBuilder().create();
                    JexlScript e = jexl.createScript(command.getJexlExp());
                    JexlContext jc = new MapContext();
                    jc.set("rafService", rafService);
                    jc.set("message", RafEMailImporter.parseEmail(message));
                    jc.set("importer", new RafEMailImporter(rafService, rafCategoryService, tagSuggestionService));
                    jc.set("path", command.getRafPath());
                    jc.set("tempPath", command.getTempPath());
                    jc.set("command", command);
                    RafRecord o = (RafRecord) e.execute(jc);
                    LOG.debug("E-mail importer jexl result. {}", o);
                    LOG.info("E-mail importer jexl command executed.");

                    // Email is imported if record is not null
                    if (o != null) {
                        switch (command.getPostImportCommand()) {
                            case ARCHIVE:
                                if ("imap".equals(command.getProtocol()) && archiveFolderName != null && !archiveFolderName.isEmpty() && emailFolder.isOpen() && archiveFolder.isOpen()) {
                                    emailFolder.copyMessages(new Message[]{message}, archiveFolder);
                                    message.setFlag(Flags.Flag.DELETED, true);
                                }
                                break;
                            case DELETE:
                                if (emailFolder.exists() && emailFolder.isOpen()) {
                                    message.setFlag(Flags.Flag.DELETED, true);
                                }
                                break;
                            default:
                                break;
                        }
                    }
                }
            }

            archiveFolder.close(false);
            emailFolder.close(true);

        } catch (MessagingException ex) {
            LOG.error("EMail Import Failed", ex);
        }
    }

    /**
     * Kahve'ye girilmiş bilgiler üzeriden EMail sesssion açacak.
     * <p>
     * Burada aslında farklı doğrulama biçimleri v.b. için sunucudan mı alsak?
     * Ama o taktirde de e-posta ayarlarının kurulumu yapan sistem yöneticisi
     * tarafından yapılması gerekiyor.
     *
     * @return
     */
    protected Session getSession(EMailFetchCommand command) {

        String protocol = command.getProtocol();

        Properties properties = new Properties();
        properties.put("mail.store.protocol", protocol);
        properties.put("mail." + protocol + ".host", command.getHost());
        properties.put("mail." + protocol + ".port", command.getPort());
        properties.put("mail." + protocol + ".starttls.enable", command.getSsl());
        properties.put("mail.imap.connectiontimeout", "1000");

        Session emailSession = Session.getDefaultInstance(properties);
        // emailSession.setDebug(true);
        return emailSession;
    }

}
