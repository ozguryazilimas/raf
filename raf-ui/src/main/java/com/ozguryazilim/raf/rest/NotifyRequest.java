package com.ozguryazilim.raf.rest;

import java.util.List;

public class NotifyRequest {
    private List<String> users;
    private String subject;
    private String message;

    //Generic notify command header parameters
    private String icon = "fa fa-bell-o";
    private String severity = "info";
    private String link = "";
    private Long duration = -1L;

    //Additional Parameters
    private boolean groupReceivers = Boolean.FALSE;
    private boolean sendMail = Boolean.FALSE;

    public NotifyRequest() {
    }

    public NotifyRequest(List<String> users, String subject, String message) {
        this.users = users;
        this.subject = subject;
        this.message = message;
    }

    public NotifyRequest(List<String> users, String subject, String message, String icon, String severity, String link, Long duration) {
        this.users = users;
        this.subject = subject;
        this.message = message;
        this.icon = icon;
        this.severity = severity;
        this.link = link;
        this.duration = duration;
    }

    public List<String> getUsers() {
        return users;
    }

    public void setUsers(List<String> users) {
        this.users = users;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public String getSeverity() {
        return severity;
    }

    public void setSeverity(String severity) {
        this.severity = severity;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public boolean isGroupReceivers() {
        return groupReceivers;
    }

    public void setGroupReceivers(boolean groupReceivers) {
        this.groupReceivers = groupReceivers;
    }

    public Long getDuration() {
        return duration;
    }

    public void setDuration(Long duration) {
        this.duration = duration;
    }

    public boolean isSendMail() {
        return sendMail;
    }

    public void setSendMail(boolean sendMail) {
        this.sendMail = sendMail;
    }
}
