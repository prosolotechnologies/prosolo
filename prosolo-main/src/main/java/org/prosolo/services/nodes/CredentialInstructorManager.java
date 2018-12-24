package org.prosolo.services.nodes;

import org.prosolo.bigdata.common.exceptions.DbConnectionException;
import org.prosolo.common.domainmodel.credential.CredentialInstructor;
import org.prosolo.common.domainmodel.credential.TargetCredential1;
import org.prosolo.common.event.context.data.UserContextData;
import org.prosolo.services.data.Result;
import org.prosolo.services.user.data.UserBasicData;
import org.prosolo.services.nodes.data.credential.CredentialData;
import org.prosolo.services.nodes.data.instructor.InstructorData;
import org.prosolo.services.nodes.data.instructor.StudentAssignData;

import java.util.List;

public interface CredentialInstructorManager {

	InstructorData getCredentialInstructor(long userId, long credentialId, 
			boolean returnNumberOfCurrentlyAssignedStudents, boolean trackChanges) 
					throws DbConnectionException;

	Result<Void> assignStudentToInstructorAndGetEvents(long instructorId, long targetCredId, long formerInstructorId, 
			UserContextData context) throws DbConnectionException;
	
	/**
	 * 
	 * @param studentId
	 * @param instructorId
	 * @param credId
	 * @param formerInstructorUserId
	 * @param context
	 * @throws DbConnectionException
	 */
	void assignStudentToInstructor(long studentId, long instructorId, long credId, long formerInstructorUserId,
			UserContextData context) throws DbConnectionException;

	Result<Void> assignStudentToInstructorAndGetEvents(long studentId, long instructorId, long credId, 
			long formerInstructorUserId, UserContextData context) throws DbConnectionException;
	
	void assignStudentToInstructor(long instructorId, long targetCredId, long formerInstructorUserId,
			UserContextData context) throws DbConnectionException;
	
	/**
	 * Assigns students with target credential ids to instructors that currently have lowest 
	 * number of students assigned.
	 * 
	 * @param credId
	 * @param targetCreds
	 * @param formerInstructorId
	 * @param updateAssessor
	 * @param context
	 * @return
	 * @throws DbConnectionException
	 */
	Result<StudentAssignData> assignStudentsToInstructorAutomatically(long credId, List<TargetCredential1> targetCreds,
    		long formerInstructorId, boolean updateAssessor, UserContextData context)
    				throws DbConnectionException;
	
	Result<StudentAssignData> assignStudentsToInstructorAutomatically(long credId, 
			List<TargetCredential1> targetCreds, long formerInstructorId, UserContextData context)
					throws DbConnectionException;
	
	List<InstructorData> getCredentialInstructorsWithLowestNumberOfStudents(long credentialId, long instructorToExcludeId) 
			throws DbConnectionException;
	
	List<CredentialData> getCredentialIdsAndAssignDateForInstructor(long userId) 
			throws DbConnectionException;
	
	void unassignStudentFromInstructor(long userId, long credId, UserContextData context)
			throws DbConnectionException;
	
	Result<Void> unassignStudentFromInstructorAndGetEvents(long userId, long credId, UserContextData context)
    		throws DbConnectionException;
	
	Result<Void> reassignStudentsAutomatically(long instructorId, long credId, UserContextData context) throws DbConnectionException;
	
	/**
	 * Remove instructor from credential and based on {@code reassignAutomatically} parameter, sets all their students
	 * as unassigned or automatically assigns students to instructors. With automatic assign, there is a possibility to
	 * have unassigned students if maximum capacity is reached for all instructors
	 *
	 * @param instructorId
	 * @param credId
	 * @param reassignAutomatically
	 * @param context
	 * @return
	 * @throws DbConnectionException
	 */
	void removeInstructorFromCredential(long instructorId, long credId, 
			boolean reassignAutomatically, UserContextData context)
					throws DbConnectionException;
	
	/**
	 * Remove instructor from credential and based on {@code reassignAutomatically} parameter, sets all their students
	 * as unassigned or automatically assigns students to instructors. With automatic assign, there is a possibility to
	 * have unassigned students if maximum capacity is reached for all instructors. This method also returns events that
	 * should be generated.
	 * 
	 * @param instructorId
	 * @param credId
	 * @param reassignAutomatically
	 * @param context
	 * @return
	 * @throws DbConnectionException
	 */
	Result<Void> removeInstructorFromCredentialAndGetEvents(long instructorId, long credId, 
			boolean reassignAutomatically, UserContextData context) throws DbConnectionException;
	
	Result<CredentialInstructor> addInstructorToCredentialAndGetEvents(long credId, long userId, 
			int maxNumberOfStudents, UserContextData context) throws DbConnectionException;
	
	void addInstructorToCredential(long credId, long userId, 
			int maxNumberOfStudents, UserContextData context) throws DbConnectionException;
	
	void updateInstructorAndStudentsAssigned(long credId, InstructorData id, 
			List<Long> studentsToAssign, List<Long> studentsToUnassign, UserContextData context)
			throws DbConnectionException;
	
	Result<Void> updateInstructorAndStudentsAssignedAndGetEvents(long credId, InstructorData id, 
			List<Long> studentsToAssign, List<Long> studentsToUnassign, UserContextData context)
			throws DbConnectionException;
	
	Result<Void> updateStudentsAssignedToInstructor(long instructorId, long credId, 
			List<Long> studentsToAssign, List<Long> studentsToUnassign, UserContextData context)
			throws DbConnectionException;
	
	List<InstructorData> getCredentialInstructors(long credentialId, 
			boolean returnNumberOfCurrentlyAssignedStudents, int limit, boolean trackChanges) 
					throws DbConnectionException;
	
	long getCredentialInstructorsCount(long credentialId) throws DbConnectionException;
	
	List<Long> getCredentialInstructorsUserIds(long credentialId) throws DbConnectionException;

	List<UserBasicData> getCredentialInstructorsBasicUserData(long credentialId, boolean returnInstructorsWithoutAssignedStudents);
}