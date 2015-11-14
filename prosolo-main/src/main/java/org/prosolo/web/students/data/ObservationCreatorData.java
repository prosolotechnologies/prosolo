package org.prosolo.web.students.data;

import org.prosolo.common.domainmodel.user.User;
import org.prosolo.common.util.ImageFormat;
import org.prosolo.web.util.AvatarUtils;

public class ObservationCreatorData {

	private long id;
	private String name;
	
	public ObservationCreatorData(User user){
		this.id = user.getId();
		this.name = user.getName() + (user.getLastname() != null ? " " + user.getLastname() : "");
	}
	
	public long getId() {
		return id;
	}
	public void setId(long id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	
	
}
