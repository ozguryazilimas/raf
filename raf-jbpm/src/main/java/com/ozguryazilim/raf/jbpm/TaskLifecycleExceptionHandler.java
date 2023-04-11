package com.ozguryazilim.raf.jbpm;

import com.ozguryazilim.telve.messages.FacesMessages;
import org.apache.deltaspike.core.api.exception.control.BeforeHandles;
import org.apache.deltaspike.core.api.exception.control.ExceptionHandler;
import org.apache.deltaspike.core.api.exception.control.event.ExceptionEvent;
import org.jbpm.services.task.exception.PermissionDeniedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.RequestScoped;

@ExceptionHandler
@RequestScoped
public class TaskLifecycleExceptionHandler {
    private static final Logger LOG = LoggerFactory.getLogger(TaskLifecycleExceptionHandler.class);

    public static final String taskLifecycleMessageElementClientId = "tfrm:taskLifecycleMessage";

    public static final String messageKeyTaskPermissionDenied = "permission.task.denied";

    void permissionDeniedExceptionHandler(@BeforeHandles ExceptionEvent<PermissionDeniedException> evt) {
        LOG.debug(evt.getException().getMessage());
        FacesMessages.error(taskLifecycleMessageElementClientId, messageKeyTaskPermissionDenied, "");

        evt.handledAndContinue();
    }
}
