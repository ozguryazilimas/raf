package com.ozguryazilim.raf.webdav;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.ws.rs.HttpMethod;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Objects;

@WebFilter(urlPatterns = "/webd/*", filterName = "RafWebdavWebFilter")
public class RafWevdavWebFilter implements Filter {

    private static final Logger LOG = LoggerFactory.getLogger(RafWevdavWebFilter.class);

    @Override
    public void init(FilterConfig filterConfig) throws ServletException { }

    @Override
    public void destroy() { }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        if (Objects.equals(((HttpServletRequest) request).getMethod(), HttpMethod.PUT)) {
            final HttpServletRequestWrapper wrappedWebdavServletRequest = new HttpServletRequestWrapper((HttpServletRequest) request) {
                @Override
                public String getPathInfo() {
                    final String originalPathInfo = ((HttpServletRequest) getRequest()).getPathInfo();
                    try {
                        return new URI(((HttpServletRequest) getRequest()).getRequestURL().toString()).getPath();
                    } catch (URISyntaxException e) {
                        LOG.error("Error while decoding RafWebdavServletRequest URL.", e);
                        return originalPathInfo;
                    }
                }
            };
            chain.doFilter(wrappedWebdavServletRequest, response);
        } else {
            chain.doFilter(request, response);
        }
    }

}
