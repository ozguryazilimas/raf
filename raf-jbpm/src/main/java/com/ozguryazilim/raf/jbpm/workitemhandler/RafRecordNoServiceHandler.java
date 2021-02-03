package com.ozguryazilim.raf.jbpm.workitemhandler;

import com.ozguryazilim.telve.sequence.SequenceManager;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import org.kie.api.runtime.process.WorkItem;
import org.kie.api.runtime.process.WorkItemHandler;
import org.kie.api.runtime.process.WorkItemManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author oyas
 */
@ApplicationScoped
public class RafRecordNoServiceHandler implements WorkItemHandler {

    private static final Logger LOG = LoggerFactory.getLogger(RafRecordNoServiceHandler.class);

    @Inject
    private SequenceManager sequenceManager;

    @Override
    public void executeWorkItem(WorkItem workItem, WorkItemManager manager) {
        LOG.info("Work Item Params : {}", workItem.getParameters());

        Map<String, Object> metadata = (Map<String, Object>) workItem.getParameter("metadata");

        //FIXME: SERİ başlangıç ve stratejilerini düzenlemek lazım. Ama bunun için ek bilgi lazım
//        String no = sequenceManager.getNewSerialNumber("ABC", 6);
        SimpleDateFormat sdfYear = new SimpleDateFormat("yyyy");
        String no = sequenceManager.getNewSerialNumber(sdfYear.format(new Date()), 1);
        metadata.put("raf:recordNo", no);

        Map<String, Object> result = new HashMap<>();
        result.put("metadata", metadata);

        if (workItem.getParameter("departman") != null) {
            result.put("departman", workItem.getParameter("departman"));
        }
        if (workItem.getParameter("uzman") != null) {
            result.put("uzman", workItem.getParameter("uzman"));
        }
        if (workItem.getParameter("departmanlar") != null) {
            result.put("departmanlar", workItem.getParameter("departmanlar"));
        }

        if (workItem.getParameter("uzmanlar") != null) {
            result.put("uzmanlar", workItem.getParameter("uzmanlar"));
        }

        if (workItem.getParameter("persons") != null) {
            result.put("persons", workItem.getParameter("persons"));
        }

        manager.completeWorkItem(workItem.getId(), result);
    }

    @Override
    public void abortWorkItem(WorkItem workItem, WorkItemManager manager) {
        //NŞA'da abort olmamalı!
        LOG.warn("Work Abort!");
    }
}
