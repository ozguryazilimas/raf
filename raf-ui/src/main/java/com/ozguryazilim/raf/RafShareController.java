package com.ozguryazilim.raf;

import com.google.common.base.Strings;
import com.ozguryazilim.raf.config.RafPages;
import com.ozguryazilim.raf.definition.RafDefinitionService;
import com.ozguryazilim.raf.entities.RafDefinition;
import com.ozguryazilim.raf.entities.RafShare;
import com.ozguryazilim.raf.member.RafMemberController;
import com.ozguryazilim.raf.member.RafMemberService;
import com.ozguryazilim.raf.models.RafObject;
import com.ozguryazilim.raf.share.RafShareService;
import com.ozguryazilim.raf.utils.UrlUtils;
import com.ozguryazilim.telve.auth.Identity;
import com.ozguryazilim.telve.view.Pages;
import org.apache.deltaspike.core.api.config.view.navigation.NavigationParameterContext;
import org.apache.deltaspike.core.api.config.view.navigation.ViewNavigationHandler;
import org.apache.deltaspike.core.api.scope.WindowScoped;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Named;
import java.io.Serializable;
import java.util.List;
import java.util.stream.Collectors;

@WindowScoped
@Named
public class RafShareController implements Serializable {

    private static final Logger LOG = LoggerFactory.getLogger(RafMemberController.class);

    @Inject
    private RafShareService rafShareService;

    @Inject
    private RafDefinitionService definitionService;

    @Inject
    private ViewNavigationHandler viewNavigationHandler;

    @Inject
    private NavigationParameterContext navigationParameterContext;

    @Inject
    private IconResolver iconResolver;

    @Inject
    private RafMemberService memberService;

    @Inject
    private RafService rafService;

    @Inject
    private Identity identity;

    private RafDefinition rafDefinition;
    private String rafCode;

    public void init() {
        if (Strings.isNullOrEmpty(rafCode)) {
            rafCode = "PRIVATE";
        }

        try {
            rafDefinition = definitionService.getRafDefinitionByCode(rafCode);
        } catch (RafException ex) {
            LOG.error("Raf Definition Not Found.", ex);
            viewNavigationHandler.navigateTo(Pages.Home.class);
        }

        try {
            //Uye değilse hemen HomePage'e geri gönderelim.
            if (!memberService.isMemberOf(identity.getLoginName(), rafDefinition)) {
                viewNavigationHandler.navigateTo(Pages.Home.class);
            }

        } catch (RafException ex) {
            LOG.error("Error", ex);
            //Gene de geldiği yere gönderelim.
            viewNavigationHandler.navigateTo(Pages.Home.class);
        }

    }

    public RafObject getSharedObject(RafShare rafShare) throws RafException {
        return rafService.getRafObject(rafShare.getNodeId());
    }

    public String getDocumentShareUrl(RafShare rafShare) throws RafException {
        return UrlUtils.getDocumentShareURL(rafShare.getToken());
    }

    public List<RafShare> getSharesByCurrentRaf() throws RafException {
        return rafShareService.getByRafCode(rafCode);
    }

    public List<RafShare> getSharesOfCurrentUserByCurrentRaf() throws RafException {
        return rafShareService.getByRafCode(rafCode).stream()
                .filter(rafShare -> rafShare.getSharedBy().equals(identity.getLoginName()))
                .collect(Collectors.toList());
    }

    public boolean isIdentityPermittedToGetAllShares() throws RafException {
        return memberService.getMemberRole(identity.getLoginName(), rafDefinition).equals("MANAGER");
    }

    public void removeShareGroupByShareAndShareList(RafShare rafShare, List<RafShare> shareList) {
        rafShareService.removeGroupSharingByShareAndShareList(rafShare, shareList);
    }

    public String getSharedFileMimeIcons(RafShare rafShare) throws RafException {
        return iconResolver.getIcon(rafService.getRafObject(rafShare.getNodeId()).getMimeType());
    }

    public String getSharedFileName(RafShare rafShare) throws RafException {
        return rafService.getRafObject(rafShare.getNodeId()).getName();
    }

    public String getRafCode() {
        return rafCode;
    }

    public void setRafCode(String rafCode) {
        this.rafCode = rafCode;
    }

    public RafDefinition getRafDefinition() {
        return rafDefinition;
    }

    public void setRafDefinition(RafDefinition rafDefinition) {
        this.rafDefinition = rafDefinition;
    }
}
