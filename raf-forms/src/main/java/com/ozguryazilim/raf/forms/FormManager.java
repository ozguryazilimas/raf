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
import java.util.List;
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
                        deployForms( inputStream );
                    }
                }).scan();
        
    }

    public void deployForms(InputStream inputStream){
        List<Form> frms = FormXmlParser.parse(inputStream);
        for( Form f : frms ){
            formMap.put(f.getFormKey(), f);
            LOG.info("Form deployed : {}", f);
        }
    }
    
    public Form getForm( String formKey ){
        //FIXME: Field üzerinde data bağlantısı olduğu için aslında her istenildiğinde ( daha doğrusu oturum başına ) yeni bir instance üretmek lazım. Clone?
        return formMap.get(formKey);
    }
}
