package org.prosolo.services.interfaceSettings;

import javax.servlet.http.HttpSession;

import org.prosolo.domainmodel.user.LearningGoal;
import org.prosolo.domainmodel.user.User;

public interface LearnPageCacheUpdater {

	void removeCollaboratorFormGoal(User collaboratorToRemove, LearningGoal goal, HttpSession userSession);
	
}
