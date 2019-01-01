package com.ozguryazilim.raf.jbpm;

import com.ozguryazilim.raf.jbpm.workitemhandler.RafExportProcessDocumentsTaskHandler;
import com.ozguryazilim.raf.jbpm.workitemhandler.RafImportProcessDocumentsTaskHandler;
import com.ozguryazilim.raf.jbpm.workitemhandler.RafNotificationTaskHandler;
import com.ozguryazilim.raf.jbpm.workitemhandler.RafRecordExportDocumentsHandler;
import com.ozguryazilim.raf.jbpm.workitemhandler.RafRecordImportDocumentsHandler;
import com.ozguryazilim.raf.jbpm.workitemhandler.RafRecordNoServiceHandler;
import com.ozguryazilim.raf.jbpm.workitemhandler.RafServiceTaskHandler;
import java.util.HashMap;
import java.util.Map;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import org.kie.api.runtime.process.WorkItemHandler;
import org.kie.internal.runtime.manager.WorkItemHandlerProducer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Custom WorkItemHandler sınıflarının register edilmesini sağlar.
 * 
 * TODO: Aslında handlerlar için bir Qualifier yapıp burada onların instancelarını alıp geri döndürebiliriz sanırım.
 * Böylece onlarda CDI bean olarak yaşayabilirler.
 * 
 * @author Hakan Uygun
 */
@ApplicationScoped
public class RafServiceHandlersProducer implements WorkItemHandlerProducer{

    private static final Logger LOG = LoggerFactory.getLogger(RafServiceHandlersProducer.class);
    
    @Inject
    private RafServiceTaskHandler rafServiceTaskHandler;
    
    @Inject
    private RafNotificationTaskHandler rafNotificationTaskHandler;
    
    @Inject
    private RafImportProcessDocumentsTaskHandler importProcessDocumentsTaskHandler;
    
    @Inject
    private RafExportProcessDocumentsTaskHandler exportProcessDocumentsTaskHandler;
    
    @Inject
    private RafRecordImportDocumentsHandler rafRecordImportDocumentsHandler;
    
    @Inject 
    private RafRecordExportDocumentsHandler rafRecordExportDocumentsHandler;
    
    @Inject
    private RafRecordNoServiceHandler rafRecordNoServiceHandler;
    
    @Override
    public Map<String, WorkItemHandler> getWorkItemHandlers(String identifier, Map<String, Object> params) {
        
        LOG.debug("Work Item Registery {}, {}", identifier, params);
        
        Map<String, WorkItemHandler> result = new HashMap<>();
        
        result.put("Raf Service Task", rafServiceTaskHandler);
        result.put("Raf Notification Task", rafNotificationTaskHandler);
        result.put("Raf Import Process Documents", importProcessDocumentsTaskHandler);
        result.put("Raf Export Process Documents", exportProcessDocumentsTaskHandler);
        result.put("Raf Record Import Process Documents", rafRecordImportDocumentsHandler);
        result.put("Raf Record Export Process Documents", rafRecordExportDocumentsHandler);
        result.put("Raf Record No Service", rafRecordNoServiceHandler);
        
        return result;
        
    }
    
}
