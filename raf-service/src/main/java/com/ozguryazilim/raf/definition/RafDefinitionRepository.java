/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ozguryazilim.raf.definition;

import com.ozguryazilim.raf.entities.RafDefinition;
import com.ozguryazilim.raf.entities.RafDefinition_;
import com.ozguryazilim.telve.data.RepositoryBase;
import com.ozguryazilim.telve.query.QueryDefinition;
import com.ozguryazilim.telve.query.filters.Filter;
import java.util.ArrayList;
import java.util.List;
import javax.enterprise.context.Dependent;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
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
        
        //Person filtremize ekledik.
        criteriaQuery.where(predicates.toArray(new Predicate[]{}));
        
        //İsme göre sıralayalım
        criteriaQuery.orderBy(criteriaBuilder.asc(from.get(RafDefinition_.name)));
        
        //Haydi bakalım sonuçları alalım
        TypedQuery<RafDefinition> typedQuery = entityManager().createQuery(criteriaQuery);
        List<RafDefinition> resultList = typedQuery.getResultList();

        return resultList;
    }
}
