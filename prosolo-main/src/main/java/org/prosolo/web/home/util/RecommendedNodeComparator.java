/**
 * 
 */
package org.prosolo.web.home.util;

import java.util.Comparator;

import org.prosolo.common.domainmodel.competences.Competence;
import org.prosolo.common.domainmodel.general.Node;
import org.prosolo.common.domainmodel.user.LearningGoal;

/**
 * @author "Nikola Milikic"
 *
 */
public class RecommendedNodeComparator implements Comparator<Node> {

	@Override
	public int compare(Node o1, Node o2) {
		if (o1 instanceof LearningGoal && o2 instanceof Competence) {
			return -1;
		} else if (o1 instanceof Competence && o2 instanceof LearningGoal) {
			return 1;
		} if (o1.getClass().equals(o2.getClass())) {
			return o1.getTitle().compareTo(o2.getTitle());
		}
		return 0;
	}

}
