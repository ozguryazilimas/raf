package com.ozguryazilim.raf.category;

import com.ozguryazilim.raf.entities.RafCategory;
import java.io.Serializable;
import java.util.List;
import java.util.Optional;
import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

/**
 * Raf Category yapısına erişim ve işlem yapmak için servis sınıfı.
 * 
 * Raf Category'leri tüm uygulama için geçerli olduğundan dolayı ApplicationScope üzerinde cache yapısı olarak da kullanılacak.
 * 
 * @author Hakan Uygun
 */
@ApplicationScoped
public class RafCategoryService implements Serializable{
    
    @Inject
    private RafCategoryRepository repository;
    
    private List<RafCategory> categories;
    
    @PostConstruct
    public void init(){
        populateCategories();
    }
    

    /**
     * Aktif olan category leri veri tabanından çekip cacheler.
     */
    protected void populateCategories(){
        categories = repository.findAllActives();
    }

    public List<RafCategory> getCategories() {
        return categories;
    }
    
    /**
     * Cache tazeler
     */
    public void refresh(){
        populateCategories();
    }
    
    public RafCategory findById( Long id ){
        Optional<RafCategory> result = categories.stream().filter( c -> c.getId().equals(id))
                .findFirst();
        
        return result.orElse(null);
    }
    
    public RafCategory findByPath( String path ){
        Optional<RafCategory> result = categories.stream().filter( c -> c.getPath().equals(path))
                .findFirst();
        
        return result.orElse(null);
    }
}
