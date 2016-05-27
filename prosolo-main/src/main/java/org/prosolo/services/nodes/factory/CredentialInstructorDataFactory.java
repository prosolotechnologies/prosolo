package org.prosolo.services.nodes.factory;

import org.prosolo.common.domainmodel.credential.CredentialInstructor;
import org.prosolo.common.domainmodel.user.User;
import org.prosolo.services.nodes.data.InstructorData;
import org.prosolo.services.nodes.data.UserData;
import org.springframework.stereotype.Component;

@Component
public class CredentialInstructorDataFactory {

	public InstructorData getInstructorData(CredentialInstructor credInstructor, User user, 
			int numberOfCurrentlyAssignedStudents) {
		if(credInstructor == null) {
			return null;
		}
		InstructorData instructor = new InstructorData();
		instructor.setInstructorId(credInstructor.getId());
		instructor.setMaxNumberOfStudents(credInstructor.getMaxNumberOfStudents());
		instructor.setNumberOfAssignedStudents(numberOfCurrentlyAssignedStudents);
		instructor.setUser(new UserData(user));
		
		return instructor;
	}

}
