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
import java.util.Collections;
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

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response notification(@Body NotifyRequest notifyRequest) {
        sendNotifyMessages(notifyRequest);
        return Response.ok().build();
    }

    private void sendNotifyMessages(NotifyRequest notify) {
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

        if (notify.getLink() != null)
            notifyParams.put("duration", notify.getDuration());

        //Notify gönder
        getNotifyReceivers(notify).forEach(user -> {
            notifyChannel.sendMessage(user, notify.getSubject(), notify.getMessage(), notifyParams);
            if (notify.isSendMail() && userService.getUserInfo(user).getEmail() != null) {
                emailChannel.sendMessage(userService.getUserInfo(user).getEmail(), emailSubject, "", emailParams);
            }
        });
    }

    private List<User> getUsersByGroup(String groupCode) {
        List<Group> groups = groupRepository.findByCode(groupCode);
        if (groups.isEmpty()) {
            return Collections.emptyList();
        }

        Group group = groups.get(0);
        List<UserGroup> userGroups = userGroupRepository.findByGroup(group);
        return userGroups.stream().map(UserGroup::getUser).collect(Collectors.toList());

    }

    private List<String> getNotifyReceivers(NotifyRequest notify) {
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
