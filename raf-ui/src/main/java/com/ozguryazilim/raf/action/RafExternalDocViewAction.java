package com.ozguryazilim.raf.action;

import com.ozguryazilim.raf.config.ActionPages;
import com.ozguryazilim.raf.entities.ExternalDoc;
import com.ozguryazilim.raf.entities.ExternalDocAnnotation;
import com.ozguryazilim.raf.entities.ExternalDocAttachement;
import com.ozguryazilim.raf.entities.ExternalDocRelatedDoc;
import com.ozguryazilim.raf.entities.ExternalDocType;
import com.ozguryazilim.raf.entities.ExternalDocTypeAttributeValue;
import com.ozguryazilim.raf.entities.ExternalDocWFStep;
import com.ozguryazilim.raf.externalappimport.ExternalDocAnnotationRepository;
import com.ozguryazilim.raf.externalappimport.ExternalDocAttachementRepository;
import com.ozguryazilim.raf.externalappimport.ExternalDocRelatedDocRepository;
import com.ozguryazilim.raf.externalappimport.ExternalDocRepository;
import com.ozguryazilim.raf.externalappimport.ExternalDocTypeAttributeValueRepository;
import com.ozguryazilim.raf.externalappimport.ExternalDocWFStepRepository;
import com.ozguryazilim.raf.ui.base.AbstractAction;
import com.ozguryazilim.raf.ui.base.Action;
import com.ozguryazilim.raf.ui.base.ActionCapability;
import com.ozguryazilim.telve.auth.Identity;
import java.util.List;
import javax.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author oyas
 */
@Action(icon = "fa-external-link",
        dialog = ActionPages.RafExternalDocView.class,
        capabilities = {ActionCapability.Ajax, ActionCapability.CollectionViews, ActionCapability.DetailViews},
        order = 1,
        excludeMimeType = "raf/rafNode,raf/folder"
)
public class RafExternalDocViewAction extends AbstractAction {

    private static final Logger LOG = LoggerFactory.getLogger(RafExternalDocViewAction.class);

    @Inject
    private Identity identity;

    @Inject
    private ExternalDocRepository externalDocRepository;

    @Inject
    private ExternalDocAnnotationRepository externalDocAnnotationRepository;

    @Inject
    private ExternalDocTypeAttributeValueRepository externalDocTypeAttributeValueRepository;

    @Inject
    private ExternalDocAttachementRepository externalDocAttachementRepository;

    @Inject
    private ExternalDocRelatedDocRepository externalDocRelatedDocRepository;

    @Inject
    private ExternalDocWFStepRepository externalDocWFStepRepository;

    private ExternalDoc externalDoc;

    private ExternalDocType externalDocType;

    private List<ExternalDocTypeAttributeValue> externalDocTypeAttributeValues;

    private List<ExternalDocAnnotation> externalDocAnnotations;

    private List<ExternalDocAttachement> externalDocAttachements;

    private List<ExternalDocRelatedDoc> externalDocRelatedDocs;

    private List<ExternalDocWFStep> externalDocWFSteps;

    @Override
    public boolean applicable(boolean forCollection) {
        return super.applicable(forCollection) && getContext() != null && getContext().getSelectedObject() != null && !externalDocRepository.findByRafFilePath(getContext().getSelectedObject().getPath()).isEmpty();
    }

    public ExternalDoc getExternalDoc() {
        return externalDoc;
    }

    public ExternalDocRepository getExternalDocRepository() {
        return externalDocRepository;
    }

    public ExternalDocType getExternalDocType() {
        return externalDocType;
    }

    public List<ExternalDocTypeAttributeValue> getExternalDocTypeAttributeValues() {
        return externalDocTypeAttributeValues;
    }

    public List<ExternalDocAnnotation> getExternalDocAnnotations() {
        return externalDocAnnotations;
    }

    public List<ExternalDocAttachement> getExternalDocAttachements() {
        return externalDocAttachements;
    }

    public List<ExternalDocRelatedDoc> getExternalDocRelatedDocs() {
        return externalDocRelatedDocs;
    }

    public List<ExternalDocWFStep> getExternalDocWFSteps() {
        return externalDocWFSteps;
    }

    @Override
    protected void initActionModel() {
        if (getContext() != null && getContext().getSelectedObject() != null) {
            String rafPath = getContext().getSelectedObject().getPath();
            externalDoc = externalDocRepository.findByRafFilePath(rafPath).isEmpty() ? null : externalDocRepository.findByRafFilePath(rafPath).get(0);
            externalDocAnnotations = externalDocAnnotationRepository.findByRafFilePath(rafPath);
            externalDocAttachements = externalDocAttachementRepository.findByParentRafFilePathLike(rafPath);
            externalDocRelatedDocs = externalDocRelatedDocRepository.findByParentRafFilePathLike(rafPath);
            externalDocTypeAttributeValues = externalDocTypeAttributeValueRepository.findByRafFilePathLike(rafPath);
            externalDocWFSteps = externalDocWFStepRepository.findByRafFilePathOrderByStartedDate(rafPath);
            if (!externalDocTypeAttributeValues.isEmpty()) {
                externalDocType = externalDocTypeAttributeValues.get(0).getDocumentType();
            }
        }

    }

    @Override
    protected boolean finalizeAction() {
        return super.finalizeAction();
    }

    public void setExternalDocRepository(ExternalDocRepository externalDocRepository) {
        this.externalDocRepository = externalDocRepository;
    }

    public void setExternalDocType(ExternalDocType externalDocType) {
        this.externalDocType = externalDocType;
    }

    public void setExternalDocTypeAttributeValues(List<ExternalDocTypeAttributeValue> externalDocTypeAttributeValues) {
        this.externalDocTypeAttributeValues = externalDocTypeAttributeValues;
    }

    public void setExternalDocAnnotations(List<ExternalDocAnnotation> externalDocAnnotations) {
        this.externalDocAnnotations = externalDocAnnotations;
    }

    public void setExternalDocAttachements(List<ExternalDocAttachement> externalDocAttachements) {
        this.externalDocAttachements = externalDocAttachements;
    }

    public void setExternalDocRelatedDocs(List<ExternalDocRelatedDoc> externalDocRelatedDocs) {
        this.externalDocRelatedDocs = externalDocRelatedDocs;
    }

    public void setExternalDocWFSteps(List<ExternalDocWFStep> externalDocWFSteps) {
        this.externalDocWFSteps = externalDocWFSteps;
    }

    public String getFileName(String rafFilePath) {
        return rafFilePath.contains("/") ? rafFilePath.substring(rafFilePath.lastIndexOf("/")) : rafFilePath;
    }

    public String getRafFromPath(String rafFilePath) {
        return rafFilePath.contains("/") ? rafFilePath.split("/")[2] : rafFilePath;
    }

    public String getFileLink(String rafFilePath, String rafFileId) {
        return String.format("/dolap/raf.jsf?id=%s&o=%s", getRafFromPath(rafFilePath), rafFileId);
    }

}
