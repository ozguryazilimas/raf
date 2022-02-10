package com.ozguryazilim.raf.definition;

import com.google.common.base.Strings;
import com.ozguryazilim.raf.entities.RafDefinition;
import com.ozguryazilim.raf.entities.RafDefinition_;
import com.ozguryazilim.telve.data.RepositoryBase;
import com.ozguryazilim.telve.idm.entities.User;
import com.ozguryazilim.telve.idm.entities.User_;
import com.ozguryazilim.telve.query.QueryDefinition;
import com.ozguryazilim.telve.query.filters.Filter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import javax.enterprise.context.Dependent;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import org.apache.deltaspike.core.api.config.ConfigResolver;
import org.apache.deltaspike.data.api.Repository;
import org.apache.deltaspike.data.api.criteria.CriteriaSupport;

/**
 *
 * @author oyas
 */
@Repository
@Dependent
public abstract class RafDefinitionRepository extends RepositoryBase<RafDefinition, RafDefinition> implements CriteriaSupport<RafDefinition> {
    
    
    public abstract RafDefinition findAnyByCode( String code );
    
    private boolean caseSensitiveSearch = "true".equals(ConfigResolver.getPropertyValue("caseSensitiveSearch", "false"));

    private Locale searchLocale = Locale.forLanguageTag(ConfigResolver.getPropertyValue("searchLocale", "tr-TR"));

    @Override
    public List<RafDefinition> browseQuery(QueryDefinition queryDefinition) {
        List<Filter<RafDefinition, ?, ?>> filters = queryDefinition.getFilters();
        
        CriteriaBuilder criteriaBuilder = entityManager().getCriteriaBuilder();
        //Geriye PersonViewModel dönecek cq'yu ona göre oluşturuyoruz.
        CriteriaQuery<RafDefinition> criteriaQuery = criteriaBuilder.createQuery(RafDefinition.class);

        //From Tabii ki         
        Root<RafDefinition> from = criteriaQuery.from(RafDefinition.class);
        
        //Filtreleri ekleyelim.
        List<Predicate> predicates = new ArrayList<>();
        
        decorateFilters(filters, predicates, criteriaBuilder, from);
        
        buildSearchTextControl(queryDefinition.getSearchText(), criteriaBuilder, predicates, from);
        
        //Person filtremize ekledik.
        criteriaQuery.where(predicates.toArray(new Predicate[]{}));
        
        //İsme göre sıralayalım
        criteriaQuery.orderBy(criteriaBuilder.asc(from.get(RafDefinition_.name)));
        
        //Haydi bakalım sonuçları alalım
        TypedQuery<RafDefinition> typedQuery = entityManager().createQuery(criteriaQuery);
        List<RafDefinition> resultList = typedQuery.getResultList();

        return resultList;
    }
    
    /**
     * Verilen searchText'i code name info like ile arayacak şekilde filtreler
     *
     * @param searchText
     * @param criteriaBuilder
     * @param predicates
     * @param from
     */
    private void buildSearchTextControl(String searchText, CriteriaBuilder criteriaBuilder, List<Predicate> predicates, Root<RafDefinition> from) {
        if (!Strings.isNullOrEmpty(searchText)) {
            if (caseSensitiveSearch) {
                predicates.add(criteriaBuilder.or(criteriaBuilder.like(from.get(RafDefinition_.code), "%" + searchText + "%"),
                    criteriaBuilder.like(from.get(RafDefinition_.name), "%" + searchText + "%"),
                    criteriaBuilder.like(from.get(RafDefinition_.info), "%" + searchText + "%")));
            }else{
                predicates.add(criteriaBuilder.or(criteriaBuilder.like(criteriaBuilder.lower(from.get(RafDefinition_.code)), "%" + searchText.toLowerCase(searchLocale) + "%"),
                    criteriaBuilder.like(criteriaBuilder.lower(from.get(RafDefinition_.name)), "%" + searchText.toLowerCase(searchLocale) + "%"),
                    criteriaBuilder.like(criteriaBuilder.lower(from.get(RafDefinition_.info)), "%" + searchText.toLowerCase(searchLocale) + "%")));
            }
        }
    }
}
