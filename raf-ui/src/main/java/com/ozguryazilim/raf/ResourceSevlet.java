package com.ozguryazilim.raf;

import com.ozguryazilim.raf.models.RafDocument;
import org.apache.commons.io.IOUtils;
import org.apache.deltaspike.core.api.provider.BeanProvider;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

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
            
            RafDocument doc = (RafDocument) service.getRafObject(resourceId);

            InputStream is;

            if(req.getPathInfo().endsWith("thumbnail")){
                is = service.getThumbnailContent(resourceId);
                resp.setContentType("image/png");
            }  else if(req.getPathInfo().endsWith("preview")){
                is = service.getPreviewContent(resourceId);
                resp.setContentType(doc.getPreviewMimeType());
            } else {
                is = service.getDocumentContent(resourceId);
                resp.setContentType(doc.getMimeType());
            }

            resp.setHeader("Content-disposition", "inline;filename=" + doc.getName());

            try (OutputStream out = resp.getOutputStream()) {
                IOUtils.copy(is, out);

                out.flush();
            }
            
        } catch (RafException ex) {
            Logger.getLogger(ResourceSevlet.class.getName()).log(Level.SEVERE, null, ex);
        }
        
    }
    
    
    
}
