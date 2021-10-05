package com.ozguryazilim.raf.imports.email;

import com.ozguryazilim.telve.messagebus.command.AbstractCommandExecuter;
import com.ozguryazilim.telve.messagebus.command.CommandExecutor;
import com.ozguryazilim.telve.messagebus.command.CommandSender;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Properties;
import javax.inject.Inject;
import javax.mail.Flags;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Store;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

                ByteArrayOutputStream out = new ByteArrayOutputStream();
                message.writeTo(out);
                String eml = out.toString();
                LOG.trace("Message : {}", eml);

                EMailImportCommand importCommand = new EMailImportCommand();
                importCommand.setEml(eml);
                importCommand.setRafPath(command.getRafPath());
                importCommand.setJexlExp(command.getJexlExp());

                
                message.setFlag(Flags.Flag.DELETED, true);
                
                
                commandSender.sendCommand(importCommand);
                message.setFlag(Flags.Flag.DELETED, true);
            }
            
            emailFolder.expunge();
            
            //FIXME: Arşive alıp almama parametreye bağlanmalı. Ki aşağıdaki kod sorunlu. 
            /*
            String archiveFolderName = command.getArchiveFolder();
            if (IMAP.equals(protocol) && archiveFolderName != null && !archiveFolderName.isEmpty()) {
                //arşivle
                Folder archiveFolder = store.getFolder(archiveFolderName);
                archiveFolder.open(Folder.READ_WRITE);
                //TODO: direk kafadan arşivledik ama EMailImportCommand işi halledemezse ne olacak?
                emailFolder.copyMessages(messages, archiveFolder);
                archiveFolder.close(false);
            }
            */
            // close the store and folder objects
            emailFolder.close(true);
            store.close();
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
}
