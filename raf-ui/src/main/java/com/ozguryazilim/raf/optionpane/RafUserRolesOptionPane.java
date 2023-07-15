package com.ozguryazilim.raf.optionpane;

import com.ozguryazilim.raf.RafUserRoleService;
import com.ozguryazilim.raf.config.SettingsPages;
import com.ozguryazilim.raf.model.RafPathUserRoleDetails;
import com.ozguryazilim.raf.model.RafUserRoleDetails;
import com.ozguryazilim.telve.config.AbstractOptionPane;
import com.ozguryazilim.telve.config.OptionPane;

import javax.inject.Inject;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@OptionPane(permission = "PUBLIC", optionPage = SettingsPages.RafUserRolesOptionPane.class)
public class RafUserRolesOptionPane extends AbstractOptionPane {

    @Inject
    private RafUserRoleService rafUserRoleService;

    public List<RafUserRoleDetails> getRafUserRoles() {
        return rafUserRoleService.getRafRolesOfCurrentUser().entrySet().stream()
                .map(entry -> new RafUserRoleDetails(entry.getKey(), entry.getValue(), getRafPathUserRoleDetailsByRafName(entry.getKey())))
                .collect(Collectors.toList());
    }

    public List<RafPathUserRoleDetails> getRafPathUserRoleDetailsByRafName(String rafName) {
        return rafUserRoleService.getPathRolesOfCurrentUserByRaf(rafName).entrySet().stream()
                .map(entry -> new RafPathUserRoleDetails(entry.getKey(), entry.getValue()))
                .sorted(Comparator.comparing(RafPathUserRoleDetails::getRafPath))
                .collect(Collectors.toList());
    }

}
