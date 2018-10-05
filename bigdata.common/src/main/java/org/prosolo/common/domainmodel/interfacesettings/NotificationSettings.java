package org.prosolo.common.domainmodel.interfacesettings;

import java.io.Serializable;

import javax.persistence.*;

import org.hibernate.annotations.Type;
import org.prosolo.common.domainmodel.user.User;
import org.prosolo.common.domainmodel.user.notifications.NotificationType;

/**
 * @author Nikola Milikic
 * @version 0.5
 *
 */
@Entity
@Table(uniqueConstraints = {@UniqueConstraint(columnNames = {"type", "user"})})
public class NotificationSettings implements Serializable {

	private static final long serialVersionUID = 7481670064380351424L;

	private long id;
	private NotificationType type;
	private boolean subscribedEmail;
	private User user;
	
	public NotificationSettings() { }
	
	public NotificationSettings(NotificationType type, boolean subscribedEmail) {
		this.type = type;
		this.subscribedEmail = subscribedEmail;
	}

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

	@Enumerated (EnumType.STRING)
	@Column(nullable = false)
	public NotificationType getType() {
		return type;
	}

	public void setType(NotificationType type) {
		this.type = type;
	}

	@Type(type = "true_false")
	@Column(columnDefinition = "char(1) DEFAULT 'F'")
	public boolean isSubscribedEmail() {
		return subscribedEmail;
	}

	public void setSubscribedEmail(boolean subscribedEmail) {
		this.subscribedEmail = subscribedEmail;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (int) (id ^ (id >>> 32));
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		NotificationSettings other = (NotificationSettings) obj;
		if (id != other.id)
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "NotificationSettings [id=" + id + ", type=" + type + ", subscribedEmail=" + subscribedEmail + "]";
	}

}
