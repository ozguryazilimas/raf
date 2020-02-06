package com.ozguryazilim.raf.folderdivcommand;

import com.ozguryazilim.telve.messagebus.command.AbstractStorableCommand;

public class FolderDivideByYearsCommand extends AbstractStorableCommand {

    private String targetFolder;
    private Boolean divideByFolderName;
    private Boolean divideByCreateDate;

    public FolderDivideByYearsCommand() {
    }

    public FolderDivideByYearsCommand(String targetFolder, Boolean divideByFolderName, Boolean divideByCreateDate) {
        this.targetFolder = targetFolder;
        this.divideByFolderName = divideByFolderName;
        this.divideByCreateDate = divideByCreateDate;
    }

    public Boolean getDivideByFolderName() {
        return divideByFolderName;
    }

    public Boolean getDivideByCreateDate() {
        return divideByCreateDate;
    }

    public String getTargetFolder() {
        return targetFolder;
    }

    public void setDivideByFolderName(Boolean divideByFolderName) {
        this.divideByFolderName = divideByFolderName;
    }

    public void setDivideByCreateDate(Boolean divideByCreateDate) {
        this.divideByCreateDate = divideByCreateDate;
    }

    public void setTargetFolder(String targetFolder) {
        this.targetFolder = targetFolder;
    }

}
