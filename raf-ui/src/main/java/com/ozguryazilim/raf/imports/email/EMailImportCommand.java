package com.ozguryazilim.raf.imports.email;

import com.ozguryazilim.raf.models.email.EMailMessage;
import com.ozguryazilim.telve.messagebus.command.AbstractCommand;

import javax.mail.Message;

/**
 * Tek bir e-posta taşır içerisinde.
 * <p>
 * Böylece importer kuyruktan çıkan her bir e-posta ile tek tek ilgilenir.
 *
 * @author Hakan Uygun
 */
public class EMailImportCommand extends AbstractCommand {

    private Message eml;
    private EMailMessage parsedEmail;
    private String rafPath;
    private String tempPath;
    private String jexlExp;

    private EMailFetchCommand fetchCommand;
    private EMailFetchCommand.PostImportCommand postImportCommand;

    public Message getEml() {
        return eml;
    }

    public String getRafPath() {
        return rafPath;
    }

    public void setEml(Message eml) {
        this.eml = eml;
    }

    public void setRafPath(String rafPath) {
        this.rafPath = rafPath;
    }

    public String getJexlExp() {
        return jexlExp;
    }

    public void setJexlExp(String jexlExp) {
        this.jexlExp = jexlExp;
    }

    public EMailFetchCommand.PostImportCommand getPostImportCommand() {
        return postImportCommand;
    }

    public void setPostImportCommand(EMailFetchCommand.PostImportCommand postImportCommand) {
        this.postImportCommand = postImportCommand;
    }

    public String getTempPath() {
        return tempPath;
    }

    public void setTempPath(String tempPath) {
        this.tempPath = tempPath;
    }

    public EMailFetchCommand getFetchCommand() {
        return fetchCommand;
    }

    public void setFetchCommand(EMailFetchCommand fetchCommand) {
        this.fetchCommand = fetchCommand;
    }

    public EMailMessage getParsedEmail() {
        return parsedEmail;
    }

    public void setParsedEmail(EMailMessage parsedEmail) {
        this.parsedEmail = parsedEmail;
    }
}
