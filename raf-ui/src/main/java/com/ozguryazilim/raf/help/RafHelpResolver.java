package com.ozguryazilim.raf.help;

import com.ozguryazilim.telve.help.HelpResolver;
import java.util.Map;
import javax.enterprise.context.ApplicationScoped;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author oyas
 */
@ApplicationScoped
public class RafHelpResolver implements HelpResolver{

    private static final Logger LOG = LoggerFactory.getLogger(RafHelpResolver.class);
    
    @Inject
    private FacesContext facesContext;
    
    @Override
    public boolean canHandle() {
        String topic = facesContext.getViewRoot().getViewId();
        return topic.contains("raf.xhtml");
    }

    @Override
    public String getHelpPath() {
        Map<String, String> parameterMap =  facesContext.getExternalContext().getRequestParameterMap();
        LOG.debug("Parameter Map : {}", parameterMap);
        
        String rafId = parameterMap.get("id");
        
        switch ( rafId ){
            case "PRIVATE": return "/docs?topic=private_raf.html";
            case "SHARED": return "/docs?topic=shared_raf.html";
            default: return "/docs?topic=raf.html";
        }
        
    }

    @Override
    public Integer getOrder() {
        return 100;
    }
    
}
