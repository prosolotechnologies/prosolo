package org.prosolo.web.notification;

import org.prosolo.common.domainmodel.user.notifications.NotificationSection;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;

/**
 * @author Bojan Trifkovic
 * @date 2017-12-19
 * @since 1.2.0
 */
@ManagedBean(name = "managerNotificationsBean")
@Component("managerNotificationsBean")
@Scope("session")
public class ManagerNotificationsBean extends TopNotificationsBean1 {

    @Override
    public NotificationSection getSection() {
        return NotificationSection.MANAGE;
    }
}
