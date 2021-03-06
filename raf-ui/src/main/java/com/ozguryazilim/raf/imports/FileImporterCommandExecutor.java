package com.ozguryazilim.raf.imports;

import com.google.common.base.Strings;
import com.ozguryazilim.raf.RafException;
import com.ozguryazilim.raf.RafService;
import com.ozguryazilim.raf.definition.RafDefinitionService;
import com.ozguryazilim.raf.encoder.RafEncoder;
import com.ozguryazilim.raf.encoder.RafEncoderFactory;
import com.ozguryazilim.raf.entities.RafDefinition;
import com.ozguryazilim.raf.models.RafFolder;
import com.ozguryazilim.raf.models.RafObject;
import com.ozguryazilim.telve.messagebus.command.AbstractCommandExecuter;
import com.ozguryazilim.telve.messagebus.command.CommandExecutor;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
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

    @Override
    public void execute(FileImporterCommand command) {
        try {
            if (Strings.isNullOrEmpty(command.getFileListRecorderFilePath()) || Strings.isNullOrEmpty(command.getLocalPath()) || Strings.isNullOrEmpty(command.getRafPath())) {
                LOG.error("File importer command configuration is not valid!");
                return;
            }
            String localPath = command.getLocalPath();
            String rafPath = command.getRafPath();
            if ("/".equals(localPath.substring(localPath.length() - 1, localPath.length()))) {
                localPath = localPath.substring(0, localPath.lastIndexOf("/"));
            }
            if ("/".equals(rafPath.substring(rafPath.length() - 1, rafPath.length()))) {
                rafPath = rafPath.substring(0, rafPath.lastIndexOf("/"));
            }
            List<File> fileList = new ArrayList();
            recursiveFileScanner(localPath, fileList);
            transferFiles(fileList, localPath, rafPath);
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

    private void transferFiles(List<File> fileList, String startDir, String raf) {
        try {
            RafEncoder re = RafEncoderFactory.getFileNameEncoder();
            RafEncoder de = RafEncoderFactory.getDirNameEncoder();
            String[] splittedRaf = raf.split("/");
            String rafName = splittedRaf[0];
            RafDefinition rafDefinition = rafDefinitionService.getRafDefinitionByCode(rafName);
            String rafPath = rafDefinition.getNode().getPath();
            if (rafPath.contains("PRIVATE") && rafPath.contains("SYSTEM")) {
                rafPath = rafPath.replace("/SYSTEM", "");
            }
            if (splittedRaf.length > 1) {
                for (int i = 1; i < splittedRaf.length; i++) {
                    rafPath = rafPath.concat("/").concat(splittedRaf[i]);
                }
            }
            LOG.debug("Directory is {}", startDir);
            double fileCount = fileList.size();
            double i = 0.d;
            double percentage;
            for (File file : fileList) {
                if (file.exists() && file.canRead()) {
                    try {
                        String folder = de.encode(file.getParent().replaceAll(startDir, "").concat("/"));
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
                            transferFile(rafFilePath, rafFolder, file);
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
            LOG.debug("Raf exception", ex);
            return null;
        }
    }

    private RafFolder getRafFolder(String rafFolder) {
        try {
            RafFolder rf = rafService.getFolder(rafFolder);
            return rf;
        } catch (RafException ex) {
            LOG.debug("Raf exception", ex);
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
