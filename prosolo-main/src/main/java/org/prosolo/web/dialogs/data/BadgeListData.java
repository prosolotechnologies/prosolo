/**
 * 
 */
package org.prosolo.web.dialogs.data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.prosolo.common.domainmodel.user.User;
import org.prosolo.common.domainmodel.workflow.evaluation.Badge;

/**
 * @author "Nikola Milikic"
 * 
 */
public class BadgeListData implements Serializable {

	private static final long serialVersionUID = -7155343682398354787L;

	private Long count;
	private Badge badge;
	private List<User> evaluators = new ArrayList<User>();

	public BadgeListData() {

	}

	public Long getCount() {
		return count;
	}

	public void setCount(Long count) {
		this.count = count;
	}

	public Badge getBadge() {
		return badge;
	}

	public void setBadge(Badge badge) {
		this.badge = badge;
	}

	public List<User> getEvaluators() {
		return evaluators;
	}

	public void setEvaluators(List<User> evaluators) {
		this.evaluators = evaluators;
	}

}
