package com.ozguryazilim.raf.department;

import com.ozguryazilim.raf.entities.RafDepartment;
import com.ozguryazilim.raf.entities.RafDepartmentMember;
import java.io.Serializable;
import java.util.List;
import java.util.Optional;
import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import org.apache.deltaspike.jpa.api.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ApplicationScoped
public class RafDepartmentService implements Serializable {

    private static final Logger LOG = LoggerFactory.getLogger(RafDepartmentService.class);

    @Inject
    private RafDepartmentRepository departmentRepository;

    @Inject
    private RafDepartmentMemberRepository departmentMemberRepository;

    private List<RafDepartment> departments;

    @PostConstruct
    public void init() {
        populateDepartments();
    }

    protected void populateDepartments() {
        departments = departmentRepository.findAll();
    }

    public void refresh() {
        populateDepartments();
    }

    public List<RafDepartment> getDepartments() {
        return this.departments;
    }

    public RafDepartment findById(Long id) {
        RafDepartment department;

        Optional<RafDepartment> result = departments.stream().filter(c -> c.getId().equals(id))
                .findFirst();

        if (result.isPresent()) {
            department = result.get();
        } else {
            department = departmentRepository.findBy(id);
        }

        return department;
    }

    public Optional<RafDepartment> findByCode(String code) {
        Optional<RafDepartment> cachedDepartment = this.departments.stream()
                .filter(rafDepartment -> code.equals(rafDepartment.getCode()))
                .findFirst();

        return Optional.ofNullable(
                cachedDepartment.orElseGet(() -> {
                    List<RafDepartment> searchResult = departmentRepository.findByCode(code);
                    return !searchResult.isEmpty() ? searchResult.get(0) : null;
                })
        );
    }

    @Transactional
    public void removeDepartment(RafDepartment department) {
        departmentRepository.remove(department);
    }

    @Transactional
    public void saveDepartment(RafDepartment department) {
        departmentRepository.save(department);
    }

    public List<RafDepartmentMember> getMemberships(String memberName) {
        //TODO: Burada cahcleme v.s. yapmak lazım.
        return departmentMemberRepository.findByMemberName(memberName);
    }

    /**
     * Geriye kullanıcı deparman adını döndürür.
     *
     * @param memberName
     * @return
     */
    public String getDerpartmentName(String memberName) {
        String result = "";
        List<RafDepartmentMember> ls = getMemberships(memberName);

        if (!ls.isEmpty()) {
            result = ls.get(0).getDepartment().getCode();
        }
        return result;
    }

}
