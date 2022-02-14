package com.ozguryazilim.raf.rest;

import com.ozguryazilim.telve.idm.IdmEvent;
import com.ozguryazilim.telve.idm.entities.Group;
import com.ozguryazilim.telve.idm.entities.User;
import com.ozguryazilim.telve.idm.entities.UserGroup;
import com.ozguryazilim.telve.idm.group.GroupRepository;
import com.ozguryazilim.telve.idm.user.UserGroupRepository;
import com.ozguryazilim.telve.idm.user.UserRepository;
import com.ozguryazilim.telve.rest.ext.Logged;
import com.ozguryazilim.telve.utils.TreeUtils;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.event.Event;
import javax.inject.Inject;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 *
 *
 * @author oyas
 */
@Logged
@RequiresPermissions("admin")
@Path("/api/group")
public class GroupRest {

    private static final Logger LOG = LoggerFactory.getLogger(GroupRest.class);

    @Inject
    private GroupRepository groupRepository;

    @Inject
    private UserRepository userRepository;

    @Inject
    private UserGroupRepository userGroupRepository;

    @Inject
    private Event<IdmEvent> idmEvent;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public List<Map<String, String>> getGroupList() {
        List<Group> groups = groupRepository.findNodes();
        return getGroupList(groups);
    }

    @GET
    @Path("/root")
    @Produces(MediaType.APPLICATION_JSON)
    public List<Map<String, String>> getRootGroupList() {
        List<Group> groups = groupRepository.findRootNodes();
        return getGroupList(groups);
    }

    @GET
    @Path("/{group}")
    @Produces(MediaType.APPLICATION_JSON)
    public Map<String, String> getGroup(@PathParam("group") String groupCode) {
        Group group = groupRepository.findByCode(groupCode).get(0);
        return getGroupInfo(group);
    }

    @GET
    @Path("/{group}/children")
    @Produces(MediaType.APPLICATION_JSON)
    public List<Map<String, String>> getGroupChildren(@PathParam("group") String groupCode) {
        List<Group> groups = groupRepository.findNodes(groupRepository.findByCode(groupCode).get(0));
        return getGroupList(groups);
    }

    @GET
    @Path("/{group}/users")
    @Produces(MediaType.APPLICATION_JSON)
    public List<User> getGroupUsers(@PathParam("group") String groupCode) {
        Group group = groupRepository.findByCode(groupCode).get(0);
        List<UserGroup> userGroups = userGroupRepository.findByGroup(group);
        return userGroups.stream().map(UserGroup::getUser).collect(Collectors.toList());
    }

    @POST()
    @Produces(MediaType.APPLICATION_JSON)
    public Response createGroup(@FormParam("groupCode") String groupCode,
                                @FormParam("groupName") String groupName,
                                @FormParam("info") String info,
                                @FormParam("parent") String parent) {

        try {
            Group group = new Group();
            group.setActive(Boolean.TRUE);
            group.setCode(groupCode);
            group.setName(groupName);
            group.setInfo(info);
            group.setAutoCreated(Boolean.FALSE);
            if (parent != null && !parent.isEmpty()) {
                group.setParent(groupRepository.findByCode(parent).get(0));
            }
            groupRepository.save(group);
            group.setPath(TreeUtils.getNodeIdPath(group));
            groupRepository.save(group);
            idmEvent.fire(new IdmEvent(IdmEvent.FROM_GROUP, IdmEvent.CREATE, groupCode));
        } catch (Exception e) {
            LOG.error(String.format("Group Create Error - %s", groupCode), e);
            return Response.status(Response.Status.CREATED).entity(e.getMessage()).build();
        }
        return Response.ok().build();
    }

    @POST()
    @Path("{group}/addUser")
    @Produces(MediaType.APPLICATION_JSON)
    public Response addUserToGroup(@PathParam("group") String groupCode,
                                   @FormParam("user") String userName) {
        try {
            User user = userRepository.findAnyByLoginName(userName);
            if (user == null) {
                LOG.warn("Could not find user: {}", userName);
                return Response.status(Response.Status.NOT_FOUND).entity("Could not find user").build();
            }

            List<Group> groups = groupRepository.findByCode(groupCode);
            if (groups.isEmpty()) {
                LOG.warn("Could not find group: {}", groupCode);
                return Response.status(Response.Status.NOT_ACCEPTABLE).entity("Could not find group").build();
            }

            Group group = groups.get(0);

            UserGroup userGroup = userGroupRepository.findAnyByUserAndGroup(user, group);
            if (userGroup == null) {
                UserGroup newUserGroup = new UserGroup();
                newUserGroup.setGroup(group);
                newUserGroup.setUser(user);
                userGroupRepository.save(newUserGroup);
                idmEvent.fire(new IdmEvent(IdmEvent.FROM_GROUP, IdmEvent.UPDATE, groupCode));
            }
        } catch (Exception e) {
            LOG.error("User Group Create Error", e);
            return Response.status(Response.Status.CREATED).entity(e.getMessage()).build();
        }
        return Response.ok().build();
    }

    private List<Map<String, String>> getGroupList(List<Group> groups) {
        List<Map<String, String>> groupList = new ArrayList<>();
        for (Group group : groups)
            groupList.add(getGroupInfo(group));
        return groupList;
    }

    private Map<String, String> getGroupInfo(Group group) {
        Map<String, String> groupInfo = new HashMap<>();
        groupInfo.put("code", group.getCode());
        groupInfo.put("name", group.getName());
        groupInfo.put("info", group.getInfo());
        Group parent = group.getParent();
        groupInfo.put("parent", parent != null ? parent.getCode() : "");
        return groupInfo;
    }
}
