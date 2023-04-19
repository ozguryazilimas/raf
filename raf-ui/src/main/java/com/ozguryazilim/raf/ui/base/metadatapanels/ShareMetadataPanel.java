package com.ozguryazilim.raf.ui.base.metadatapanels;

import com.google.common.base.Strings;
import com.ozguryazilim.raf.RafException;
import com.ozguryazilim.raf.RafService;
import com.ozguryazilim.raf.config.MetadataPanelPages;
import com.ozguryazilim.raf.entities.RafShare;
import com.ozguryazilim.raf.models.RafObject;
import com.ozguryazilim.raf.share.RafShareService;
import com.ozguryazilim.raf.ui.base.AbstractMetadataPanel;
import com.ozguryazilim.raf.ui.base.MetadataPanel;
import com.ozguryazilim.raf.utils.UrlUtils;
import com.ozguryazilim.telve.auth.Identity;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@MetadataPanel(type = "nt:file", view = MetadataPanelPages.ShareMetadataPanel.class, order = 1)
public class ShareMetadataPanel extends AbstractMetadataPanel {

    private static final Logger LOG = LoggerFactory.getLogger(ShareMetadataPanel.class);

    @Inject
    private RafService rafService;

    @Inject
    private RafShareService shareService;

    @Inject
    private Identity identity;

    public boolean getHasShare(){
        return CollectionUtils.isNotEmpty(shareService.get(getObject().getId()));
    }

    private RafObject object;

    public RafObject getObject() {
        return object;
    }

    public void setObject(RafObject object) {
        this.object = object;
    }

    public List<RafShare> getShareObjects() {
        return shareService.get(getObject().getId())
                .stream()
                .sorted(Comparator.comparing(RafShare::getStartDate).reversed())
                .collect(Collectors.toList());
    }

    public List<RafObject> getShareGroup(RafShare rafShare) {
        if (!Strings.isNullOrEmpty(rafShare.getShareGroup())) {
            return shareService.getShareGroup(rafShare.getShareGroup())
                    .stream().sorted(Comparator.comparing(RafShare::getNodeId))
                    .map((RafShare rshare) -> {
                        try {
                            return rafService.getRafObject(rshare.getNodeId());
                        } catch (RafException e) {
                            LOG.error("Error while finding shared object with id {}", rshare.getId(), e);
                            return null;
                        }
                    })
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
        }
        return Collections.emptyList();
    }

    public boolean getHasRemoveSharing(RafShare rafShare) {
        if(rafShare != null){
            return rafShare.getSharedBy().equals(identity.getLoginName());
        }
        return false;
    }

    public void removeSharing(RafShare rafShare) {
        if(rafShare != null){
            shareService.clear(rafShare.getToken());
            shareService.rafShareEndAuditLog(rafShare);
        }
    }

    public void removeGroupSharing(List<RafShare> rafShares) {
        if(CollectionUtils.isNotEmpty(rafShares) && !Strings.isNullOrEmpty(rafShares.get(0).getShareGroup())){
            shareService.clearShareGroup(rafShares.get(0).getShareGroup());
        }
        rafShares.forEach(shareService::rafShareEndAuditLog);
    }

    public String getSharingUrl(RafShare rafShare) {
        if (rafShare != null) {
            return UrlUtils.getDocumentShareURL(rafShare.getToken());
        }
        return "";
    }
}
