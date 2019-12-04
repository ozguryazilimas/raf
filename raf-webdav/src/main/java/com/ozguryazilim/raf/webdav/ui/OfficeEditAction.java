package com.ozguryazilim.raf.webdav.ui;

import com.ozguryazilim.raf.RafException;
import com.ozguryazilim.raf.RafService;
import com.ozguryazilim.raf.ui.base.AbstractAction;
import com.ozguryazilim.raf.ui.base.Action;
import com.ozguryazilim.raf.ui.base.ActionCapability;
import com.ozguryazilim.telve.auth.Identity;
import com.ozguryazilim.telve.messages.FacesMessages;
import java.io.IOException;
import java.util.Arrays;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.primefaces.PrimeFaces;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author oyas
 */
@Action(icon = "fa-edit",
        capabilities = {ActionCapability.DetailViews},
        excludeMimeType = "raf/folder", includedMimeType = "application/vnd,application/ms")
public class OfficeEditAction extends AbstractAction {

    private static final Logger LOG = LoggerFactory.getLogger(OfficeEditAction.class);

    @Inject
    RafService rafService;

    @Inject
    Identity identity;

    private String getOSFromUserAgent(HttpServletRequest request) {
        String os = "Windows";
        String userAgent = request.getHeader("User-Agent").toLowerCase();
        if (userAgent.contains("windows")) {
            os = "Windows";
        } else if (userAgent.contains("mac")) {
            os = "Mac";
        } else if (userAgent.contains("x11")) {
            os = "Unix";
        } else if (userAgent.contains("android")) {
            os = "Android";
        } else if (userAgent.contains("iphone")) {
            os = "IPhone";
        } else {
            os = "UnKnown, More-Info: " + userAgent;
        }
        return os;
    }

    private String getMSOfficeWebDavProtoForMT() {
        String mt = getContext().getSelectedObject().getMimeType().toLowerCase();
        String webDavProto;
        if (Arrays.asList("application/vnd.openxmlformats-officedocument.wordprocessingml.document", "application/msword", "application/vnd.openxmlformats-officedocument.wordprocessingml.template").contains(mt)) {
            webDavProto = "ms-word:ofe|u|";
        } else if (Arrays.asList("application/vnd.ms-excel", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", "application/vnd.openxmlformats-officedocument.spreadsheetml.template").contains(mt)) {
            webDavProto = "ms-excel:ofe|u|";
        } else if (Arrays.asList("application/vnd.ms-powerpoint", "application/vnd.openxmlformats-officedocument.presentationml.presentation", "application/vnd.openxmlformats-officedocument.presentationml.template").contains(mt)) {
            webDavProto = "ms-powerpoint:ofe|u|";
        } else {
            webDavProto = "";
        }
        return webDavProto;
    }

    private String getWebDavProtoForOS(String os, HttpServletRequest request) {
        String webDavProto = "vnd.sun.star.webdav://";
        if ("Windows".equals(os)) {
            webDavProto = getMSOfficeWebDavProtoForMT();
            String protocol = "HTTP/1.1".equals(request.getProtocol()) ? "http:///" : "https:///";
            webDavProto = webDavProto + protocol + request.getServerName() + ":" + request.getServerPort();
        } else if ("Unix".equals(os)) {
            if ("https".equals(request.getProtocol())) {
                webDavProto = "vnd.sun.star.webdavs://";
            }
            webDavProto = webDavProto + request.getServerName() + ":" + request.getServerPort();
        }
        return webDavProto;
    }

    @Override
    protected boolean finalizeAction() {

        LOG.debug("Check in control: {}", getContext().getSelectedObject().getName());

        try {
            String filePath = getContext().getSelectedObject().getPath();
            String checkerUser = rafService.getRafCheckerUser(filePath);
            if (rafService.getRafCheckStatus(filePath) && !identity.getUserName().equals(checkerUser)) {
                FacesMessages.error("Dosya başka bir kullanıcı tarafından kilitlenmiş.", String.format("Kilitleyen kullanıcı : %s", checkerUser)); //FIXME : i118
            } else {
                String projectPath = FacesContext.getCurrentInstance().getExternalContext().getRequestContextPath();

                HttpServletRequest request = (HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest();
                String os = getOSFromUserAgent(request);
                //FIXME: aslında burada protokol isimlerini configden alsak iyi olacak MSOffice için bu değerler farklı olabilir.
                String webDavProto = getWebDavProtoForOS(os, request);
                String webDavRequestPath = buildWebDAvRequestPath(filePath);

                HttpServletResponse response = (HttpServletResponse) FacesContext.getCurrentInstance().getExternalContext().getResponse();
                try {
                    String redirectTo = webDavProto + projectPath + "/webdav" + webDavRequestPath;
                    LOG.debug("WebDAV redirection : {}", redirectTo);
                    if ("Windows".equals(os)) {
                        String script = "location.href='".concat(redirectTo).concat("'");
                        PrimeFaces.current().executeScript(script);
                    } else {
                        response.sendRedirect(redirectTo.replace("///", "//"));
                    }
                } catch (IOException ex) {
                    LOG.error("WebDAV IO Exption", ex);
                }
            }
        } catch (RafException ex) {
            LOG.error("RafException", ex);
        }
        return super.finalizeAction();
    }

    protected String buildWebDAvRequestPath(String path) {

        //Eğer private ile başlıyorsa bir sonraki kullanıcı adı. Silelim.
        if (path.startsWith("/PRIVATE")) {
            //Kullanıcı adının başı
            int i = path.indexOf("/", 2);
            int j = path.indexOf("/", i + 1);
            return "/PRIVATE" + path.substring(j);
        } else if (path.startsWith("/RAF")) {
            //Raf ile başlıyor ise RAF kısmını silsek yeterli
            return path.substring(4);
        }

        //Geri kalan durumlar için eldeki path'i dönelim
        return path;

    }

}
