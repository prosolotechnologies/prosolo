package org.prosolo.services.nodes.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import javax.inject.Inject;

import org.apache.log4j.Logger;
import org.hibernate.Query;
import org.prosolo.common.domainmodel.credential.Credential1;
import org.prosolo.common.domainmodel.credential.CredentialInstructor;
import org.prosolo.common.domainmodel.credential.TargetCredential1;
import org.prosolo.common.domainmodel.user.User;
import org.prosolo.services.common.exception.DbConnectionException;
import org.prosolo.services.general.impl.AbstractManagerImpl;
import org.prosolo.services.nodes.CredentialInstructorManager;
import org.prosolo.services.nodes.CredentialManager;
import org.prosolo.services.nodes.data.CredentialData;
import org.prosolo.services.nodes.data.UserData;
import org.prosolo.services.nodes.data.instructor.InstructorData;
import org.prosolo.services.nodes.data.instructor.StudentAssignData;
import org.prosolo.services.nodes.data.instructor.StudentInstructorPair;
import org.prosolo.services.nodes.factory.CredentialInstructorDataFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service("org.prosolo.services.nodes.CredentialInstructorManager")
public class CredentialInstructorManagerImpl extends AbstractManagerImpl implements CredentialInstructorManager {

	private static final long serialVersionUID = 394489300560570371L;

	private static Logger logger = Logger.getLogger(CredentialInstructorManagerImpl.class);
	
	@Inject
	private CredentialManager credManager;
	@Inject
	private CredentialInstructorDataFactory credInstructorFactory;
	
	@Override
	@Transactional(readOnly = true)
	public InstructorData getCredentialInstructor(long userId, long credentialId, 
			boolean returnNumberOfCurrentlyAssignedStudents, boolean trackChanges) 
					throws DbConnectionException {
		try {
			InstructorData instructor = null;
			String query = 
					"SELECT credInstructor " +
					"FROM CredentialInstructor credInstructor " +
					"INNER JOIN fetch credInstructor.user instructor " +
					"WHERE instructor.id = :userId " +
					"AND credInstructor.credential.id = :credId";
			
			CredentialInstructor result = (CredentialInstructor) persistence
					.currentManager()
					.createQuery(query)
					.setLong("userId", userId)
					.setLong("credId", credentialId)
					.uniqueResult();
			
			if (result != null) {
				int numberOfAssigned = 0;
				if(returnNumberOfCurrentlyAssignedStudents) {
					numberOfAssigned = result.getAssignedStudents().size();
				}
				instructor = credInstructorFactory.getInstructorData(
						result, result.getUser(), numberOfAssigned, trackChanges);			
		    }
					
			return instructor;
		} catch(Exception e) {
			logger.error(e);
			e.printStackTrace();
			throw new DbConnectionException("Error while loading credential instructor data");
		}
	}
	
	@Override
	@Transactional(readOnly = false) 
	public void assignStudentToInstructor(long studentId, long instructorId, long credId) 
			throws DbConnectionException {
		TargetCredential1 targetCred = credManager.getTargetCredential(credId, studentId, false, false);
		assignStudentToInstructor(instructorId, targetCred);
	}
	
	@Override
	@Transactional(readOnly = false) 
	public void assignStudentToInstructor(long instructorId, long targetCredId) 
			throws DbConnectionException {
		TargetCredential1 targetCred = (TargetCredential1) persistence.currentManager().load(
				TargetCredential1.class, targetCredId);
		assignStudentToInstructor(instructorId, targetCred);
	}
	
	@Transactional(readOnly = false) 
	private void assignStudentToInstructor(long instructorId, TargetCredential1 targetCred) 
			throws DbConnectionException {
		try {
			CredentialInstructor instructor = (CredentialInstructor) persistence
					.currentManager().load(CredentialInstructor.class, instructorId);
			
			setInstructorForStudent(targetCred, instructor);
		} catch(Exception e) {
			logger.error(e);
			e.printStackTrace();
			throw new DbConnectionException("Error while assigning student to an instructor");
		}
	}
	
	@Transactional(readOnly = false) 
	private void setInstructorForStudent(TargetCredential1 targetCred, CredentialInstructor instructor) {
		if(targetCred != null) {
			boolean assigned = instructor != null;
			targetCred.setAssignedToInstructor(assigned);
			targetCred.setInstructor(instructor);
		}	
	}
	
	@Override
	@Transactional(readOnly = true)
	public List<InstructorData> getCredentialInstructorsWithLowestNumberOfStudents(long credentialId, 
			int numberOfInstructorsToReturn, long instructorToExcludeId) throws DbConnectionException {
		try {
			List<InstructorData> instructors = new ArrayList<>();
			StringBuilder queryBuilder = new StringBuilder(
					"SELECT instructor.id, count(student), instructor.maxNumberOfStudents, instructor.user.id " +
					"FROM CredentialInstructor instructor " +
					"LEFT JOIN instructor.assignedStudents student " +
					"WHERE instructor.credential.id = :credId " +
					"GROUP BY instructor.id " +
					"HAVING instructor.maxNumberOfStudents = :unlimitedNo " +
					"OR count(student) < instructor.maxNumberOfStudents ");
			
			if(instructorToExcludeId > 0) {
				queryBuilder.append("AND instructor.id != :excludeId ");
			}
			queryBuilder.append("ORDER BY count(student)");
			
			Query q = persistence
					.currentManager()
					.createQuery(queryBuilder.toString())
					.setLong("credId", credentialId)
					.setInteger("unlimitedNo", 0);
			
			if(instructorToExcludeId > 0) {
				q.setLong("excludeId", instructorToExcludeId);
			}
			q.setFetchSize(numberOfInstructorsToReturn);
			
			@SuppressWarnings("unchecked")
			List<Object[]> result = q.list();
			
			if (result != null && !result.isEmpty()) {
				for(Object[] row : result) {
					if(row != null) {
						InstructorData instructor = new InstructorData(false);
						instructor.setInstructorId((long) row[0]);
						long no = (long) row[1];
						instructor.setNumberOfAssignedStudents((int) no);
						instructor.setMaxNumberOfStudents((int) row[2]);
						UserData user = new UserData();
						user.setId((long) row[3]);
						instructor.setUser(user);
						instructors.add(instructor);
					}
				}
		    }
					
			return instructors;
		} catch(Exception e) {
			logger.error(e);
			e.printStackTrace();
			throw new DbConnectionException("Error while loading credential instructor data");
		}
	}
	
	@Override
    @Transactional(readOnly = false)
    public StudentAssignData assignStudentsToInstructorAutomatically(long credId, 
		List<Long> targetCredIds, long instructorToExcludeId) throws DbConnectionException {
        List<InstructorData> instructors = getCredentialInstructorsWithLowestNumberOfStudents(credId, 
        		targetCredIds.size(), instructorToExcludeId);
        StudentAssignData data = new StudentAssignData();
        if(instructors != null && targetCredIds != null) {
            List<Long> targetCredIdsCopy = new ArrayList<>(targetCredIds);
            Iterator<Long> iterator = targetCredIdsCopy.iterator();
            while(iterator.hasNext()) {
                long tCredId = iterator.next();
                InstructorData instructorToAssign = getInstructorWithLowestNumberOfStudents(instructors);
                if(instructorToAssign == null) {
                	break;
                }
                assignStudentToInstructor(instructorToAssign.getInstructorId(), tCredId);
                instructorToAssign.setNumberOfAssignedStudents(instructorToAssign
                		.getNumberOfAssignedStudents() + 1);
                if(instructorToAssign.isFull()) {
                	instructors.remove(instructorToAssign);
                }
                swapInstructorsIfNeeded(instructors);
                StudentInstructorPair pair = new StudentInstructorPair(tCredId, instructorToAssign);
                data.addAssignedPair(pair);
                iterator.remove();
            }
            if(!targetCredIdsCopy.isEmpty()) {
            	data.setUnassigned(targetCredIdsCopy);
            }
        }
        return data;
    }

	private void swapInstructorsIfNeeded(List<InstructorData> instructors) {
		int size = instructors.size();
		if(size > 1) {
			InstructorData i1 = instructors.get(0);
			InstructorData i2 = instructors.get(1);
			if(i2.getNumberOfAssignedStudents() < i1.getNumberOfAssignedStudents()) {
				Collections.swap(instructors, 0, 1);
			}
		}
	}
	
	/**
	 * Returns instructor with lowest number of students who has available spots from list of instructors
	 * @param instructors
	 * @return
	 */
    private InstructorData getInstructorWithLowestNumberOfStudents(List<InstructorData> instructors) {
        InstructorData instructorWithLowestNumberOfStudents = null;
        if(!instructors.isEmpty()) {
        	InstructorData id = instructors.get(0);
        	if(id.getMaxNumberOfStudents() == 0 || 
        			id.getNumberOfAssignedStudents() < id.getMaxNumberOfStudents()) {
        		instructorWithLowestNumberOfStudents = id;
        	}
        }
        return instructorWithLowestNumberOfStudents;
    }
    
    @Override
	@Transactional(readOnly = true)
	public List<CredentialData> getCredentialIdsAndAssignDateForInstructor(long userId) 
			throws DbConnectionException {
		try {
			List<CredentialData> data = new ArrayList<>();
			String query = 
					"SELECT instructor.credential.id, instructor.dateAssigned " +
					"FROM CredentialInstructor instructor " +
					"WHERE instructor.user.id = :userId";
			
			@SuppressWarnings("unchecked")
			List<Object[]> result = persistence.currentManager().createQuery(query)
					.setLong("userId", userId)
					.list();
			if(result != null) {
				for(Object[] row : result) {
					if(row != null) {
						CredentialData cd = new CredentialData(false);
						cd.setId((long) row[0]);
						cd.setDate((Date) row[1]);
						data.add(cd);
					}
				}
			}
					
			return data;
		} catch(Exception e) {
			logger.error(e);
			e.printStackTrace();
			throw new DbConnectionException("Error while loading credential ids");
		}
	}
    
    @Override
    @Transactional(readOnly = false)
    public void unassignStudentFromInstructor(long userId, long credId) throws DbConnectionException {
    	try {
    		TargetCredential1 targetCred = credManager.getTargetCredential(credId, userId, false, false);
    		setInstructorForStudent(targetCred, null);
		} catch(Exception e) {
			logger.error(e);
			e.printStackTrace();
			throw new DbConnectionException("Error while loading credential ids");
		}
    }
    
    private StudentAssignData assignStudentsAutomatically(long credId, List<Long> targetCredIds, 
    		long instructorId) {
		StudentAssignData result = assignStudentsToInstructorAutomatically(credId, 
				targetCredIds, instructorId);
		List<Long> unassigned = result.getUnassigned();
		if(!unassigned.isEmpty()) {
//			CredentialInstructor instructor = (CredentialInstructor) persistence.currentManager().
//					load(CredentialInstructor.class, instructorId);
			updateStudentsAssigned(null, null, unassigned);
		}
		return result;
	}
	
	@Override
	@Transactional(readOnly = false)
	public StudentAssignData reassignStudentsAutomatically(long instructorId, long credId) 
			throws DbConnectionException {
		try {
			List<Long> targetCredIds = credManager.getTargetCredentialIdsForInstructor(instructorId);
			return assignStudentsAutomatically(credId, targetCredIds, instructorId);
		} catch(Exception e) {
			throw new DbConnectionException("Error while reassigning students");
		}
	}
	
	/**
	  remove instructor from credential and based on {@code reassignAutomatically} parameter, sets all their students
	  as unassigned or automatically assigns students to instructors. With automatic assign, there is a possibility to
	  have unassigned students if maximum capacity is reached for all instructors
	 * @param instructorId
	 * @param credId
	 * @param reassignAutomatically
	 * @return
	 * @throws DbConnectionException
	 */
	@Override
	@Transactional(readOnly = false)
	public StudentAssignData removeInstructorFromCredential(long instructorId, long credId, 
			boolean reassignAutomatically) throws DbConnectionException {
		try {
			CredentialInstructor instructor = (CredentialInstructor) persistence.currentManager().
					load(CredentialInstructor.class, instructorId);
			List<Long> targetCredIds = credManager.getTargetCredentialIdsForInstructor(instructorId);
			StudentAssignData result = null;
			if(reassignAutomatically) {
				result = assignStudentsAutomatically(credId, targetCredIds, 
						instructorId);
			} else {
				updateStudentsAssigned(null, null, targetCredIds);
				result = new StudentAssignData(null, targetCredIds);
			}
			persistence.currentManager().delete(instructor);
			return result;
		} catch(Exception e) {
			logger.error(e);
			e.printStackTrace();
			throw new DbConnectionException("Error while removing instructor from credential");
		}
	}
	
	private void updateStudentsAssigned(CredentialInstructor instructor, List<Long> targetCredIdsToAssign, 
			List<Long> targetCredIdsForUnassign) throws DbConnectionException {
		try {
			String query = 
					"UPDATE " +
					"TargetCredential1 cred " +
				    "set cred.instructor = :instructor, " +
					"cred.assignedToInstructor = :assigned " +
				    "WHERE cred.id IN " +
						"(:ids)";
			
			if(targetCredIdsToAssign != null && !targetCredIdsToAssign.isEmpty()) {
				persistence.currentManager().createQuery(query)
								.setParameter("instructor", instructor)
								.setBoolean("assigned", true)
								.setParameterList("ids", targetCredIdsToAssign)
								.executeUpdate();
			}
			if(targetCredIdsForUnassign != null && !targetCredIdsForUnassign.isEmpty()) {						
				persistence.currentManager().createQuery(query)
								.setParameter("instructor", null)
								.setBoolean("assigned", false)
								.setParameterList("ids", targetCredIdsForUnassign)
								.executeUpdate();
			}
				
		} catch(Exception e) {
			logger.error(e);
			e.printStackTrace();
			throw new DbConnectionException("Error while updating instructor");
		}
	}
	
	public CredentialInstructor addInstructorToCredential(long credId, long userId, 
			int maxNumberOfStudents) throws DbConnectionException {
		try {
			Credential1 cred = (Credential1) persistence
					.currentManager().load(Credential1.class, credId);
			
			User user = (User) persistence
					.currentManager().load(User.class, userId);
			
			CredentialInstructor instructor = new CredentialInstructor();
			instructor.setUser(user);
			instructor.setCredential(cred);
			instructor.setMaxNumberOfStudents(maxNumberOfStudents);
			instructor.setDateAssigned(new Date());
			
			return saveEntity(instructor);
		} catch(Exception e) {
			logger.error(e);
			e.printStackTrace();
			throw new DbConnectionException("Error while assigning student to an instructor");
		}
	}
	
	@Override
	@Transactional(readOnly = false)
	public void updateInstructorAndStudentsAssigned(long credId, InstructorData id, 
			List<Long> studentsToAssign, List<Long> studentsToUnassign) throws DbConnectionException {
		try {
			if(id.hasObjectChanged()) {
				String query = "UPDATE CredentialInstructor instructor SET " +
							   "instructor.maxNumberOfStudents = :maxNo " +
							   "WHERE instructor.id = :instructorId";
				
				persistence.currentManager()
					.createQuery(query)
					.setInteger("maxNo", id.getMaxNumberOfStudents())
					.setLong("instructorId", id.getInstructorId())
					.executeUpdate();
			}
			
			updateStudentsAssignedToInstructor(id.getInstructorId(), credId, studentsToAssign, 
					studentsToUnassign);
				
		} catch(Exception e) {
			logger.error(e);
			e.printStackTrace();
			throw new DbConnectionException("Error while updating instructor");
		}
	}
	
	@Override
	@Transactional(readOnly = false)
	public void updateStudentsAssignedToInstructor(long instructorId, long credId, 
			List<Long> studentsToAssign, List<Long> studentsToUnassign) throws DbConnectionException {
		try {
			CredentialInstructor instructor = (CredentialInstructor) persistence.currentManager()
					.load(CredentialInstructor.class, instructorId);
			
			List<Long> idsAssign = null;
			if(studentsToAssign != null && !studentsToAssign.isEmpty()) {
				idsAssign = credManager.getTargetCredentialIdsForUsers(studentsToAssign, credId);
			}
			List<Long> idsUnAssign = null;
			if(studentsToUnassign != null && !studentsToUnassign.isEmpty()) {		
				idsUnAssign = credManager.getTargetCredentialIdsForUsers(studentsToUnassign, credId);
			}
			
			updateStudentsAssigned(instructor, idsAssign, idsUnAssign);
				
		} catch(Exception e) {
			logger.error(e);
			e.printStackTrace();
			throw new DbConnectionException("Error while updating instructor");
		}
	}

//	/**
//	 * Returns instructor with lowest number of students who has available spots from list of instructors
//	 * @param instructors
//	 * @return
//	 */
//    private InstructorData getInstructorWithLowestNumberOfStudents(List<InstructorData> instructors) {
//        InstructorData instructorWithLowestNumberOfStudents = null;
//        for(InstructorData instructor : instructors) {
//            int max = instructor.getMaxNumberOfStudents();
//            int assigned = instructor.getNumberOfAssignedStudents();
//            if(assigned < max && (instructorWithLowestNumberOfStudents == null || 
//            		assigned < instructorWithLowestNumberOfStudents.getNumberOfAssignedStudents())) {
//                instructorWithLowestNumberOfStudents = instructor;
//            }      
//        }
//        return instructorWithLowestNumberOfStudents;
//    }
	
}