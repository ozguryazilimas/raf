package com.ozguryazilim.raf.jcr;

import com.google.common.base.Strings;
import com.ozguryazilim.raf.RafException;
import com.ozguryazilim.raf.definition.RafDefinitionRepository;
import com.ozguryazilim.raf.entities.RafDefinition;
import com.ozguryazilim.raf.member.RafMemberService;
import com.ozguryazilim.raf.objet.member.RafPathMemberService;
import com.ozguryazilim.telve.auth.Identity;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import javax.inject.Inject;
import javax.jcr.Credentials;
import javax.servlet.http.HttpServletRequest;

import org.apache.deltaspike.core.api.provider.BeanProvider;
import org.apache.shiro.web.util.WebUtils;
import org.modeshape.jcr.ExecutionContext;
import org.modeshape.jcr.api.ServletCredentials;
import org.modeshape.jcr.security.AuthenticationProvider;
import org.modeshape.jcr.security.AuthorizationProvider;
import org.modeshape.jcr.security.SecurityContext;
import org.modeshape.jcr.value.Path;
import org.slf4j.LoggerFactory;

/**
 *
 * @author oyas
 */
public class RafSecurityProvider implements AuthenticationProvider, AuthorizationProvider, SecurityContext {

    private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(RafSecurityProvider.class);
    private String READ = "read";
    private String WRITE = "set_property";
    private String CREATE = "add_node";
    private String DELETE = "remove";
    private String DELETE_CHILD = "remove_child_nodes";

    @Inject
    private ServletCredentials servletCredentials;

    @Override
    public ExecutionContext authenticate(Credentials credentials, String repositoryName, String workspaceName, ExecutionContext repositoryContext, Map<String, Object> sessionAttributes) {
        return repositoryContext.with(this);
    }

    public boolean hasRafPermission(String docPath, List<String> actionList) {
        boolean rafPermission = true;
//        LOG.debug("Path : {}", docPath);
        String[] paths = docPath.split("/");
        if (paths != null && paths.length > 2) {
            String rafCode = paths[2];
//            LOG.debug("Raf Code : {}", rafCode);
            if (!Strings.isNullOrEmpty(rafCode)) {
                RafDefinition rafDef;
                try {
                    rafDef = getRafDefinitionRepository().findAnyByCode(rafCode);
                    if (rafDef != null) {
                        if (actionList.contains(READ)) {
                            rafPermission = getRafMemberService().hasReadRole(getIdentity().getLoginName(), rafDef);
                        } else if (actionList.contains(WRITE) || actionList.contains(CREATE)) {
                            rafPermission = getRafMemberService().hasWriteRole(getIdentity().getLoginName(), rafDef);
                        } else if (actionList.contains(DELETE) || actionList.contains(DELETE_CHILD)) {
                            rafPermission = getRafMemberService().hasDeleteRole(getIdentity().getLoginName(), rafDef);
                        }
                    }
                } catch (RafException ex) {
                    LOG.error("RafException", ex);
                }

            }
        }
//        LOG.debug("Raf Permission : {}", rafPermission);
        return rafPermission;
    }

    public boolean hasRafPathPermission(String docPath, List<String> actionList) {
        boolean permission = false;
//        LOG.debug("Path : {}", docPath);
        if (!Strings.isNullOrEmpty(docPath)) {
            try {
                if (actionList.contains(READ)) {
                    permission = getRafPathMemberService().hasReadRole(getIdentity().getLoginName(), docPath);
                } else if (actionList.contains(WRITE) || actionList.contains(CREATE)) {
                    permission = getRafPathMemberService().hasWriteRole(getIdentity().getLoginName(), docPath);
                } else if (actionList.contains(DELETE) || actionList.contains(DELETE_CHILD)) {
                    permission = getRafPathMemberService().hasDeleteRole(getIdentity().getLoginName(), docPath);
                }

            } catch (RafException ex) {
                LOG.error("RafException", ex);
            }
        }
//        LOG.debug("Raf Permission : {}", permission);
        return permission;
    }

    @Override
    public boolean hasPermission(ExecutionContext context, String repositoryName, String repositorySourceName, String workspaceName, Path absPath, String... actions) {
        // public document share
        if(getHttpServletRequest().getServletPath().startsWith("/public")){
            return true;
        }
        //FIXME: Bunun detaylarına bir bakmak lazım.
        boolean permission = false;
        if (absPath != null) {
            try {
                if (absPath.isAbsolute()) {
//                    LOG.debug("Actions : {}", actions);
                    List<String> actionList = Arrays.asList(actions);
                    String docPath = absPath.getString().replaceAll("\\{\\}", "").replaceAll("%", "_").replaceAll("\\+", "_");
                    if (getIdentity() != null) {
                        if ("SYSTEM".equals(getIdentity().getLoginName()) || "SUPERADMIN".equals(getIdentity().getUserInfo().getUserType())) {
                            //SYSTEM kullanıcısı zamanlanmış görevlerin çalıştırıldığı kullanıcıdır..
                            permission = true;
                        } else {
                            //path içinde herhangi bir üyelği varsa önce ona bak.
                            if (!Strings.isNullOrEmpty(getIdentity().getLoginName()) && !Strings.isNullOrEmpty(docPath) && getRafPathMemberService().hasMemberInPath(getIdentity().getLoginName(), docPath)) {
                                permission = hasRafPathPermission(docPath, actionList);
                            } else {
                                permission = hasRafPermission(docPath, actionList);
                            }
                        }
                    } else {
                        //identity bulunamadı attack olabilir. false döndürelim.
                        return false;
                    }
                } else {
                    return true;
                }
            } catch (Exception ex) {
                LOG.debug("Error in path : {}", absPath);
                LOG.error("Exception", ex);
            }
            return permission;
        }
        return true;
    }

    @Override
    public boolean isAnonymous() {
        return false;
    }

    @Override
    public String getUserName() {
        return getIdentity().getLoginName();
    }

    @Override
    public boolean hasRole(String roleName) {
        //FIXME: Buraya gelen rol isimleri ModeShape'in readwrite, admin felan şeklinde. Bunların telve rollerine çevrilmesi lazım.
        //Kullanıcının grupları içerisinde istenilen role/grup adı var mı diye bakılıyor.
        //return getIdentity().getGroups().contains(roleName);
        return true;
    }

    @Override
    public void logout() {
        //Logout sırasında yapacak bişeyimiz yok gibi görünüyor.
    }

    protected RafDefinitionRepository getRafDefinitionRepository() {
        return BeanProvider.getContextualReference(RafDefinitionRepository.class, true);
    }

    protected Identity getIdentity() {
        return BeanProvider.getContextualReference(Identity.class, true);
    }

    protected RafMemberService getRafMemberService() {
        return BeanProvider.getContextualReference(RafMemberService.class, true);
    }

    protected RafPathMemberService getRafPathMemberService() {
        return BeanProvider.getContextualReference(RafPathMemberService.class, true);
    }

    protected HttpServletRequest getHttpServletRequest(){
        return BeanProvider.getContextualReference(HttpServletRequest.class, true);
    }
}
