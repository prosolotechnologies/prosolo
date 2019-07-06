package org.prosolo.web.courses.credential.announcements;

import lombok.Getter;
import org.prosolo.common.domainmodel.credential.AnnouncementPublishMode;

public enum AnnouncementPublishModeDescription {

    ACTIVE_STUDENTS(AnnouncementPublishMode.ACTIVE_STUDENTS, "Only active students"),
    ALL_STUDENTS(AnnouncementPublishMode.ALL_STUDENTS, "All students");

    @Getter
    private final AnnouncementPublishMode announcementPublishMode;
    @Getter
    private final String text;

    AnnouncementPublishModeDescription(AnnouncementPublishMode announcementPublishMode, String text) {
        this.announcementPublishMode = announcementPublishMode;
        this.text = text;
    }
}
