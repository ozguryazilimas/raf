package com.ozguryazilim.raf.imports.email;

import com.ozguryazilim.telve.messagebus.command.AbstractStorableCommand;

/**
 *
 * @author oyas
 */
public class EMailFetchCommand extends AbstractStorableCommand {

    private String protocol;
    private String host;
    private Integer port;
    private String user;
    private String pass;
    private Boolean ssl;
    private PostImportCommand postImportCommand = PostImportCommand.NONE;
    private String folder;
    private String archiveFolder;
    private String domain;
    private String defaultUser;
    private String rafPath;
    private String tempPath;
    private String jexlExp;

    public enum PostImportCommand {
        NONE,
        ARCHIVE,
        DELETE_AND_EXPUNGE
    }

    public EMailFetchCommand() {
    }

    public EMailFetchCommand(String protocol, String host, Integer port, String user, String pass, Boolean ssl, PostImportCommand postImportCommand, String folder, String archiveFolder, String domain, String defaultUser, String rafPath, String tempPath, String jexlExp) {
        this.protocol = protocol;
        this.host = host;
        this.port = port;
        this.user = user;
        this.pass = pass;
        this.ssl = ssl;
        this.postImportCommand = postImportCommand;
        this.folder = folder;
        this.archiveFolder = archiveFolder;
        this.domain = domain;
        this.defaultUser = defaultUser;
        this.rafPath = rafPath;
        this.tempPath = tempPath;
        this.jexlExp = jexlExp;
    }

    public String getProtocol() {
        return protocol;
    }

    public String getRafPath() {
        return rafPath;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    public String getUser() {
        return user;
    }

    public void setRafPath(String rafPath) {
        this.rafPath = rafPath;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getPass() {
        return pass;
    }

    public void setPass(String pass) {
        this.pass = pass;
    }

    public Boolean getSsl() {
        return ssl;
    }

    public void setSsl(Boolean ssl) {
        this.ssl = ssl;
    }

    public String getFolder() {
        return folder;
    }

    public void setFolder(String folder) {
        this.folder = folder;
    }

    public String getArchiveFolder() {
        return archiveFolder;
    }

    public void setArchiveFolder(String archiveFolder) {
        this.archiveFolder = archiveFolder;
    }

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public String getDefaultUser() {
        return defaultUser;
    }

    public void setDefaultUser(String defaultUser) {
        this.defaultUser = defaultUser;
    }

    public String getJexlExp() {
        return jexlExp;
    }

    public void setJexlExp(String jexlExp) {
        this.jexlExp = jexlExp;
    }

    public String getTempPath() {
        return tempPath;
    }

    public void setTempPath(String tempPath) {
        this.tempPath = tempPath;
    }

    public PostImportCommand getPostImportCommand() {
        return postImportCommand;
    }

    public void setPostImportCommand(PostImportCommand postImportCommand) {
        this.postImportCommand = postImportCommand;
    }
}
