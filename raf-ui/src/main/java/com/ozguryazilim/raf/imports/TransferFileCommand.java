/*
 * 
 * 
 * 
 */
package com.ozguryazilim.raf.imports;

import com.ozguryazilim.telve.messagebus.command.AbstractCommand;
import java.io.File;

/**
 *
 * @author Cihan Coşgun 2019 TSPB web:https://www.tspb.org.tr
 * mailto:cihan_cosgun@outlook.com
 */
public class TransferFileCommand extends AbstractCommand {

    private String rafFilePath;
    private String fileName;
    private String rafFolder;
    private File file;

    public TransferFileCommand(String rafFilePath, String fileName, String rafFolder, File file) {
        this.rafFilePath = rafFilePath;
        this.fileName = fileName;
        this.rafFolder = rafFolder;
        this.file = file;
    }

    public String getRafFilePath() {
        return rafFilePath;
    }

    public void setRafFilePath(String rafFilePath) {
        this.rafFilePath = rafFilePath;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getRafFolder() {
        return rafFolder;
    }

    public void setRafFolder(String rafFolder) {
        this.rafFolder = rafFolder;
    }

    public File getFile() {
        return file;
    }

    public void setFile(File file) {
        this.file = file;
    }

}
