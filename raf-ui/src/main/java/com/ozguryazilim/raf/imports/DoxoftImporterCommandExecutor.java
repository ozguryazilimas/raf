package com.ozguryazilim.raf.imports;

import com.google.common.base.Strings;
import com.ozguryazilim.raf.RafException;
import com.ozguryazilim.raf.RafService;
import com.ozguryazilim.raf.definition.RafDefinitionService;
import com.ozguryazilim.raf.encoder.RafEncoder;
import com.ozguryazilim.raf.encoder.RafEncoderFactory;
import com.ozguryazilim.raf.entities.ExternalDoc;
import com.ozguryazilim.raf.entities.ExternalDocAnnotation;
import com.ozguryazilim.raf.entities.ExternalDocAttachement;
import com.ozguryazilim.raf.entities.ExternalDocRelatedDoc;
import com.ozguryazilim.raf.entities.ExternalDocType;
import com.ozguryazilim.raf.entities.ExternalDocTypeAttribute;
import com.ozguryazilim.raf.entities.ExternalDocTypeAttributeValue;
import com.ozguryazilim.raf.entities.ExternalDocWF;
import com.ozguryazilim.raf.entities.ExternalDocWFStep;
import com.ozguryazilim.raf.externalappimport.ExternalDocAnnotationRepository;
import com.ozguryazilim.raf.externalappimport.ExternalDocAttachementRepository;
import com.ozguryazilim.raf.externalappimport.ExternalDocRelatedDocRepository;
import com.ozguryazilim.raf.externalappimport.ExternalDocRepository;
import com.ozguryazilim.raf.externalappimport.ExternalDocTypeAttributeRepository;
import com.ozguryazilim.raf.externalappimport.ExternalDocTypeAttributeValueRepository;
import com.ozguryazilim.raf.externalappimport.ExternalDocTypeRepository;
import com.ozguryazilim.raf.externalappimport.ExternalDocWFRepository;
import com.ozguryazilim.raf.externalappimport.ExternalDocWFStepRepository;
import com.ozguryazilim.raf.models.RafDocument;
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

    @Inject
    ExternalDocTypeRepository externalDocTypeRepository;

    @Inject
    ExternalDocTypeAttributeRepository externalDocTypeAttributeRepository;

    @Inject
    ExternalDocTypeAttributeValueRepository externalDocTypeAttributeValueRepository;

    @Inject
    ExternalDocRepository externalDocRepository;

    @Inject
    ExternalDocAttachementRepository externalDocAttachementRepository;

    @Inject
    ExternalDocRelatedDocRepository externalDocRelatedDocRepository;

    @Inject
    ExternalDocAnnotationRepository externalDocAnnotationRepository;

    @Inject
    ExternalDocWFRepository externalDocWFRepository;

    @Inject
    ExternalDocWFStepRepository externalDocWFStepRepository;

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
        importDocumentTypes();
        importAttributes();

        importFinishedWFDocuments();
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

    private void importDocumentTypes() {
        LOG.debug("External document types is importing.");
        Connection con = getMysqlConnection();
        if (con != null) {
            Statement st;
            try {
                st = con.createStatement();
                ResultSet rs = st.executeQuery("select distinctrow tp.NAME TYP   from dm_type tp\n"
                        + "inner join dm_type_attribute att on att.`TYPE` = tp.ID\n"
                        + "inner join co_attribute catt on catt.ID = att.`ATTRIBUTE`\n"
                        + "inner join dm_type_attribute_value val on val.TYPE_ATTRIBUTE = att.ID\n"
                        + "left join co_list clist on clist.ID = catt.ATTRIBUTE_LISTE\n"
                        + "order by tp.NAME asc");
                while (rs.next()) {
                    String documentType = rs.getString("TYP");
                    LOG.debug("{} document type is importing.", documentType);
                    if (externalDocTypeRepository.findByDocumentType(documentType).isEmpty()) {
                        ExternalDocType externalDocType = new ExternalDocType();
                        externalDocType.setDocumentType(documentType);
                        externalDocTypeRepository.saveAndFlush(externalDocType);
                    }
                }
                con.close();
            } catch (SQLException ex) {
                LOG.error("SQLException", ex);
            }
        }
    }

    private void importAttributes() {
        LOG.debug("External attributes is importing.");
        Connection con = getMysqlConnection();
        if (con != null) {
            Statement st;
            try {
                st = con.createStatement();
                ResultSet rs = st.executeQuery("select distinctrow tp.NAME TYP, catt.NAME ATT  from dm_type tp\n"
                        + "inner join dm_type_attribute att on att.`TYPE` = tp.ID\n"
                        + "inner join co_attribute catt on catt.ID = att.`ATTRIBUTE`\n"
                        + "inner join dm_type_attribute_value val on val.TYPE_ATTRIBUTE = att.ID\n"
                        + "left join co_list clist on clist.ID = catt.ATTRIBUTE_LISTE\n"
                        + "order by tp.NAME  asc , catt.NAME  asc");
                while (rs.next()) {
                    String documentType = rs.getString("TYP");
                    String attributeName = rs.getString("ATT");
                    LOG.debug("{} attribute is importing.", attributeName);
                    List<ExternalDocType> docTypes = externalDocTypeRepository.findByDocumentType(documentType);
                    ExternalDocType externalDocType = docTypes.isEmpty() ? null : docTypes.get(0);
                    if (externalDocType != null) {
                        List<ExternalDocTypeAttribute> existsAttributes = externalDocTypeAttributeRepository.findByDocumentTypeAndAttributeName(externalDocType, attributeName);
                        if (existsAttributes.isEmpty()) {
                            ExternalDocTypeAttribute externalDocTypeAttribute = new ExternalDocTypeAttribute();
                            externalDocTypeAttribute.setDocumentType(externalDocType);
                            externalDocTypeAttribute.setAttributeName(attributeName);
                            externalDocTypeAttributeRepository.saveAndFlush(externalDocTypeAttribute);
                        }
                    }
                }
                con.close();
            } catch (SQLException ex) {
                LOG.error("SQLException", ex);
            }
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

    private String getRafPath(String docName, String docFormat, String folder, String parentFolder) {
        String[] splittedDoxoftFolderNames = command.getDoxoftFolderNames().split(",");
        String[] splittedRafNames = command.getRafNames().split(",");
        int pathIndex = Arrays.asList(splittedDoxoftFolderNames).indexOf(parentFolder);
        if (pathIndex > -1 && pathIndex < splittedRafNames.length) {
            return "/RAF/".concat(splittedRafNames[pathIndex]).concat("/EVRAKLAR/").concat(folder).concat("/").concat(docName).concat(".").concat(docFormat);
        } else {
            LOG.debug("{} Parent folder not found.", parentFolder);
            return null;
        }
    }

    private boolean checkRafFolder(String folder) {
        try {
            return rafService.getFolder(folder) != null;
        } catch (RafException ex) {
            return false;
        }
    }

    private void importDocument(Connection con, ResultSet rs) {
        try {
            String docId = String.valueOf(rs.getLong("ID"));
            if (externalDocRepository.findByDocumentId(docId).isEmpty()) {
                LOG.debug("{} Document is importing.", rs.getString("NAME"));
                String vaultPath = getVaultPath(rs.getString("VAULT_PATH"), rs.getString("VAULT_LEVEL"), docId, rs.getString("FORMAT"));
                Path filePath = Paths.get(vaultPath);
                if (Files.exists(filePath) && Files.isReadable(filePath)) {
                    try {
                        FileInputStream fileInputStream = new FileInputStream(filePath.toFile());
                        BufferedInputStream bis = new BufferedInputStream(fileInputStream);
                        String rafPath = re.encode(getRafPath(rs.getString("NAME"), rs.getString("FORMAT"), rs.getString("FOLDER"), rs.getString("PARENT_FOLDER")));
                        String folder = rafPath.substring(0, rafPath.lastIndexOf("/"));
                        if (!checkRafFolder(folder)) {
                            rafService.createFolder(folder);
                        }
                        RafDocument rafDocument = rafService.uploadDocument(rafPath, bis);
                        ExternalDoc externalDoc = new ExternalDoc();
                        externalDoc.setDocumentId(docId);
                        externalDoc.setDocumentCreator(rs.getString("REGISTER_USER"));
                        externalDoc.setDocumentCreateDate(rs.getDate("REGISTER_DATE"));
                        externalDoc.setDocumentFolder(rs.getString("FOLDER"));
                        externalDoc.setDocumentFormat(rs.getString("FORMAT"));
                        externalDoc.setDocumentName(rs.getString("NAME"));
                        externalDoc.setDocumentParentFolder(rs.getString("PARENT_FOLDER"));
                        externalDoc.setDocumentType(rs.getString("DOCUMENT_TYPE"));
                        externalDoc.setRafFilePath(rafDocument.getPath());
                        externalDoc.setRafFileId(rafDocument.getId());
                        externalDocRepository.saveAndFlush(externalDoc);
                        bis.close();
                        fileInputStream.close();
                        importDocumentAttachements(con, docId, rs.getString("FOLDER"), rs.getString("PARENT_FOLDER"), rafDocument.getPath(), rafDocument.getId());
                        importDocumentAnnotations(con, docId, rafDocument.getPath(), rafDocument.getId());
                        importDocumentMetaDatas(con, docId, rafDocument.getPath(), rafDocument.getId());
                        importDocumentFinishedWF(con, docId, rafDocument.getPath(), rafDocument.getId());
                    } catch (FileNotFoundException ex) {
                        LOG.error("FileNotFoundException", ex);
                    } catch (RafException ex) {
                        LOG.error("RafException", ex);
                    } catch (IOException ex) {
                        LOG.error("IOException", ex);
                    }
                }
            } else {
                LOG.debug("{} Document is exists.", rs.getString("NAME"));
            }
        } catch (SQLException ex) {
            LOG.error("SQLException", ex);
        }
    }

    private void importDocumentAttachements(Connection con, String parentDocId, String documentFolder, String documentParentFolder, String parentRafFilePath, String parentRafFileId) {
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
                                ExternalDocAttachement externalDocAttachement = new ExternalDocAttachement();
                                externalDocAttachement.setParentRafFileId(parentRafFileId);
                                externalDocAttachement.setParentRafFilePath(parentRafFilePath);
                                externalDocAttachement.setRafFileId(rafDocument.getId());
                                externalDocAttachement.setRafFilePath(rafDocument.getPath());
                                try {
                                    externalDocAttachementRepository.saveAndFlush(externalDocAttachement);
                                } catch (Exception ex) {
                                }

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

    private void importDocumentAnnotations(Connection con, String parentDocId, String parentRafFilePath, String parentRafFileId) {
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
                    while (rs.next()) {
                        LOG.debug("{} Document anotation is importing.", rs.getString("ID"));
                        ExternalDocAnnotation externalDocAnnotation = new ExternalDocAnnotation();
                        externalDocAnnotation.setRafFilePath(parentRafFilePath);
                        externalDocAnnotation.setRafFileId(parentRafFileId);
                        externalDocAnnotation.setOrderNo(rs.getInt("ORDER_NO"));
                        externalDocAnnotation.setAnnotationDate(rs.getDate("ANNOTATION_DATE"));
                        externalDocAnnotation.setAnnotation(rs.getString("ANNOTATION"));
                        externalDocAnnotation.setAnnotationUser(rs.getString("ANNOTATION_USER"));
                        try {
                            externalDocAnnotationRepository.saveAndFlush(externalDocAnnotation);
                        } catch (Exception ex) {
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

    private void importDocumentMetaDatas(Connection con, String parentDocId, String parentRafFilePath, String parentRafFileId) {
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
                    while (rs.next()) {
                        LOG.debug("{} Document metadatas is importing.", rs.getString("ATT"));
                        List<ExternalDocType> externalDocType = externalDocTypeRepository.findByDocumentType(rs.getString("TYP"));
                        if (!externalDocType.isEmpty()) {
                            List<ExternalDocTypeAttribute> externalDocTypeAttribute = externalDocTypeAttributeRepository.findByDocumentTypeAndAttributeName(externalDocType.get(0), rs.getString("ATT"));
                            if (!externalDocTypeAttribute.isEmpty()) {
                                if (externalDocTypeAttributeValueRepository.findByDocumentTypeAndExternalDocTypeAttributeAndRafFilePath(externalDocType.get(0), externalDocTypeAttribute.get(0), parentRafFilePath).isEmpty()) {
                                    ExternalDocTypeAttributeValue externalDocTypeAttributeValue = new ExternalDocTypeAttributeValue();
                                    externalDocTypeAttributeValue.setDocumentType(externalDocType.get(0));
                                    externalDocTypeAttributeValue.setExternalDocTypeAttribute(externalDocTypeAttribute.get(0));
                                    externalDocTypeAttributeValue.setRafFileId(parentRafFileId);
                                    externalDocTypeAttributeValue.setRafFilePath(parentRafFilePath);
                                    if (rs.getObject("LIST_VAL") != null) {
                                        externalDocTypeAttributeValue.setValue(rs.getString("LIST_VAL"));
                                    } else if (rs.getObject("VALUE_DATE") != null) {
                                        externalDocTypeAttributeValue.setValue(sdf.format(rs.getDate("VALUE_DATE")));
                                    } else if (rs.getObject("VALUE_NUMERIC") != null) {
                                        externalDocTypeAttributeValue.setValue(String.valueOf(rs.getInt("VALUE_NUMERIC")));
                                    } else if (rs.getObject("VALUE_CHAR") != null) {
                                        externalDocTypeAttributeValue.setValue(rs.getString("VALUE_CHAR").trim());
                                    }
                                    if (externalDocTypeAttributeValue.getValue() != null) {
                                        try {
                                            externalDocTypeAttributeValueRepository.saveAndFlush(externalDocTypeAttributeValue);
                                        } catch (Exception ex) {
                                        }
                                    }
                                }
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

    private void importDocumentRelatedDocuments(Connection con) {
        try {
            LOG.debug("{} Document related documents importing.");
            if (con != null) {
                Statement st;
                try {
                    st = con.createStatement();
                    ResultSet rs = st.executeQuery("select rd.DOCUMENT, rd.RELATED_DOCUMENT from dm_related_document rd");
                    while (rs.next()) {
                        LOG.debug("{} Document is importing.", rs.getString("RELATED_DOCUMENT"));
                        String docId = rs.getString("DOCUMENT");
                        String relatedDocId = rs.getString("RELATED_DOCUMENT");
                        List<ExternalDoc> externalDoc = externalDocRepository.findByDocumentId(docId);
                        List<ExternalDoc> relatedExternalDoc = externalDocRepository.findByDocumentId(relatedDocId);
                        if (!externalDoc.isEmpty() && !relatedExternalDoc.isEmpty()) {
                            if (externalDocRelatedDocRepository.findByParentRafFilePathAndRafFilePath(externalDoc.get(0).getRafFilePath(), relatedExternalDoc.get(0).getRafFilePath()).isEmpty()) {
                                ExternalDocRelatedDoc externalDocRelatedDoc = new ExternalDocRelatedDoc();
                                externalDocRelatedDoc.setParentRafFileId(externalDoc.get(0).getRafFileId());
                                externalDocRelatedDoc.setParentRafFilePath(externalDoc.get(0).getRafFilePath());
                                externalDocRelatedDoc.setRafFileId(relatedExternalDoc.get(0).getRafFileId());
                                externalDocRelatedDoc.setRafFilePath(relatedExternalDoc.get(0).getRafFilePath());
                                try {
                                    externalDocRelatedDocRepository.saveAndFlush(externalDocRelatedDoc);
                                } catch (Exception ex) {
                                }
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

    private void importDocumentAttachedDocuments(Connection con) {
        try {
            //doxoft'ta bazı ek dosyalar aslında ilişkili dosya biçimindedir.
            LOG.debug("{} Document attached documents importing.");
            if (con != null) {
                Statement st;
                try {
                    st = con.createStatement();
                    ResultSet rs = st.executeQuery("select PARENT, DOCUMENT from dm_attachment where DOCUMENT is not null");
                    while (rs.next()) {
                        LOG.debug("{} Document is importing.", rs.getString("DOCUMENT"));
                        String docId = rs.getString("PARENT");
                        String attachedDocId = rs.getString("DOCUMENT");
                        List<ExternalDoc> externalDoc = externalDocRepository.findByDocumentId(docId);
                        List<ExternalDoc> attachedExternalDoc = externalDocRepository.findByDocumentId(attachedDocId);
                        if (!externalDoc.isEmpty() && !attachedExternalDoc.isEmpty()) {
                            if (externalDocRelatedDocRepository.findByParentRafFilePathAndRafFilePath(externalDoc.get(0).getRafFilePath(), attachedExternalDoc.get(0).getRafFilePath()).isEmpty()) {
                                ExternalDocRelatedDoc externalDocRelatedDoc = new ExternalDocRelatedDoc();
                                externalDocRelatedDoc.setParentRafFileId(externalDoc.get(0).getRafFileId());
                                externalDocRelatedDoc.setParentRafFilePath(externalDoc.get(0).getRafFilePath());
                                externalDocRelatedDoc.setRafFileId(attachedExternalDoc.get(0).getRafFileId());
                                externalDocRelatedDoc.setRafFilePath(attachedExternalDoc.get(0).getRafFilePath());
                                try {
                                    externalDocRelatedDocRepository.saveAndFlush(externalDocRelatedDoc);
                                } catch (Exception ex) {
                                }
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

    private void importDocumentFinishedWF(Connection con, String parentDocId, String parentRafFilePath, String parentRafFileId) {
        try {
            LOG.debug("{} Document workflow importing.", parentDocId);
            if (con != null) {
                Statement st;
                try {
                    st = con.createStatement();
                    ResultSet rs = st.executeQuery(String.format("select distinctrow  workflow.ID, workflow.STARTED_DATE, usr.FULLNAME STARTER, \n"
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
                    ));
                    while (rs.next()) {
                        LOG.debug("{} Document workflow is importing.", rs.getString("ID"));
                        if (externalDocWFRepository.findByDocumentWFId(rs.getString("ID")).isEmpty()) {
                            ExternalDocWF externalDocWF = new ExternalDocWF();
                            if (rs.getObject("COMPLATE_DATE") != null) {
                                externalDocWF.setCompleteDate(rs.getDate("COMPLATE_DATE"));
                                externalDocWF.setCompleter(rs.getString("END_USER"));
                            }
                            externalDocWF.setDocumentId(parentDocId);
                            externalDocWF.setDocumentWFId(rs.getString("ID"));
                            externalDocWF.setStartedDate(rs.getDate("STARTED_DATE"));
                            externalDocWF.setStarter(rs.getString("STARTER"));
                            externalDocWF.setState(rs.getString("INSTANCE_STATE"));
                            externalDocWF.setRafFileId(parentRafFileId);
                            externalDocWF.setRafFilePath(parentRafFilePath);
                            externalDocWFRepository.saveAndFlush(externalDocWF);
                            importDocumentFinishedWFSteps(con, parentDocId, rs.getString("ID"), rs.getString("INST_UUID_"), parentRafFilePath, parentRafFileId);
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

    private void importDocumentFinishedWFSteps(Connection con, String parentDocId, String parentDocWFId, String instanceUID, String parentRafFilePath, String parentRafFileId) {
        try {
            LOG.debug("{} Document workflow steps importing.", parentDocId);
            if (con != null) {
                Statement st;
                try {
                    st = con.createStatement();
                    ResultSet rs = st.executeQuery(String.format("select body.POSTED_DATE, body.CREATED_DATE_, starter.FULLNAME STARTED_BY_, body.COMPLETED_DATE, body.COMPLETED_TIME, ender.FULLNAME ENDED_BY_, body.DETAIL_STATUS, body.DETAIL_COMMENT, acdef.NAME_ from arc_wf_detail_activity activity \n"
                            + "inner join arc_wf_detail_body body on body.DBID_ = activity.BODY_\n"
                            + "inner join wf_b_activity_def acdef on acdef.ACTIVITY_ID_ = activity.ACTIVITY_ID_ and acdef.PROCESS_UUID_ = activity.PROCESS_UUID_ \n"
                            + "inner join co_user starter on starter.ID = body.STARTED_BY_\n"
                            + "left join co_user ender on ender.ID = body.ENDED_BY_\n"
                            + "where activity.INST_UUID_ = '%s' order by body.CREATED_DATE_", instanceUID
                    ));
                    while (rs.next()) {
                        LOG.debug("{} Document workflow step is importing.", rs.getString("NAME_"));
                        if (externalDocWFStepRepository.findByDocumentWFIdAndStepName(parentDocWFId, rs.getString("NAME_")).isEmpty()) {
                            ExternalDocWFStep externalDocWFStep = new ExternalDocWFStep();
                            externalDocWFStep.setComment(rs.getString("DETAIL_COMMENT"));
                            if (rs.getObject("COMPLETED_TIME") != null) {
                                externalDocWFStep.setCompletedDate(new Date(rs.getLong("COMPLETED_TIME")));
                                externalDocWFStep.setCompleter(rs.getString("ENDED_BY_"));
                            }
                            externalDocWFStep.setDocumentWFId(parentDocWFId);
                            externalDocWFStep.setRafFileId(parentRafFileId);
                            externalDocWFStep.setRafFilePath(parentRafFilePath);
                            externalDocWFStep.setStartedDate(rs.getDate("CREATED_DATE_"));
                            externalDocWFStep.setStarter(rs.getString("STARTED_BY_"));
                            externalDocWFStep.setState(rs.getString("DETAIL_STATUS"));
                            externalDocWFStep.setStepName(rs.getString("NAME_"));
                            externalDocWFStepRepository.saveAndFlush(externalDocWFStep);
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

    private String getFolderNamesForQuery() {
        String result = "";
        String[] splittedList = command.getDoxoftFolderNames().split(",");
        for (String folderName : splittedList) {
            result += String.format("'%s',", folderName.trim());
        }
        return result.substring(0, result.length() > 0 ? result.length() - 1 : 0);
    }

    private void importFinishedWFDocuments() {
        LOG.debug("Workflow documents is importing.");
        Connection con = getMysqlConnection();
        if (con != null) {
            Statement st;
            try {
                st = con.createStatement();
                ResultSet rs = st.executeQuery(String.format("SELECT distinct \n"
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
                        + "where  parentfolder.NAME in ( %s ) order by ID DESC", getFolderNamesForQuery()
                ));
                while (rs.next()) {
                    importDocument(con, rs);
                }
                importDocumentRelatedDocuments(con);
                importDocumentAttachedDocuments(con);
                con.close();
                LOG.debug("Workflow documents import command is executed.");
            } catch (SQLException ex) {
                LOG.error("SQLException", ex);
            }
        }
    }

}
