package com.ozguryazilim.raf.externaldoc.importer;

import com.ozguryazilim.telve.messagebus.command.AbstractStorableCommand;

public class DoxoftImporterCommand extends AbstractStorableCommand {

    private String dbHostName;
    private int dbPort;
    private String dbName;
    private String dbUserName;
    private String dbPassword;
    private String doxoftFileServerDirectoryNames;
    private String doxoftFileServerDirectoryPaths;
    private String doxoftFolderNames;
    private String rafNames;
    private boolean importInWFDocuments;
    private boolean importDocumentAttributes;

    public DoxoftImporterCommand() {
    }

    public DoxoftImporterCommand(String dbHostName, int dbPort, String dbName, String dbUserName, String dbPassword, String doxoftFileServerDirectoryNames, String doxoftFileServerDirectoryPaths, String doxoftFolderNames, String rafNames, boolean importInWFDocuments, boolean importDocumentAttributes) {
        this.dbHostName = dbHostName;
        this.dbPort = dbPort;
        this.dbName = dbName;
        this.dbUserName = dbUserName;
        this.dbPassword = dbPassword;
        this.doxoftFileServerDirectoryNames = doxoftFileServerDirectoryNames;
        this.doxoftFileServerDirectoryPaths = doxoftFileServerDirectoryPaths;
        this.doxoftFolderNames = doxoftFolderNames;
        this.rafNames = rafNames;
        this.importInWFDocuments = importInWFDocuments;
        this.importDocumentAttributes = importDocumentAttributes;
    }

    public String getDbHostName() {
        return dbHostName;
    }

    public String getDbName() {
        return dbName;
    }

    public boolean isImportDocumentAttributes() {
        return importDocumentAttributes;
    }

    public boolean isImportInWFDocuments() {
        return importInWFDocuments;
    }

    public void setDbHostName(String dbHostName) {
        this.dbHostName = dbHostName;
    }

    public int getDbPort() {
        return dbPort;
    }

    public void setDbName(String dbName) {
        this.dbName = dbName;
    }

    public void setDbPort(int dbPort) {
        this.dbPort = dbPort;
    }

    public String getDbUserName() {
        return dbUserName;
    }

    public void setDbUserName(String dbUserName) {
        this.dbUserName = dbUserName;
    }

    public String getDbPassword() {
        return dbPassword;
    }

    public void setDbPassword(String dbPassword) {
        this.dbPassword = dbPassword;
    }

    public String getDoxoftFileServerDirectoryNames() {
        return doxoftFileServerDirectoryNames;
    }

    public void setDoxoftFileServerDirectoryNames(String doxoftFileServerDirectoryNames) {
        this.doxoftFileServerDirectoryNames = doxoftFileServerDirectoryNames;
    }

    public String getDoxoftFileServerDirectoryPaths() {
        return doxoftFileServerDirectoryPaths;
    }

    public void setDoxoftFileServerDirectoryPaths(String doxoftFileServerDirectoryPaths) {
        this.doxoftFileServerDirectoryPaths = doxoftFileServerDirectoryPaths;
    }

    public String getDoxoftFolderNames() {
        return doxoftFolderNames;
    }

    public void setDoxoftFolderNames(String doxoftFolderNames) {
        this.doxoftFolderNames = doxoftFolderNames;
    }

    public String getRafNames() {
        return rafNames;
    }

    public void setImportDocumentAttributes(boolean importDocumentAttributes) {
        this.importDocumentAttributes = importDocumentAttributes;
    }

    public void setImportInWFDocuments(boolean importInWFDocuments) {
        this.importInWFDocuments = importInWFDocuments;
    }

    public void setRafNames(String rafNames) {
        this.rafNames = rafNames;
    }

}
