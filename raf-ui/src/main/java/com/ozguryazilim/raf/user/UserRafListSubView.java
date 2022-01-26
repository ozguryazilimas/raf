package com.ozguryazilim.raf.user;

import com.ozguryazilim.raf.RafException;
import com.ozguryazilim.raf.config.SettingsPages;
import com.ozguryazilim.raf.definition.RafDefinitionService;
import com.ozguryazilim.raf.entities.RafDefinition;
import com.ozguryazilim.raf.member.RafMemberService;
import com.ozguryazilim.telve.forms.SubView;
import com.ozguryazilim.telve.forms.SubViewPageBase;
import com.ozguryazilim.telve.idm.config.IdmPages;
import com.ozguryazilim.telve.idm.user.UserHome;
import java.util.List;
import javax.inject.Inject;
import org.slf4j.LoggerFactory;

/**
 *
 * @author evren.cengiz
 */
@SubView(containerPage = IdmPages.UserView.class, viewPage = SettingsPages.UserRafListSubView.class, permission = "userRole", order = 44)
public class UserRafListSubView extends SubViewPageBase {

    private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(UserRafListSubView.class);

    private List<RafDefinition> rafs;

    @Inject
    private UserHome userHome;

    @Inject
    private RafMemberService rafMemberService;

    @Inject
    private RafDefinitionService rafDefinitionService;

    @Override
    public void reload() {
        rafs = rafDefinitionService.getRafsForUser(userHome.getEntity().getLoginName());
    }

    public List<RafDefinition> getRafs() {
        return rafs;
    }

    public void setRafs(List<RafDefinition> rafs) {
        this.rafs = rafs;
    }

    public String getMemberRole(RafDefinition raf) {
        try {
            return rafMemberService.getMemberRole(userHome.getEntity().getLoginName(), raf);
        } catch (RafException ex) {
            LOG.debug("ERROR", ex);
        }
        return "";
    }

}
