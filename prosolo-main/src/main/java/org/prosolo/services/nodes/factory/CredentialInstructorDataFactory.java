package org.prosolo.services.nodes.factory;

import org.prosolo.common.domainmodel.credential.CredentialInstructor;
import org.prosolo.common.domainmodel.user.User;
import org.prosolo.services.user.data.UserData;
import org.prosolo.services.nodes.data.instructor.InstructorData;
import org.springframework.stereotype.Component;

@Component
public class CredentialInstructorDataFactory {

	public InstructorData getInstructorData(CredentialInstructor credInstructor, User user, 
			int numberOfCurrentlyAssignedStudents, boolean trackChanges) {
		if(credInstructor == null) {
			return null;
		}
		InstructorData instructor = new InstructorData(false);
		instructor.setInstructorId(credInstructor.getId());
		instructor.setMaxNumberOfStudents(credInstructor.getMaxNumberOfStudents());
		instructor.setNumberOfAssignedStudents(numberOfCurrentlyAssignedStudents);
		instructor.setUser(new UserData(user));
		if(trackChanges) {
			instructor.startObservingChanges();
		}
		
		return instructor;
	}

}
