/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ozguryazilim.raf;

import com.ozguryazilim.raf.jcr.RafRepository;
import com.ozguryazilim.raf.models.RafFolder;
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
    private RafRepository rafRepository;
    
    
    public List<RafFolder> getFolderList( String raf ) throws RafException{
        return rafRepository.getFolderList(raf);
    }
}
