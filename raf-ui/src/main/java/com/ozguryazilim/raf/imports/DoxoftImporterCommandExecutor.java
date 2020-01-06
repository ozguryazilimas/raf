package com.ozguryazilim.raf.imports;

import com.google.common.base.Strings;
import com.ozguryazilim.raf.RafService;
import com.ozguryazilim.raf.definition.RafDefinitionService;
import com.ozguryazilim.raf.entities.ExternalDocType;
import com.ozguryazilim.raf.entities.ExternalDocTypeAttribute;
import com.ozguryazilim.raf.imports.repository.ExternalDocTypeAttributeRepository;
import com.ozguryazilim.raf.imports.repository.ExternalDocTypeRepository;
import com.ozguryazilim.telve.messagebus.command.AbstractCommandExecuter;
import com.ozguryazilim.telve.messagebus.command.CommandExecutor;
import com.ozguryazilim.telve.messages.FacesMessages;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import javax.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@CommandExecutor(command = DoxoftImporterCommand.class)
public class DoxoftImporterCommandExecutor extends AbstractCommandExecuter<DoxoftImporterCommand> {

    private static final Logger LOG = LoggerFactory.getLogger(DoxoftImporterCommandExecutor.class);

    @Inject
    RafService rafService;

    @Inject
    RafDefinitionService rafDefinitionService;

    @Inject
    ExternalDocTypeRepository externalDocTypeRepository;

    @Inject
    ExternalDocTypeAttributeRepository externalDocTypeAttributeRepository;

    DoxoftImporterCommand command;

    @Override
    public void execute(DoxoftImporterCommand command) {
        this.command = command;
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

}
