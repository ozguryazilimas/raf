package com.ozguryazilim.raf.jbpm.ui;

import java.util.HashMap;
import java.util.Map;

/**
 * Tanımlı TaskAction'ların listesini tutar.
 * @author oyas
 */
public class TaskActionRegistery {
    
    
    private static final Map<String, TaskAction> actionMap = new HashMap<>();
    
    public static void registerTaskAction( TaskAction taskAction ){
        actionMap.put(taskAction.getAction(), taskAction);
    }
    
    
    public static TaskAction getAction( String action ){
        TaskAction result = actionMap.get(action);
        
        if( result == null ){
            result = new TaskAction(action);
            registerTaskAction(result);
        }
        
        return result;
    }
    
    static {
        registerTaskAction(new TaskAction("APPROVE", "task.action.APPROVE", "fa-check", "btn-primary"));
        registerTaskAction(new TaskAction("REJECT", "task.action.REJECT", "fa-delete", "btn-danger"));
        registerTaskAction(new TaskAction("CONTINUE", "general.button.Ok", "fa-check", "btn-primary"));
        registerTaskAction(new TaskAction("SIGN", "task.action.SIGN", "fa-check", "btn-success"));
    }
}
