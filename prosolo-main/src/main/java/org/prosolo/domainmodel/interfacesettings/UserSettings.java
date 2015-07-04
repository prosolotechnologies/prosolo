/**
 * 
 */
package org.prosolo.domainmodel.interfacesettings;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;

import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import org.prosolo.domainmodel.interfacesettings.ActivityWallSettings;
import org.prosolo.domainmodel.interfacesettings.LocaleSettings;
import org.prosolo.domainmodel.interfacesettings.StickyMenuSettings;
import org.prosolo.domainmodel.interfacesettings.TermsOfUse;
import org.prosolo.domainmodel.interfacesettings.UserSettings;
import org.prosolo.domainmodel.user.User;

/**
 * @author "Nikola Milikic"
 *
 */
@Entity
public class UserSettings implements Serializable {
	
	private static final long serialVersionUID = -8745462230086992129L;
	
	private User user;
	private ActivityWallSettings activityWallSettings;
	private StickyMenuSettings stickyMenuSettings;
	private LocaleSettings localeSettings;
	private TermsOfUse termsOfUse;
	private Set<String> pagesTutorialPlayed;
	
	public UserSettings() {}
	
	public UserSettings(User user) {
		this.user = user;
		this.activityWallSettings = new ActivityWallSettings();
		this.stickyMenuSettings = new StickyMenuSettings();
		this.termsOfUse = new TermsOfUse();
		this.pagesTutorialPlayed = new HashSet<String>();
	}
	
	@Id
	@OneToOne
	@JoinColumn(name = "id")
	public User getUser() {
		return user;
	}
	
	public void setUser(User user) {
		this.user = user;
	}
	
	@OneToOne
	@Cascade({ CascadeType.MERGE, CascadeType.SAVE_UPDATE, CascadeType.DELETE })
	public ActivityWallSettings getActivityWallSettings() {
		return activityWallSettings;
	}
	
	public void setActivityWallSettings(ActivityWallSettings activityWallSettings) {
		this.activityWallSettings = activityWallSettings;
	}
	
	@OneToOne
	@Cascade({ CascadeType.MERGE, CascadeType.SAVE_UPDATE, CascadeType.DELETE })
	public StickyMenuSettings getStickyMenuSettings() {
		return stickyMenuSettings;
	}
	
	public void setStickyMenuSettings(StickyMenuSettings stickyMenuSettings) {
		this.stickyMenuSettings = stickyMenuSettings;
	}
	
	@OneToOne
	@Cascade({ CascadeType.MERGE, CascadeType.SAVE_UPDATE, CascadeType.DELETE })
	public LocaleSettings getLocaleSettings() {
		return localeSettings;
	}
	
	public void setLocaleSettings(LocaleSettings localeSettings) {
		this.localeSettings = localeSettings;
	}
	
	@OneToOne
	@Cascade({ CascadeType.MERGE, CascadeType.SAVE_UPDATE, CascadeType.DELETE })
	public TermsOfUse getTermsOfUse() {
		return termsOfUse;
	}
	
	public void setTermsOfUse(TermsOfUse termsOfUse) {
		this.termsOfUse = termsOfUse;
	}
	
	@ElementCollection(fetch = FetchType.EAGER)
	@Cascade(value = CascadeType.SAVE_UPDATE)
	public Set<String> getPagesTutorialPlayed() {
		return pagesTutorialPlayed;
	}
	
	public void setPagesTutorialPlayed(Set<String> pagesTutorialPlayed) {
		this.pagesTutorialPlayed = pagesTutorialPlayed;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = getUser().hashCode();
		result = (int) (prime * result + getUser().getId());
		return result;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (this.getClass() != obj.getClass())
			return false;
		
		UserSettings other = (UserSettings) obj;
		if (this.getUser().getId() > 0 && other.getUser().getId() > 0) {
			return this.getUser().getId() == other.getUser().getId();
		}
		return true;
	}
	
}
