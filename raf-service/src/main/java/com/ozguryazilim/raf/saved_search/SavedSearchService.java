package com.ozguryazilim.raf.saved_search;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ozguryazilim.raf.entities.SavedSearch;
import com.ozguryazilim.raf.models.DetailedSearchModel;
import com.ozguryazilim.telve.auth.Identity;
import java.io.IOException;
import java.io.Serializable;
import java.util.List;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import org.apache.deltaspike.jpa.api.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author oyaş
 *
 */
@ApplicationScoped
public class SavedSearchService implements Serializable {

    @Inject
    private SavedSearchRepository savedSearchRepository;

    @Inject
    private Identity identity;

    private static final Logger LOG = LoggerFactory.getLogger(SavedSearchService.class);
    private static final ObjectMapper objectMapper = new ObjectMapper();

    public List<SavedSearch> getSavedSearchs() {
        return savedSearchRepository.findByMemberName(identity.getLoginName());
    }

    @Transactional
    public void saveSearch(String searchName, DetailedSearchModel detailedSearchModel) throws JsonProcessingException {
        String search = objectMapper.writeValueAsString(detailedSearchModel);
        SavedSearch savedSearch = new SavedSearch();
        savedSearch.setMemberName(identity.getLoginName());
        savedSearch.setSearchName(searchName);
        savedSearch.setSearch(search);
        savedSearchRepository.saveAndFlush(savedSearch);
    }

    public void removeSearchById(Long searchId) {
        SavedSearch search = savedSearchRepository.findBy(searchId);
        if (search != null) {
            savedSearchRepository.remove(search);
        }
    }

    public void removeSearch(String searchName) {
        savedSearchRepository.removeByMemberNameAndSearchName(identity.getLoginName(), searchName);
    }

    public DetailedSearchModel getSearchModel(Long searchId) throws IOException {
        SavedSearch search = savedSearchRepository.findBy(searchId);
        if (search != null) {
            return objectMapper.readValue(search.getSearch(), DetailedSearchModel.class);
        } else {
            return new DetailedSearchModel();
        }
    }

}
