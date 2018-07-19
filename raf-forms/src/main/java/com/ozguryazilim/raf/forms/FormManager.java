/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ozguryazilim.raf.forms;

import com.ozguryazilim.raf.forms.model.Form;
import io.github.lukehutch.fastclasspathscanner.FastClasspathScanner;
import io.github.lukehutch.fastclasspathscanner.matchprocessor.FileMatchProcessor;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Sistemde tanımlı form bilgilerini, bunların deployment süreçlerini v.b. yönetir.
 * 
 * @author Hakan Uygun
 */
@ApplicationScoped
public class FormManager {
    
    private static final Logger LOG = LoggerFactory.getLogger(FormManager.class);
    
    private Map<String,Form> formMap = new HashMap<>();
    
    @PostConstruct
    public void init(){
        //İlk defa çağırılıyor. Classpath altında bulunan frm.xml'leri toparlayalım.
        
        new FastClasspathScanner()
                .matchFilenameExtension("frm.xml", new FileMatchProcessor() {
                    @Override
                    public void processMatch(String relativePath, InputStream inputStream, long lengthBytes) throws IOException {
                        LOG.info("Deploying classpath form : {}", relativePath);
                        deployForm( inputStream );
                    }
                }).scan();
        
    }

    public void deployForm(InputStream inputStream){
        Form frm = FormXmlParser.parse(inputStream);
        formMap.put(frm.getFormKey(), frm);
        LOG.info("Form deployed : {}", frm);
    }
    
    public Form getForm( String formKey ){
        return formMap.get(formKey);
    }
}
