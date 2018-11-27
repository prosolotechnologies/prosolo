package org.prosolo.web.notification;

import org.prosolo.common.domainmodel.user.notifications.NotificationSection;
import org.prosolo.services.authentication.annotations.AuthenticationChangeType;
import org.prosolo.services.authentication.annotations.SessionAttributeScope;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.faces.bean.ManagedBean;

/**
 * @author Bojan Trifkovic
 * @date 2017-12-19
 * @since 1.2.0
 */
@ManagedBean(name = "studentNotificationsBean")
@Component("studentNotificationsBean")
@Scope("session")
@SessionAttributeScope(end = AuthenticationChangeType.USER_AUTHENTICATION_CHANGE)
public class StudentNotificationsBean extends TopNotificationsBean1 {

    @Override
    public NotificationSection getSection() {
        return NotificationSection.STUDENT;
    }

}
