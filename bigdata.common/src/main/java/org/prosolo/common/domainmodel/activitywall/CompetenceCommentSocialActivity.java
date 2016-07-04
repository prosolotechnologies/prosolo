package org.prosolo.common.domainmodel.activitywall;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;

import org.prosolo.common.domainmodel.credential.Competence1;

@Entity
public class CompetenceCommentSocialActivity extends CommentSocialActivity {

	private static final long serialVersionUID = 7084416789331547125L;
	
	private Competence1 competenceTarget;

	@ManyToOne(fetch = FetchType.LAZY)
	public Competence1 getCompetenceTarget() {
		return competenceTarget;
	}

	public void setCompetenceTarget(Competence1 competenceTarget) {
		this.competenceTarget = competenceTarget;
	}

}
