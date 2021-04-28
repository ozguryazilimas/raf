package com.ozguryazilim.raf.elasticsearch.search;

import com.ozguryazilim.telve.messagebus.command.AbstractStorableCommand;
import java.util.Date;

public class ElasticSearchExporterCommand extends AbstractStorableCommand {

    private String dbHostName;
    private int dbPort;
    private String dbName;
    private String dbUserName;
    private String dbPassword;
    private Boolean owerwrite;
    private Boolean reindexRepository;
    private Date startDate;

    public ElasticSearchExporterCommand() {
    }

    public ElasticSearchExporterCommand(String dbHostName, int dbPort, String dbName, String dbUserName, String dbPassword, Boolean owerwrite, Boolean reindexRepository, Date startDate) {
        this.dbHostName = dbHostName;
        this.dbPort = dbPort;
        this.dbName = dbName;
        this.dbUserName = dbUserName;
        this.dbPassword = dbPassword;
        this.owerwrite = owerwrite;
        this.reindexRepository = reindexRepository;
        this.startDate = startDate;
    }

    public String getDbHostName() {
        return dbHostName;
    }

    public Boolean getOwerwrite() {
        return owerwrite;
    }

    public Boolean getReindexRepository() {
        return reindexRepository;
    }

    public Date getStartDate() {
        return startDate;
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

    public void setOwerwrite(Boolean owerwrite) {
        this.owerwrite = owerwrite;
    }

    public void setReindexRepository(Boolean reindexRepository) {
        this.reindexRepository = reindexRepository;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

}
