package org.prosolo.search.util.learningevidence;

import org.prosolo.common.domainmodel.credential.LearningEvidenceType;

public enum LearningEvidenceSearchFilter {

	ALL(null),
	FILE(LearningEvidenceType.FILE),
	URL(LearningEvidenceType.LINK),
	TEXT(LearningEvidenceType.TEXT);
	
	private LearningEvidenceType evidenceType;
	
	private LearningEvidenceSearchFilter(LearningEvidenceType evidenceType) {
		this.evidenceType = evidenceType;
	}

	public LearningEvidenceType getEvidenceType() {
		return evidenceType;
	}
	
}