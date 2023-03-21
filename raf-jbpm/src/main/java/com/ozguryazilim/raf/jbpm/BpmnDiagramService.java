package com.ozguryazilim.raf.jbpm;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import org.jbpm.kie.services.impl.model.ProcessAssetDesc;
import org.jbpm.services.api.DeploymentService;
import org.jbpm.services.api.model.DeployedAsset;
import org.jbpm.services.api.model.DeployedUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * BPMN Diagramı deployment içinden bulup döndürür.
 * 
 * @author Hakan Uygun
 */
@ApplicationScoped
public class BpmnDiagramService {
 
    private static final Logger LOG = LoggerFactory.getLogger(BpmnDiagramService.class);
    
    @Inject
    private DeploymentService deploymentService;
    
    /**
     * Verilen depoleyment için BPMN XML dosyasını döndürür.
     * 
     * FIXME: Burada bir cacheleme yapalım. Aslında genelde aynı XML dosyaları talep ediliyor olacak.
     * Cache için Guava Cache olabilir
     * 
     * @param deploymentId
     * @param processId
     * @return 
     */
    public String getBpmnDiagram( String deploymentId, String processId ) throws UnsupportedEncodingException {
        String decodedDeploymentId = URLDecoder.decode(deploymentId, StandardCharsets.UTF_8.displayName());
        DeployedUnit du = deploymentService.getDeployedUnit(decodedDeploymentId);
        LOG.debug("Deployed Assests : {}", du.getDeployedAssets()); 
        for( DeployedAsset da : du.getDeployedAssets() ){
            if( da.getId().equals(processId)){
                LOG.debug("Deployed Assest Source : {}",((ProcessAssetDesc)da).getEncodedProcessSource());
                return new String(Base64.getDecoder().decode(((ProcessAssetDesc)da).getEncodedProcessSource()));
            }
        }
        
        return null;
    }
    
}
