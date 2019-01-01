package com.ozguryazilim.raf.jbpm.identity;

import com.ozguryazilim.telve.idm.group.GroupRepository;
import com.ozguryazilim.telve.idm.user.UserGroupRepository;
import java.util.List;
import org.apache.deltaspike.core.api.provider.BeanProvider;
import org.kie.api.task.TaskContext;
import org.kie.api.task.model.Group;
import org.kie.api.task.model.OrganizationalEntity;
import org.kie.api.task.model.Task;
import org.kie.internal.task.api.assignment.Assignment;
import org.kie.internal.task.api.assignment.AssignmentStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Task Atamaları için Raf tarafafında kullanılacak olan Strategy.
 * 
 * Bu sınıf içerisinde şunlar yapılacak : 
 * 
 * - Eğer atama yapılan grup içinde tek kişi varsa doğrudan atayalım
 * - Eğer grup adı manager.huygun gibi bir şey ise aslında grup değil, bir kullanıcının yöneticisinden bahsediliyordur, yöneticiyi bulup atayalım
 * 
 * TODO: Burada diğer stratejilere düşmeyi de şününebiliriz ( LoadBalancer, RoundRobin, Busyness v.b. )
 * 
 * @author Hakan Uygun
 */
public class RafTaskAssignmentStrategy implements AssignmentStrategy{

    private static final Logger LOG = LoggerFactory.getLogger(RafTaskAssignmentStrategy.class);
    private static final String IDENTIFIER = "RafTaskAssignment";
    
    @Override
    public String getIdentifier() {
        return IDENTIFIER;
    }

    @Override
    public Assignment apply(Task task, TaskContext tc, String string) {
        LOG.debug("Raf Task Assignment Strategy working!");
        
        List<OrganizationalEntity> potentialOwners = task.getPeopleAssignments().getPotentialOwners();
        
        //Eğer sadece bir tane içerik varsa
        if( potentialOwners.size() == 1 ){
            OrganizationalEntity oe = potentialOwners.get(0);
            if( oe instanceof Group ){
                
                //Eğer grup adı "manager." ile başlıyor ise gerçek bir grup değildir. Kullanıcının yöneticisine ulaşmaya çalışılıyordur.
                if( oe.getId().startsWith("manager.") ){
                    LOG.debug("Find manager for : {}", oe.getId());
                    return null;
                }
                
                /*
                List<com.ozguryazilim.telve.idm.entities.Group> gls = getGroupRepository().findByCode(oe.getId());
                if( !gls.isEmpty()){
                   List<UserGroup> ugs = getUserGroupRepository().findByGroup(gls.get(0));
                   //Eğer grupta sadece 1 kişi var ise doğrudan ona atayalım
                   if( ugs.size() == 1 ){
                       UserGroup ug = ugs.get(0);
                       return new Assignment(ug.getUser().getLoginName());
                   } 
                   
                   //Eğer birden fazla kişi var ise default davranışa bırakalım!
                   
                }
                */
            } else {
                return new Assignment(oe.getId());
            }
        }
        
        /* FIXME: POT 1 tane değil ise neler olacak?
        for( OrganizationalEntity oe : potentialOwners ){
            if( oe instanceof Group ){
                
            } else {
                
            }
        }
        */
        
        return null;
    }


    protected GroupRepository getGroupRepository(){
        return BeanProvider.getContextualReference(GroupRepository.class, true);
    }
    
    protected UserGroupRepository getUserGroupRepository(){
        return BeanProvider.getContextualReference(UserGroupRepository.class, true);
    }
}
