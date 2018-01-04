package org.prosolo.services.notifications.factory;

import org.prosolo.common.domainmodel.user.notifications.NotificationSection;
import org.prosolo.web.util.page.PageSection;
import org.springframework.stereotype.Component;

/**
 * @author Bojan Trifkovic
 * @date 2017-12-14
 * @since 1.2.0
 */

@Component
public class NotificationSectionDataFactory {

    public NotificationSectionDataFactory(){}

    public PageSection getSectionData(NotificationSection section){
        if(section.equals(NotificationSection.MANAGE)){
            return PageSection.MANAGE;
        } else if (section.equals(NotificationSection.STUDENT)){
            return PageSection.STUDENT;
        }
        return null;
    }

    public NotificationSection getSection(PageSection section){
        if(section.equals(PageSection.MANAGE)){
            return NotificationSection.MANAGE;
        } else if (section.equals(PageSection.STUDENT)) {
            return NotificationSection.STUDENT;
        }
        return null;
    }
}
