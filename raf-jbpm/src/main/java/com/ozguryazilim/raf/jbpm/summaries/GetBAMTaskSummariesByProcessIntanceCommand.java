/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ozguryazilim.raf.jbpm.summaries;

import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import org.jbpm.services.task.audit.impl.model.BAMTaskSummaryImpl;
import org.jbpm.services.task.commands.TaskCommand;
import org.jbpm.services.task.utils.ClassUtil;
import org.kie.api.runtime.Context;
import org.kie.internal.task.api.TaskContext;
import org.kie.internal.task.api.TaskPersistenceContext;

/**
 * ProcessIntanceId üzerinden BAMTaskSummary bilgisi döndürür.
 * 
 * @author Hakan Uygun
 */
@XmlRootElement(name = "get-bam-task-summaries-for-process-command")
@XmlAccessorType(XmlAccessType.NONE)
public class GetBAMTaskSummariesByProcessIntanceCommand extends TaskCommand<List<BAMTaskSummaryImpl>> {

    private Long processInstanceId;

    public GetBAMTaskSummariesByProcessIntanceCommand() {

    }

    public GetBAMTaskSummariesByProcessIntanceCommand(long processInstanceId) {
        this.processInstanceId = processInstanceId;
    }

    @Override
    public List<BAMTaskSummaryImpl> execute(Context context) {
        TaskPersistenceContext persistenceContext = ((TaskContext) context).getPersistenceContext();
        if (this.processInstanceId != null) {
            return persistenceContext.queryStringWithParametersInTransaction("SELECT t FROM BAMTaskSummaryImpl t where t.processInstanceId = :processInstanceId",
                    persistenceContext.addParametersToMap("processInstanceId", processInstanceId),
                    ClassUtil.<List<BAMTaskSummaryImpl>>castClass(List.class));
        } else {
            return persistenceContext.queryStringInTransaction("FROM BAMTaskSummaryImpl",
                    ClassUtil.<List<BAMTaskSummaryImpl>>castClass(List.class));
        }
    }
}
