package com.ozguryazilim.raf.events;

import com.ozguryazilim.telve.messagebus.command.AbstractCommand;

/**
 *
 * @author oyas
 */
public class EventLogCommand extends AbstractCommand{
    
    private String username;
    private String message;
    private String info;
    private String code;
    private String refId;
    private String path;
    private String type;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getInfo() {
        return info;
    }

    public void setInfo(String info) {
        this.info = info;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getRefId() {
        return refId;
    }

    public void setRefId(String refId) {
        this.refId = refId;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return "EventLogCommand{" + "username=" + username + ", message=" + message + ", info=" + info + ", code=" + code + ", refId=" + refId + ", path=" + path + ", type=" + type + '}';
    }
    
}
