package org.prosolo.services.nodes.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import javax.inject.Inject;

import org.apache.log4j.Logger;
import org.hibernate.Query;
import org.prosolo.bigdata.common.exceptions.DbConnectionException;
import org.prosolo.common.domainmodel.credential.Credential1;
import org.prosolo.common.domainmodel.credential.CredentialInstructor;
import org.prosolo.common.domainmodel.credential.TargetCredential1;
import org.prosolo.common.domainmodel.user.User;
import org.prosolo.services.general.impl.AbstractManagerImpl;
import org.prosolo.services.nodes.AssessmentManager;
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
	@Inject
	private AssessmentManager assessmentManager;
	
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
		assignStudentToInstructor(instructorId, targetCred, true);
	}
	
	@Override
	@Transactional(readOnly = false) 
	public void assignStudentToInstructor(long instructorId, long targetCredId) 
			throws DbConnectionException {
		assignStudentToInstructor(instructorId, targetCredId, true);
	}
	
	@Transactional(readOnly = false) 
	private void assignStudentToInstructor(long instructorId, long targetCredId, boolean updateAsessor) 
			throws DbConnectionException {
		TargetCredential1 targetCred = (TargetCredential1) persistence.currentManager().load(
				TargetCredential1.class, targetCredId);
		assignStudentToInstructor(instructorId, targetCred, updateAsessor);
	}
	
	@Transactional(readOnly = false) 
	private void assignStudentToInstructor(long instructorId, TargetCredential1 targetCred,
			boolean updateAssessor) throws DbConnectionException {
		try {
			CredentialInstructor instructor = (CredentialInstructor) persistence
					.currentManager().load(CredentialInstructor.class, instructorId);
			
			setInstructorForStudent(targetCred, instructor, updateAssessor);
		} catch(Exception e) {
			logger.error(e);
			e.printStackTrace();
			throw new DbConnectionException("Error while assigning student to an instructor");
		}
	}
	
	@Transactional(readOnly = false) 
	private void setInstructorForStudent(TargetCredential1 targetCred, CredentialInstructor instructor,
			boolean updateAssessor) {
		if(targetCred != null) {
			boolean assigned = instructor != null;
			targetCred.setAssignedToInstructor(assigned);
			targetCred.setInstructor(instructor);
			
			if(updateAssessor) {
				//update assessor for default assessment if exists
				long instructorUserId = instructor != null ? instructor.getUser().getId() : 0;
				assessmentManager.updateDefaultAssessmentAssessor(targetCred.getId(), 
						instructorUserId);
			}
		}	
	}
	
	@Override
	@Transactional(readOnly = true)
	public List<InstructorData> getCredentialInstructorsWithLowestNumberOfStudents(long credentialId, 
			long instructorToExcludeId) throws DbConnectionException {
		try {
			List<InstructorData> instructors = new ArrayList<>();
			StringBuilder queryBuilder = new StringBuilder(
					"SELECT instructor.id, count(student), instructor.maxNumberOfStudents, instructor.user.id " +
					"FROM CredentialInstructor instructor " +
					"LEFT JOIN instructor.assignedStudents student " +
					"WHERE instructor.credential.id = :credId ");
			
			if(instructorToExcludeId > 0) {
				queryBuilder.append("AND instructor.id != :excludeId ");
			}
			
			queryBuilder.append(
					"GROUP BY instructor.id " +
					"HAVING instructor.maxNumberOfStudents = :unlimitedNo " +
					"OR count(student) < instructor.maxNumberOfStudents ");
			
			queryBuilder.append("ORDER BY count(student)");
			
			Query q = persistence
					.currentManager()
					.createQuery(queryBuilder.toString())
					.setLong("credId", credentialId)
					.setInteger("unlimitedNo", 0);
			
			if(instructorToExcludeId > 0) {
				q.setLong("excludeId", instructorToExcludeId);
			}
			//q.setFetchSize(numberOfInstructorsToReturn);
			
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
		List<TargetCredential1> targetCreds, long instructorToExcludeId) throws DbConnectionException {
        return assignStudentsToInstructorAutomatically(credId, targetCreds, instructorToExcludeId, true);
    }
	
	@Override
    @Transactional(readOnly = false)
    public StudentAssignData assignStudentsToInstructorAutomatically(long credId, List<TargetCredential1> targetCreds,
    		long instructorToExcludeId, boolean updateAssessor) throws DbConnectionException {
        List<InstructorData> instructors = getCredentialInstructorsWithLowestNumberOfStudents(credId, 
        		instructorToExcludeId);
        StudentAssignData data = new StudentAssignData();
        if(instructors != null && targetCreds != null) {
            List<TargetCredential1> targetCredsCopy = new ArrayList<>(targetCreds);
            Iterator<TargetCredential1> iterator = targetCredsCopy.iterator();
            while(iterator.hasNext()) {
                TargetCredential1 tCred = iterator.next();
                InstructorData instructorToAssign = getInstructorWithLowestNumberOfStudents(instructors, tCred.getUser().getId());
                if(instructorToAssign != null) {
	                assignStudentToInstructor(instructorToAssign.getInstructorId(), tCred.getId(), updateAssessor);
	                instructorToAssign.setNumberOfAssignedStudents(instructorToAssign
	                		.getNumberOfAssignedStudents() + 1);
	                if(instructorToAssign.isFull()) {
	                	instructors.remove(instructorToAssign);
	                	if(instructors.isEmpty()) {
		                	break;
		                }
	                }
	                
	                StudentInstructorPair pair = new StudentInstructorPair(tCred.getId(), instructorToAssign);
	                data.addAssignedPair(pair);
	                iterator.remove();
                }
            }
            if(!targetCredsCopy.isEmpty()) {
            	List<Long> unassignedCreds = new ArrayList<>();
            	for(TargetCredential1 tc : targetCredsCopy) {
            		unassignedCreds.add(tc.getId());
            	}
            	data.setUnassigned(unassignedCreds);
            }
        }
        return data;
    }
	
	/**
	 * Returns instructor with lowest number of students who has available spots from list of instructors
	 * @param instructors
	 * @return
	 */
    private InstructorData getInstructorWithLowestNumberOfStudents(List<InstructorData> instructors, long userToExclude) {
        InstructorData instructorWithLowestNumberOfStudents = null;
        int minNumberOfAssignedStudents = Integer.MAX_VALUE;
        for(InstructorData id : instructors) {
        	if(id.getUser().getId() != userToExclude && (id.getMaxNumberOfStudents() == 0 || 
        			id.getNumberOfAssignedStudents() < id.getMaxNumberOfStudents()) 
        			&& id.getNumberOfAssignedStudents() < minNumberOfAssignedStudents) {
        		instructorWithLowestNumberOfStudents = id;
        		minNumberOfAssignedStudents = id.getNumberOfAssignedStudents();
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
    		setInstructorForStudent(targetCred, null, true);
		} catch(Exception e) {
			logger.error(e);
			e.printStackTrace();
			throw new DbConnectionException("Error while loading credential ids");
		}
    }
    
    @Transactional(readOnly = false)
    private StudentAssignData assignStudentsAutomatically(long credId, List<TargetCredential1> targetCreds, 
    		long instructorId) {
		StudentAssignData result = assignStudentsToInstructorAutomatically(credId, 
				targetCreds, instructorId);
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
			List<TargetCredential1> targetCreds = credManager.getTargetCredentialsForInstructor(instructorId);
			return assignStudentsAutomatically(credId, targetCreds, instructorId);
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
			List<TargetCredential1> targetCreds = credManager.getTargetCredentialsForInstructor(instructorId);
			StudentAssignData result = null;
			if(reassignAutomatically) {
				result = assignStudentsAutomatically(credId, targetCreds, 
						instructorId);
			} else {
				List<Long> unassignedCreds = new ArrayList<>();
            	for(TargetCredential1 tc : targetCreds) {
            		unassignedCreds.add(tc.getId());
            	}
				updateStudentsAssigned(null, null, unassignedCreds);
				result = new StudentAssignData(null, unassignedCreds);
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
				
				assessmentManager.updateDefaultAssessmentsAssessor(targetCredIdsToAssign, 
						instructor.getUser().getId());
			}
			if(targetCredIdsForUnassign != null && !targetCredIdsForUnassign.isEmpty()) {						
				persistence.currentManager().createQuery(query)
								.setParameter("instructor", null)
								.setBoolean("assigned", false)
								.setParameterList("ids", targetCredIdsForUnassign)
								.executeUpdate();
				
				assessmentManager.updateDefaultAssessmentsAssessor(targetCredIdsForUnassign, 0);
			}
				
		} catch(Exception e) {
			logger.error(e);
			e.printStackTrace();
			throw new DbConnectionException("Error while updating instructor");
		}
	}
	
	@Override
	@Transactional(readOnly = false)
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
			throw new DbConnectionException("Error while assigning instructor to a credential");
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
	
	@Override
	@Transactional(readOnly = true)
	public List<InstructorData> getCredentialInstructors(long credentialId, 
			boolean returnNumberOfCurrentlyAssignedStudents, int limit, boolean trackChanges) 
					throws DbConnectionException {
		try {
			String query = 
					"SELECT credInstructor " +
					"FROM CredentialInstructor credInstructor " +
					"INNER JOIN fetch credInstructor.user instructor " +
					"WHERE credInstructor.credential.id = :credId " +
					"ORDER BY credInstructor.dateAssigned DESC";
			
			@SuppressWarnings("unchecked")
			List<CredentialInstructor> result = persistence
					.currentManager()
					.createQuery(query)
					.setLong("credId", credentialId)
					.setMaxResults(limit)
					.list();
			
			List<InstructorData> instructors = new ArrayList<>();
			if (result != null) {
				for(CredentialInstructor inst : result) {
					int numberOfAssigned = 0;
					if(returnNumberOfCurrentlyAssignedStudents) {
						numberOfAssigned = inst.getAssignedStudents().size();
					}
					InstructorData instructor = credInstructorFactory.getInstructorData(
							inst, inst.getUser(), numberOfAssigned, trackChanges);
					instructors.add(instructor);
				}
		    }
					
			return instructors;
		} catch(Exception e) {
			logger.error(e);
			e.printStackTrace();
			throw new DbConnectionException("Error while loading credential instructors");
		}
	}
	
	@Override
	@Transactional(readOnly = true)
	public long getCredentialInstructorsCount(long credentialId) 
					throws DbConnectionException {
		try {
			String query = 
					"SELECT COUNT(credInstructor.id) " +
					"FROM CredentialInstructor credInstructor " +
					"WHERE credInstructor.credential.id = :credId";
			
			Long result = (Long) persistence
					.currentManager()
					.createQuery(query)
					.setLong("credId", credentialId)
					.uniqueResult();
					
			return result == null ? 0 : result;
		} catch(Exception e) {
			logger.error(e);
			e.printStackTrace();
			throw new DbConnectionException("Error while loading credential instructors number");
		}
	}
	
	@Override
	@Transactional(readOnly = true)
	public List<Long> getCredentialInstructorsUserIds(long credentialId) 
			throws DbConnectionException {
		try {
			String query = 
					"SELECT instructor.id " +
					"FROM CredentialInstructor credInstructor " +
					"INNER JOIN credInstructor.user instructor " +
					"WHERE credInstructor.credential.id = :credId";
			
			@SuppressWarnings("unchecked")
			List<Long> result = persistence
					.currentManager()
					.createQuery(query)
					.setLong("credId", credentialId)
					.list();
			
			return result != null ? result : Collections.emptyList();
		} catch(Exception e) {
			logger.error(e);
			e.printStackTrace();
			throw new DbConnectionException("Error while retrieving credential instructors user ids");
		}
	}
	
}