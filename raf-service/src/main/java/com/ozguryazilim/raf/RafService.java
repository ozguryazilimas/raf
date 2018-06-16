/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ozguryazilim.raf;

import com.ozguryazilim.raf.category.RafCategoryService;
import com.ozguryazilim.raf.entities.RafCategory;
import com.ozguryazilim.raf.jcr.RafModeshapeRepository;
import com.ozguryazilim.raf.models.RafCollection;
import com.ozguryazilim.raf.models.RafDocument;
import com.ozguryazilim.raf.models.RafFolder;
import com.ozguryazilim.raf.models.RafMetadata;
import com.ozguryazilim.raf.models.RafObject;
import java.io.InputStream;
import java.io.Serializable;
import java.util.Comparator;
import java.util.List;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

/**
 * Raf hizmetleri için temel Service sınıfı.
 *
 * Burada bir çeşit Facede yapılmış durumda.
 *
 * @author Hakan Uygun
 */
@ApplicationScoped
public class RafService implements Serializable {

    @Inject
    private RafModeshapeRepository rafRepository;
    
    @Inject
    private RafCategoryService categoryService;

    public List<RafFolder> getFolderList(String rafPath) throws RafException {
        //FIXME: yetki kontrolleri, sıralama v.b.
        return rafRepository.getFolderList(rafPath);
    }

    public RafCollection getCollection(String id) throws RafException {
        //FIXME: yetki kontrolleri, sıralama v.b.
        RafCollection result = rafRepository.getCollectionById(id);

        result.getItems().sort(new Comparator<RafObject>() {
                @Override
                public int compare(RafObject o1, RafObject o2) {
                    int m1 = "raf/folder".equals(o1.getMimeType()) ? -1 : 1;
                    int m2 = "raf/folder".equals(o2.getMimeType()) ? -1 : 1;
                    
                    //Eğer mimeType'lar aynıysa o zaman isme göre sırala
                    if( m1 == m2 ){
                        return o1.getTitle().compareTo(o2.getTitle());
                    }
                    
                    return m1 < m2 ? -1 : m1 > 2 ? 1 : 0;
                }
            }
        );

        return result;
    }
    
    public RafCollection getCategoryCollection(Long categoryId, String category, String categoryPath, String rootPath ) throws RafException {
        //FIXME: sıralama, yetki v.s.
        return rafRepository.getCategoryCollection(categoryId, category, categoryPath, rootPath, false);
    }
    
    public RafCollection getCategoryCollectionById(Long categoryId, String rootPath ) throws RafException {
        //FIXME: sıralama, yetki v.s.
        //FIXME: NPE kontorol
        RafCategory cat = categoryService.findById(categoryId);
        return getCategoryCollection( cat.getId(), cat.getName(), cat.getPath(), rootPath);
    }

    public void createFolder(RafFolder folder) throws RafException {
        //FIXME: yetki kontrolü
        rafRepository.createFolder(folder);

        //TODO: klasör eklendiğine dair burada bir event fırlatmak lazım.
    }

    public RafDocument uploadDocument(String fileName, InputStream in) throws RafException {
        //FIXME: yetki kontrolü
        return rafRepository.uploadDocument(fileName, in);
    }

    public RafObject getRafObject(String id) throws RafException {
        //FIXME: yetki kontrolü gerekli.
        //FIXME: event fırlatalım ki log yazılabilsin v.b.
        return rafRepository.getRafObject(id);
    }

    public InputStream getDocumentContent(String id) throws RafException {
        return rafRepository.getDocumentContent(id);
    }

    public void saveMetadata(String id, RafMetadata data) throws RafException {
        //FIXME: Yetki kontrolü + event
        rafRepository.saveMetadata(id, data);
    }

    /**
     * RafObject üzerinde bulunan title, info v.b. alanları günceller.
     *
     * @param data
     * @throws RafException
     */
    public void saveProperties(RafObject data) throws RafException {
        //FIXME: Yetki kontrolü + event
        rafRepository.saveProperties(data);
    }

    public void deleteObject(RafObject data) throws RafException {
        //FIXME: Yetki kontrolü
        deleteObject(data.getId());
    }

    public void deleteObject(String id) throws RafException {
        //FIXME: Yetki kontrolü
        rafRepository.deleteObject(id);
    }

    public void copyObject(List<RafObject> from, RafFolder to) throws RafException {
        //FIXME: yetki kontrolü
        rafRepository.copyObject(from, to);
    }

    public void moveObject(List<RafObject> from, RafFolder to) throws RafException {
        //FIXME: yetki kontrolü
        rafRepository.moveObject(from, to);
    }
}
