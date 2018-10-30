/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ozguryazilim.raf.webdav;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import org.modeshape.webdav.exceptions.WebdavException;

/**
 * Interface for a method of resolving a request into a repository name,
 * workspace name, and node path. Implementations can use additional information
 * in the request (such as the
 * {@link HttpServletRequest#getUserPrincipal() principal} to resolve the URI.
 * <p>
 * Implementations of this class must be thread-safe and must provide a public,
 * nilary (no-argument) constructor.
 * </p>
 *
 * @see SingleRepositoryRequestResolver
 * @see MultiRepositoryRequestResolver
 */
public interface RequestResolver {

    /**
     * Initialize the resolver based on the provided context
     *
     * @param context the servlet context for this servlet
     */
    void initialize(ServletContext context);

    /**
     * Resolve the given request to the repository, workspace, and path of the
     * node
     *
     * @param request the request to be resolved
     * @param path the requested relative path; never null or empty
     * @return the repository, workspace, and path to a node
     * @throws WebdavException if the URI cannot be resolved to a repository,
     * workspace, and path
     */
    ResolvedRequest resolve(HttpServletRequest request, String path) throws WebdavException;
}
