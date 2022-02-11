package com.ozguryazilim.raf.jbpm;

import com.ozguryazilim.raf.RafService;
import com.ozguryazilim.raf.auth.KJarResourceHandler;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Collection;
import java.util.Map;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import org.drools.compiler.kie.builder.impl.InternalKieModule;
import org.jbpm.kie.services.impl.DeployedUnitImpl;
import org.jbpm.kie.services.impl.bpmn2.ProcessDescriptor;
import org.jbpm.services.api.model.DeploymentUnit;
import org.jbpm.services.cdi.impl.DeploymentServiceCDIImpl;
import org.kie.api.builder.ReleaseId;
import org.kie.api.runtime.KieContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author oyas
 */
@ApplicationScoped
public class RModuleDeploymentService extends DeploymentServiceCDIImpl {

    private static final Logger LOG = LoggerFactory.getLogger(RModuleDeploymentService.class);

    @Inject
    private Instance<KJarResourceHandler> resourceHandlers;

    @Inject
    private RafService rafService;

    @Override
    protected void processResources(InternalKieModule module, Collection<String> files, KieContainer kieContainer, DeploymentUnit unit, DeployedUnitImpl deployedUnit, ReleaseId releaseId, Map<String, ProcessDescriptor> processes) {
        super.processResources(module, files, kieContainer, unit, deployedUnit, releaseId, processes);

        for (String fileName : files) {

            resourceHandlers.forEach(h -> {
                if (h.canHandle(fileName)) {
                    LOG.debug("File {} process by handler {}, {}", fileName, h, releaseId);
                    InputStream is = new ByteArrayInputStream(module.getBytes(fileName));
                    h.handle(releaseId.toString(), is);
                    rafService.setBpmnSystemEnabled(Boolean.TRUE);
                }
            });

            /*
            if (fileName.matches(".+frm.xml$")) {
                LOG.info("Form File {} found", fileName);
                InputStream is = new ByteArrayInputStream(module.getBytes(fileName));
                formManager.deployForms(is);
            } else if(fileName.matches(".+rts.xml$")){
                LOG.info("Record Type File {} found", fileName);
                InputStream is = new ByteArrayInputStream(module.getBytes(fileName));
                //FIXME: buraya injection ile kjar resource processor almak gerek.
                //formManager.deployForms(is);
            }
             */
        }

    }

}
