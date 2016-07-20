package org.prosolo.web.people;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;

import org.apache.log4j.Logger;
import org.prosolo.common.domainmodel.user.User;
import org.prosolo.common.web.activitywall.data.UserData;
import org.prosolo.recommendation.CollaboratorsRecommendation;
import org.prosolo.services.activityWall.UserDataFactory;
import org.prosolo.web.LoggedUserBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@ManagedBean(name = "usersToFollowBean")
@Component("usersToFollowBean")
@Scope("view")
public class UsersToFollowBean implements Serializable {

	private static final long serialVersionUID = 4010227642462336065L;

	protected static Logger logger = Logger.getLogger(UsersToFollowBean.class);

	@Autowired
	private LoggedUserBean loggedUser;
	@Autowired
	private CollaboratorsRecommendation cRecommendation;

	private List<UserData> usersToFollow;

	@PostConstruct
	public void init() {
		initUsersToFollow();
	}

	public void initUsersToFollow() {
		try {
			usersToFollow = new ArrayList<UserData>();
			List<User> usersToFollowList = cRecommendation
					.getRecommendedCollaboratorsBasedOnLocation(loggedUser.getUserId(), 3);

			if (usersToFollowList != null && !usersToFollowList.isEmpty()) {
				for (User user : usersToFollowList) {
					UserData userData = UserDataFactory.createUserData(user);
					usersToFollow.add(userData);
				}
			}
			usersToFollow.add(new UserData(4, "Richards Anderson", null));
		} catch (Exception e) {
			logger.error(e);
		}
	}

	public List<UserData> getUsersToFollow() {
		return usersToFollow;
	}

	public void setUsersToFollow(List<UserData> usersToFollow) {
		this.usersToFollow = usersToFollow;
	}

}
