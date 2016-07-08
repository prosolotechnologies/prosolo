package org.prosolo.services.nodes;

import org.prosolo.common.domainmodel.organization.VisibilityType;
import org.prosolo.common.domainmodel.organization.Visible;
import org.prosolo.common.exceptions.ResourceCouldNotBeLoadedException;
import org.prosolo.services.event.EventException;
import org.prosolo.services.nodes.exceptions.VisibilityCoercionError;

public interface VisibilityManager {

	Visible setResourceVisibility(long userId, Visible resource, String visibility, String context) throws VisibilityCoercionError, EventException;

	Visible setResourceVisibility(long userId, long resId, VisibilityType visType, String context) throws EventException, ResourceCouldNotBeLoadedException;

	Visible setResourceVisibility(long userId, Visible resource, VisibilityType visType, String context) throws EventException;

	VisibilityType retrieveTargetCompetenceVisibility(Long tCompId);

	VisibilityType retrieveTargetActivityVisibility(Long tActId);
}
