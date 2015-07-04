/**
 * 
 */
package org.prosolo.common.domainmodel.organization;

import org.prosolo.common.domainmodel.organization.VisibilityType;

/**
 * @author "Nikola Milikic"
 *
 */
public interface Visible {

	VisibilityType getVisibility();
	
	void setVisibility(VisibilityType visibility);
}
