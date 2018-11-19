package com.ozguryazilim.raf.cmis;

import com.ozguryazilim.raf.RafException;
import com.ozguryazilim.raf.definition.RafDefinitionService;
import com.ozguryazilim.raf.entities.RafDefinition;
import org.apache.chemistry.opencmis.commons.exceptions.CmisObjectNotFoundException;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.deltaspike.core.api.provider.BeanProvider;
import org.apache.shiro.SecurityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Bu sınıf içerisinde Raf'lar birer CMIS repository olarak tanımlanır ve kullanıcı yetkileri çerçecesinde duyurulurlar.
 * 
 * Repository listesi performans için lazy olarak cachelenmektedir. İlk talepte liste oluşturulacak ve yetkiye göre sunulacak.
 * 
 * @author Hakan Uygun
 */
public class RafCmisRepositoryManager {

    private final static Logger LOG = LoggerFactory.getLogger(RafCmisRepositoryManager.class);
    
    private Map<String, RafCmisRepository> repositories;
    private RafCmisTypeManager typeManager = new RafCmisTypeManager();

    /**
     * Geriye ID'si verilen repository'i sunar.
     * 
     * @param repositoryId
     * @return 
     */
    public RafCmisRepository getRepository(String repositoryId) {
        //FIXME: yetkiye bağlı olarak repository sunulmalı. ID olması sunulabileceği anlamına gelmiyor.
        RafCmisRepository result = getRepositoryMap().get(repositoryId);
        if (result == null) {
            throw new CmisObjectNotFoundException("Unknown repository '" + repositoryId + "'!");
        }

        return result;
    }

    /**
     * Sistemde bulunan reposiory listesini kullanıcı login bilgisine göre sunar.
     * 
     * @return 
     */
    public Collection<RafCmisRepository> getRepositories() {
        //FIXME: burada kullanıcı yetkisine göre filtreleme yapılacak! Kullanıcının yetkisi olmayan repository'ler döndürülmeyecek.
        //Ayarlardan alınacak bilgiye göre PRIVATE ve SHARED repolar da listeye eklenmesi gerekcek.
        
        //Eğer auth olunmamış ise boş dönüş yapıyoruz!
        if( !SecurityUtils.getSubject().isAuthenticated()  ){
            return Collections.emptyList();
        }

        //Kullanıcı adını alalım
        String username = SecurityUtils.getSubject().getPrincipal().toString();
        
        //Kişisel raf tanımlanmış mı bir bakalım tanımlanmamış ise ekleyelim
        if( !getRepositoryMap().containsKey(username) ){
            try {
                RafDefinition raf = getRafDefinitionService().getPrivateRaf(username);
                raf.setCode(username);
                repositories.put(username, new RafCmisRepository( raf, typeManager));
            } catch (RafException ex) {
                LOG.error(ex.getMessage(), ex);
            }
        }
        
        //FIXME: Yetkiye göre filtreleme yapılacak.
        
        return getRepositoryMap().values();
    }

    
    private Map<String, RafCmisRepository> getRepositoryMap(){
        if( repositories == null ){
            repositories = new HashMap<>();
            populateRepositories();
        }
        
        return repositories;
    }
    
    /**
     * Sistemde tanımlı rafları birer repository olarak kayıt altına alır.
     */
    public void populateRepositories(){
        List<RafDefinition> rafs = getRafDefinitionService().getRafs();
        for( RafDefinition raf : rafs ){
            //Her raf için bir RafCmisRepository oluşturulacak
            RafCmisRepository repo = new RafCmisRepository( raf, typeManager);
            repositories.put(repo.getRepositoryId(), repo);
        }
        
        //FIXME: Configden shared raf kullanılıp kullanılamadığına bakılmalı
        try {
            RafDefinition raf = getRafDefinitionService().getSharedRaf();
            RafCmisRepository repo = new RafCmisRepository( raf, typeManager);
            repositories.put(repo.getRepositoryId(), repo);
        } catch (RafException ex) {
            LOG.error("Shared Raf için CMIS repo oluşturulamadı", ex);
        }
    }
    
    public void refresh(){
        repositories = null;
    }
    
    
    private RafDefinitionService getRafDefinitionService(){
        return BeanProvider.getContextualReference(RafDefinitionService.class, true);
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        for (RafCmisRepository repository : getRepositoryMap().values()) {
            sb.append('[');
            sb.append(repository.getRepositoryId());
            sb.append(']');
        }

        return sb.toString();
    }
}