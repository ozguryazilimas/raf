/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ozguryazilim.raf;

import com.google.common.base.Strings;
import java.io.IOException;
import java.io.InputStream;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.FileItemIterator;
import org.apache.commons.fileupload.FileItemStream;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.fileupload.util.Streams;
import org.apache.deltaspike.core.api.provider.BeanProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author oyas
 */
@WebServlet(urlPatterns = "/upload")
public class UploadServlet extends HttpServlet {

    private static final Logger LOG = LoggerFactory.getLogger(UploadServlet.class);

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        //FIXME: Yetki kontrolü sırasında dikkat!
        //Eğer işlem checkin ise RAF code olarak CHECKIN geliyor!
        //Yetki kontrolü için aslında path kontrolü yapmak daha temiz olacak sanırım. Yani raf code kısmı biraz sorunlu!
        
        //FIXME: Yetki kontrolü yapılacak. Hem login hem raf için
        LOG.debug("Upload Servlet Start");

        boolean isMultipart = ServletFileUpload.isMultipartContent(req);

        if (isMultipart) {
            ServletFileUpload upload = new ServletFileUpload();

            try {
                UploadRequest ur = new UploadRequest();
                FileItemFactory fileItemFactory = new DiskFileItemFactory();

                // Parse the request
                FileItemIterator iter = upload.getItemIterator(req);
                while (iter.hasNext()) {
                    FileItemStream item = iter.next();
                    String name = item.getFieldName();

                    if (item.isFormField()) {
                        InputStream stream = item.openStream();

                        String value = Streams.asString(stream);
                        LOG.debug("Form field {} with value {} detected.", name, value);

                        switch (name) {
                            case "raf":
                                ur.setRaf(value);
                                break;
                            case "rafPath":
                                ur.setRafPath(value);
                                break;
                            case "qquuid":
                                ur.setUuid(value);
                                break;
                            case "qqfilename":
                                ur.setFileName(value);
                                break;
                            case "qqpartindex":
                                ur.setPartIndex(Integer.parseInt(value));
                                break;
                            case "qqpartbyteoffset":
                                ur.setPartByteOffset(Integer.parseInt(value));
                                break;
                            case "qqchunksize":
                                ur.setChunkSize(Integer.parseInt(value));
                                break;
                            case "qqtotalparts":
                                ur.setTotalParts(Integer.parseInt(value));
                                break;
                            case "qqtotalfilesize":
                                ur.setTotalSize(Integer.parseInt(value));
                                break;
                        }

                    } else {
                        LOG.debug("File field {} with file name {} detected.", name, item.getName());
                        // Process the input stream
                        FileItem fileItem = fileItemFactory.createItem(item.getFieldName(),
                                item.getContentType(),
                                item.isFormField(),
                                item.getName());
                        Streams.copy(item.openStream(), fileItem.getOutputStream(), true);
                        ur.setData(fileItem);
                    }

                }

                //Gelen request chunk ise bir kenara alalım yoksa raf'a gönderelim
                if (ur.isChunkRequest()) {
                    //Storage servisine

                    ChunkStorage storage = new ChunkStorage();
                    storage.save(ur);
                } else {
                    //RAF'a yazmaya
                    LOG.debug("{} dosyasını RAF'a yerleştiriyoruz.", ur.getFileName());

                    //Eğer check in işlemi ise doğrudan path'i kullanıyoruz.
                    if ("CHECKIN".equals(ur.getRaf())) {
                        checkinToRaf(ur.getRafPath(), ur.getData().getInputStream());
                    } else {
                        uploadToRaf(ur.getRafPath() + "/" + ur.getFileName(), ur.getData().getInputStream());
                    }
                }

                reponseWriter(req, resp, null);
            } catch (FileUploadException | RafException ex) {
                LOG.error("Upload Error", ex);
                reponseWriter(req, resp, ex.getLocalizedMessage());
            }
        } else {
            //Chunk Kapatma Mesajı
            LOG.debug("Request : {}", req.getParameterMap());

            //FIXME: yetki kontrolü
            String raf = req.getParameter("raf");
            String rafPath = req.getParameter("rafPath");
            String fileName = req.getParameter("qqfilename");
            String uuid = req.getParameter("qquuid");
            int totalParts = Integer.parseInt(req.getParameter("qqtotalparts"));
            int fileSize = Integer.parseInt(req.getParameter("qqtotalfilesize"));

            //Gelen değerleri parse edip Storage'a birleştirmesini söyleceğiz ardından gelen değeri RAF'a göndereceğiz
            ChunkStorage storage = new ChunkStorage();
            try {
                InputStream file = storage.mergeChunks(uuid, fileName, totalParts, fileSize);

                String path = rafPath + "/" + fileName;
                
                //CHECKIN ise farklı operasyon
                if ("CHECKIN".equals(raf)) {
                    checkinToRaf(rafPath, file);
                } else {
                    uploadToRaf(path, file);
                }

                //Ve geride bişi bırakmayalım
                storage.delete(uuid);

            } catch (RafException ex) {
                LOG.error("Hata", ex);
                reponseWriter(req, resp, ex.getLocalizedMessage());
            }
        }

    }

    protected void reponseWriter(HttpServletRequest req, HttpServletResponse resp, String error) throws IOException {
        resp.setContentType("text/plain");
        //resp.setStatus(SUCCESS_RESPONSE_CODE);
        if (Strings.isNullOrEmpty(error)) {
            resp.setStatus(HttpServletResponse.SC_OK);
            resp.getWriter().print("{\"success\": true}");
        } else {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().print("{\"error\": \"" + error + "\"}");
        }
    }

    protected void uploadToRaf(String path, InputStream file) throws RafException {
        RafService rafService = BeanProvider.getContextualReference(RafService.class, true);
        rafService.uploadDocument(path, file);
    }

    protected void checkinToRaf(String path, InputStream file) throws RafException {
        RafService rafService = BeanProvider.getContextualReference(RafService.class, true);
        rafService.checkin(path, file);
    }
}
