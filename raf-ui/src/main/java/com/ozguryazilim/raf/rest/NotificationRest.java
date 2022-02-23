package com.ozguryazilim.raf.rest;

import com.ozguryazilim.telve.channel.email.EmailChannel;
import com.ozguryazilim.telve.channel.notify.NotifyChannel;
import org.apache.camel.Body;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 *
 * @author oyas
 */
@RequiresPermissions("admin")
@Path("/api/notification")
public class NotificationRest {

    private static final Logger LOG = LoggerFactory.getLogger(NotificationRest.class);

    @Inject
    private NotifyChannel notifyChannel;

    @Inject
    private EmailChannel emailChannel;

    public static class NotifyResponse implements Serializable {
        private List<String> users;
        private String subject;
        private String message;

        //Generic notify command header parameters
        private String icon = "fa fa-bell-o";
        private String severity = "info";
        private String link = "";

        //Additional Parameters
        private long expirationMin;
        private boolean groupReceivers = Boolean.FALSE;

        public NotifyResponse() {
        }

        public NotifyResponse(List<String> users, String subject, String message) {
            this.users = users;
            this.subject = subject;
            this.message = message;
        }

        public NotifyResponse(List<String> users, String subject, String message, String icon, String severity, String link) {
            this.users = users;
            this.subject = subject;
            this.message = message;
            this.icon = icon;
            this.severity = severity;
            this.link = link;
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

        public long getExpirationMin() {
            return expirationMin;
        }

        public void setExpirationMin(long expirationMin) {
            this.expirationMin = expirationMin;
        }

        public boolean isGroupReceivers() {
            return groupReceivers;
        }

        public void setGroupReceivers(boolean groupReceivers) {
            this.groupReceivers = groupReceivers;
        }
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response notification(@Body NotifyResponse notifyRequest) {
        sendNotifyMessage(notifyRequest);
        return Response.ok().build();
    }

    private void sendNotifyMessage(NotifyResponse notify) {
        Map<String, Object> params = new HashMap<>();
        params.put("messageClass", "notifyRest");

        if (notify.getIcon() != null)
            params.put("icon", notify.getIcon());

        if (notify.getSeverity() != null)
            params.put("severity", notify.getSeverity());

        if (notify.getLink() != null)
            params.put("link", notify.getLink());


        notifyChannel.sendMessage(notify.getUsers().get(0), notify.getSubject(), notify.getMessage(), params);
    }

}
