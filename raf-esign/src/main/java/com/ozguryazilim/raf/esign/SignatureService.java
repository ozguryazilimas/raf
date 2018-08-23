/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ozguryazilim.raf.esign;

import com.ozguryazilim.muhurdar.utils.SignWrapper;
import com.ozguryazilim.muhurdar.utils.ValidationUtil;
import com.ozguryazilim.raf.RafException;
import com.ozguryazilim.raf.RafService;
import com.ozguryazilim.raf.models.RafDocument;
import com.ozguryazilim.raf.models.RafObject;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tr.gov.tubitak.uekae.esya.api.cmssignature.CMSSignatureException;
import tr.gov.tubitak.uekae.esya.api.cmssignature.validation.SignedDataValidationResult;
import tr.gov.tubitak.uekae.esya.api.common.ESYAException;
import tr.gov.tubitak.uekae.esya.api.signature.SignatureValidationResult;
import tr.gov.tubitak.uekae.esya.api.signature.ValidationResultDetail;

/**
 *
 * @author oyas
 */
@ApplicationScoped
public class SignatureService {
    
    private static final Logger LOG = LoggerFactory.getLogger(SignatureService.class);
    
    @Inject
    private RafService rafService;
    
    
    public void getSignatureDetails( String id ) throws RafException, IOException, ESYAException, CMSSignatureException{
        RafObject object = rafService.getRafObject(id);
        if( object instanceof RafDocument ){
            RafDocument doc = (RafDocument) object;
            
            if( doc.getSigned()){
                
                InputStream sing = rafService.getSignatureContent(doc.getId());
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                IOUtils.copy(sing, baos);
                
                FileOutputStream fos = new FileOutputStream("/home/oyas/tmp/test.imz");
                ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
                IOUtils.copy(bais, fos);
                fos.close();
                
                SignWrapper signWrapper = new SignWrapper( baos.toByteArray());
                
                
                /*
                signWrapper.getCertificateContent();
                for( Signer s : signWrapper.getSignerList()){
                    s.getSignerInfo().getCertificateValues().getCertificates()
                }
                    */
                
                LOG.debug("Data : {}, {}", signWrapper.getCertificateContent(), signWrapper.getCertificateSerialNumber()); 
                LOG.debug("Singers : {}", signWrapper.getSignerList()); 
                LOG.debug("SingData : {}", signWrapper.getSignedData()); 
                
                SignedDataValidationResult sdvr = ValidationUtil.signedDataValidation(baos.toByteArray());
                
                sdvr.printDetails();

                for(SignatureValidationResult svr : sdvr.getSDValidationResults()){
                    LOG.debug("Val : {} - {}, {}", svr.getCheckMessage(), svr.getCheckResult(), svr.getResultType()); 
                    for( ValidationResultDetail vrd : svr.getDetails()){
                        LOG.debug("ValDet : {} - {}, {}", vrd.getCheckMessage(), vrd.getCheckResult(), vrd.getResultType()); 
                    }
                    
                }

                
                LOG.debug("Validation : {}", sdvr.toString());
            }
            
        }
        
        
    }
    
}
