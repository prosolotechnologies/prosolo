package org.prosolo.services.nodes.data.instructor;

import org.prosolo.common.domainmodel.credential.CredentialInstructor;
import org.prosolo.common.domainmodel.credential.TargetCredential1;

public class StudentInstructorPair {

	private TargetCredential1 targetCred;
	private CredentialInstructor instructor;
	
	public StudentInstructorPair() {
		
	}
	
	public StudentInstructorPair(TargetCredential1 targetCred, CredentialInstructor instructor) {
		this.targetCred = targetCred;
		this.instructor = instructor;
	}

	public TargetCredential1 getTargetCred() {
		return targetCred;
	}

	public void setTargetCred(TargetCredential1 targetCred) {
		this.targetCred = targetCred;
	}

	public CredentialInstructor getInstructor() {
		return instructor;
	}

	public void setInstructor(CredentialInstructor instructor) {
		this.instructor = instructor;
	}
	
}
