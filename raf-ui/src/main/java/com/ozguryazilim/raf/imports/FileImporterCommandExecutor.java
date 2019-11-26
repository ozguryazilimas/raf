package com.ozguryazilim.raf.imports;

import com.google.common.base.Strings;
import com.ozguryazilim.raf.RafException;
import com.ozguryazilim.raf.RafService;
import com.ozguryazilim.raf.definition.RafDefinitionService;
import com.ozguryazilim.raf.encoder.RafEncoder;
import com.ozguryazilim.raf.encoder.RafEncoderFactory;
import com.ozguryazilim.raf.entities.RafDefinition;
import com.ozguryazilim.raf.models.RafDocument;
import com.ozguryazilim.raf.models.RafFolder;
import com.ozguryazilim.raf.models.RafObject;
import com.ozguryazilim.telve.messagebus.command.AbstractCommandExecuter;
import com.ozguryazilim.telve.messagebus.command.CommandExecutor;
import com.ozguryazilim.telve.messagebus.command.CommandSender;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.TimerTask;
import javax.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@CommandExecutor(command = FileImporterCommand.class)
public class FileImporterCommandExecutor extends AbstractCommandExecuter<FileImporterCommand> {

    private static final Logger LOG = LoggerFactory.getLogger(FileImporterCommandExecutor.class);

    @Inject
    RafService rafService;

    @Inject
    RafDefinitionService rafDefinitionService;

    @Inject
    private CommandSender commandSender;

    @Override
    public void execute(FileImporterCommand command) {
        try {
            if (Strings.isNullOrEmpty(command.getFileListRecorderFilePath()) || Strings.isNullOrEmpty(command.getLocalPath()) || Strings.isNullOrEmpty(command.getRafPath())) {
                LOG.error("File importer command configuration is not valid!");
                return;
            }

            List<File> fileList = new ArrayList();
            recursiveFileScanner(command.getLocalPath(), fileList);
            List<File> transferedFileList = new ArrayList();
            transferFiles(fileList, command.getLocalPath(), command.getRafPath(), transferedFileList);
        } catch (Exception e) {
            LOG.error("There was an error during FileImporterCommand", e);
        }
    }

    private void recursiveFileScanner(String path, List<File> fileList) {
        File f = new File(path);
        File[] allSubFiles = f.listFiles();
        for (File file : allSubFiles) {
            if (file.isDirectory()) {
                recursiveFileScanner(file.getAbsolutePath(), fileList);
            } else {
                fileList.add(file);
            }
        }
    }

    private void transferFiles(List<File> fileList, String startDir, String raf, List<File> transferedFileList) {
        try {
            RafEncoder re = RafEncoderFactory.getEncoder();
            RafDefinition rafDefinition = rafDefinitionService.getRafDefinitionByCode(raf);
            String rafPath = rafDefinition.getNode().getPath();
            LOG.debug("Directory is {}", startDir);
            double fileCount = fileList.size();
            double i = 0.d;
            double transferred = 0.d;
            double percentage;
            for (File file : fileList) {
                if (file.exists() && file.canRead()) {
                    try {
                        String folder = re.encode(file.getParent().replaceAll(startDir, "").concat("/"));
                        String fileName = re.encode(file.getName());
                        String rafFolder = rafPath.concat(folder);
                        String rafFilePath = rafFolder.concat(fileName);
                        LOG.debug("Local file is {}", file.getAbsolutePath());
                        LOG.debug("RAF file is {} ", rafFilePath);
                        LOG.debug("File existing is checking {}", rafFilePath);
                        RafObject ro = getRafObject(rafFilePath);
                        if (ro != null || fileName.startsWith("~") || fileName.contains("Thumbs.db")) {
                            LOG.debug("File is exists");
                        } else {
//                            TransferFileCommand command = new TransferFileCommand(rafFilePath, fileName, rafFolder, file);
//                            commandSender.sendCommand(command);
                            transferFile(rafFilePath, rafFolder, file);
                            transferred++;
//                            Thread.sleep(1000);
                        }
                    } catch (Exception e) {
                        LOG.debug("transferFile exception", e);
                    }
                } else {
                    LOG.error("{} is can not reading.", file.getAbsolutePath());
                }
                i++;
                percentage = (i / fileCount) * 100.0d;
                LOG.info("{} percent file is tranfered", percentage);
            }
        } catch (RafException ex) {
            LOG.error("Raf exception", ex);
        }
    }

    private RafObject getRafObject(String rafFilePath) {
        try {
            RafObject ro = rafService.getRafObjectByPath(rafFilePath);
            return ro;
        } catch (RafException ex) {
//            LOG.debug("Raf exception", ex);
            return null;
        }
    }

    private RafFolder getRafFolder(String rafFolder) {
        try {
            RafFolder rf = rafService.getFolder(rafFolder);
            return rf;
        } catch (RafException ex) {
//            LOG.debug("Raf exception", ex);
            return null;
        }
    }

    private void transferFile(String rafFilePath, String rafFolder, File file) {
        try {
            LOG.debug("Checking RAF folder {}", rafFolder);
            RafFolder rf = getRafFolder(rafFolder);
            LOG.debug("Checking status : {}", rf != null);
            if (rf == null) {
                LOG.debug("Raf folder is creating");
                rf = rafService.createFolder(rafFolder);
            }
            LOG.debug("File is transfering {}", rafFilePath);
            FileInputStream fileInputStream = new FileInputStream(file);
            if (fileInputStream != null) {
                LOG.debug("File Input Stream is created.");
                rafService.uploadDocument(rafFilePath, fileInputStream);
                fileInputStream.close();
            }
        } catch (RafException ex) {
            LOG.error("Raf exception", ex);
        } catch (FileNotFoundException ex) {
            LOG.error("File Not Found exception", ex);
        } catch (IOException ex) {
            LOG.error("IOException", ex);
        }
    }

}
