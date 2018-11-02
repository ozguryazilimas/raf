/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ozguryazilim.raf.webdav.ui;

import com.ozguryazilim.raf.ui.base.AbstractAction;
import com.ozguryazilim.raf.ui.base.Action;
import com.ozguryazilim.raf.ui.base.ActionCapability;
import java.io.IOException;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author oyas
 */
@Action(icon = "fa-edit", 
        capabilities = {ActionCapability.DetailViews}, 
        excludeMimeType = "raf/folder")
public class OfficeEditAction extends AbstractAction{

    private static final Logger LOG = LoggerFactory.getLogger(OfficeEditAction.class);
    
    @Override
    protected boolean finalizeAction() {
    
        String projectPath = FacesContext.getCurrentInstance().getExternalContext().getRequestContextPath();

        HttpServletRequest request = (HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest();
        
        //FIXME: aslında burada protokol isimlerini configden alsak iyi olacak MSOffice için bu değerler farklı olabilir.
        String webDavProto = "vnd.sun.star.webdav://";
        if( "https".equals(request.getProtocol())){
            webDavProto = "vnd.sun.star.webdavs://";
        }
        
        webDavProto = webDavProto + request.getServerName() + ":" + request.getServerPort();
        String webDavRequestPath = buildWebDAvRequestPath(getContext().getSelectedObject().getPath());
        
        HttpServletResponse response = (HttpServletResponse) FacesContext.getCurrentInstance().getExternalContext().getResponse();
        try {
            String redirectTo = webDavProto + projectPath + "/webdav" + webDavRequestPath;
            LOG.debug("WebDAV redirection : {}", redirectTo);
            response.sendRedirect( redirectTo );
        } catch (IOException ex) {
            LOG.error("WebDAV IO Exption", ex);
        }
        
        return super.finalizeAction();
    }
    
    
    protected String buildWebDAvRequestPath( String path ){
        
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
