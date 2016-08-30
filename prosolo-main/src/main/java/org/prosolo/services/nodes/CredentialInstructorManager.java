package org.prosolo.services.nodes;

import java.util.List;

import org.prosolo.common.domainmodel.credential.CredentialInstructor;
import org.prosolo.services.common.exception.DbConnectionException;
import org.prosolo.services.nodes.data.CredentialData;
import org.prosolo.services.nodes.data.instructor.InstructorData;
import org.prosolo.services.nodes.data.instructor.StudentAssignData;

public interface CredentialInstructorManager {

	InstructorData getCredentialInstructor(long userId, long credentialId, 
			boolean returnNumberOfCurrentlyAssignedStudents, boolean trackChanges) 
					throws DbConnectionException;

	void assignStudentToInstructor(long studentId, long instructorId, long credId) throws DbConnectionException;

	void assignStudentToInstructor(long instructorId, long targetCredId) 
			throws DbConnectionException;
	
	/**
	 * Assigns students with target credential ids to instructors that currently have lowest 
	 * number of students assigned. 
	 * @param credId
	 * @param targetCredIds
	 * @param instructorToExcludeId
	 * @return
	 * @throws DbConnectionException
	 */
	StudentAssignData assignStudentsToInstructorAutomatically(long credId, 
			List<Long> targetCredIds, long instructorToExcludeId) throws DbConnectionException;
	
	StudentAssignData assignStudentsToInstructorAutomatically(long credId, List<Long> targetCredIds,
    		long instructorToExcludeId, boolean updateAssessor) throws DbConnectionException;
	
	List<InstructorData> getCredentialInstructorsWithLowestNumberOfStudents(long credentialId, 
			int numberOfInstructorsToReturn, long instructorToExcludeId) throws DbConnectionException;
	
	List<CredentialData> getCredentialIdsAndAssignDateForInstructor(long userId) 
			throws DbConnectionException;
	
	void unassignStudentFromInstructor(long userId, long credId) throws DbConnectionException;
	
	StudentAssignData reassignStudentsAutomatically(long instructorId, long credId) 
			throws DbConnectionException;
	
	StudentAssignData removeInstructorFromCredential(long instructorId, long credId, 
			boolean reassignAutomatically) throws DbConnectionException;
	
	CredentialInstructor addInstructorToCredential(long credId, long userId, int maxNumberOfStudents) 
			throws DbConnectionException;
	
	void updateInstructorAndStudentsAssigned(long credId, InstructorData id, List<Long> studentsToAssign, 
			List<Long> studentsToUnassign) throws DbConnectionException;
	
	void updateStudentsAssignedToInstructor(long instructorId, long credId, 
			List<Long> studentsToAssign, List<Long> studentsToUnassign) throws DbConnectionException;
}