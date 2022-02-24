package com.ozguryazilim.raf.rest;

import com.ozguryazilim.telve.auth.UserService;
import com.ozguryazilim.telve.channel.email.EmailChannel;
import com.ozguryazilim.telve.channel.notify.NotifyChannel;
import com.ozguryazilim.telve.idm.entities.Group;
import com.ozguryazilim.telve.idm.entities.User;
import com.ozguryazilim.telve.idm.entities.UserGroup;
import com.ozguryazilim.telve.idm.group.GroupRepository;
import com.ozguryazilim.telve.idm.user.UserGroupRepository;
import com.ozguryazilim.telve.idm.user.UserRepository;
import com.ozguryazilim.telve.idm.user.UserRoleRepository;
import com.ozguryazilim.telve.messages.Messages;
import org.apache.camel.Body;
import org.apache.deltaspike.core.api.config.ConfigResolver;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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

    @Inject
    private UserService userService;

    @Inject
    private UserRepository userRepository;

    @Inject
    private GroupRepository groupRepository;

    @Inject
    private UserGroupRepository userGroupRepository;

    public static class NotifyResponse implements Serializable {
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

        public NotifyResponse() {
        }

        public NotifyResponse(List<String> users, String subject, String message) {
            this.users = users;
            this.subject = subject;
            this.message = message;
        }

        public NotifyResponse(List<String> users, String subject, String message, String icon, String severity, String link, Long duration) {
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

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response notification(@Body NotifyResponse notifyRequest) {
        sendNotifyMessages(notifyRequest);
        return Response.ok().build();
    }

    private void sendNotifyMessages(NotifyResponse notify) {
        Map<String, Object> notifyParams = new HashMap<>();
        Map<String, Object> emailParams = new HashMap<>();
        emailParams.put("messageClass", "NOTIFICATION_API");
        emailParams.put("message", notify.getMessage());
        String emailSubject = ConfigResolver.getPropertyValue("app.title") + " - " + notify.getSubject();

        notifyParams.put("messageClass", "NOTIFICATION_API");

        if (notify.getIcon() != null)
            notifyParams.put("icon", notify.getIcon());

        if (notify.getSeverity() != null)
            notifyParams.put("severity", notify.getSeverity());

        if (notify.getLink() != null)
            notifyParams.put("link", notify.getLink());

        //Notify gönder
        getNotifyReceivers(notify).forEach(user -> {
            notifyChannel.sendMessage(user, notify.getSubject(), notify.getMessage(), notifyParams);
            if (notify.isSendMail() && userService.getUserInfo(user).getEmail() != null) {
                emailChannel.sendMessage(userService.getUserInfo(user).getEmail(), emailSubject, "", emailParams);
            }
        });
    }

    private List<User> getUsersByGroup(String groupCode) {
        Group group = groupRepository.findByCode(groupCode).get(0);
        List<UserGroup> userGroups = userGroupRepository.findByGroup(group);
        return userGroups.stream().map(UserGroup::getUser).collect(Collectors.toList());

    }

    private List<String> getNotifyReceivers(NotifyResponse notify) {
        //Eğer kişi listesi boşsa herkese gönder
        if (!notify.isGroupReceivers()) {
            if (notify.getUsers().isEmpty()) {
                return userRepository.findAll().stream().map(User::getLoginName).collect(Collectors.toList());
            } else {
                return notify.getUsers();
            }
        } else {
            return notify.getUsers().stream().flatMap(u -> getUsersByGroup(u).stream().map(User::getLoginName)).collect(Collectors.toList());
        }
    }
}
