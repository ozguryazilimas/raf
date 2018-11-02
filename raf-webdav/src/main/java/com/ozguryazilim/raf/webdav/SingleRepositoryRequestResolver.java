/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ozguryazilim.raf.webdav;

import com.ozguryazilim.raf.encoder.RafEncoder;
import com.ozguryazilim.raf.encoder.RafEncoderFactory;
import com.ozguryazilim.telve.auth.TelveIdmPrinciple;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;

/**
 * Default {@link RequestResolver} that performs a direct mapping from all incoming URIs to the same path within a single
 * repository and workspace.
 * 
 * @see MultiRepositoryRequestResolver
 */
public class SingleRepositoryRequestResolver implements RequestResolver{
   public static final String INIT_REPOSITORY_NAME = "org.modeshape.web.jcr.webdav.SINGLE_REPOSITORY_RESOLVER_REPOSITORY_NAME";
    public static final String INIT_WORKSPACE_NAME = "org.modeshape.web.jcr.webdav.SINGLE_REPOSITORY_RESOLVER_WORKSPACE_NAME";

    private String repositoryName;
    private String workspaceName;

    private RafEncoder encoder;
    
    @Override
    public void initialize( ServletContext context ) {
        repositoryName = context.getInitParameter(INIT_REPOSITORY_NAME);
        if (repositoryName == null) {
            repositoryName = "raf";
            //I18n msg = WebdavI18n.requiredParameterMissing;
            //throw new IllegalStateException(msg.text(INIT_REPOSITORY_NAME));
        }

        workspaceName = context.getInitParameter(INIT_WORKSPACE_NAME);
        if (workspaceName == null) {
            workspaceName = "default";
            //I18n msg = WebdavI18n.requiredParameterMissing;
            //throw new IllegalStateException(msg.text(INIT_WORKSPACE_NAME));
        }
        
        encoder = RafEncoderFactory.getEncoder();
    }

    @Override
    public ResolvedRequest resolve( HttpServletRequest request,
                                    String relativePath ) {
        
        
        
        if( relativePath.contains("PRIVATE") ){
            relativePath = revolvePrivateRaf( relativePath );
        } else if( relativePath.contains("SHARED") ){
            //YApacak bişi yok
            //TODO: Aslında /RAF/SHARED şeklinde olması sağlanabilir mi acaba? Bu bişi fark ettirir mi?
        } else if( !"/".equals( relativePath ) && !"".equals(relativePath)){
            //Geri kalanlar RAF altında yer almalı.
            relativePath = "/RAF" + relativePath;
        } else {
            //FIXME: Buraya WARN seviyesinde logger ekle
            relativePath = "/";
        }
        
        //Bazen çift // geliyor teke indirelim.
        relativePath = relativePath.replace("//", "/");
        
        return new ResolvedRequest(request, repositoryName, workspaceName, encoder.encode(relativePath));
    } 
    
    
    
    protected String revolvePrivateRaf( String path ){
        Subject currentUser = SecurityUtils.getSubject();
        if( !currentUser.isAuthenticated() ){
            //FIXME: Doğru HTTP hata kodunu dönelim
            //FIXME: Ayrıca talep edilen dosyaya erişim yetkisi var mı o da kontrol edilmeli.
        }
        return path.replace("/PRIVATE", "/PRIVATE/"+ ((TelveIdmPrinciple)currentUser.getPrincipal()).getName());
    }
}
