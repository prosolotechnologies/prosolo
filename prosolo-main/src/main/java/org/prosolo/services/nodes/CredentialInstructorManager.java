package org.prosolo.services.nodes;

import org.prosolo.bigdata.common.exceptions.DbConnectionException;
import org.prosolo.bigdata.common.exceptions.IllegalDataStateException;
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

//	Result<Void> assignStudentToInstructorAndGetEvents(long instructorId, long targetCredId, long formerInstructorId,
//			UserContextData context) throws DbConnectionException;
	
	/**
	 * 
	 * @param studentId
	 * @param instructorId
	 * @param credId
	 * @param context
	 * @throws DbConnectionException
	 */
	void assignStudentToInstructor(long studentId, long instructorId, long credId,
			UserContextData context) throws DbConnectionException;

	Result<Void> assignStudentToInstructorAndGetEvents(long studentId, long instructorId, long credId, UserContextData context) throws DbConnectionException;
	
//	void assignStudentToInstructor(long instructorId, long targetCredId, long formerInstructorUserId,
//			UserContextData context) throws DbConnectionException;
//

	/**
	 * Assigns students with target credential ids to instructors that currently have lowest
	 * number of students assigned.
	 *
	 * @param credId
	 * @param targetCreds
	 * @param instructorIdToExclude
	 * @param context
	 * @return
	 * @throws DbConnectionException
	 * @throws IllegalDataStateException
	 */
	Result<StudentAssignData> assignStudentsToInstructorAutomatically(long credId, 
			List<TargetCredential1> targetCreds, long instructorIdToExclude, UserContextData context)
			throws DbConnectionException, IllegalDataStateException;
	
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

	/**
	 * Withdraws instructor from student and if automatic instructor assign is enabled assigns new
	 * instructor automatically. Previous instructor assessment is declined and new one is created if new instructor
	 * is assigned. Returns events that should be fired.
	 *
	 * @param targetCredentialId
	 * @param context
	 * @return
	 * @throws IllegalDataStateException
	 * @throws DbConnectionException
	 */
	Result<Void> withdrawFromBeingInstructorAndGetEvents(long targetCredentialId, UserContextData context) throws IllegalDataStateException;

	/**
	 *  Withdraws instructor from student and if automatic instructor assign is enabled assigns new
	 * 	instructor automatically. Previous instructor assessment is declined and new one is created if new instructor
	 * 	is assigned. Also, it fires generated events.
	 *
	 * @param targetCredentialId
	 * @param context
	 * @throws IllegalDataStateException
	 * @throws DbConnectionException
	 */
	void withdrawFromBeingInstructor(long targetCredentialId, UserContextData context) throws IllegalDataStateException;

	/**
	 *  Withdraws instructor from student and if automatic instructor assign is enabled assigns new
	 * 	instructor automatically. Previous instructor assessment is declined and new one is created if new instructor
	 * 	is assigned. Also, it fires generated events.
	 *
	 * @param credentialId
	 * @param studentUserId
	 * @param context
	 * @throws IllegalDataStateException
	 */
	void withdrawFromBeingInstructor(long credentialId, long studentUserId, UserContextData context) throws IllegalDataStateException;

	/**
	 * Withdraws instructor from student and if automatic instructor assign is enabled assigns new
	 * instructor automatically. Previous instructor assessment is declined and new one is created if new instructor
	 * is assigned. Returns events that should be fired.
	 *
	 * @param credentialId
	 * @param studentUserId
	 * @param context
	 * @return
	 * @throws IllegalDataStateException
	 */
	Result<Void> withdrawFromBeingInstructorAndGetEvents(long credentialId, long studentUserId, UserContextData context) throws IllegalDataStateException;
}