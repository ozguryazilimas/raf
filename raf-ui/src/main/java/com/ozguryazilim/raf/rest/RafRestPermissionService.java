package com.ozguryazilim.raf.rest;

import com.ozguryazilim.raf.RafUserRoleService;
import com.ozguryazilim.raf.utils.RafPathUtils;
import com.ozguryazilim.telve.auth.Identity;
import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import org.apache.deltaspike.core.api.config.ConfigResolver;

import java.util.HashSet;
import java.util.Set;

//TODO: Refactor. Bu servise ihtiyaç olmamalı
@ApplicationScoped
public class RafRestPermissionService {

    private static final String PERMISSION_HAS_WRITE = "hasWrite";

    /**
     * TODO: readRoles ve writeRoles setlerinin buralarda olmaları doğru değil
     *        Rol kontrolleri üzerindeki mevcut karmaşıklığın çözülmesi gerekli.
     *        RafMemberService ve RafPathMemberService incelenebilir
     */
    private static final Set<String> readRoles = new HashSet<>();
    private static final Set<String> writeRoles = new HashSet<>();

    @PostConstruct
    private void init() {
        readRoles.add("CONSUMER");
        readRoles.add("CONTRIBUTER");
        readRoles.add("EDITOR");
        readRoles.add("SUPPORTER");
        readRoles.add("MANAGER");

        writeRoles.add("CONTRIBUTER");
        writeRoles.add("EDITOR");
        writeRoles.add("SUPPORTER");
        writeRoles.add("MANAGER");
    }

    @Inject
    private Identity identity;

    @Inject
    private RafUserRoleService rafUserRoleService;

    //TODO: Yeri burası değil. Refactor
    public boolean hasCreatePermission(String path) {

        //TODO: Refactor
        if (!RafPathUtils.isInGeneralRaf(path)) {
            //If private raf
            if (RafPathUtils.isInPrivateRaf(path)) {
                if (!path.split("/")[2].equals(identity.getUserName())) {
                    return false;
                }
            } else if (RafPathUtils.isInSharedRaf(path)) {
                boolean sharedRafEnabled = ConfigResolver.resolve("raf.shared.enabled")
                        .as(Boolean.class)
                        .withDefault(Boolean.TRUE)
                        .getValue();
                boolean sharedRafActionPermissionsEnabled = ConfigResolver.resolve("raf.shared.enable.action.permission")
                        .as(Boolean.class)
                        .withDefault(Boolean.TRUE)
                        .getValue();
                String createFolderPermission = ConfigResolver.getPropertyValue("createFolder.permission", PERMISSION_HAS_WRITE);

                if (sharedRafActionPermissionsEnabled) {
                    boolean hasPermissionOnSharedRaf = identity.hasPermission("sharedRaf", "insert");
                    return PERMISSION_HAS_WRITE.equals(createFolderPermission) &&
                            sharedRafEnabled &&
                            writeRoles.contains(rafUserRoleService.getRoleInPath(identity.getLoginName(), path)) &&
                            hasPermissionOnSharedRaf;
                } else {
                    return PERMISSION_HAS_WRITE.equals(createFolderPermission) &&
                            sharedRafEnabled &&
                            writeRoles.contains(rafUserRoleService.getRoleInPath(identity.getLoginName(), path));
                }
            }

        } else {
            String role = rafUserRoleService.getRoleInPath(identity.getLoginName(), path);
            if (!writeRoles.contains(role)) {
                return false;
            }
        }

        return true;
    }

    //TODO: Yeri burası değil. Refactor
    public boolean hasReadPermission(String path) {
        //TODO: Refactor
        if (!RafPathUtils.isInGeneralRaf(path)) {
            //If private raf
            if (RafPathUtils.isInPrivateRaf(path)) {
                if (!path.split("/")[2].equals(identity.getLoginName())) {
                    return false;
                }
            } else if (RafPathUtils.isInSharedRaf(path)) {
                boolean sharedRafEnabled = ConfigResolver.resolve("raf.shared.enabled")
                        .as(Boolean.class)
                        .withDefault(Boolean.TRUE)
                        .getValue();

                boolean hasPermissionOnSharedRaf = identity.hasPermission("sharedRaf", "select");
                if (!sharedRafEnabled || !hasPermissionOnSharedRaf) {
                    return false;
                }
            }
        } else {
            String role = rafUserRoleService.getRoleInPath(identity.getLoginName(), path);
            if (!readRoles.contains(role)) {
                return false;
            }
        }
        return true;
    }
}
