package com.ozguryazilim.raf.email;

import com.ozguryazilim.mutfak.kahve.Kahve;
import com.ozguryazilim.raf.RafException;
import com.ozguryazilim.raf.RafService;
import com.ozguryazilim.raf.encoder.RafEncoder;
import com.ozguryazilim.raf.encoder.RafEncoderFactory;
import com.ozguryazilim.raf.entities.RafDocumentComment;
import com.ozguryazilim.raf.entities.RafShare;
import com.ozguryazilim.raf.entities.UserFavorite;
import com.ozguryazilim.raf.enums.EmailNotificationActionType;
import com.ozguryazilim.raf.enums.EmailNotificationType;
import com.ozguryazilim.raf.favorite.UserFavoriteService;
import com.ozguryazilim.raf.models.RafObject;
import com.ozguryazilim.raf.utils.UrlUtils;
import com.ozguryazilim.telve.auth.Identity;
import com.ozguryazilim.telve.auth.UserInfo;
import com.ozguryazilim.telve.auth.UserService;
import com.ozguryazilim.telve.channel.email.EmailChannel;
import com.ozguryazilim.telve.messages.Messages;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.deltaspike.core.api.config.ConfigResolver;
import org.omnifaces.el.functions.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@ApplicationScoped
public class EmailNotificationService implements Serializable {

    private static final Logger LOG = LoggerFactory.getLogger(EmailNotificationService.class);

    @Inject
    private RafService rafService;

    @Inject
    private EmailChannel emailChannel;

    @Inject
    private UserFavoriteService favoriteService;

    @Inject
    private UserService userService;

    @Inject
    private Identity identity;

    @Inject
    private Kahve kahve;

    // Raf ana dizini
    private static final String RAF_ROOT_PATH = "/RAF";
    private static final String RAF_SHARED_ROOT_PATH = "/SHARED";
    private static final String RAF_PRIVATE_ROOF_PATH_PREFIX = "/PRIVATE/";

    // DB'den kullanıcı bildirim ayarlarını çekeceğimiz suffix
    private static final String KAHVE_NOTIFICATION_TYPE_SUFFIX = "::email.notification.type";

    // Varsayılan bildirim seçeneği
    private final EmailNotificationType defaultNotificationType = EmailNotificationType.valueOf(ConfigResolver.getPropertyValue("email.notification.default"));

    // Yönlendirme linki oluşturmak için kullanacağımız domain.
    private final String appLinkDomain = ConfigResolver.getPropertyValue("app.linkDomain");

    /**
     * Değişiklik yapılan obje üzerinde ve onun parent'larındaki kayıtlı favorilere eklemiş kullanıcıları bulur
     * ve güncelleme e-postası gönderir.
     *
     * @param object
     * @param actionType
     */
    public void sendEmailToFavorites(RafObject object, EmailNotificationActionType actionType) {
        List<UserInfo> consumers = findConsumersWhoAddedFavorites(object.getPath());
            if (!consumers.isEmpty()) {

            String link = appLinkDomain + "raf.jsf" + "?id=" + "&o=" + object.getId();
            String objectName = object.getName();
            String objectType = object.getClass().getSimpleName();
            LOG.debug("Sending email to Consumers:{} for the ObjectName:{} ActionType:{} ", consumers, objectName, actionType);

            //If SecurityManager is not present (and UserInfo is null), it's a system message
            if (Objects.isNull(identity.getUserInfo())) {
                sendSystemMessage(consumers, link, objectName, objectType, actionType);
            } else {
                String name = Strings.capitalize(identity.getUserInfo().getFirstName());
                String surname = Strings.capitalize(identity.getUserInfo().getLastName());
                sendMessage(consumers, name, surname, link, objectName, objectType, actionType);
            }
        }
    }

    public void sendEmailToSharedContacts(RafShare rafShare, String filename) {
        if (CollectionUtils.isNotEmpty(rafShare.getEmails())) {
            Map<String, Object> headers = new HashMap();
            headers.put("messageClass", "SHARE");
            headers.put("filename", filename);
            headers.put("sharedBy", userService.getUserName(rafShare.getSharedBy()));
            headers.put("link", UrlUtils.getDocumentShareURL(rafShare.getToken()));
            headers.put("password", rafShare.getPassword());
            headers.put("footerAppName", ConfigResolver.getPropertyValue("email.footer.app.name", "RAF"));
            headers.put("footerAppLink", ConfigResolver.getPropertyValue("app.link", ""));

            for (String consumerEmail : rafShare.getEmails()) {
                String subject = ConfigResolver.getPropertyValue("app.title")
                        + " - "
                        + Messages.getMessage("Sizinle Bir Dosya Paylaşıldı.");
                emailChannel.sendMessage(consumerEmail, subject, "", headers);
            }
        }
    }

    public void sendEmailToSharedContacts(List<RafShare> rafShares, List<String> filenames) {
        if (!rafShares.isEmpty() && CollectionUtils.isNotEmpty(rafShares.get(0).getEmails())) {
            String sharedBy = rafShares.get(0).getSharedBy();
            String password = rafShares.get(0).getPassword();
            List<String> links = rafShares.stream().map(item -> UrlUtils.getDocumentShareURL(item.getToken())).collect(Collectors.toList());

            List<String> shareList = IntStream.range(0, filenames.size())
                    .collect(HashMap::new, (m, i) -> m.put(filenames.get(i), links.get(i)), Map::putAll)
                    .entrySet().stream()
                    .map(entry -> String.format("\"%s\": %s", entry.getKey(), entry.getValue()))
                    .collect(Collectors.toList());

            Map<String, Object> headers = new HashMap();
            headers.put("messageClass", "MULTIPLE_SHARE");
            headers.put("shareList", shareList);
            headers.put("sharedBy", userService.getUserName(sharedBy));
            headers.put("password", password);
            headers.put("footerAppName", ConfigResolver.getPropertyValue("email.footer.app.name", "RAF"));
            headers.put("footerAppLink", ConfigResolver.getPropertyValue("app.link", ""));

            for (String consumerEmail : rafShares.get(0).getEmails()) {
                String subject = String.format("%s - Sizinle %d Dosya Paylaşıldı.", ConfigResolver.getPropertyValue("app.title"), rafShares.size());
                emailChannel.sendMessage(consumerEmail, subject, "", headers);
            }
        }
    }

    public void sendNewDocumentCommentEmailToUsers(RafDocumentComment rafDocumentComment, List<UserInfo> users) throws RafException {
        RafObject document = rafService.getRafObject(rafDocumentComment.getNodeId());
        String commentOwnerUserName = userService.getUserName(rafDocumentComment.getCommentOwner());
        String link = appLinkDomain + "raf.jsf" + "?id=" + "&o=" + document.getId();

        Map<String, Object> headers = new HashMap<>();
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy hh:mm:ss");

        headers.put("messageClass", "NEW_DOCUMENT_COMMENT");

        headers.put("commentedDocument", document);
        headers.put("commentOwner", commentOwnerUserName);
        headers.put("commentDate", dateFormat.format(rafDocumentComment.getDate()));
        headers.put("comment", rafDocumentComment.getComment());
        headers.put("documentLink", link);

        headers.put("footerAppName", ConfigResolver.getPropertyValue("email.footer.app.name", "RAF"));
        headers.put("footerAppLink", ConfigResolver.getPropertyValue("app.link", ""));

        String subject = String.format(
                "%s - %s isimli dosyaya %s tarafından yeni yorum eklendi.",
                ConfigResolver.getPropertyValue("app.title"),
                document.getTitle(),
                userService.getUserName(commentOwnerUserName)
        );

        users.forEach(user ->
                emailChannel.sendMessage(user.getEmail(), subject, "", headers)
        );
    }

    /**
     * Object path'ten başlayarak yukarı doğru pathleri tarayarak favorilere eklemiş tüm kullanıcı e-postalarını toplar.
     *
     * @param path
     * @return
     */
    private List<UserInfo> findConsumersWhoAddedFavorites(String path) {
        if (path == null || path.isEmpty()) {
            return new ArrayList<>();
        }

        //Private raf path
        RafEncoder rafEncoder = RafEncoderFactory.getRafNameEncoder();
        String privateRootPath = "";
        if (identity.getLoginName() != null)  {
            privateRootPath = RAF_PRIVATE_ROOF_PATH_PREFIX + rafEncoder.encode(identity.getLoginName());
        }

        List<UserInfo> users = new ArrayList<>();
        while (!(path.equals(RAF_ROOT_PATH) || path.equals(RAF_SHARED_ROOT_PATH) || path.equals(privateRootPath) || StringUtils.isEmpty(path))) {
            List<UserFavorite> userFavoriteList = favoriteService.getFavoritesByPath(path);
            for (UserFavorite u : userFavoriteList) {
                // Kullanıcı bazlı bildirim ayarını kontrol ediyoruz.
                EmailNotificationType notificationType = kahve.get(u.getUsername() + KAHVE_NOTIFICATION_TYPE_SUFFIX,
                        EmailNotificationType.DEFAULT.name()).getAsEnum(EmailNotificationType.class);
                if (notificationType.equals(EmailNotificationType.ONLY_FAVORITE) || (notificationType.equals(EmailNotificationType.DEFAULT) && defaultNotificationType.equals(EmailNotificationType.ONLY_FAVORITE))) {
                    UserInfo userInfo = userService.getUserInfo(u.getUsername());
                    if (isUserActiveAndValidEmailAddress(users, userInfo)) {
                        users.add(userInfo);
                    }
                }
            }
            // Son '/' ifadesinden sonrakileri siliyoruz.
            int endIndex = path.lastIndexOf("/");
            if (endIndex != -1) {
                path = path.substring(0, endIndex);
            }
        }
        return users;
    }

    /**
     * Is user in users list has valid email address and is active
     * @param users Current users list
     * @param userInfo relevant user
     * @return True if user is active and has valid email address
     */
    private boolean isUserActiveAndValidEmailAddress(List<UserInfo> users, UserInfo userInfo) {
        return Boolean.TRUE.equals(userInfo.getActive()) &&
                users.stream().noneMatch(user -> user.getEmail().equals(userInfo.getEmail())) &&
                userInfo.getEmail() != null &&
                !userInfo.getEmail().isEmpty();
    }

    /**
     * @param consumers  -> e-posta gönderilecek olan liste (Consumer List)
     * @param name       -> değişiklik yapan kullanıcının ismi (Producer Name)
     * @param surname    -> değişiklik yapan kullanıcının soyismi (Producer Surname)
     * @param link       -> e-posta üzerinde bağlantı verilecek link
     * @param objectName -> değişiklik yapılan obje ismi
     * @param objectType -> değişiklik yapılan objenin tipi. Klasör ya da Belge olabilir.
     * @param actionType -> ne yapıldı? Örn: ADD, UPDATE, REMOVE
     */
    private void sendMessage(List<UserInfo> consumers,
                             String name,
                             String surname,
                             String link,
                             String objectName,
                             String objectType,
                             EmailNotificationActionType actionType
    ) {
        Map<String, Object> headers = new HashMap();
        headers.put("messageClass", "FAVORITE");
        headers.put("name", name);
        headers.put("surname", surname);
        headers.put("objectName", objectName);
        headers.put("objectType", objectType);
        headers.put("link", link);
        headers.put("notificationSettingsLink", appLinkDomain + "options.jsf");
        headers.put("actionType", actionType);

        for (UserInfo consumer : consumers) {
            headers.put("consumer", consumer);
            String subject = ConfigResolver.getPropertyValue("app.title")
                    + " - "
                    + Messages.getMessage("email.subject.favorites");
            emailChannel.sendMessage(consumer.getEmail(), subject, "", headers);
        }

    }

    /**
     * @param consumers  -> e-posta gönderilecek olan liste (Consumer List)
     * @param link       -> e-posta üzerinde bağlantı verilecek link
     * @param objectName -> değişiklik yapılan obje ismi
     * @param objectType -> değişiklik yapılan objenin tipi. Klasör ya da Belge olabilir.
     * @param actionType -> ne yapıldı? Örn: ADD, UPDATE, REMOVE
     */
    private void sendSystemMessage(List<UserInfo> consumers,
                             String link,
                             String objectName,
                             String objectType,
                             EmailNotificationActionType actionType
    ) {
        sendMessage(consumers, "SYSTEM", "", link, objectName, objectType, actionType);
    }

}