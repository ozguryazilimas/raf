/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ozguryazilim.raf.auth;

import java.io.InputStream;
import java.util.List;

/**
 * KJar yüklenirken içinden çıkan resourceları handle etmek için API
 * 
 * @author Hakan Uygun
 */
public interface KJarResourceHandler {
   
    /**
     * İsmi verilen resource handle edilecek mi?
     * @param fileName
     * @return 
     */
    boolean canHandle( String fileName );
    
    /**
     * Verilen input stream handle edilir.
     * @param is 
     */
    void handle( String kjarId, InputStream is );
    
    /**
     * Verilen isimle yüklenmiş resourceları undeploy eder.
     * @param kjarId 
     */
    void undeploy( String kjarId );
    
    List<RafAsset> getAssests(String kjarId);
}
