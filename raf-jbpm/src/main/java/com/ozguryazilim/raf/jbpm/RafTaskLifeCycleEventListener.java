package com.ozguryazilim.raf.jbpm;

import com.google.common.base.Joiner;
import com.ozguryazilim.telve.messagebus.command.CommandSender;
import com.ozguryazilim.telve.notification.NotificationCommand;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import org.jbpm.services.task.lifecycle.listeners.TaskLifeCycleEventListener;
import org.kie.api.task.TaskEvent;
import org.kie.api.task.model.Group;
import org.kie.api.task.model.PeopleAssignments;
import org.kie.api.task.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author oyas
 */
@ApplicationScoped
public class RafTaskLifeCycleEventListener implements TaskLifeCycleEventListener{

    private static final Logger LOG = LoggerFactory.getLogger(RafTaskLifeCycleEventListener.class);
    
    @Inject
    private CommandSender commandSender;
    
    @Override
    public void beforeTaskActivatedEvent(TaskEvent event) {
        LOG.debug("beforeTaskActivatedEvent : {}", event);
    }

    @Override
    public void beforeTaskClaimedEvent(TaskEvent event) {
        LOG.debug("beforeTaskClaimedEvent : {}", event);
    }

    @Override
    public void beforeTaskSkippedEvent(TaskEvent event) {
        LOG.debug("beforeTaskSkippedEvent : {}", event);
    }

    @Override
    public void beforeTaskStartedEvent(TaskEvent event) {
        LOG.debug("beforeTaskStartedEvent : {}", event);
    }

    @Override
    public void beforeTaskStoppedEvent(TaskEvent event) {
        LOG.debug("beforeTaskStoppedEvent : {}", event);
    }

    @Override
    public void beforeTaskCompletedEvent(TaskEvent event) {
        LOG.debug("beforeTaskCompletedEvent : {}", event);
    }

    @Override
    public void beforeTaskFailedEvent(TaskEvent event) {
        LOG.debug("beforeTaskFailedEvent : {}", event);
    }

    @Override
    public void beforeTaskAddedEvent(TaskEvent event) {
        LOG.debug("beforeTaskAddedEvent : {}", event);
    }

    @Override
    public void beforeTaskExitedEvent(TaskEvent event) {
        LOG.debug("beforeTaskExitedEvent : {}", event);
    }

    @Override
    public void beforeTaskReleasedEvent(TaskEvent event) {
        LOG.debug("beforeTaskReleasedEvent : {}", event);
    }

    @Override
    public void beforeTaskResumedEvent(TaskEvent event) {
        LOG.debug("beforeTaskResumedEvent : {}", event);
    }

    @Override
    public void beforeTaskSuspendedEvent(TaskEvent event) {
        LOG.debug("beforeTaskSuspendedEvent : {}", event);
    }

    @Override
    public void beforeTaskForwardedEvent(TaskEvent event) {
        LOG.debug("beforeTaskForwardedEvent : {}", event);
    }

    @Override
    public void beforeTaskDelegatedEvent(TaskEvent event) {
        LOG.debug("beforeTaskDelegatedEvent : {}", event);
    }

    @Override
    public void beforeTaskNominatedEvent(TaskEvent event) {
        LOG.debug("beforeTaskNominatedEvent : {}", event);
    }

    @Override
    public void afterTaskActivatedEvent(TaskEvent event) {
        LOG.debug("afterTaskActivatedEvent : {}", event);
    }

    @Override
    public void afterTaskClaimedEvent(TaskEvent event) {
        LOG.debug("afterTaskClaimedEvent : {}", event);
    }

    @Override
    public void afterTaskSkippedEvent(TaskEvent event) {
        LOG.debug("afterTaskSkippedEvent : {}", event);
    }

    @Override
    public void afterTaskStartedEvent(TaskEvent event) {
        LOG.debug("afterTaskStartedEvent : {}", event);
    }

    @Override
    public void afterTaskStoppedEvent(TaskEvent event) {
        LOG.debug("afterTaskStoppedEvent : {}", event);
    }

    @Override
    public void afterTaskCompletedEvent(TaskEvent event) {
        LOG.debug("afterTaskCompletedEvent : {}", event);
    }

    @Override
    public void afterTaskFailedEvent(TaskEvent event) {
        LOG.debug("afterTaskFailedEvent : {}", event);
    }

    @Override
    public void afterTaskAddedEvent(TaskEvent event) {
        LOG.debug("afterTaskAddedEvent : {}", event);
        PeopleAssignments pa = event.getTask().getPeopleAssignments();
        LOG.debug("Task Initiator : {}", pa.getTaskInitiator());
        LOG.debug("Task PotOwners : {}", pa.getPotentialOwners());
        LOG.debug("Task BAs : {}", pa.getBusinessAdministrators());
        LOG.debug("Task Owner : {}", event.getTask().getTaskData().getActualOwner());
    
        NotificationCommand ncm = new NotificationCommand();
        
        ncm.setNotificationClass("TaskAssignment");
        ncm.setSender("SYSTEM");
        
        //FIXME: Burada pots, bas ve stakeholder'lara da ayrı mesajlar iletmek lazım.
        ncm.setSubject( "Göreviniz var : " + event.getTask().getName());
        //Eğer gerçek bir kişiye atanmış ise ona bildirim göndereceğiz
        if( event.getTask().getTaskData().getActualOwner() != null ){
            ncm.setTarget("cs=user;id=" + event.getTask().getTaskData().getActualOwner().getId());
        } else {
            //Gerçek kişi yoksa grup olsa gerek
            PeopleAssignments pas = event.getTask().getPeopleAssignments();
            LOG.debug("Atanabilecek kullanıcılar : {}", pas.getPotentialOwners());
            LOG.debug("BAs kullanıcılar : {}", pas.getBusinessAdministrators());
            
            List<String> targets = new ArrayList<>();
            
            pas.getPotentialOwners().forEach((oe) -> {
                if( oe instanceof Group ){
                    targets.add("cs=group;id=" + oe.getId());
                } else if( oe instanceof User ){
                    targets.add("cs=user;id=" + oe.getId());
                }
            });
            
            ncm.setTarget(Joiner.on("||").join(targets));
        }
        
        Map<String, Object> params = new HashMap<>();
        
        params.put("TaskSubject", event.getTask().getSubject());
        params.put("TaskDescription", event.getTask().getDescription());
        params.put("TaskId", event.getTask().getId());
        params.put("Task Name", event.getTask().getName());
        params.put("ProcessId", event.getTask().getTaskData().getProcessId());
        params.put("ProcessInstanceId", event.getTask().getTaskData().getProcessInstanceId());
        params.put("DeploymentId", event.getTask().getTaskData().getDeploymentId());
        ncm.setParams(params);
        
        commandSender.sendCommand(ncm);
    }

    @Override
    public void afterTaskExitedEvent(TaskEvent event) {
        LOG.debug("afterTaskExitedEvent : {}", event);
    }

    @Override
    public void afterTaskReleasedEvent(TaskEvent event) {
        LOG.debug("afterTaskReleasedEvent : {}", event);
    }

    @Override
    public void afterTaskResumedEvent(TaskEvent event) {
        LOG.debug("afterTaskResumedEvent : {}", event);
    }

    @Override
    public void afterTaskSuspendedEvent(TaskEvent event) {
        LOG.debug("afterTaskSuspendedEvent : {}", event);
    }

    @Override
    public void afterTaskForwardedEvent(TaskEvent event) {
        LOG.debug("afterTaskForwardedEvent : {}", event);
    }

    @Override
    public void afterTaskDelegatedEvent(TaskEvent event) {
        LOG.debug("afterTaskDelegatedEvent : {}", event);
    }

    @Override
    public void afterTaskNominatedEvent(TaskEvent event) {
        LOG.debug("afterTaskNominatedEvent : {}", event);
    }
    
}
