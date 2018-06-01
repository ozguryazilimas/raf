/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ozguryazilim.raf.web;

import com.ozguryazilim.raf.RafException;
import com.ozguryazilim.raf.RafRepository;
import com.ozguryazilim.raf.repo.RafModeshapeRepository;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.RequestScoped;
import javax.enterprise.inject.Produces;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author oyas
 */
@ApplicationScoped
public class RafRepositoryProducer {
    
    private static final Logger LOG = LoggerFactory.getLogger(RafRepositoryProducer.class);
    
    @Produces
    @RequestScoped
    public RafRepository produceRafRepository(){
        RafRepository repo = new RafModeshapeRepository();
        try {
            repo.start();
        } catch (RafException ex) {
            LOG.error("Repository cannot initialized!", ex);
        }
        return repo;
    }
    
}
