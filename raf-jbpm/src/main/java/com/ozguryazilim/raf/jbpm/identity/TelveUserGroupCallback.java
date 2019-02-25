package com.ozguryazilim.raf.jbpm.identity;

import com.ozguryazilim.raf.department.RafDepartmentService;
import com.ozguryazilim.raf.entities.RafDepartmentMember;
import com.ozguryazilim.telve.auth.UserInfo;
import com.ozguryazilim.telve.auth.UserService;
import java.util.ArrayList;
import java.util.List;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import org.kie.api.task.UserGroupCallback;

/**
 *
 * @author oyas
 */
@ApplicationScoped
public class TelveUserGroupCallback implements UserGroupCallback{

    @Inject
    private UserService userService;
    
    @Inject
    private RafDepartmentService departmentService;
    
    private List<String> adminstratorGroups;
    
    @Override
    public boolean existsUser(String userId) {
        //FIXME: İçeride kafadan administrator diye bir kullanıcı var! Buna bi rçözüm bulmalı. Task'lar üzerinde admin yetkisi kimde olacak belirlenmeli.
        if( "Administrator".equals(userId)){
            return true;
        }
        
        UserInfo ui = userService.getUserInfo(userId);
        return ui != null;
    }

    @Override
    public boolean existsGroup(String groupId) {
        //FIXME: burada mevcut gruplara erişmek lazım.
        return true;
    }

    @Override
    public List<String> getGroupsForUser(String userId) {
        if( "Administrator".equals(userId)){
            if( adminstratorGroups == null ){
                adminstratorGroups = new ArrayList<>();
                adminstratorGroups.add("Administrators");
            }
            return adminstratorGroups;
        }
        
        List<String> result = new ArrayList<>();

        //Deparman üyelikleri rol ile birlikte grup tanımı olarak ekleniyor
        List<RafDepartmentMember> ls = departmentService.getMemberships( userId );
        for( RafDepartmentMember dm : ls ){
            result.add( dm.getDepartment().getCode() + "-" + dm.getRole());
        }
        
        result.addAll(userService.getUserGroups(userId));
        
        return result;
    }
    
}
