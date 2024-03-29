package org.prosolo.web.people;

import org.apache.log4j.Logger;
import org.prosolo.search.util.users.UserSearchConfig;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.faces.bean.ManagedBean;

/**
 * @author stefanvuckovic
 * @date 2018-05-31
 * @since 1.2.0
 */
@ManagedBean(name = "peopleFollowingBean")
@Component("peopleFollowingBean")
@Scope("view")
public class PeopleFollowingBean extends PeopleBean {

	private static final long serialVersionUID = -2311428814027411264L;

	protected static Logger logger = Logger.getLogger(PeopleFollowingBean.class);

	@Override
	protected UserSearchConfig.UserScope getUserScope() {
		return UserSearchConfig.UserScope.FOLLOWING;
	}
}
