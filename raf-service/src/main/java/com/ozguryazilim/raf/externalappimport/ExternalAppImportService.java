package com.ozguryazilim.raf.externalappimport;

import com.ozguryazilim.raf.RafException;
import com.ozguryazilim.raf.RafService;
import com.ozguryazilim.raf.entities.ExternalDoc;
import com.ozguryazilim.raf.entities.ExternalDocAnnotation;
import com.ozguryazilim.raf.entities.ExternalDocAttachement;
import com.ozguryazilim.raf.entities.ExternalDocRelatedDoc;
import com.ozguryazilim.raf.entities.ExternalDocTypeAttributeValue;
import com.ozguryazilim.raf.entities.ExternalDocWF;
import com.ozguryazilim.raf.entities.ExternalDocWFStep;
import com.ozguryazilim.raf.models.RafObject;
import java.io.Serializable;
import java.util.List;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import org.apache.deltaspike.jpa.api.transaction.Transactional;
import org.slf4j.LoggerFactory;

/**
 *
 * @author oyas
 */
@ApplicationScoped
public class ExternalAppImportService implements Serializable {

    private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(ExternalAppImportService.class);

    @Inject
    ExternalDocRepository externalDocRepository;

    @Inject
    ExternalDocTypeAttributeValueRepository externalDocTypeAttributeValueRepository;

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

    @Inject
    RafService rafService;

    @Transactional
    public void deleteExternalDoc(String path) {
        externalDocRepository.removeByRafFilePathLike(path.concat("%"));
        externalDocTypeAttributeValueRepository.removeByRafFilePathLike(path.concat("%"));
        externalDocAttachementRepository.removeByRafFilePathLike(path.concat("%"));
        externalDocRelatedDocRepository.removeByRafFilePathLike(path.concat("%"));
        externalDocAttachementRepository.removeByParentRafFilePathLike(path.concat("%"));
        externalDocRelatedDocRepository.removeByParentRafFilePathLike(path.concat("%"));
        externalDocAnnotationRepository.removeByRafFilePathLike(path.concat("%"));
        externalDocWFRepository.removeByRafFilePathLike(path.concat("%"));
        externalDocWFStepRepository.removeByRafFilePathLike(path.concat("%"));
    }

    @Transactional
    public void copyExternalDoc(List<RafObject> sources, RafObject target) {
        sources.stream().forEach(sourceRafObject -> {
            externalDocRepository.findByRafFilePathLike(sourceRafObject.getPath().concat("%")).forEach(extDoc -> {
                try {
                    RafObject sourceParentFolder = rafService.getRafObject(sourceRafObject.getParentId());
                    String exFilePath = extDoc.getRafFilePath();
                    String filePath = extDoc.getRafFilePath().replace(sourceParentFolder.getPath(), target.getPath());
                    String fileId = rafService.getRafObjectByPath(filePath).getId();
                    ExternalDoc newDoc = (ExternalDoc) extDoc.clone();
                    newDoc.setId(null);
                    newDoc.setRafFilePath(filePath);
                    newDoc.setRafFileId(fileId);
                    externalDocRepository.saveAndFlush(newDoc);

                    externalDocTypeAttributeValueRepository.findByRafFilePathLike(exFilePath).forEach(extDocA -> {
                        try {
                            ExternalDocTypeAttributeValue newDocA = (ExternalDocTypeAttributeValue) extDocA.clone();
                            newDocA.setId(null);
                            newDocA.setRafFilePath(filePath);
                            newDocA.setRafFileId(fileId);
                            externalDocTypeAttributeValueRepository.saveAndFlush(newDocA);
                        } catch (CloneNotSupportedException ex) {
                            LOG.error("CloneNotSupportedException", ex);
                        }
                    });

                    externalDocAttachementRepository.findByRafFilePathLike(exFilePath).forEach(extDocA -> {
                        try {
                            ExternalDocAttachement newDocA = (ExternalDocAttachement) extDocA.clone();
                            newDocA.setId(null);
                            newDocA.setRafFilePath(filePath);
                            newDocA.setRafFileId(fileId);
                            externalDocAttachementRepository.saveAndFlush(newDocA);
                        } catch (CloneNotSupportedException ex) {
                            LOG.error("CloneNotSupportedException", ex);
                        }
                    });

                    externalDocRelatedDocRepository.findByRafFilePathLike(exFilePath).forEach(extDocA -> {
                        try {
                            ExternalDocRelatedDoc newDocA = (ExternalDocRelatedDoc) extDocA.clone();
                            newDocA.setId(null);
                            newDocA.setRafFilePath(filePath);
                            newDocA.setRafFileId(fileId);
                            externalDocRelatedDocRepository.saveAndFlush(newDocA);
                        } catch (CloneNotSupportedException ex) {
                            LOG.error("CloneNotSupportedException", ex);
                        }
                    });

                    externalDocAttachementRepository.findByParentRafFilePathLike(exFilePath).forEach(extDocA -> {
                        try {
                            ExternalDocAttachement newDocA = (ExternalDocAttachement) extDocA.clone();
                            newDocA.setId(null);
                            newDocA.setParentRafFilePath(filePath);
                            newDocA.setParentRafFileId(fileId);
                            externalDocAttachementRepository.saveAndFlush(newDocA);
                        } catch (CloneNotSupportedException ex) {
                            LOG.error("CloneNotSupportedException", ex);
                        }
                    });

                    externalDocRelatedDocRepository.findByParentRafFilePathLike(exFilePath).forEach(extDocA -> {
                        try {
                            ExternalDocRelatedDoc newDocA = (ExternalDocRelatedDoc) extDocA.clone();
                            newDocA.setId(null);
                            newDocA.setParentRafFilePath(filePath);
                            newDocA.setParentRafFileId(fileId);
                            externalDocRelatedDocRepository.saveAndFlush(newDocA);
                        } catch (CloneNotSupportedException ex) {
                            LOG.error("CloneNotSupportedException", ex);
                        }
                    });

                    externalDocAnnotationRepository.findByRafFilePathLike(exFilePath).forEach(extDocA -> {
                        try {
                            ExternalDocAnnotation newDocA = (ExternalDocAnnotation) extDocA.clone();
                            newDocA.setId(null);
                            newDocA.setRafFilePath(filePath);
                            newDocA.setRafFileId(fileId);
                            externalDocAnnotationRepository.saveAndFlush(newDocA);
                        } catch (CloneNotSupportedException ex) {
                            LOG.error("CloneNotSupportedException", ex);
                        }
                    });

                    externalDocWFRepository.findByRafFilePathLike(exFilePath).forEach(extDocA -> {
                        try {
                            ExternalDocWF newDocA = (ExternalDocWF) extDocA.clone();
                            newDocA.setId(null);
                            newDocA.setRafFilePath(filePath);
                            newDocA.setRafFileId(fileId);
                            externalDocWFRepository.saveAndFlush(newDocA);
                        } catch (CloneNotSupportedException ex) {
                            LOG.error("CloneNotSupportedException", ex);
                        }
                    });
                    externalDocWFStepRepository.findByRafFilePathLike(exFilePath).forEach(extDocA -> {
                        try {
                            ExternalDocWFStep newDocA = (ExternalDocWFStep) extDocA.clone();
                            newDocA.setId(null);
                            newDocA.setRafFilePath(filePath);
                            newDocA.setRafFileId(fileId);
                            externalDocWFStepRepository.saveAndFlush(newDocA);
                        } catch (CloneNotSupportedException ex) {
                            LOG.error("CloneNotSupportedException", ex);
                        }
                    });
                } catch (RafException ex) {
                    LOG.error("RafException", ex);
                } catch (CloneNotSupportedException ex) {
                    LOG.error("CloneNotSupportedException", ex);
                }
            });
        });
    }

    @Transactional
    public void moveExternalDoc(List<RafObject> sources, RafObject target) {
        sources.stream().forEach(sourceRafObject -> {
            externalDocRepository.findByRafFilePathLike(sourceRafObject.getPath().concat("%")).forEach(extDoc -> {
                try {
                    RafObject sourceParentFolder = rafService.getRafObject(sourceRafObject.getParentId());
                    String exFilePath = extDoc.getRafFilePath();
                    String filePath = extDoc.getRafFilePath().replace(sourceParentFolder.getPath(), target.getPath());
                    String fileId = rafService.getRafObjectByPath(filePath).getId();
                    extDoc.setRafFilePath(filePath);
                    extDoc.setRafFileId(fileId);
                    externalDocRepository.saveAndFlush(extDoc);

                    externalDocTypeAttributeValueRepository.findByRafFilePathLike(exFilePath).forEach(extDocA -> {
                        extDocA.setRafFilePath(filePath);
                        extDocA.setRafFileId(fileId);
                        externalDocTypeAttributeValueRepository.saveAndFlush(extDocA);
                    });

                    externalDocAttachementRepository.findByRafFilePathLike(exFilePath).forEach(extDocA -> {
                        extDocA.setRafFilePath(filePath);
                        extDocA.setRafFileId(fileId);
                        externalDocAttachementRepository.saveAndFlush(extDocA);
                    });

                    externalDocRelatedDocRepository.findByRafFilePathLike(exFilePath).forEach(extDocA -> {
                        extDocA.setRafFilePath(filePath);
                        extDocA.setRafFileId(fileId);
                        externalDocRelatedDocRepository.saveAndFlush(extDocA);
                    });

                    externalDocAttachementRepository.findByParentRafFilePathLike(exFilePath).forEach(extDocA -> {
                        extDocA.setParentRafFilePath(filePath);
                        extDocA.setParentRafFileId(fileId);
                        externalDocAttachementRepository.saveAndFlush(extDocA);
                    });

                    externalDocRelatedDocRepository.findByParentRafFilePathLike(exFilePath).forEach(extDocA -> {
                        extDocA.setParentRafFilePath(filePath);
                        extDocA.setParentRafFileId(fileId);
                        externalDocRelatedDocRepository.saveAndFlush(extDocA);
                    });

                    externalDocAnnotationRepository.findByRafFilePathLike(exFilePath).forEach(extDocA -> {
                        extDocA.setRafFilePath(filePath);
                        extDocA.setRafFileId(fileId);
                        externalDocAnnotationRepository.saveAndFlush(extDocA);
                    });

                    externalDocWFRepository.findByRafFilePathLike(exFilePath).forEach(extDocA -> {
                        extDocA.setRafFilePath(filePath);
                        extDocA.setRafFileId(fileId);
                        externalDocWFRepository.saveAndFlush(extDocA);
                    });
                    externalDocWFStepRepository.findByRafFilePathLike(exFilePath).forEach(extDocA -> {
                        extDocA.setRafFilePath(filePath);
                        extDocA.setRafFileId(fileId);
                        externalDocWFStepRepository.saveAndFlush(extDocA);
                    });
                } catch (RafException ex) {
                    LOG.error("RafException", ex);
                }
            });
        });
    }
}
