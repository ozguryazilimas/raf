package com.ozguryazilim.raf.mongo.search;

import com.ozguryazilim.telve.messagebus.command.AbstractStorableCommand;

public class MongoSearchExporterCommand extends AbstractStorableCommand {

    private String dbHostName;
    private int dbPort;
    private String dbName;
    private String dbUserName;
    private String dbPassword;

    public MongoSearchExporterCommand() {
    }

    public MongoSearchExporterCommand(String dbHostName, int dbPort, String dbName, String dbUserName, String dbPassword) {
        this.dbHostName = dbHostName;
        this.dbPort = dbPort;
        this.dbName = dbName;
        this.dbUserName = dbUserName;
        this.dbPassword = dbPassword;
    }

    public String getDbHostName() {
        return dbHostName;
    }

    public void setDbHostName(String dbHostName) {
        this.dbHostName = dbHostName;
    }

    public int getDbPort() {
        return dbPort;
    }

    public void setDbPort(int dbPort) {
        this.dbPort = dbPort;
    }

    public String getDbName() {
        return dbName;
    }

    public void setDbName(String dbName) {
        this.dbName = dbName;
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

}
