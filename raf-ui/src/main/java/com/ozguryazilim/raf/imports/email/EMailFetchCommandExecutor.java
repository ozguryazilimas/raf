package com.ozguryazilim.raf.imports.email;

import com.ozguryazilim.telve.messagebus.command.AbstractCommandExecuter;
import com.ozguryazilim.telve.messagebus.command.CommandExecutor;
import com.ozguryazilim.telve.messagebus.command.CommandSender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Store;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
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
    private EMailImportCommandExecutor eMailImportCommandExecutor;

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

            // retrieve the messages from the folder in an array and print it
            Message[] messages = emailFolder.getMessages();
            LOG.info("messages.length : {}", messages.length);

            for (Message message : messages) {
                if (!message.isExpunged()) {
                    EMailImportCommand importCommand = getImportCommand(message, command, store);
                    commandSender.sendCommand(importCommand);
                }
            }

        } catch (MessagingException | IOException ex) {
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

    private EMailImportCommand getImportCommand(Message message, EMailFetchCommand command, Store store) throws MessagingException, IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        message.writeTo(out);
        String eml = out.toString();
        LOG.trace("Message : {}", eml);

        EMailImportCommand importCommand = new EMailImportCommand();
        importCommand.setEml(message);
        importCommand.setRafPath(command.getRafPath());
        importCommand.setTempPath(command.getTempPath());
        importCommand.setJexlExp(command.getJexlExp());
        importCommand.setPostImportCommand(command.getPostImportCommand());
        importCommand.setFetchCommand(command);
        importCommand.setParsedEmail(RafEMailImporter.parseEmail(message));
        return importCommand;
    }

}
