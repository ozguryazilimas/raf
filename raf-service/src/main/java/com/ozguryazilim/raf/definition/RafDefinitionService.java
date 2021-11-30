package com.ozguryazilim.raf.definition;

import com.ozguryazilim.raf.RafException;
import com.ozguryazilim.raf.entities.RafDefinition;
import com.ozguryazilim.raf.entities.RafMemberType;
import com.ozguryazilim.raf.jcr.RafModeshapeRepository;
import com.ozguryazilim.raf.member.RafMemberService;
import com.ozguryazilim.raf.models.RafNode;
import com.ozguryazilim.telve.auth.Identity;
import com.ozguryazilim.telve.idm.entities.Group;
import com.ozguryazilim.telve.idm.group.GroupRepository;
import com.google.common.base.Strings;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import org.apache.deltaspike.core.api.config.ConfigResolver;
import org.apache.deltaspike.jpa.api.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author oyas
 */
@ApplicationScoped
public class RafDefinitionService implements Serializable {

    private static final Logger LOG = LoggerFactory.getLogger(RafDefinitionService.class);
    public static final String RAF_ROLE_MANAGER = "MANAGER";

    @Inject
    private Identity identity;

    @Inject
    private RafDefinitionRepository repository;

    @Inject
    private RafModeshapeRepository rafRepository;

    @Inject
    private RafMemberService memberService;

    @Inject
    private GroupRepository groupRepository;

    private List<RafDefinition> rafs = new ArrayList<>();

    @PostConstruct
    public void init() {
        try {
            populateRafs();
        } catch (RafException ex) {
            LOG.error("Cannot populate rafs", ex);
        }
    }

    public void createNewRaf(RafDefinition rd) throws RafException {

        //Raf daha önce tanımlı mı?
        if (repository.findAnyByCode(rd.getCode()) != null) {
            //Bu isimli bir raf tanımı zaten var.
            throw new RafException("[RAF-0040] Raf Alreaddy exist");
        }

        RafNode n = rafRepository.createRafNode(rd);

        rd.setNodeId(n.getId());

        repository.save(rd);

        //Oluşturan kişiyi MANAGER olarak atayalım.
        memberService.addMember(rd, identity.getLoginName(), RafMemberType.USER, RAF_ROLE_MANAGER);

        //Varsayılan manager grubu varsa, grubu rafa MANAGER yetkisi ile ekleyelim.
        addDefaultManagerMembers(rd);

        refresh();
    }

    public RafDefinition getRafDefinitionByCode(String code) throws RafException {

        if ("PRIVATE".equals(code)) {
            return getPrivateRaf(identity.getLoginName());
        } else if ("SHARED".equals(code)) {
            return getSharedRaf();
        } else {
            //FIXME: burada yetki kontrolü de yapılması gerekiyor.

            RafDefinition result = repository.findAnyByCode(code);

            if (result != null) {
                RafNode n = rafRepository.getRafNode(code);
                //NodeId aynı olmalı. Kontrol etsek mi?
                result.setNode(n);
            }

            return result;
        }

    }

    public RafDefinition getPrivateRaf(String username) throws RafException {
        RafDefinition result = new RafDefinition();

        result.setId(-1L);
        result.setCode("PRIVATE");
        result.setName("raf.label.Private");

        RafNode n = rafRepository.getPrivateRafNode(username);
        result.setNodeId(n.getId());
        result.setNode(n);

        return result;
    }

    public RafDefinition getSharedRaf() throws RafException {
        RafDefinition result = new RafDefinition();

        result.setId(-2L);
        result.setCode("SHARED");
        result.setName("raf.label.Shared");

        RafNode n = rafRepository.getSharedRafNode();
        result.setNodeId(n.getId());
        result.setNode(n);

        return result;
    }

    @Transactional
    public void save(RafDefinition rafDefinition) throws RafException {
        //PRIVATE ya da SHARED raf için saklanacak bişi yok.
        if (rafDefinition.getId() < 1) {
            return;
        }

        repository.saveAndFlush(rafDefinition);
        rafRepository.updateRafNode(rafDefinition);
    }

    public List<RafDefinition> getRafs() {

        return rafs;

    }

    public List<RafDefinition> getRafsForUser(String username) {
        return getRafsForUser(username, false);
    }

    public List<RafDefinition> getRafsForUser(String username, Boolean addCommonRafs) {

        List<RafDefinition> restult = rafs.stream()
                .filter(r -> {
                    try {
                        return memberService.isMemberOf(username, r);
                    } catch (RafException ex) {
                        //Yapacak bişi yok
                    }
                    return false;
                })
                .collect(Collectors.toList());

        if (addCommonRafs) {
            try {
                restult.add(getPrivateRaf(username));
                restult.add(getSharedRaf());
            } catch (RafException ex) {
                //Hata bildirsek mi?
            }

        }

        return restult;
    }

    protected void populateRafs() throws RafException {
        rafs = repository.findAll();
        for (RafDefinition raf : rafs) {
            RafNode rn = rafRepository.getRafNode(raf.getCode());
            raf.setNode(rn);
        }
    }

    public void refresh() {
        try {
            populateRafs();
        } catch (RafException ex) {
            LOG.error("Raf bilgileri toplanamadı", ex);
        }
    }

    private void addDefaultManagerMembers(RafDefinition rd) {

        String defaultManagerGroup = ConfigResolver.getPropertyValue("raf.default.managerGroup", "");
        if (Strings.isNullOrEmpty(defaultManagerGroup)) return;

        List<Group> groupList = groupRepository.findByCode(defaultManagerGroup);
        if (groupList.isEmpty()) return;

        try {
            memberService.addMember(rd, groupList.get(0).getCode(), RafMemberType.GROUP, RAF_ROLE_MANAGER);
        } catch (RafException e) {
            LOG.error("Raf icin on tanimli yonetici grubu atanamadi.", e);
        }
    }

}
