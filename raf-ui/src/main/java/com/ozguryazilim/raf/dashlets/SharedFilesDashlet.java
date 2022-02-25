package com.ozguryazilim.raf.dashlets;

import com.ozguryazilim.raf.IconResolver;
import com.ozguryazilim.raf.RafException;
import com.ozguryazilim.raf.RafService;
import com.ozguryazilim.raf.entities.RafShare;
import com.ozguryazilim.raf.share.RafShareService;
import com.ozguryazilim.telve.auth.Identity;
import com.ozguryazilim.telve.dashboard.AbstractDashlet;
import com.ozguryazilim.telve.dashboard.Dashlet;
import com.ozguryazilim.telve.dashboard.DashletCapability;

import javax.inject.Inject;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Objects;

/**
 * Paylaşılan dosyalar ile ilgili bilgileri gösterir
 */
@Dashlet(capability = {DashletCapability.canHide, DashletCapability.canMinimize, DashletCapability.canRefresh}, permission = "public")
public class SharedFilesDashlet extends AbstractDashlet {

    @Inject
    private RafShareService rafShareService;

    @Inject
    private Identity identity;

    @Inject
    private RafService rafService;

    @Inject
    private IconResolver iconResolver;

    private List<RafShare> shareLogs;

    @Override
    public void load() {
        this.shareLogs = rafShareService.findByLoginName(identity.getLoginName());
    }

    public List<RafShare> getShareLogs() {
        return this.shareLogs;
    }

    @Override
    public void refresh() {
        load();
    }

    public String getSharedFileMimeIcons(RafShare rafShare) throws RafException {
        return iconResolver.getIcon(rafService.getRafObject(rafShare.getNodeId()).getMimeType());
    }

    public String getSharedFileName(RafShare rafShare) throws RafException {
        return rafService.getRafObject(rafShare.getNodeId()).getName();
    }

    public String getSharedFileStartEndDatesText(RafShare rafShare) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm");
        return sdf.format(rafShare.getStartDate()) + (Objects.nonNull(rafShare.getEndDate()) ? " - " + sdf.format(rafShare.getEndDate()) : "");
    }

    private String clearHtmlTags(String input) {
        return input.replaceAll("<", "&lt").replaceAll(">", "&gt");
    }

}
