package org.prosolo.web.profile;

import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.prosolo.services.urlencoding.UrlIdEncoder;
import org.prosolo.services.user.StudentProfileManager;
import org.prosolo.services.user.data.profile.ProfileSettingsData;
import org.prosolo.web.LoggedUserBean;
import org.prosolo.web.util.page.PageUtil;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.faces.bean.ManagedBean;
import javax.inject.Inject;
import java.util.Optional;

/**
 * This class serves the old profile evidence page (student-profile-evidence-legacy.xhtml) that enables the legacy
 * paths to profile evidence {domain}/profile/{studentId}/evidence/{competenceEvidenceId}. The purpose of this bean is
 * to redirect to the new profile evidence page in the format: {domain}/p/{customProfileUrl}/evidence/{competenceEvidenceId}
 *
 * @author Nikola Milikic
 * @date 2019-04-24
 * @since 1.3.2
 */
@ManagedBean(name = "legacyProfileEvidenceBean")
@Component("legacyProfileEvidenceBean")
@Scope("view")
public class LegacyProfileEvidenceBean {
	
	private static Logger logger = Logger.getLogger(LegacyProfileEvidenceBean.class);

	@Inject
	private UrlIdEncoder idEncoder;
	@Inject
	private StudentProfileManager studentProfileManager;

	@Getter @Setter
	private String studentId;
	@Getter @Setter
	private String competenceEvidenceId;

	public void init() {
		long decodedStudentId = idEncoder.decodeId(studentId);

		if (decodedStudentId > 0) {
			logger.info("Redirecting from the old Profile Evidence page to the new one for the user " + decodedStudentId) ;

			Optional<ProfileSettingsData> profileSettingsData = studentProfileManager.getProfileSettingsData(decodedStudentId);

			if (profileSettingsData.isPresent()) {
				PageUtil.redirect("/p/" + profileSettingsData.get().getCustomProfileUrl()+"/evidence/"+competenceEvidenceId);
			}
		} else {
			PageUtil.notFound();
		}
	}

}
