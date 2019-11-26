package com.ozguryazilim.raf.webdav.ui;

import com.ozguryazilim.raf.ui.base.AbstractAction;
import com.ozguryazilim.raf.ui.base.Action;
import com.ozguryazilim.raf.ui.base.ActionCapability;
import java.io.IOException;
import java.util.Arrays;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author oyas
 */
@Action(icon = "fa-edit",
        capabilities = {ActionCapability.DetailViews},
        excludeMimeType = "raf/folder")
public class OfficeEditAction extends AbstractAction {

    private static final Logger LOG = LoggerFactory.getLogger(OfficeEditAction.class);

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

    private String getWebDavProtoForOS(HttpServletRequest request) {
        String os = getOSFromUserAgent(request);
        String webDavProto = "vnd.sun.star.webdav://";
        if (os.equals("Windows")) {
            webDavProto = getMSOfficeWebDavProtoForMT();
        } else if (os.equals("Unix")) {
            if ("https".equals(request.getProtocol())) {
                webDavProto = "vnd.sun.star.webdavs://";
            }
        }
        return webDavProto;
    }

    @Override
    protected boolean finalizeAction() {

        String projectPath = FacesContext.getCurrentInstance().getExternalContext().getRequestContextPath();

        HttpServletRequest request = (HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest();

        //FIXME: aslında burada protokol isimlerini configden alsak iyi olacak MSOffice için bu değerler farklı olabilir.
        String webDavProto = getWebDavProtoForOS(request);

        webDavProto = webDavProto + request.getServerName() + ":" + request.getServerPort();
        String webDavRequestPath = buildWebDAvRequestPath(getContext().getSelectedObject().getPath());

        HttpServletResponse response = (HttpServletResponse) FacesContext.getCurrentInstance().getExternalContext().getResponse();
        try {
            String redirectTo = webDavProto + projectPath + "/webdav" + webDavRequestPath;
            LOG.debug("WebDAV redirection : {}", redirectTo);
            response.sendRedirect(redirectTo);
        } catch (IOException ex) {
            LOG.error("WebDAV IO Exption", ex);
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
