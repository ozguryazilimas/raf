/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ozguryazilim.raf.esign;

import com.google.common.base.Strings;
import com.ozguryazilim.raf.RafException;
import com.ozguryazilim.raf.RafService;
import com.ozguryazilim.raf.models.RafObject;
import com.ozguryazilim.raf.models.RafSignature;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;
import org.apache.commons.io.IOUtils;
import org.apache.deltaspike.core.api.provider.BeanProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author oyas
 */
@WebServlet(name = "muhurdarServlet", urlPatterns = "/muhurdar")
@MultipartConfig(location = "/tmp/muhurdar")
public class MuhurdarServlet extends HttpServlet {

    private static final Logger LOG = LoggerFactory.getLogger(MuhurdarServlet.class);

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        LOG.info("Muhurdar Servlet Called");

        String r = req.getParameter("mhr");
        String c = req.getParameter("cancel");
        if (!Strings.isNullOrEmpty(r)) {
            String result = "";

            //Response olarak mhr dosyası gönderilecek.
            //Dolayısı ile diğer contentId paramları da lazım .
            String ci = req.getParameter("contentId");
            
            //Eğer ci boş ise yani contentId boş ise hata verelim
            if( Strings.isNullOrEmpty(ci)){
                LOG.error("Invalid ContentId : {}", ci);
                resp.sendError(HttpServletResponse.SC_NOT_FOUND);
                return;
            }
            
            try {
                RafObject rafObject = getRafObject(ci);
                String token = TokenManager.instance().getToken(ci);
                result = buildMhrFile(rafObject, token);
            } catch (RafException ex) {
                LOG.error("Raf Service Error", ex);
                resp.sendError(HttpServletResponse.SC_NOT_FOUND);
                return;
            }

            resp.setContentType("application/muhurdar");
            PrintWriter w = resp.getWriter();
            w.print(result);

        } else if (!Strings.isNullOrEmpty(c)) {
            //İmzalama işlemi cancel ediliyor.

            //Token kontrolü
            String t = req.getParameter("token");
            if (!Strings.isNullOrEmpty(t)) {
                if (!TokenManager.instance().isTokenValid(t)) {
                    LOG.warn("Invalid Token {}", t);
                    resp.sendError(HttpServletResponse.SC_UNAUTHORIZED);
                    return;
                }

                //Token bir daha kullanılmasın
                TokenManager.instance().invalidateToken(t);
            }

        } else {
            //content imzalanacak içerik isteniyor. Hadi token kontrolü ile verelim...
            String t = req.getParameter("token");
            if (!Strings.isNullOrEmpty(t)) {
                if (!TokenManager.instance().isTokenValid(t)) {
                    LOG.warn("Invalid Token {}", t);
                    resp.sendError(HttpServletResponse.SC_UNAUTHORIZED);
                    return;
                }

                //Token valid. Şimdi de dosyayı döndüreceğiz
                
                InputStream is;
                try {
                    //TODO: Content id yoksa da provider hata verecek
                    String ci = (String) TokenManager.instance().getTokenData(t);
                    
                    is = getRafService().getDocumentContent(ci);
                } catch (RafException ex) {
                    LOG.error("Document read error", ex);
                    resp.sendError(HttpServletResponse.SC_NOT_FOUND);
                    return;
                }

                //resp.setContentType();
                OutputStream os = resp.getOutputStream();
                IOUtils.copy(is, os);
                return;
            }

            LOG.warn("Token not provided");
            resp.sendError(HttpServletResponse.SC_UNAUTHORIZED);
            return;

        }

    }

    
    /**
     * İmzalı içerik alınacak.
     * 
     * TODO: Burada aslında imza atma yöntemine bağlı olarak UploadServlet üzerinde olduğu gibi büyük dosya alma yöntemleri düşünülebilir!
     * Eğer imza zarf olarak değil de sadece imza kısmı alınacak ise dert olmayacaktır.
     * Fakat PAdES imza'da bu da bir problem olabilir. Çünkü o zaman PDF'in kendisi geri gelecek imzalı olarak.
     * 
     * @param req
     * @param resp
     * @throws ServletException
     * @throws IOException 
     */
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        LOG.debug("{} {} {}", req.getMethod(), req.getPathInfo(), req.getContentType());

        //Token kontrolü
        String t = req.getParameter("token");
        if (!Strings.isNullOrEmpty(t)) {
            if (!TokenManager.instance().isTokenValid(t)) {
                resp.sendError(HttpServletResponse.SC_UNAUTHORIZED);
                return;
            }
        } else {
            resp.sendError(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }
        
        //Token var ve geçerli. Şimdi dosyayı alalım ve temp bir yere koyalım
        //String description = req.getParameter("description"); 
        Part filePart = req.getPart("file");
        if (filePart == null) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "ERROR: Please Use multipart post with file");
            return;
        }
        //Dosya ismi olarak token id'yi kullanalım
        String fileName = t;//filePart.getSubmittedFileName();
        LOG.debug("File Name : {} - {}", fileName, filePart.getSize());
        
        try {
            RafSignature signature = new RafSignature();
            //FIXME: burada aslında imza tipi ve formatı ile ilgili bir dert var. PAdES ise ne olacak? Format hep BES orası ok.
            signature.setType("CAdES");
            signature.setFormat("BES");
            signature.setStatus("NOT_VALIDATED");
            
            String id = (String) TokenManager.instance().getTokenData(t);
            
            getRafService().saveSignature(id, signature, filePart.getInputStream());
            
            //Token bir daha kullanılmasın
            TokenManager.instance().invalidateToken(t);
            
            //Ve artık cevap
            resp.sendError(HttpServletResponse.SC_OK, "File upload completed");
            LOG.debug("Signature saved");
        } catch (FileNotFoundException fne) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "You either did not specify a file to upload");
            LOG.error("Problems during file upload.", fne);
        } catch (RafException ex) {
            resp.sendError(HttpServletResponse.SC_SERVICE_UNAVAILABLE, "Unexpected Error");
            LOG.error("Raf Exeption", ex);
        } finally {
            //Şimdi tmp file'ı siliyoruz
        }


    }
    
    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        LOG.info("Put Request");
        resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "ERROR: Please Use multipart post");
    }

    

    private String buildMhrFile(RafObject rafObject, String token) {

        StringBuilder sb = new StringBuilder();

        //FIXME: URL conf dosyasından alınacak
        sb.append("input=").append("http://localhost:8080/dolap/muhurdar").append("\n");
        sb.append("output=").append("http://localhost:8080/dolap/muhurdar").append("\n");
        sb.append("subject=").append(rafObject.getTitle()).append("\n");
        //FIXME: bu prodda CARD olmalı. Bunu ConfigResolver'dan alabiliriz. ProjectStage ile mesela
        sb.append("signer=").append("PFX").append("\n");
        //FIXME: bundan pek emin değilim? CAdES olmalı aslında ama orada da zarf değil de sadece imza olmalı sanırım. PAdES desteği de aslında gerekiyor.
        sb.append("type=").append("CAdES").append("\n");
        //FIXME: bu da bir başka dert aslında. Word, Excell gibi şeylerin imzalanması bir sorun bence.
        sb.append("slient=").append("true").append("\n");
        sb.append("token=").append(token).append("\n");
        //CAdES İmza atılırken veri ile imzayı birleştirme. 
        sb.append("format=").append("DETACHED").append("\n");
        sb.append("base64=").append("true").append("\n");
        

        return sb.toString();
    }

    private RafObject getRafObject(String id) throws RafException {
        RafService rafService = getRafService();

        return rafService.getRafObject(id);
    }
    
    private RafService getRafService(){
        return BeanProvider.getContextualReference(RafService.class, true);
    }
    
    
}
