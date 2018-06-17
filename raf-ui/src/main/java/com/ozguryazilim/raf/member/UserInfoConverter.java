/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ozguryazilim.raf.member;

import com.ozguryazilim.telve.auth.UserInfo;
import com.ozguryazilim.telve.auth.UserLookup;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;
import org.apache.deltaspike.core.api.provider.BeanProvider;

/**
 *
 * @author oyas
 */
@FacesConverter("userInfoConverter")
public class UserInfoConverter implements Converter{

    @Override
    public Object getAsObject(FacesContext fc, UIComponent uic, String string) {
        UserLookup userLookup = BeanProvider.getContextualReference(UserLookup.class, true);
        return userLookup.getUserInfo(string);
    }

    @Override
    public String getAsString(FacesContext fc, UIComponent uic, Object o) {
        return ((UserInfo)o).getLoginName();
    }
    
}
