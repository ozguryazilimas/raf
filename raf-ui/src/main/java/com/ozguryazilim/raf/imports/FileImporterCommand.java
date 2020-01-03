package com.ozguryazilim.raf.imports;

import com.ozguryazilim.telve.messagebus.command.AbstractStorableCommand;

public class FileImporterCommand extends AbstractStorableCommand {

    private String localPath;
    private String rafPath;
    private String fileListRecorderFilePath;

    public FileImporterCommand(String localPath, String rafPath, String fileListRecorderFilePath) {
        this.localPath = localPath;
        this.rafPath = rafPath;
        this.fileListRecorderFilePath = fileListRecorderFilePath;
    }

    public String getFileListRecorderFilePath() {
        return fileListRecorderFilePath;
    }

    public String getLocalPath() {
        return localPath;
    }

    public void setFileListRecorderFilePath(String fileListRecorderFilePath) {
        this.fileListRecorderFilePath = fileListRecorderFilePath;
    }

    public void setLocalPath(String localPath) {
        this.localPath = localPath;
    }

    public String getRafPath() {
        return rafPath;
    }

    public void setRafPath(String rafPath) {
        this.rafPath = rafPath;
    }

}
