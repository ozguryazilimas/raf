package com.ozguryazilim.raf.department;

import com.ozguryazilim.raf.entities.RafDepartment;
import com.ozguryazilim.raf.entities.RafDepartmentMember;
import com.ozguryazilim.telve.auth.UserInfo;
import com.ozguryazilim.telve.auth.UserService;
import com.ozguryazilim.telve.contact.AbstractContactSource;
import com.ozguryazilim.telve.contact.Contact;
import com.ozguryazilim.telve.contact.ContactSource;
import org.apache.commons.lang3.StringUtils;

import javax.inject.Inject;
import java.util.List;
import java.util.Map;

@ContactSource(name = "department")
public class DepartmentContactSource extends AbstractContactSource {

    private static final String DEPARTMENT_NAME = "name";
    private static final String DEPARTMENT_ROLE = "role";

    @Inject
    private RafDepartmentService departmentService;

    @Inject
    private UserService userService;

    @Override
    public void resolve(Map<String, String> map, List<Contact> list) {

        String departmentName = map.get(DEPARTMENT_NAME);

        if (StringUtils.isBlank(departmentName))
            return;

        departmentService.findByCode(departmentName)
            .map(RafDepartment::getMembers)
            .ifPresent(rafDepartmentMembers -> {
                String departmentRole = map.get(DEPARTMENT_ROLE);

                rafDepartmentMembers.stream()
                    .filter(member -> {
                        if (StringUtils.isNotBlank(departmentRole))
                            return departmentRole.equals(member.getRole());
                        else
                            return true;
                    })
                    .map(this::rafDepartmentMemberToContact)
                    .forEach(list::add);

            });
    }

    private Contact rafDepartmentMemberToContact(RafDepartmentMember rafDepartmentMember) {
        Contact contact = new Contact();

        UserInfo ui = userService.getUserInfo(rafDepartmentMember.getMemberName());
        if( ui != null ){
            contact.setSource(getClass().getSimpleName());
            contact.setType("User");
            contact.setId(ui.getLoginName());
            contact.setFirstname(ui.getFirstName());
            contact.setLastname(ui.getLastName());
            contact.setEmail(ui.getEmail());
            contact.setMobile(ui.getMobile());
        }
        return contact;
    }

}
