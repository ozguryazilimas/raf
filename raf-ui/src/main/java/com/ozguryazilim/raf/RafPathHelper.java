package com.ozguryazilim.raf;

import com.ozguryazilim.raf.definition.RafDefinitionService;
import com.ozguryazilim.raf.entities.RafDefinition;
import com.ozguryazilim.raf.utils.RafPathUtils;
import com.ozguryazilim.telve.auth.Identity;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.inject.Named;
import java.io.Serializable;

@Named
@ApplicationScoped
public class RafPathHelper implements Serializable {

    @Inject
    private RafDefinitionService rafDefinitionService;

    @Inject
    private Identity identity;

    public String getRelativeRafPath(String fullPath) {
        return RafPathUtils.getRelativeRafObjectPath(fullPath);
    }

    public RafDefinition getRafDefinitionFromFullPath(String fullPath) throws RafException {
        if (RafPathUtils.isInPrivateRaf(fullPath)) {
            return rafDefinitionService.getPrivateRaf(identity.getLoginName());
        } else if (RafPathUtils.isInSharedRaf(fullPath)) {
            return rafDefinitionService.getSharedRaf();
        } else if (RafPathUtils.isInGeneralRaf(fullPath)) {
            return rafDefinitionService.getRafDefinitionByCode(fullPath.split("/")[2]);
        } else {
            return null;
        }
    }
    
}
