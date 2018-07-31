/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ozguryazilim.raf.jbpm;

import com.ozguryazilim.raf.RafException;
import com.ozguryazilim.raf.RafService;
import com.ozguryazilim.raf.ResourceSevlet;
import com.ozguryazilim.raf.models.RafObject;
import java.io.ByteArrayInputStream;
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
 *
 * @author oyas
 */
@WebServlet(urlPatterns = "/bpmnDiagram/*")
public class BpmnDiagramServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        Subject currentUser = SecurityUtils.getSubject();
        if (!currentUser.isAuthenticated()) {
            //FIXME: Doğru HTTP hata kodunu dönelim
            //FIXME: Ayrıca talep edilen dosyaya erişim yetkisi var mı o da kontrol edilmeli.
            return;
        }

        String[] parts = req.getPathInfo().split("/");
        //FIXME: doğru değilse hata ver.
        String deploymentId = parts[1];
        String processId = parts[2];

        BpmnDiagramService service = BeanProvider.getContextualReference(BpmnDiagramService.class, true);

        String s = service.getBpmnDiagram(deploymentId, processId);

        InputStream is = new ByteArrayInputStream(s.getBytes());

        resp.setContentType("application/bpmn-xml");

        resp.setHeader("Content-disposition", "inline;filename=" + deploymentId + "." + processId + ".bpmn");
        //response.setContentLength((int) content.getProperty("jcr:data").getBinary().getSize());

        try (OutputStream out = resp.getOutputStream()) {
            IOUtils.copy(is, out);
            out.flush();
        }

    }
}
