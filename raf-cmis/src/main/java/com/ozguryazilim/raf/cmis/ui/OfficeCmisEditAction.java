/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ozguryazilim.raf.cmis.ui;

import com.ozguryazilim.raf.ui.base.AbstractAction;
import com.ozguryazilim.raf.ui.base.Action;
import com.ozguryazilim.raf.ui.base.ActionCapability;
import com.ozguryazilim.telve.auth.Identity;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Şu format olması lazım imiş libre office için : 
 * 
 * vnd.libreoffice.cmis://encodedbinding + repo_id/object_id
 * 
 *  vnd.libreoffice.cmis://telve@http:%2F%2Flocalhost:8080%2Fdolap%2Fcmis%2F1.1%2Fatom%23raf/SHARED/hakancv.odt
 *  vnd.libreoffice.cmis://telve@http:%2F%2Flocalhost:8080%2Fdolap%2Fcmis%2F1.1%2Fatom%2Fraf/SHARED/hakancv.odt
 * 
 * @author oyas
 */
@Action(icon = "fa-edit", 
        capabilities = {ActionCapability.DetailViews}, 
        excludeMimeType = "raf/folder")
public class OfficeCmisEditAction extends AbstractAction{

    private static final Logger LOG = LoggerFactory.getLogger(OfficeCmisEditAction.class);
    
    @Inject
    private Identity identity;
    
    @Override
    protected boolean finalizeAction() {
    
        String projectPath = FacesContext.getCurrentInstance().getExternalContext().getRequestContextPath();

        HttpServletRequest request = (HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest();
        
        //FIXME: aslında burada protokol isimlerini configden alsak iyi olacak MSOffice için bu değerler farklı olabilir.
        String cmisProto = "vnd.libreoffice.cmis://" + identity.getLoginName() + "@";
        String cmisRepo = "";
        if( "https".equals(request.getProtocol())){
            cmisRepo = cmisRepo + "https://" + request.getServerName() + ":" + request.getServerPort() + projectPath + "/cmis/1.1/atom#raf";
        } else {
            cmisRepo = cmisRepo + "http://" + request.getServerName() + ":" + request.getServerPort() + projectPath + "/cmis/1.1/atom#raf";;
        }
        try {
            //Repo yolunun URLEncoded olması gerek
            cmisRepo = URLEncoder.encode(cmisRepo, "UTF-8");
        } catch (UnsupportedEncodingException ex) {
            LOG.error("URL Encoded Error", ex);
            return false;
        }
        
        //cmisProto = cmisProto + request.getServerName() + ":" + request.getServerPort() + "%2F" + projectPath + "%2Fcmis%2F1.1%2Fatom%23raf";
        String cmisRequestPath = buildCmisRequestPath(getContext().getSelectedObject().getPath());
        
        HttpServletResponse response = (HttpServletResponse) FacesContext.getCurrentInstance().getExternalContext().getResponse();
        try {
            String redirectTo = cmisProto + cmisRepo + cmisRequestPath;
            LOG.debug("CMIS redirection : {}", redirectTo);
            response.sendRedirect( redirectTo );
        } catch (IOException ex) {
            LOG.error("CMIS IO Exption", ex);
        }
        
        return super.finalizeAction();
    }
    
    
    protected String buildCmisRequestPath( String path ){
        
        //Eğer private ile başlıyorsa bir sonraki kullanıcı adı. Silelim.
        if( path.startsWith("/PRIVATE")){
            //Kullanıcı adının başı
            int i = path.indexOf("/", 2);
            int j = path.indexOf("/", i+1);
            return "/PRIVATE" +  path.substring(j);
        } else if( path.startsWith("/RAF") ){
            //Raf ile başlıyor ise RAF kısmını silsek yeterli
            return path.substring(4);
        } 
        
        //Geri kalan durumlar için eldeki path'i dönelim
        return path;
        
        
    }
    
}
