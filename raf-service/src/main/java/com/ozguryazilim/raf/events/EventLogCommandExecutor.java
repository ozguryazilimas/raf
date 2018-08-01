/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ozguryazilim.raf.events;

import com.ozguryazilim.raf.entities.RafEventLog;
import com.ozguryazilim.telve.messagebus.command.AbstractCommandExecuter;
import com.ozguryazilim.telve.messagebus.command.CommandExecutor;
import java.util.Date;
import javax.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author oyas
 */
@CommandExecutor(command = EventLogCommand.class)
public class EventLogCommandExecutor extends AbstractCommandExecuter<EventLogCommand>{
    
    private static final Logger LOG = LoggerFactory.getLogger(EventLogCommandExecutor.class);

    @Inject
    private RafEventLogService eventLogService;
    
    @Override
    public void execute(EventLogCommand command) {
        
        RafEventLog event = new RafEventLog();
        
        event.setCode(command.getCode());
        event.setInfo(command.getInfo());
        event.setMessage(command.getMessage());
        event.setPath(command.getPath());
        event.setRefId(command.getRefId());
        event.setType(command.getType());
        event.setUsername(command.getUsername());
        event.setLogTime(new Date());
        
        LOG.debug("Event Logged : {}", command);
        
        eventLogService.addEventLog(event);
        
    }
    
    
}
