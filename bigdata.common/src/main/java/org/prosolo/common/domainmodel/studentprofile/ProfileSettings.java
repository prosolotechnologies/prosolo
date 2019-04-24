package org.prosolo.common.domainmodel.studentprofile;

import org.hibernate.annotations.Type;
import org.prosolo.common.domainmodel.user.User;

import javax.persistence.*;

/**
 * Stores UI settings for the student Profile page.
 *
 * @author Nikola Milikic
 * @date 2019-04-09
 * @since 1.3.2
 */
@Entity
//unique constraint added from the script
public class ProfileSettings {

    private long id;
    private String customProfileUrl;
    private boolean summarySidebarEnabled;
    private User user;

    @Id
    @Column(name = "id", unique = true, nullable = false, insertable = false, updatable = false)
    @GeneratedValue(strategy = GenerationType.TABLE)
    @Type(type = "long")
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    @Column(length = 60)
    public String getCustomProfileUrl() {
        return customProfileUrl;
    }

    public void setCustomProfileUrl(String customProfileUrl) {
        this.customProfileUrl = customProfileUrl;
    }

    @Column(columnDefinition = "char(1) DEFAULT 'F'")
    @Type(type = "true_false")
    public boolean isSummarySidebarEnabled() {
        return summarySidebarEnabled;
    }

    public void setSummarySidebarEnabled(boolean summarySidebarEnabled) {
        this.summarySidebarEnabled = summarySidebarEnabled;
    }

    @OneToOne
    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
}
