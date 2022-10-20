package com.ozguryazilim.raf;

import com.ozguryazilim.raf.entities.RafDefinition;
import com.ozguryazilim.raf.jcr.RafModeshapeRepository;
import com.ozguryazilim.raf.member.RafMemberService;
import com.ozguryazilim.raf.models.DetailedSearchModel;
import com.ozguryazilim.raf.models.RafCollection;
import com.ozguryazilim.raf.objet.member.RafPathMemberService;
import com.ozguryazilim.telve.auth.Identity;
import java.io.Serializable;
import java.util.List;
import java.util.Locale;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.modeshape.jcr.value.IoException;
import org.primefaces.model.SortOrder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author oyas
 */
@ApplicationScoped
public class SearchService implements Serializable {

    private static final Logger LOG = LoggerFactory.getLogger(SearchService.class);

    @Inject
    private RafModeshapeRepository modeshapeRepository;

    @Inject
    private RafMemberService rafMemberService;

    @Inject
    private RafPathMemberService rafPathMemberService;

    @Inject
    private Identity identity;

    @Inject
    CaseSensitiveSearchService caseSensitiveSearchService;

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

    public long detailedSearchCount(DetailedSearchModel searchModel, List<RafDefinition> rafs, List extendedQuery) throws RafException {
        //FIXME: yetki kontrolleri yapılmalı.
        //Note: JCR implementasyonlarında henüz count özelliği yok bu yüzden pagination larda şimdilik sonuç sayısı 1000 adet miş gibi cevap vereceğiz.
        return 1000;//modeshapeRepository.getDetailedSearchCount(searchModel, rafs, rafPathMemberService, identity.getLoginName());
    }

    public RafCollection detailedSearch(DetailedSearchModel searchModel, List<RafDefinition> rafs, int limit, int offset, String sortField, SortOrder sortOrder, List extendedQuery, List extendedSortQuery) throws RafException, FileBinaryNotFoundException {
        //FIXME: yetki kontrolleri yapılmalı.
        Locale searchLocale = Locale.US;
        if (!searchModel.getSearchInDocumentName()) {
            searchLocale = caseSensitiveSearchService.getSearchLocale();
        }
        try {
            return modeshapeRepository.getDetailedSearchCollection(searchModel, rafs, rafPathMemberService, identity.getLoginName(), limit, offset, extendedQuery, extendedSortQuery, searchLocale);
        } catch (IoException ex) {
            throw new FileBinaryNotFoundException("[RAF-0048] Could not found one or more nodes binary data.", ex);
        }
    }

}
