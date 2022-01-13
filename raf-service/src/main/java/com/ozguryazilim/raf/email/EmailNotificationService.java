package com.ozguryazilim.raf.email;

import com.ozguryazilim.mutfak.kahve.Kahve;
import com.ozguryazilim.raf.entities.UserFavorite;
import com.ozguryazilim.raf.enums.EmailNotificationActionType;
import com.ozguryazilim.raf.enums.EmailNotificationType;
import com.ozguryazilim.raf.favorite.UserFavoriteService;
import com.ozguryazilim.raf.models.RafObject;
import com.ozguryazilim.telve.auth.Identity;
import com.ozguryazilim.telve.auth.UserInfo;
import com.ozguryazilim.telve.auth.UserService;
import com.ozguryazilim.telve.channel.email.EmailChannel;
import com.ozguryazilim.telve.messages.Messages;
import org.apache.deltaspike.core.api.config.ConfigResolver;
import org.omnifaces.el.functions.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@ApplicationScoped
public class EmailNotificationService implements Serializable {

    private static final Logger LOG = LoggerFactory.getLogger(EmailNotificationService.class);

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
        String name = Strings.capitalize(identity.getUserInfo().getFirstName());
        String surname = Strings.capitalize(identity.getUserInfo().getLastName());
        String link = appLinkDomain + "raf.jsf" + "?id=" + "&o=" + object.getId();
        String objectName = object.getName();
        String objectType = object.getClass().getSimpleName();
        LOG.debug("Sending email to Consumers:{} for the ObjectName:{} ActionType:{} ", consumers, objectName, actionType);
        sendMessage(consumers, name, surname, link, objectName, objectType, actionType);
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
        List<UserInfo> users = new ArrayList<>();
        while (!path.equals(RAF_ROOT_PATH)) {
            List<UserFavorite> userFavoriteList = favoriteService.getFavoritesByPath(path);
            for (UserFavorite u : userFavoriteList) {
                // Kullanıcı bazlı bildirim ayarını kontrol ediyoruz.
                EmailNotificationType notificationType = kahve.get(u.getUsername() + KAHVE_NOTIFICATION_TYPE_SUFFIX,
                        EmailNotificationType.DEFAULT.name()).getAsEnum(EmailNotificationType.class);
                if (notificationType.equals(EmailNotificationType.ONLY_FAVORITE) || (notificationType.equals(EmailNotificationType.DEFAULT) && defaultNotificationType.equals(EmailNotificationType.ONLY_FAVORITE))) {
                    UserInfo userInfo = userService.getUserInfo(u.getUsername());
                    if (users.stream().noneMatch(user -> user.getEmail().equals(userInfo.getEmail()))
                            && userInfo.getEmail() != null && !userInfo.getEmail().isEmpty()) {
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

}