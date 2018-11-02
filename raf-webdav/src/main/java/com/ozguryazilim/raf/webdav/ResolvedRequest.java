/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ozguryazilim.raf.webdav;

import javax.servlet.http.HttpServletRequest;

/**
 * The resolved repository name, workspace name, and path of node for a given {@link HttpServletRequest request}.
 */
public final class ResolvedRequest {
   private final String repositoryName;
    private final String workspaceName;
    private final String path;
    private final HttpServletRequest request;

    public ResolvedRequest( HttpServletRequest request,
                            String repositoryName,
                            String workspaceName,
                            String path ) {
        super();
        this.request = request;
        assert this.request != null;
        this.repositoryName = repositoryName;
        this.workspaceName = workspaceName;
        this.path = path;
    }

    /**
     * Get the request.
     * 
     * @return request the request; never null
     */
    public HttpServletRequest getRequest() {
        return request;
    }

    /**
     * Get the name of the repository.
     * 
     * @return the repository name; may be null if the request did not resolve to a repository
     */
    public String getRepositoryName() {
        return repositoryName;
    }

    /**
     * Get the name of the workspace.
     * 
     * @return the workspace name; may be null if the request did not resolve to a node
     */
    public String getWorkspaceName() {
        return workspaceName;
    }

    /**
     * Get the path to the node.
     * 
     * @return the path of the node; may be null if the request did not resolve to a node
     */
    public String getPath() {
        return path;
    }

    /**
     * Create a new request that is similar to this request except with the supplied path. This can only be done if the repository
     * name and workspace name are non-null
     * 
     * @param path the new path
     * @return the new request; never null
     */
    public ResolvedRequest withPath( String path ) {
        assert repositoryName != null;
        assert workspaceName != null;
        return new ResolvedRequest(request, repositoryName, workspaceName, path);
    }

    public boolean isRoot(){
        return path == null || "/".equals(path);
    }
    
    /**
     * {@inheritDoc}
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "/" + repositoryName + "/" + workspaceName + path;
    } 
}
