/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ozguryazilim.raf.webdav;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

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
    }

    @Override
    public ResolvedRequest resolve( HttpServletRequest request,
                                    String relativePath ) {
        return new ResolvedRequest(request, repositoryName, workspaceName, relativePath);
    } 
}
