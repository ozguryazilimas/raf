/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ozguryazilim.raf;

import com.ozguryazilim.raf.jcr.RafModeshapeRepository;
import com.ozguryazilim.raf.models.RafCollection;
import com.ozguryazilim.raf.models.RafDocument;
import com.ozguryazilim.raf.models.RafFolder;
import com.ozguryazilim.raf.models.RafMetadata;
import com.ozguryazilim.raf.models.RafObject;
import java.io.InputStream;
import java.io.Serializable;
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
public class RafService implements Serializable{
    
    @Inject
    private RafModeshapeRepository rafRepository;
    
    
    
    
    public List<RafFolder> getFolderList( String raf ) throws RafException{
        //FIXME: yetki kontrolleri, sıralama v.b.
        return rafRepository.getFolderList(raf);
    }
    
    public RafCollection getCollection( String id ) throws RafException{
        //FIXME: yetki kontrolleri, sıralama v.b.
        return rafRepository.getCollectionById(id);
    }
    
    public void createFolder( RafFolder folder ) throws RafException{
        //FIXME: yetki kontrolü
        rafRepository.createFolder(folder);
        
        //TODO: klasör eklendiğine dair burada bir event fırlatmak lazım.
    }
    
    public RafDocument uploadDocument( String fileName, InputStream in ) throws RafException{
        //FIXME: yetki kontrolü
        return rafRepository.uploadDocument(fileName, in);
    }
    
    public RafObject getRafObject( String id ) throws RafException{
        //FIXME: yetki kontrolü gerekli.
        //FIXME: event fırlatalım ki log yazılabilsin v.b.
        return rafRepository.getRafObject( id );
    }
    
    public InputStream getDocumentContent( String id ) throws RafException{
        return rafRepository.getDocumentContent( id );
    }
    
    public void saveMetadata( String id, RafMetadata data )throws RafException{
        //FIXME: Yetki kontrolü + event
        rafRepository.saveMetadata(id, data);
    }
    
    /**
     * RafObject üzerinde bulunan title, info v.b. alanları günceller.
     * @param data
     * @throws RafException 
     */
    public void saveProperties( RafObject data )throws RafException{
        //FIXME: Yetki kontrolü + event
        rafRepository.saveProperties(data);
    }
    
    public void deleteObject( RafObject data )throws RafException{
        //FIXME: Yetki kontrolü
        deleteObject(data.getId());
    }
    
    public void deleteObject( String id )throws RafException{
        //FIXME: Yetki kontrolü
        rafRepository.deleteObject(id);
    }
    
    public void copyObject( List<RafObject> from, RafFolder to ) throws RafException{
        //FIXME: yetki kontrolü
        rafRepository.copyObject(from, to);
    }
    
    public void moveObject( List<RafObject> from, RafFolder to ) throws RafException{
        //FIXME: yetki kontrolü
        rafRepository.moveObject(from, to);
    }
}
