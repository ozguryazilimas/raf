package com.ozguryazilim.raf.optionpane;

import com.ozguryazilim.mutfak.kahve.Kahve;
import com.ozguryazilim.mutfak.kahve.annotations.UserAware;
import com.ozguryazilim.raf.config.SettingsPages;
import com.ozguryazilim.raf.enums.EmailNotificationType;
import com.ozguryazilim.telve.config.AbstractOptionPane;
import com.ozguryazilim.telve.config.OptionPane;
import com.ozguryazilim.telve.messages.FacesMessages;
import org.apache.deltaspike.jpa.api.transaction.Transactional;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

@OptionPane(permission = "PUBLIC", optionPage = SettingsPages.NotificationOptionPane.class)
public class NotificationOptionPane extends AbstractOptionPane {

    private static final String EMAIL_NOTIFICATION_TYPE_KEY = "email.notification.type";

    private EmailNotificationType emailNotificationType = EmailNotificationType.DEFAULT;

    @Inject
    @UserAware
    private Kahve kahve;

    @PostConstruct
    public void init() {
        emailNotificationType = kahve.get(EMAIL_NOTIFICATION_TYPE_KEY, EmailNotificationType.DEFAULT.name())
                .getAsEnum(EmailNotificationType.class);
    }

    @Override
    @Transactional
    public void save() {
        // Save to kahve user aware configurations
        kahve.put(EMAIL_NOTIFICATION_TYPE_KEY, emailNotificationType.name());
        FacesMessages.info("NotificationOptionPane.message.success");
    }

    public EmailNotificationType getEmailNotificationType() {
        return emailNotificationType;
    }

    public void setEmailNotificationType(EmailNotificationType emailNotificationType) {
        this.emailNotificationType = emailNotificationType;
    }

}