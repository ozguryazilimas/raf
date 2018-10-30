/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ozguryazilim.raf.webdav;

import java.io.File;
import java.io.IOException;
import javax.jcr.AccessDeniedException;
import javax.jcr.ItemExistsException;
import javax.jcr.ItemNotFoundException;
import javax.jcr.LoginException;
import javax.jcr.NoSuchWorkspaceException;
import javax.jcr.PathNotFoundException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebInitParam;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.modeshape.common.logging.Logger;
import org.modeshape.webdav.IWebdavStore;
import org.modeshape.webdav.WebdavServlet;
import org.modeshape.webdav.exceptions.ObjectAlreadyExistsException;
import org.modeshape.webdav.exceptions.ObjectNotFoundException;
import org.modeshape.webdav.exceptions.WebdavException;

/**
 * Buradaki kodlar büyük oranda modeshape-web-jcr-webdav projesinden kopyalanmıştır.
 * 
 * Doğrudan projenin kullanımı yerine kopyalanma nedeni Repository erişim yöntemlerinde değişiklik yapılmasıdır.
 * 
 * @author Hakan Uygun
 */
@WebServlet(urlPatterns = "/webdav/*", initParams = { @WebInitParam( name = "rootpath", value = ".")})
public class RafWebdavServlet extends WebdavServlet{
 
    private static final long serialVersionUID = 1L;

    public static final String INIT_CONTENT_MAPPER_CLASS_NAME = "org.modeshape.web.jcr.webdav.CONTENT_MAPPER_CLASS_NAME";
    public static final String INIT_REQUEST_RESOLVER_CLASS_NAME = "org.modeshape.web.jcr.webdav.REQUEST_RESOLVER_CLASS_NAME";

    private RequestResolver requestResolver;
    private ContentMapper contentMapper;

    @Override
    protected IWebdavStore constructStore( String clazzName,
                                           File root ) {
        return new RafWebdavStore(requestResolver, contentMapper);
    }

    protected String getParam( String name ) {
        return getServletContext().getInitParameter(name);
    }

    /**
     * Loads and initializes the {@link #requestResolver}
     */
    private void constructRequestResolver() {
        // Initialize the request resolver
        String requestResolverClassName = getParam(INIT_REQUEST_RESOLVER_CLASS_NAME);
        Logger.getLogger(getClass()).debug("WebDAV Servlet resolver class name = " + requestResolverClassName);
        if (requestResolverClassName == null) {
            this.requestResolver = new SingleRepositoryRequestResolver();
        } else {
            try {
                Class<? extends RequestResolver> clazz = Class.forName(requestResolverClassName).asSubclass(RequestResolver.class);
                this.requestResolver = clazz.newInstance();
            } catch (Exception ex) {
                throw new IllegalStateException(ex);
            }
        }
        Logger.getLogger(getClass()).debug("WebDAV Servlet using resolver class = " + requestResolver.getClass().getName());
        this.requestResolver.initialize(getServletContext());
    }

    /**
     * Loads and initializes the {@link #contentMapper}
     */
    private void constructContentMapper() {
        // Initialize the request resolver
        String contentMapperClassName = getParam(INIT_CONTENT_MAPPER_CLASS_NAME);
        Logger.getLogger(getClass()).debug("WebDAV Servlet content mapper class name = " + contentMapperClassName);
        if (contentMapperClassName == null) {
            this.contentMapper = new DefaultContentMapper();
        } else {
            try {
                Class<? extends ContentMapper> clazz = Class.forName(contentMapperClassName).asSubclass(ContentMapper.class);
                this.contentMapper = clazz.newInstance();
            } catch (Exception ex) {
                throw new IllegalStateException(ex);
            }
        }
        Logger.getLogger(getClass()).debug("WebDAV Servlet using content mapper class = " + contentMapper.getClass().getName());
        this.contentMapper.initialize(getServletContext());
    }

    @Override
    public void init() throws ServletException {
        constructRequestResolver();
        constructContentMapper();

        super.init();
    }

    /**
     * <p>
     * This method also sets and clears a thread-local reference to the incoming {@link HttpServletRequest request}.
     * </p>
     */
    @Override
    protected void service( HttpServletRequest req,
                            HttpServletResponse resp ) throws ServletException, IOException {
        RafWebdavStore.setRequest(req);
        try {
            super.service(req, resp);
        } finally {
            RafWebdavStore.setRequest(null);
        }
    }

    @Override
    protected Throwable translate( Throwable t ) {
        return translateError(t);
    }

    protected static WebdavException translateError( Throwable t ) {
        if (t instanceof AccessDeniedException) {
            return new org.modeshape.webdav.exceptions.AccessDeniedException(t.getMessage(), t);
        } else if (t instanceof LoginException) {
            return new org.modeshape.webdav.exceptions.AccessDeniedException(t.getMessage(), t);
        } else if (t instanceof ItemExistsException) {
            return new ObjectAlreadyExistsException(t.getMessage(), t);
        } else if (t instanceof PathNotFoundException) {
            return new ObjectNotFoundException(t.getMessage(), t);
        } else if (t instanceof ItemNotFoundException) {
            return new ObjectNotFoundException(t.getMessage(), t);
        } else if (t instanceof NoSuchWorkspaceException) {
            return new ObjectNotFoundException(t.getMessage(), t);
        } else {
            return new WebdavException(t.getMessage(), t);
        }
    }
}
