package com.ozguryazilim.raf.externalappimport;

import com.google.common.base.Strings;
import com.ozguryazilim.raf.entities.ExternalDoc;
import com.ozguryazilim.raf.entities.ExternalDoc_;
import com.ozguryazilim.telve.data.RepositoryBase;
import java.util.Date;
import java.util.List;
import java.util.Map;
import javax.enterprise.context.Dependent;
import javax.persistence.Query;
import org.apache.deltaspike.data.api.Repository;
import org.apache.deltaspike.data.api.criteria.Criteria;
import org.apache.deltaspike.data.api.criteria.CriteriaSupport;

/**
 *
 * @author oyas
 */
@Repository
@Dependent
public abstract class ExternalDocRepository extends RepositoryBase<ExternalDoc, ExternalDoc> implements CriteriaSupport<ExternalDoc> {

    public abstract List<ExternalDoc> findByDocumentId(String documentId);

    public abstract List<ExternalDoc> findByRafFilePath(String rafFilePath);

    public abstract List<ExternalDoc> findByRafFilePathLike(String rafFilePath);

    public abstract void removeByDocumentId(String documentId);

    public abstract void removeByRafFilePath(String rafFilePath);

    public abstract void removeByRafFilePathLike(String rafFilePath);

    public List<ExternalDoc> search(String documentName, Date registerDateFrom, Date registerDateTo, String externalDocType, List<String> documentTypes, List<String> attributeNames, List<String> attributeValues) {
        Criteria<ExternalDoc, ExternalDoc> crt = criteria()
                .select(ExternalDoc.class)
                .like(ExternalDoc_.documentName, "%".concat(documentName).concat("%"));

        if (registerDateFrom != null) {
            crt = crt.gtOrEq(ExternalDoc_.documentCreateDate, registerDateFrom);
        }

        if (registerDateTo != null) {
            crt = crt.ltOrEq(ExternalDoc_.documentCreateDate, registerDateTo);
        }

        if (externalDocType != null) {
            crt = crt.eq(ExternalDoc_.documentType, externalDocType);
        }

        return crt.getResultList();
    }

    public String prepareQuery(String documentStatus,
            String documentName, String documentType,
            Date registerDateFrom, Date registerDateTo,
            Map<String, Object> mapAttributeValue, boolean forCount) {
        String childQuery = "";
        for (Map.Entry<String, Object> entry : mapAttributeValue.entrySet()) {
            if (entry.getValue() != null && !Strings.isNullOrEmpty(entry.getValue().toString())) {
                childQuery += "(eda.attribute_name = '" + entry.getKey().split("_")[1] + "' and edv.value like '%" + entry.getValue() + "%') or ";
            }
        }
        childQuery = Strings.isNullOrEmpty(childQuery) ? "" : childQuery.substring(0, childQuery.length() - 3);
        String mainQuery = "select " + (forCount ? "count(distinct ed.*)" : "distinct ed.*") + " from external_doc ed\n"
                + "left join external_doc_type_attribute_value edv on edv.raf_file_id = ed.raf_file_id\n"
                + "left join external_doc_type_attribute eda on eda.id = edv.attribute_id\n"
                + "where ed.document_status like :documentStatus and ed.document_name like :documentName and ed.document_type like :documentType";

        if (!Strings.isNullOrEmpty(childQuery)) {
            mainQuery += " and ".concat(childQuery);
        }

        if (registerDateFrom != null) {
            mainQuery += " and ed.document_create_date >= :dateA ";
        }
        if (registerDateTo != null) {
            mainQuery += " and ed.document_create_date <= :dateB ";
        }

        return mainQuery;
    }

    public int countNative(String documentStatus, String documentName, String documentType, Date registerDateFrom, Date registerDateTo, Map<String, Object> mapAttributeValue) {
        String mainQuery = prepareQuery(documentStatus, documentName, documentType, registerDateFrom, registerDateTo, mapAttributeValue, true);

        if (documentStatus == null) {
            documentStatus = "";
        }
        if (documentName == null) {
            documentName = "";
        }
        if (documentType == null) {
            documentType = "";
        }

        Query q = entityManager().createNativeQuery(mainQuery)
                .setParameter("documentStatus", "%".concat(documentStatus).concat("%"))
                .setParameter("documentName", "%".concat(documentName).concat("%"))
                .setParameter("documentType", "%".concat(documentType).concat("%"));

        if (registerDateFrom != null) {
            q.setParameter("dateA", registerDateFrom);
        }
        if (registerDateTo != null) {
            q.setParameter("dateB", registerDateTo);
        }

        return ((Number) q.getSingleResult()).intValue();
    }

    public List<ExternalDoc> searchNative(String documentStatus, String documentName, String documentType, Date registerDateFrom, Date registerDateTo, Map<String, Object> mapAttributeValue, int page, int size) {
        String mainQuery = prepareQuery(documentStatus, documentName, documentType, registerDateFrom, registerDateTo, mapAttributeValue, false);

        if (documentStatus == null) {
            documentStatus = "";
        }
        if (documentName == null) {
            documentName = "";
        }
        if (documentType == null) {
            documentType = "";
        }
        Query q = entityManager().createNativeQuery(mainQuery.concat(String.format(" offset %d limit %d; ", page, size)), ExternalDoc.class)
                .setParameter("documentStatus", "%".concat(documentStatus).concat("%"))
                .setParameter("documentName", "%".concat(documentName).concat("%"))
                .setParameter("documentType", "%".concat(documentType).concat("%"));

        if (registerDateFrom != null) {
            q.setParameter("dateA", registerDateFrom);
        }
        if (registerDateTo != null) {
            q.setParameter("dateB", registerDateTo);
        }

        return q.getResultList();
    }

}
