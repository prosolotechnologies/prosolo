/**
 * 
 */
package org.prosolo.domainmodel.organization;

import org.prosolo.domainmodel.organization.VisibilityType;

/**
 * @author "Nikola Milikic"
 *
 */
public interface Visible {

	VisibilityType getVisibility();
	
	void setVisibility(VisibilityType visibility);
}
