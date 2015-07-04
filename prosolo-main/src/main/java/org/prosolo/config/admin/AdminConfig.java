/**
 * 
 */
package org.prosolo.config.admin;

import org.prosolo.util.StringUtils;
import org.simpleframework.xml.Element;

/**
 * @author "Nikola Milikic"
 *
 */
public class AdminConfig {

	@Element(name = "user-can-create-competence")
	public boolean userCanCreateCompetence = false;
	
	@Element(name = "selected-users-can-do-evaluation")
	public boolean selectedUsersCanDoEvaluation = false;
	
	@Element(name = "individual-competences-can-not-be-evaluated")
	public boolean individualCompetencesCanNotBeEvaluated = false;
	
	@Override
	public String toString() {
		return StringUtils.toStringByReflection(this);
	}
}
