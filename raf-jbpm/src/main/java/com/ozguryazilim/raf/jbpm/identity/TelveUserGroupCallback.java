/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ozguryazilim.raf.jbpm.identity;

import com.ozguryazilim.telve.auth.UserInfo;
import com.ozguryazilim.telve.auth.UserService;
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
    
    @Override
    public boolean existsUser(String userId) {
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
        return userService.getUserGroups(userId);
    }
    
}
