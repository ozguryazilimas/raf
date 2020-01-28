package com.ozguryazilim.raf;

import com.ozguryazilim.raf.entities.RafDefinition;
import com.ozguryazilim.raf.jcr.RafModeshapeRepository;
import com.ozguryazilim.raf.member.RafMemberService;
import com.ozguryazilim.raf.models.RafCollection;
import com.ozguryazilim.raf.objet.member.RafPathMemberService;
import com.ozguryazilim.telve.auth.Identity;
import java.io.Serializable;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

/**
 *
 * @author oyas
 */
@ApplicationScoped
public class SearchService implements Serializable {

    @Inject
    private RafModeshapeRepository modeshapeRepository;

    @Inject
    private RafMemberService rafMemberService;

    @Inject
    private RafPathMemberService rafPathMemberService;

    @Inject
    private Identity identity;

    public RafCollection search(String searchText, RafDefinition raf) throws RafException {
        //FIXME: yetki kontrolleri yapılmalı.
        if (rafMemberService.hasReadRole(identity.getLoginName(), raf)) {
            return modeshapeRepository.getSearchCollection(searchText, raf.getNode().getPath(), rafPathMemberService, identity.getLoginName());
        } else {
            RafCollection result = new RafCollection();
            result.setId("SEARCH");
            result.setMimeType("raf/search");
            result.setTitle(searchText);
            result.setPath("SEARCH");
            result.setName(searchText);
            return result;
        }
    }

}
