package org.prosolo.services.nodes;

import java.util.List;
import java.util.Map;

import org.prosolo.services.lti.exceptions.DbConnectionException;
import org.prosolo.services.nodes.data.CredentialData;
import org.prosolo.services.nodes.data.InstructorData;

public interface CredentialInstructorManager {

	InstructorData getCredentialInstructor(long userId, long credentialId,
			boolean returnNumberOfCurrentlyAssignedStudents) throws DbConnectionException;
;
	void assignStudentToInstructor(long studentId, long instructorId, long credId) throws DbConnectionException;

	void assignStudentToInstructor(long instructorId, long targetCredId) 
			throws DbConnectionException;
	
	/**
	 * Assigns students with target credential ids to instructors that currently have lowest 
	 * number of students assigned. 
	 * Returns map with two entries: entry with key assigned gives Map<Long, Long> with id of a 
	 * target credential of student that is assigned and instructor id, and second entry List<Long>
	 * with ids of target credentials of students that are not assigned because instructors are full.
	 * @param credId
	 * @param targetCredIds
	 * @param instructorToExcludeId
	 * @return
	 * @throws DbConnectionException
	 */
	Map<String, Object> assignStudentsToInstructorAutomatically(long credId, 
    		List<Long> targetCredIds, long instructorToExcludeId) throws DbConnectionException;
	
	List<InstructorData> getCredentialInstructorsWithLowestNumberOfStudents(long credentialId, 
			int numberOfInstructorsToReturn, long instructorToExcludeId) throws DbConnectionException;
	
	List<CredentialData> getCredentialIdsAndAssignDateForInstructor(long userId) 
			throws DbConnectionException;
	
	void unassignStudentFromInstructor(long userId, long credId) throws DbConnectionException;
}