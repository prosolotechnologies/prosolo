package org.prosolo.services.nodes.data;

import java.io.Serializable;

import org.prosolo.services.common.observable.StandardObservable;

public class LearningInfo extends StandardObservable implements Serializable {

	private static final long serialVersionUID = -260363794575957716L;

	private String credentialTitle;
	private String competenceTitle;
	private boolean mandatoryFlow;
	private long nextCompetenceToLearn;
	private long nextActivityToLearn;
	
	private LearningInfo(String credentialTitle, String competenceTitle, boolean mandatoryFlow,
			long nextCompetenceToLearn, long nextActivityToLearn) {
		this.credentialTitle = credentialTitle;
		this.competenceTitle = competenceTitle;
		this.mandatoryFlow = mandatoryFlow;
		this.nextCompetenceToLearn = nextCompetenceToLearn;
		this.nextActivityToLearn = nextActivityToLearn;
	}
	
	public static LearningInfo getLearningInfoForCredential(String credentialTitle, boolean mandatoryFlow, 
			long nextCompetenceToLearn) {
		return new LearningInfo(credentialTitle, null, mandatoryFlow, nextCompetenceToLearn, 0);
	}
	
	public static LearningInfo getLearningInfoForCompetence(String competenceTitle, long nextActivityToLearn) {
		return new LearningInfo(null, competenceTitle, false, 0, nextActivityToLearn);
	}
	
	/**
	 * Merges credential learning info and competence learning info and returns new object.
	 * 
	 * @param credInfo
	 * @param compInfo
	 * @return
	 */
	public static LearningInfo merge(LearningInfo credInfo, LearningInfo compInfo) {
		return new LearningInfo(credInfo.getCredentialTitle(), compInfo.getCompetenceTitle(), 
				credInfo.isMandatoryFlow(), credInfo.getNextCompetenceToLearn(), compInfo.getNextActivityToLearn());
	}

	public String getCredentialTitle() {
		return credentialTitle;
	}

	public void setCredentialTitle(String credentialTitle) {
		this.credentialTitle = credentialTitle;
	}

	public String getCompetenceTitle() {
		return competenceTitle;
	}

	public void setCompetenceTitle(String competenceTitle) {
		this.competenceTitle = competenceTitle;
	}

	public boolean isMandatoryFlow() {
		return mandatoryFlow;
	}

	public void setMandatoryFlow(boolean mandatoryFlow) {
		this.mandatoryFlow = mandatoryFlow;
	}

	public long getNextCompetenceToLearn() {
		return nextCompetenceToLearn;
	}

	public void setNextCompetenceToLearn(long nextCompetenceToLearn) {
		this.nextCompetenceToLearn = nextCompetenceToLearn;
	}

	public long getNextActivityToLearn() {
		return nextActivityToLearn;
	}

	public void setNextActivityToLearn(long nextActivityToLearn) {
		this.nextActivityToLearn = nextActivityToLearn;
	}
	
}
