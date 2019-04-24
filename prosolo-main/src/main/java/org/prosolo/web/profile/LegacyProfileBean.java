package org.prosolo.web.profile;

import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.prosolo.services.urlencoding.UrlIdEncoder;
import org.prosolo.services.user.StudentProfileManager;
import org.prosolo.services.user.data.profile.*;
import org.prosolo.web.LoggedUserBean;
import org.prosolo.web.util.page.PageUtil;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.faces.bean.ManagedBean;
import javax.inject.Inject;
import java.util.Optional;

/**
 * This class serves the old profile page (profile-legacy.xhtml) that enables legacy profile paths
 * {domain}/profile/{encodedUserId}. The purpose of this bean is to redirect to the new profile page that is in the
 * format: {domain}/p/{customProfilePath}
 *
 * @author Nikola Milikic
 * @date 2019-04-11
 * @since 1.3.2
 */
@ManagedBean(name = "legacyProfileBean")
@Component("legacyProfileBean")
@Scope("view")
public class LegacyProfileBean {
	
	private static Logger logger = Logger.getLogger(LegacyProfileBean.class);

	@Inject
	private UrlIdEncoder idEncoder;
	@Inject
	private LoggedUserBean loggedUserBean;
	@Inject
	private StudentProfileManager studentProfileManager;

	@Getter @Setter
	private String studentId;

	public void init() {
		long decodedStudentId;

		if (studentId == null || StringUtils.isBlank(studentId)) {
			decodedStudentId = loggedUserBean.getUserId();
		} else {
			decodedStudentId = idEncoder.decodeId(studentId);
		}

		logger.info("Redirecting from the old Profile page to the new one for the user "+decodedStudentId) ;

		if (decodedStudentId > 0) {
			Optional<ProfileSettingsData> profileSettingsData = studentProfileManager.getProfileSettingsData(decodedStudentId);

			if (profileSettingsData.isPresent()) {
				PageUtil.redirect("/p/" + profileSettingsData.get().getCustomProfileUrl());
				return;
			}
		}
		PageUtil.notFound();
	}

}
