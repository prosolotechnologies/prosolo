package org.prosolo.services.user.data.profile;

import lombok.Getter;
import lombok.Setter;
import org.prosolo.common.domainmodel.studentprofile.ProfileSettings;
import org.prosolo.services.common.observable.StandardObservable;

/**
 * @author Nikola Milikic
 * @date 2019-04-08
 * @since 1.3.1
 */

public class ProfileSettingsData extends StandardObservable {

    @Getter @Setter
    private long id;
    @Getter
    private String customProfileUrl;
    @Getter
    private boolean summarySidebarEnabled;

    public ProfileSettingsData() {
        startObservingChanges();
    }

    public ProfileSettingsData(ProfileSettings profileSettings) {
        this();
        this.id = profileSettings.getId();
        this.customProfileUrl = profileSettings.getCustomProfileUrl();
        this.summarySidebarEnabled = profileSettings.isSummarySidebarEnabled();
    }

    public void setCustomProfileUrl(String customProfileUrl) {
        observeAttributeChange("customProfileUrl", this.customProfileUrl, customProfileUrl);
        this.customProfileUrl = customProfileUrl;
    }

    public void setSummarySidebarEnabled(boolean summarySidebarEnabled) {
        observeAttributeChange("summarySidebarEnabled", this.summarySidebarEnabled, summarySidebarEnabled);
        this.summarySidebarEnabled = summarySidebarEnabled;
    }
}
