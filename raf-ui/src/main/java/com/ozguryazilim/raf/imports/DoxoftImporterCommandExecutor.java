package com.ozguryazilim.raf.imports;

import com.google.common.base.Strings;
import com.ozguryazilim.raf.RafException;
import com.ozguryazilim.raf.RafService;
import com.ozguryazilim.raf.definition.RafDefinitionService;
import com.ozguryazilim.raf.encoder.RafEncoder;
import com.ozguryazilim.raf.encoder.RafEncoderFactory;
import com.ozguryazilim.raf.models.RafDocument;
import com.ozguryazilim.raf.models.RafMetadata;
import com.ozguryazilim.raf.models.RafObject;
import com.ozguryazilim.raf.models.RafRecord;
import com.ozguryazilim.telve.messagebus.command.AbstractCommandExecuter;
import com.ozguryazilim.telve.messagebus.command.CommandExecutor;
import com.ozguryazilim.telve.messages.FacesMessages;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import javax.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@CommandExecutor(command = DoxoftImporterCommand.class)
public class DoxoftImporterCommandExecutor extends AbstractCommandExecuter<DoxoftImporterCommand> {

    private static final Logger LOG = LoggerFactory.getLogger(DoxoftImporterCommandExecutor.class);
    private SimpleDateFormat sdf;

    @Inject
    RafService rafService;

    @Inject
    RafDefinitionService rafDefinitionService;

    DoxoftImporterCommand command;

    RafEncoder re;

    @Override
    public void execute(DoxoftImporterCommand command) {
        this.command = command;
        this.re = RafEncoderFactory.getEncoder();
        this.sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
        if (command == null || Strings.isNullOrEmpty(command.getDbHostName())) {
            FacesMessages.error("Komut ayarları tanımlı değil.");
            return;
        }
        if (Strings.isNullOrEmpty(command.getDbHostName())) {
            FacesMessages.error("Veritabanı sunucu adı tanımlı değil.");
            return;
        }
        if (Strings.isNullOrEmpty(command.getDbName())) {
            FacesMessages.error("Veritabanı adı tanımlı değil.");
            return;
        }
        if (Strings.isNullOrEmpty(command.getDoxoftFileServerDirectoryNames())) {
            FacesMessages.error("Doxoft fiziki klasör adları tanımlı değil.");
            return;
        }

        if (Strings.isNullOrEmpty(command.getDoxoftFileServerDirectoryPaths())) {
            FacesMessages.error("Doxoft fiziki klasör yolu tanımlı değil.");
            return;
        }

        if (Strings.isNullOrEmpty(command.getDoxoftFolderNames())) {
            FacesMessages.error("Doxoft sanal klasör adları tanımlı değil.");
            return;
        }

        if (Strings.isNullOrEmpty(command.getRafNames())) {
            FacesMessages.error("RAF adları tanımlı değil.");
            return;
        }

        importWFDocuments(true);//kapalı işler
        importWFDocuments(false);//açık işler
    }

    private Connection getMysqlConnection() {
        try {
            this.getClass().forName("com.mysql.jdbc.Connection");
            Connection con = (Connection) DriverManager.getConnection(String.format("jdbc:mysql://%s:%d/%s?useUnicode=true&useJDBCCompliantTimezoneShift=true&useLegacyDatetimeCode=false&serverTimezone=UTC", command.getDbHostName(), command.getDbPort(), command.getDbName()), command.getDbUserName(), command.getDbPassword());
            return con;
        } catch (ClassNotFoundException ex) {
            LOG.error("ClassNotFoundException", ex);
            return null;
        } catch (SQLException ex) {
            LOG.error("SQLException", ex);
            return null;
        }
    }

    private String getVaultPath(String vaultPath, String vaultLevel, String docId, String docFormat) {
        String[] splittedPathNames = command.getDoxoftFileServerDirectoryNames().split(",");
        String[] splittedPaths = command.getDoxoftFileServerDirectoryPaths().split(",");
        int pathIndex = Arrays.asList(splittedPathNames).indexOf(vaultPath);
        if (pathIndex > -1 && pathIndex < splittedPaths.length) {
            return splittedPaths[pathIndex].concat(File.separator).concat(vaultLevel).concat(File.separator).concat(docId).concat(".").concat(docFormat);
        } else {
            LOG.debug("{} Vault path not found.", vaultPath);
            return null;
        }
    }

    private String getRafPathFolder(String docName, String docFormat, String folder, String parentFolder) {
        String[] splittedDoxoftFolderNames = command.getDoxoftFolderNames().split(",");
        String[] splittedRafNames = command.getRafNames().split(",");
        int pathIndex = Arrays.asList(splittedDoxoftFolderNames).indexOf(parentFolder);
        if (pathIndex > -1 && pathIndex < splittedRafNames.length) {
//            return "/RAF/".concat(splittedRafNames[pathIndex]).concat("/EVRAKLAR/").concat(folder).concat("/").concat(docName).concat(".").concat(docFormat);
            return "/RAF/".concat(splittedRafNames[pathIndex]).concat("/EVRAKLAR/").concat(folder);
        } else {
            LOG.debug("{} Parent folder not found.", parentFolder);
            return null;
        }
    }

    private String getRafRecordPath(String docName, String docFormat, String folder, String parentFolder) {
        String[] splittedDoxoftFolderNames = command.getDoxoftFolderNames().split(",");
        String[] splittedRafNames = command.getRafNames().split(",");
        int pathIndex = Arrays.asList(splittedDoxoftFolderNames).indexOf(parentFolder);
        if (pathIndex > -1 && pathIndex < splittedRafNames.length) {
            return "/RAF/".concat(splittedRafNames[pathIndex]).concat("/EVRAKLAR/").concat(folder).concat("/").concat(docName).concat(".").concat(docFormat);
        } else {
            LOG.debug("{} Parent folder not found.", parentFolder);
            return "";
        }
    }

    private String getRafPath(String docName, String docFormat, String folder, String parentFolder) {
        String[] splittedDoxoftFolderNames = command.getDoxoftFolderNames().split(",");
        String[] splittedRafNames = command.getRafNames().split(",");
        int pathIndex = Arrays.asList(splittedDoxoftFolderNames).indexOf(parentFolder);
        if (pathIndex > -1 && pathIndex < splittedRafNames.length) {
            return "/RAF/".concat(splittedRafNames[pathIndex]).concat("/EVRAKLAR/EKLER/").concat(folder).concat("/").concat(docName).concat(".").concat(docFormat);
        } else {
            LOG.debug("{} Parent folder not found.", parentFolder);
            return "";
        }
    }

    private boolean checkRafFolder(String folder) {
        try {
            return rafService.getFolder(folder) != null;
        } catch (RafException ex) {
            return false;
        }
    }

    private boolean checkRafPath(String rafPath) {
        try {
            return rafService.getRafObjectByPath(rafPath) != null;
        } catch (RafException e) {
            return false;
        }
    }

    private void importDocument(Connection con, ResultSet rs, boolean finishedWF) {
        try {
            String docId = String.valueOf(rs.getLong("ID"));
            LOG.debug("{} Document is importing.", rs.getString("NAME"));
            String vaultPath = getVaultPath(rs.getString("VAULT_PATH"), rs.getString("VAULT_LEVEL"), docId, rs.getString("FORMAT"));
            Path filePath = Paths.get(vaultPath);
            if (Files.exists(filePath) && Files.isReadable(filePath)) {
                try {
                    FileInputStream fileInputStream = new FileInputStream(filePath.toFile());
                    BufferedInputStream bis = new BufferedInputStream(fileInputStream);
                    String rafPathMain = re.encode(getRafPathFolder(rs.getString("NAME"), rs.getString("FORMAT"), rs.getString("FOLDER"), rs.getString("PARENT_FOLDER")));
                    String rafPath = re.encode(getRafPath(rs.getString("NAME"), rs.getString("FORMAT"), rs.getString("FOLDER"), rs.getString("PARENT_FOLDER")));

                    if (checkRafPath(rafPath)) {
                        LOG.debug("{} Document is exists.", rs.getString("NAME"));
                    } else {
                        String folder = rafPath.substring(0, rafPath.lastIndexOf("/"));
                        if (!checkRafFolder(folder)) {
                            rafService.createFolder(folder);
                        }
                        RafDocument rafDocument = rafService.uploadDocument(rafPath, bis);
                        RafRecord record = new RafRecord();
                        record.setName(rafDocument.getName());
                        record.setTitle(rafDocument.getName());
                        record.setInfo("İçeri aktarılan dokuman.");
                        record.setPath(rafPathMain);
                        record.setMainDocument(rafDocument.getName());
                        record.setProcessIntanceId(0L);
                        record.setRecordType("externalDoc");
                        record.setDocumentType("externalDoc");
                        record.setElectronicDocument(true);
                        record.setCreateBy(rs.getString("REGISTER_USER"));
                        record.setCreateDate(rs.getDate("REGISTER_DATE"));
                        record = rafService.createRecord(record);

                        rafService.copyObject(rafDocument, record);

                        List<RafMetadata> metaDatas = new ArrayList();
                        RafMetadata m = new RafMetadata();
                        m.setType("externalDoc:metadata");
                        m.getAttributes().put("externalDoc:documentId", docId);
                        m.getAttributes().put("externalDoc:documentCreator", rs.getString("REGISTER_USER"));
                        m.getAttributes().put("externalDoc:documentCreateDate", rs.getDate("REGISTER_DATE"));
                        m.getAttributes().put("externalDoc:documentFolder", rs.getString("FOLDER"));
                        m.getAttributes().put("externalDoc:documentFormat", rs.getString("FORMAT"));
                        m.getAttributes().put("externalDoc:documentName", rs.getString("NAME"));
                        m.getAttributes().put("externalDoc:documentParentFolder", rs.getString("PARENT_FOLDER"));
                        m.getAttributes().put("externalDoc:documentType", rs.getString("DOCUMENT_TYPE"));
                        m.getAttributes().put("externalDoc:documentStatus", finishedWF ? "KAPALI" : "AÇIK");
                        metaDatas.add(m);

                        metaDatas.addAll(importDocumentAnnotations(con, docId, rafDocument.getPath(), rafDocument.getId(), record));
                        metaDatas.addAll(importDocumentMetaDatas(con, docId, rafDocument.getPath(), rafDocument.getId(), record));
                        metaDatas.addAll(importDocumentWF(con, docId, rafDocument.getPath(), rafDocument.getId(), finishedWF, record));

                        rafService.saveMetadatas(record.getId(), metaDatas);

                        bis.close();
                        fileInputStream.close();

                        importDocumentAttachements(con, docId,
                                rs.getString("FOLDER"),
                                rs.getString("PARENT_FOLDER"),
                                rafDocument.getPath(), rafDocument.getId(),
                                record);
                    }
                } catch (FileNotFoundException ex) {
                    LOG.error("FileNotFoundException", ex);
                } catch (RafException ex) {
                    LOG.error("RafException", ex);
                } catch (IOException ex) {
                    LOG.error("IOException", ex);
                }
            }
        } catch (SQLException ex) {
            LOG.error("SQLException", ex);
        }
    }

    private void importDocumentAttachements(Connection con, String parentDocId,
            String documentFolder, String documentParentFolder,
            String parentRafFilePath, String parentRafFileId, RafRecord record) {
        try {
            LOG.debug("{} Document attachements importing.", parentDocId);
            if (con != null) {
                Statement st;
                try {
                    st = con.createStatement();
                    ResultSet rs = st.executeQuery(String.format("select d.ID, d.NAME, d.FORMAT, '/attachment' VAULT_PATH from dm_attachment d \n"
                            + "where d.DOCUMENT is null and d.PARENT = %s", parentDocId
                    ));
                    while (rs.next()) {
                        LOG.debug("{} Document is importing.", rs.getString("NAME"));
                        String docId = rs.getString("ID");
                        String vaultPath = getVaultPath(rs.getString("VAULT_PATH"), "", docId, rs.getString("FORMAT")).replace("//", "/");
                        Path filePath = Paths.get(vaultPath);
                        if (Files.exists(filePath) && Files.isReadable(filePath)) {
                            try {
                                FileInputStream fileInputStream = new FileInputStream(filePath.toFile());
                                BufferedInputStream bis = new BufferedInputStream(fileInputStream);
                                String rafPath = re.encode(getRafPath(rs.getString("NAME"), rs.getString("FORMAT"), documentFolder, documentParentFolder));
                                String folder = rafPath.substring(0, rafPath.lastIndexOf("/"));
                                if (!checkRafFolder(folder)) {
                                    rafService.createFolder(folder);
                                }
                                RafDocument rafDocument = rafService.uploadDocument(rafPath, bis);
                                rafService.copyObject(rafDocument, record);
                                fileInputStream.close();
                                bis.close();
                            } catch (FileNotFoundException ex) {
                                LOG.error("FileNotFoundException", ex);
                            } catch (RafException ex) {
                                LOG.error("RafException", ex);
                            } catch (IOException ex) {
                                LOG.error("IOException", ex);
                            }
                        }
                    }
                } catch (SQLException ex) {
                    LOG.error("SQLException", ex);
                }
            }
        } catch (Exception ex) {
            LOG.error("SQLException", ex);
        }
    }

    private List<RafMetadata> importDocumentAnnotations(Connection con, String parentDocId, String parentRafFilePath, String parentRafFileId, RafRecord record) {
        List<RafMetadata> result = new ArrayList();
        try {
            LOG.debug("{} Document annotations importing.", parentDocId);
            if (con != null) {
                Statement st;
                try {
                    st = con.createStatement();
                    ResultSet rs = st.executeQuery(String.format("select a.ID, a.ORDER_NO, a.ANNOTATION_DATE, antusr.FULLNAME ANNOTATION_USER, a.ANNOTATION from dm_annotation a  \n"
                            + " inner join co_user antusr on antusr.ID = a.ANNOTATION_USER\n"
                            + " where a.DOCUMENT = %s \n"
                            + " order by a.ORDER_NO ", parentDocId
                    ));
                    String annotationDate = "";
                    String annotation = "";
                    String annotationUser = "";
                    while (rs.next()) {
                        LOG.debug("{} Document anotation is importing.", rs.getString("ID"));
                        annotationDate += sdf.format(rs.getDate("ANNOTATION_DATE")).concat(";");
                        annotation += rs.getString("ANNOTATION").replaceAll(";", "").concat(";");
                        annotationUser += rs.getString("ANNOTATION_USER").concat(";");
                    }

                    RafMetadata m = new RafMetadata();
                    m.setType("externalDocAnnotation:metadata");
                    m.getAttributes().put("externalDocAnnotation:annotationDateTime", annotationDate);
                    m.getAttributes().put("externalDocAnnotation:annotation", annotation);
                    m.getAttributes().put("externalDocAnnotation:annotationUser", annotationUser);
                    result.add(m);

                } catch (SQLException ex) {
                    LOG.error("SQLException", ex);
                }
            }
        } catch (Exception ex) {
            LOG.error("SQLException", ex);
        }

        return result;
    }

    private List<RafMetadata> importDocumentMetaDatas(Connection con, String parentDocId, String parentRafFilePath, String parentRafFileId, RafRecord record) {
        List<RafMetadata> result = new ArrayList();
        try {
            LOG.debug("{} Document metadatas importing.", parentDocId);
            if (con != null) {
                Statement st;
                try {
                    st = con.createStatement();
                    ResultSet rs = st.executeQuery(String.format("select tp.NAME TYP, catt.NAME ATT, val.VALUE_CHAR, val.VALUE_NUMERIC, val.VALUE_DATE, clistitm.VALUE LIST_VAL from dm_type tp\n"
                            + "inner join dm_type_attribute att on att.`TYPE` = tp.ID\n"
                            + "inner join co_attribute catt on catt.ID = att.`ATTRIBUTE`\n"
                            + "inner join dm_type_attribute_value val on val.TYPE_ATTRIBUTE = att.ID\n"
                            + "left join co_list clist on clist.ID = catt.ATTRIBUTE_LISTE\n"
                            + "left join co_list_item clistitm on clistitm.ID = val.VALUE_NUMERIC and clistitm.LISTE = clist.ID\n"
                            + "where val.DOCUMENT = %s ", parentDocId
                    ));
                    String attribute = "";
                    String value = "";
                    while (rs.next()) {
                        LOG.debug("{} Document metadatas is importing.", rs.getString("ATT"));
                        String strValue = "";
                        if (rs.getObject("LIST_VAL") != null) {
                            strValue = rs.getString("LIST_VAL");
                        } else if (rs.getObject("VALUE_DATE") != null) {
                            strValue = sdf.format(rs.getDate("VALUE_DATE"));
                        } else if (rs.getObject("VALUE_NUMERIC") != null) {
                            strValue = String.valueOf(rs.getInt("VALUE_NUMERIC"));
                        } else if (rs.getObject("VALUE_CHAR") != null) {
                            strValue = rs.getString("VALUE_CHAR").trim();
                        }
                        attribute += rs.getString("ATT").replaceAll(";", "").concat(";");
                        value += strValue.replaceAll(";", "").concat(";");
                    }
                    RafMetadata m = new RafMetadata();
                    m.setType("externalDocMetaTag:metadata");
                    m.getAttributes().put("externalDocMetaTag:externalDocTypeAttribute", attribute);
                    m.getAttributes().put("externalDocMetaTag:value", value);
                    result.add(m);
                } catch (SQLException ex) {
                    LOG.error("SQLException", ex);
                }
            }
        } catch (Exception ex) {
            LOG.error("SQLException", ex);
        }

        return result;
    }

    private List<RafMetadata> importDocumentWF(Connection con, String parentDocId, String parentRafFilePath, String parentRafFileId, boolean finishedWF, RafRecord record) {
        List<RafMetadata> result = new ArrayList();
        try {
            LOG.debug("{} Document workflow importing.", parentDocId);
            if (con != null) {
                Statement st;
                try {
                    st = con.createStatement();
                    String query = String.format("select distinctrow  workflow.ID, workflow.STARTED_DATE, usr.FULLNAME STARTER, \n"
                            + "workflow.INSTANCE_STATE, workflow.COMPLATE_DATE, endusr.FULLNAME END_USER, workflow.INST_UUID_  \n"
                            + "FROM arc_wf_workflow workflow\n"
                            + "inner join co_user usr on usr.ID = workflow.START_USER\n"
                            + "left join co_user endusr on endusr.ID = workflow.END_USER\n"
                            + "inner join arc_wf_workflow_document awfdoc on awfdoc.INST_UUID_ = workflow.INST_UUID_ \n"
                            + "inner join arc_wf_document document on document.DBID = awfdoc.DOCUMENT\n"
                            + "inner join dm_type dmtype on dmtype.ID = document.`TYPE`\n"
                            + "inner join dm_document dmdoc on dmdoc.ID = document.ID\n"
                            + "inner join dm_vault_folder vaultfolder on vaultfolder.ID = dmdoc.VAULT_FOLDER\n"
                            + "inner join dm_vault vault on vault.ID = vaultfolder.VAULT\n"
                            + "inner join dm_document_folder docfold on docfold.DOCUMENT = dmdoc.ID\n"
                            + "inner join dm_folder folder on folder.ID = docfold.FOLDER\n"
                            + "left join dm_folder parentfolder on parentfolder.Id = folder.PARENT_FOLDER\n"
                            + "where  dmdoc.ID = %s ", parentDocId
                    );
                    if (!finishedWF) {
                        query = query.replaceAll("arc_", "");
                    }
                    ResultSet rs = st.executeQuery(query);
                    while (rs.next()) {
                        LOG.debug("{} Document workflow is importing.", rs.getString("ID"));

                        RafMetadata m = new RafMetadata();
                        m.setType("externalDocWF:metadata");
                        m.getAttributes().put("externalDocWF:documentId", parentDocId);
                        m.getAttributes().put("externalDocWF:documentWFId", rs.getString("ID"));
                        m.getAttributes().put("externalDocWF:startedDate", rs.getDate("STARTED_DATE"));
                        m.getAttributes().put("externalDocWF:starter", rs.getString("STARTER"));
                        m.getAttributes().put("externalDocWF:state", rs.getString("INSTANCE_STATE"));

                        if (rs.getObject("COMPLATE_DATE") != null) {
                            m.getAttributes().put("externalDocWF:completeDate", rs.getDate("COMPLATE_DATE"));
                            m.getAttributes().put("externalDocWF:completer", rs.getString("END_USER"));
                        }

                        result.add(m);
                        result.addAll(importDocumentWFSteps(con, parentDocId, rs.getString("ID"), rs.getString("INST_UUID_"), parentRafFilePath, parentRafFileId, finishedWF, record));
                    }
                } catch (SQLException ex) {
                    LOG.error("SQLException", ex);
                }
            }
        } catch (Exception ex) {
            LOG.error("SQLException", ex);
        }

        return result;
    }

    private List<RafMetadata> importDocumentWFSteps(Connection con, String parentDocId, String parentDocWFId, String instanceUID, String parentRafFilePath, String parentRafFileId, boolean finishedWF, RafRecord record) {
        List<RafMetadata> result = new ArrayList();
        try {
            LOG.debug("{} Document workflow steps importing.", parentDocId);
            if (con != null) {
                Statement st;
                try {
                    st = con.createStatement();
                    String query = String.format("select body.POSTED_DATE, body.CREATED_DATE_, starter.FULLNAME STARTED_BY_, body.COMPLETED_DATE, body.COMPLETED_TIME, ender.FULLNAME ENDED_BY_, body.DETAIL_STATUS, body.DETAIL_COMMENT, acdef.NAME_ from arc_wf_detail_activity activity \n"
                            + "inner join arc_wf_detail_body body on body.DBID_ = activity.BODY_\n"
                            + "inner join wf_b_activity_def acdef on acdef.ACTIVITY_ID_ = activity.ACTIVITY_ID_ and acdef.PROCESS_UUID_ = activity.PROCESS_UUID_ \n"
                            + "inner join co_user starter on starter.ID = body.STARTED_BY_\n"
                            + "left join co_user ender on ender.ID = body.ENDED_BY_\n"
                            + "where activity.INST_UUID_ = '%s' order by body.CREATED_DATE_", instanceUID
                    );
                    if (!finishedWF) {
                        query = query.replaceAll("arc_", "");
                    }
                    ResultSet rs = st.executeQuery(query);
                    String startedDate = "";
                    String starter = "";
                    String state = "";
                    String completeDate = "";
                    String completer = "";
                    String stepName = "";
                    String comment = "";
                    while (rs.next()) {
                        LOG.debug("{} Document workflow step is importing.", rs.getString("NAME_"));
                        startedDate += sdf.format(rs.getDate("CREATED_DATE_")).concat(";");
                        starter += rs.getString("STARTED_BY_").concat(";");
                        state += rs.getString("DETAIL_STATUS").concat(";");
                        if (rs.getObject("COMPLETED_TIME") != null) {
                            completeDate += sdf.format(new Date(rs.getLong("COMPLETED_TIME"))).concat(";");
                            completer += rs.getString("ENDED_BY_").concat(";");
                        } else {
                            completeDate += "-;";
                            completer += "-;";
                        }
                        stepName += rs.getString("NAME_").concat(";");
                        comment += rs.getString("DETAIL_COMMENT") != null ? rs.getString("DETAIL_COMMENT").replaceAll(";", "").concat(";") : "-;";
                    }
                    RafMetadata m = new RafMetadata();
                    m.setType("externalDocWFStep:metadata");
                    m.getAttributes().put("externalDocWFStep:documentWFId", parentDocWFId);
                    m.getAttributes().put("externalDocWFStep:startedDateTime", startedDate);
                    m.getAttributes().put("externalDocWFStep:starter", starter);
                    m.getAttributes().put("externalDocWFStep:state", state);
                    m.getAttributes().put("externalDocWFStep:completeDateTime", completeDate);
                    m.getAttributes().put("externalDocWFStep:completer", completer);
                    m.getAttributes().put("externalDocWFStep:stepName", stepName);
                    m.getAttributes().put("externalDocWFStep:comment", comment);

                    result.add(m);
                } catch (SQLException ex) {
                    LOG.error("SQLException", ex);
                }
            }
        } catch (Exception ex) {
            LOG.error("SQLException", ex);
        }

        return result;
    }

    private String getFolderNamesForQuery() {
        String result = "";
        String[] splittedList = command.getDoxoftFolderNames().split(",");
        for (String folderName : splittedList) {
            result += String.format("'%s',", folderName.trim());
        }
        return result.substring(0, result.length() > 0 ? result.length() - 1 : 0);
    }

    private void importWFDocuments(boolean finishedWF) {
        LOG.debug("Workflow documents is importing.", finishedWF);
        Connection con = getMysqlConnection();
        if (con != null) {
            Statement st;
            try {
                st = con.createStatement();
                String query = String.format("SELECT distinct \n"
                        + "dmtype.NAME DOCUMENT_TYPE,\n"
                        + "dmdoc.ID,\n"
                        + "dmdoc.NAME,\n"
                        + "dmdoc.REGISTER_DATE,\n"
                        + "usr.FULLNAME REGISTER_USER,\n"
                        + "dmdoc.FORMAT,\n"
                        + "folder.NAME FOLDER,\n"
                        + "parentfolder.NAME PARENT_FOLDER,\n"
                        + "vault.`PATH` VAULT_PATH,\n"
                        + "vaultfolder.VAULT_LEVEL \n"
                        + "FROM arc_wf_workflow workflow\n"
                        + "inner join arc_wf_workflow_document awfdoc on awfdoc.INST_UUID_ = workflow.INST_UUID_ \n"
                        + "inner join arc_wf_document document on document.DBID = awfdoc.DOCUMENT\n"
                        + "inner join dm_type dmtype on dmtype.ID = document.`TYPE`\n"
                        + "inner join dm_document dmdoc on dmdoc.ID = document.ID\n"
                        + "inner join dm_vault_folder vaultfolder on vaultfolder.ID = dmdoc.VAULT_FOLDER\n"
                        + "inner join dm_vault vault on vault.ID = vaultfolder.VAULT\n"
                        + "inner join dm_document_folder docfold on docfold.DOCUMENT = dmdoc.ID\n"
                        + "inner join dm_folder folder on folder.ID = docfold.FOLDER\n"
                        + "left join dm_folder parentfolder on parentfolder.Id = folder.PARENT_FOLDER\n"
                        + "inner join co_user usr on usr.ID = dmdoc.REGISTER_USER\n"
                        + "where  parentfolder.NAME in ( %s ) ", getFolderNamesForQuery()
                );
                if (!finishedWF) {
                    query = query.replaceAll("arc_", "");
                }
                ResultSet rs = st.executeQuery(query);
                int i = 0;//test için 100 dokuman
                while (rs.next() && i < 100) {
                    importDocument(con, rs, finishedWF);
                    i++;
                }
                importDocumentRelatedDocuments(con, getFolderNamesForQuery());
                importDocumentAttachedDocuments(con, getFolderNamesForQuery());
                con.close();
                LOG.debug("Workflow documents import command is executed.", finishedWF);
            } catch (SQLException ex) {
                LOG.error("SQLException", ex);
            }
        }
    }

    private void importDocumentRelatedDocuments(Connection con, String parentFolders) {
        try {
            LOG.debug("{} Document related documents importing.");
            if (con != null) {
                Statement st;
                try {
                    st = con.createStatement();
                    ResultSet rs = st.executeQuery("select d.NAME, d.FORMAT, parentfolder.NAME PARENT_FOLDER, folder.NAME FOLDER, dr.NAME NAME_R, dr.FORMAT FORMAT_R, parentfolderr.NAME PARENT_FOLDER_R, folderr.NAME FOLDER_R from dm_related_document rd\n"
                            + "inner join dm_document d on d.ID = rd.DOCUMENT\n"
                            + "inner join dm_document_folder docfold on docfold.DOCUMENT = d.ID\n"
                            + "inner join dm_folder folder on folder.ID = docfold.FOLDER\n"
                            + "left join dm_folder parentfolder on parentfolder.Id = folder.PARENT_FOLDER\n"
                            + "inner join dm_document dr on dr.ID = rd.RELATED_DOCUMENT\n"
                            + "inner join dm_document_folder docfoldr on docfoldr.DOCUMENT = dr.ID\n"
                            + "inner join dm_folder folderr on folderr.ID = docfoldr.FOLDER\n"
                            + "left join dm_folder parentfolderr on parentfolderr.Id = folderr.PARENT_FOLDER"
                            + " where parentfolder.NAME in (" + parentFolders + ") ");
                    while (rs.next()) {
                        LOG.debug("{} Document is importing.", rs.getString("NAME"));
                        String parentRecordPath = re.encode(getRafRecordPath(rs.getString("NAME"), rs.getString("FORMAT"), rs.getString("FOLDER"), rs.getString("PARENT_FOLDER")));
                        String relatedDocPath = re.encode(getRafPath(rs.getString("NAME_R"), rs.getString("FORMAT_R"), rs.getString("FOLDER_R"), rs.getString("PARENT_FOLDER_R")));

                        if (checkRafPath(parentRecordPath) && checkRafPath(relatedDocPath)) {
                            RafRecord parentRecord = (RafRecord) rafService.getRafObjectByPath(parentRecordPath);
                            RafObject relatedDoc = rafService.getRafObjectByPath(relatedDocPath);

                            if (parentRecord != null && relatedDoc != null && !childIsExists(parentRecord, relatedDoc)) {
                                rafService.copyObject(relatedDoc, parentRecord);
                            }
                        }

                    }
                } catch (SQLException ex) {
                    LOG.error("SQLException", ex);
                }
            }
        } catch (Exception ex) {
            LOG.error("SQLException", ex);
        }
    }

    private boolean childIsExists(RafRecord record, RafObject rafObject) {
        boolean result = false;
        for (RafDocument document : record.getDocuments()) {
            result = document.getName().equals(rafObject.getName());
            if (result) {
                break;
            }
        }
        return result;
    }

    private void importDocumentAttachedDocuments(Connection con, String parentFolders) {
        try {
            //doxoft'ta bazı ek dosyalar aslında ilişkili dosya biçimindedir.
            LOG.debug("{} Document attached documents importing.");
            if (con != null) {
                Statement st;
                try {
                    st = con.createStatement();
                    ResultSet rs = st.executeQuery("select d.NAME, d.FORMAT, parentfolder.NAME PARENT_FOLDER, folder.NAME FOLDER, dr.NAME NAME_R, dr.FORMAT FORMAT_R, parentfolderr.NAME PARENT_FOLDER_R, folderr.NAME FOLDER_R \n"
                            + "from dm_attachment rd\n"
                            + "inner join dm_document d on d.ID = rd.PARENT\n"
                            + "inner join dm_document_folder docfold on docfold.DOCUMENT = d.ID\n"
                            + "inner join dm_folder folder on folder.ID = docfold.FOLDER\n"
                            + "left join dm_folder parentfolder on parentfolder.Id = folder.PARENT_FOLDER\n"
                            + "inner join dm_document dr on dr.ID = rd.DOCUMENT\n"
                            + "inner join dm_document_folder docfoldr on docfoldr.DOCUMENT = dr.ID\n"
                            + "inner join dm_folder folderr on folderr.ID = docfoldr.FOLDER\n"
                            + "left join dm_folder parentfolderr on parentfolderr.Id = folderr.PARENT_FOLDER"
                            + " where parentfolder.NAME in (" + parentFolders + ") ");
                    while (rs.next()) {
                        LOG.debug("{} Document is importing.", rs.getString("NAME"));
                        String parentRecordPath = re.encode(getRafRecordPath(rs.getString("NAME"), rs.getString("FORMAT"), rs.getString("FOLDER"), rs.getString("PARENT_FOLDER")));
                        String relatedDocPath = re.encode(getRafPath(rs.getString("NAME_R"), rs.getString("FORMAT_R"), rs.getString("FOLDER_R"), rs.getString("PARENT_FOLDER_R")));

                        if (checkRafPath(parentRecordPath) && checkRafPath(relatedDocPath)) {
                            RafRecord parentRecord = (RafRecord) rafService.getRafObjectByPath(parentRecordPath);
                            RafObject relatedDoc = rafService.getRafObjectByPath(relatedDocPath);

                            if (parentRecord != null && relatedDoc != null && !childIsExists(parentRecord, relatedDoc)) {
                                rafService.copyObject(relatedDoc, parentRecord);
                            }
                        }
                    }
                } catch (SQLException ex) {
                    LOG.error("SQLException", ex);
                }
            }
        } catch (Exception ex) {
            LOG.error("SQLException", ex);
        }
    }

}
