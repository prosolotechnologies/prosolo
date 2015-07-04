package org.prosolo.services.nodes;

import org.prosolo.domainmodel.organization.VisibilityType;
import org.prosolo.domainmodel.organization.Visible;
import org.prosolo.domainmodel.user.User;
import org.prosolo.services.event.EventException;
import org.prosolo.services.exceptions.ResourceCouldNotBeLoadedException;
import org.prosolo.services.nodes.exceptions.VisibilityCoercionError;

public interface VisibilityManager {

	Visible setResourceVisibility(User user, Visible resource, String visibility, String context) throws VisibilityCoercionError, EventException;

	Visible setResourceVisibility(User user, long resId, VisibilityType visType, String context) throws EventException, ResourceCouldNotBeLoadedException;

	Visible setResourceVisibility(User user, Visible resource, VisibilityType visType, String context) throws EventException;

	VisibilityType retrieveTargetCompetenceVisibility(Long tCompId);

	VisibilityType retrieveTargetActivityVisibility(Long tActId);
}
