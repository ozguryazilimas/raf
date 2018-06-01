/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ozguryazilim.raf.repo;

import com.ozguryazilim.raf.RafException;
import com.ozguryazilim.raf.RafRepository;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import org.modeshape.common.text.UrlEncoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author oyas
 */
public class RafModeshapeRepository implements RafRepository{

    private static final Logger LOG = LoggerFactory.getLogger(RafModeshapeRepository.class);
    
    private UrlEncoder encoder;
    
    @Override
    public void start() throws RafException {
        try {
            encoder = new UrlEncoder();
            encoder.setSlashEncoded(false);

            //Engine'de başlatılsın
            Session session = ModeShapeRepositoryFactory.getSession();
            session.logout();
        } catch (RepositoryException ex) {
            throw new RafException();
        }
    }

    @Override
    public void stop() throws RafException {
        ModeShapeRepositoryFactory.shutdown();
    }

}
