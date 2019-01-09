package com.ozguryazilim.raf.department;

import com.ozguryazilim.raf.entities.RafDepartment;
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

    @Transactional
    public void removeDepartment(RafDepartment department) {
        departmentRepository.remove(department);
    }

    @Transactional
    public void saveDepartment(RafDepartment department) {
        departmentRepository.save(department);
    }
}
