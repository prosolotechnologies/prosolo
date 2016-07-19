package org.prosolo.common.domainmodel.interfacesettings;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;

import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import org.hibernate.annotations.Type;
import org.prosolo.common.domainmodel.user.User;

/**
 * @author Nikola Milikic
 * @version 0.5
 *
 */
@Entity
public class UserNotificationsSettings implements Serializable {

	private static final long serialVersionUID = -3181131731366581048L;
	
	private long id;
	private User user;
	private List<NotificationSettings> notifications;
	
	public UserNotificationsSettings() {
		this.notifications = new ArrayList<NotificationSettings>();
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

	@OneToOne
	@JoinColumn(unique = true)
	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}
	
	@OneToMany (fetch = FetchType.EAGER)
	@Column (name = "user_notifications_settings_collection")
	@Cascade({ CascadeType.MERGE, CascadeType.SAVE_UPDATE, CascadeType.DELETE })
	public List<NotificationSettings> getNotifications() {
		return notifications;
	}

	public void setNotifications(List<NotificationSettings> notifications) {
		this.notifications = notifications;
	}

	public void addNotification(NotificationSettings notificationSettings) {
		this.notifications.add(notificationSettings);
	}
	
}
