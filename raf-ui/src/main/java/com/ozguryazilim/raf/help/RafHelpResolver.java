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
        
        if( rafId != null ){
            switch ( rafId ){
                case "PRIVATE": return "/help/private-raf.html";
                case "SHARED": return "/help/shared-raf.html";
                default: return "/help/raf.html";
            }
        } else {
            return "/help/raf.html";
        }
    }

    @Override
    public Integer getOrder() {
        return 100;
    }
    
}
