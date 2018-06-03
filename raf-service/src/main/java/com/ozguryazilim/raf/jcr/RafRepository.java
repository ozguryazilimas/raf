/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ozguryazilim.raf.jcr;

import com.ozguryazilim.raf.RafException;
import com.ozguryazilim.raf.entities.RafDefinition;
import com.ozguryazilim.raf.models.RafFolder;
import com.ozguryazilim.raf.models.RafNode;
import java.util.List;

/**
 * RAF Repository için temel API.
 * 
 * CDI ile birlikte kullanmak için bir Producer kullanılarak doğru IMPL'den bir tane üretmek gerekir.
 * 
 * @author Hakan Uygun
 */
public interface RafRepository {
 
    /**
     * Repository init olması için çağrılır.
     * 
     * @throws RafException 
     */
    void start() throws RafException;
    
    /**
     * Repository kapatılması için çağrılır.
     * 
     * @throws RafException 
     */
    void stop() throws RafException;
    
    /**
     * Verilen deifiniton üzerindeki bilgileri kullanarak yeni bir raf nodu oluşturur.
     * @param definition
     * @return 
     */
    RafNode createRafNode( RafDefinition definition ) throws RafException;
    
    /**
     * Code ile verilen raf nodunu döndürür. Yoksa null döner.
     * @param code
     * @return 
     */
    RafNode getRafNode( String code ) throws RafException;
    
    /**
     * Kullanıcının kişisel raf nodunu döndürür. Yoksa oluşturur.
     * 
     * @param username
     * @return 
     */
    RafNode getPrivateRafNode( String username ) throws RafException;
    
    /**
     * Ortak kullanılan RafNodu'nu döndürür. Yoksa oluşturur.
     * @return 
     */
    RafNode getSharedRafNode() throws RafException;
    
    /**
     * Verilen RafNode için Folder listesini döndürür. 
     * 
     * Folder'lar ağaç olmasına rağmen sadece parentId ile dolu liste döner. Ağaç formunda değil.
     * 
     * @param rafNode
     * @return
     * @throws RafException 
     */
    List<RafFolder> getFolderList( RafNode rafNode ) throws RafException;
    
    /**
     * Verilen RafNode code için Folder listesini döndürür. 
     * 
     * Folder'lar ağaç olmasına rağmen sadece parentId ile dolu liste döner. Ağaç formunda değil.
     * 
     * @param rafNode
     * @return
     * @throws RafException 
     */
    List<RafFolder> getFolderList( String rafCode ) throws RafException;
}
