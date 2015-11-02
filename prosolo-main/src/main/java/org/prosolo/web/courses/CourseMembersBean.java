/**
 * 
 */
package org.prosolo.web.courses;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.faces.bean.ManagedBean;
import javax.inject.Inject;

import org.apache.log4j.Logger;
import org.prosolo.common.domainmodel.user.User;
import org.prosolo.common.util.ImageFormat;
import org.prosolo.services.nodes.CourseManager;
import org.prosolo.services.urlencoding.UrlIdEncoder;
import org.prosolo.web.administration.data.UserData;
import org.prosolo.web.util.AvatarUtils;
import org.prosolo.web.util.PageUtil;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@ManagedBean(name = "courseMembersBean")
@Component("courseMembersBean")
@Scope("view")
public class CourseMembersBean implements Serializable {

	private static final long serialVersionUID = 1827743731093959636L;
	
	@SuppressWarnings("unused")
	private static Logger logger = Logger.getLogger(CourseMembersBean.class);
	
	private List<UserData> members;
	private List<UserData> filteredMembers;
	
	@Inject
	private CourseManager courseManager;
	@Inject 
	private UrlIdEncoder idEncoder;
	
	// PARAMETERS
	private String id;
	
	
	public void init() {
		long decodedId = idEncoder.decodeId(id);
		if (decodedId > 0) {
			try{
				List<User> users = courseManager.getCourseParticipants(decodedId);
				populateCourseMembersData(users);
			}catch(Exception e){
				PageUtil.fireErrorMessage("Error while loading course members");
			}
		}
	}
	
	
	private void populateCourseMembersData(List<User> users) {
		members = new ArrayList<>();
		for (User user:users){
			UserData ud = new UserData();
			ud.setId(user.getId());
			ud.setName(user.getName());
			ud.setLastName(user.getLastname());
			ud.setAvatarUrl(AvatarUtils.getAvatarUrlInFormat(user, ImageFormat.size60x60));
			members.add(ud);
		}
	}

	/*
	 * PARAMETERS
	 */
	public void setId(String id) {
		this.id = id;
	}
	
	public String getId() {
		return id;
	}
	
	/*
	 * GETTERS / SETTERS
	 */
	
	public List<UserData> getMembers() {
		return members;
	}

	public void setMembers(List<UserData> members) {
		this.members = members;
	}

	public List<UserData> getFilteredMembers() {
		return filteredMembers;
	}

	public void setFilteredMembers(List<UserData> filteredMembers) {
		this.filteredMembers = filteredMembers;
	}
	
	
}
