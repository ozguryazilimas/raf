/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ozguryazilim.raf;

import com.ozguryazilim.raf.models.RafObject;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.io.IOUtils;
import org.apache.deltaspike.core.api.provider.BeanProvider;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;

/**
 * Preview v.b. de kullanmak üzere resource servlet.
 * 
 * FIXME: yetki kontrolü yapılacak. Aksi halde tüm bilgi buradan dışarı sızar.
 * 
 * FIXME: memory kullanımı için optimizasyon lazım
 * 
 * FIXME: LOG'lama lazım, Event ne yazık ki fırlatamayız gibi duruyor.
 * 
 * @author Hakan Uygun
 */
@WebServlet(urlPatterns = "/resource/*")
public class ResourceSevlet extends HttpServlet{

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        
        Subject currentUser = SecurityUtils.getSubject();
        if( !currentUser.isAuthenticated() ){
            //FIXME: Doğru HTTP hata kodunu dönelim
            //FIXME: Ayrıca talep edilen dosyaya erişim yetkisi var mı o da kontrol edilmeli.
            return;
        }
        
        String[] parts = req.getPathInfo().split("/");
        
        //FIXME: doğru değilse hata ver.
        String resourceId = parts[1];
        
        RafService service = BeanProvider.getContextualReference(RafService.class, true);
        
        try {
            
            RafObject doc = service.getRafObject(resourceId);
            
            InputStream is = service.getDocumentContent( resourceId );
            
            resp.setContentType(doc.getMimeType());

            resp.setHeader("Content-disposition", "inline;filename=" + doc.getName());
            //response.setContentLength((int) content.getProperty("jcr:data").getBinary().getSize());

            try (OutputStream out = resp.getOutputStream()) {
                IOUtils.copy(is, out);

                out.flush();
            }
            
        } catch (RafException ex) {
            Logger.getLogger(ResourceSevlet.class.getName()).log(Level.SEVERE, null, ex);
        }
        
    }
    
    
    
}
