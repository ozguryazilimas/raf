package com.ozguryazilim.raf.events;

import com.ozguryazilim.raf.models.RafObject;

/**
 *
 * @author oyas
 */
public class EventLogCommandBuilder {

    private EventLogCommand command = new EventLogCommand();
    
    public static EventLogCommandBuilder forRaf( String rafCode ){
        EventLogCommandBuilder builder = new EventLogCommandBuilder();
        builder.command.setCode(rafCode);
        return builder;
    }
    
    public static EventLogCommandBuilder forProcess(){
        EventLogCommandBuilder builder = new EventLogCommandBuilder();
        builder.command.setCode("PROCESS");
        return builder;
    }
    
    public static EventLogCommandBuilder forTask(){
        EventLogCommandBuilder builder = new EventLogCommandBuilder();
        builder.command.setCode("TASK");
        return builder;
    }
    
    public EventLogCommandBuilder eventType( String type ){
        command.setType(type);
        return this;
    }
    
    public EventLogCommandBuilder user( String username ){
        command.setUsername(username);
        return this;
    }
    
    public EventLogCommandBuilder message( String message ){
        command.setMessage(message);
        return this;
    }
    
    public EventLogCommandBuilder info( String info ){
        command.setInfo(info);
        return this;
    }
    
    public EventLogCommandBuilder path( String path ){
        command.setPath(path);
        return this;
    }
    
    public EventLogCommandBuilder forRafObject( RafObject object ){
        command.setRefId(object.getId());
        command.setPath(object.getPath());
        return this;
    }
    
    public EventLogCommand build(){
        return command;
    }
            
    
}
