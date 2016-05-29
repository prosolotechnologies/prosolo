package org.prosolo.services.nodes.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.apache.log4j.Logger;
import org.hibernate.Query;
import org.prosolo.common.domainmodel.credential.CredentialInstructor;
import org.prosolo.common.domainmodel.credential.TargetCredential1;
import org.prosolo.services.general.impl.AbstractManagerImpl;
import org.prosolo.services.lti.exceptions.DbConnectionException;
import org.prosolo.services.nodes.CredentialInstructorManager;
import org.prosolo.services.nodes.CredentialManager;
import org.prosolo.services.nodes.data.CredentialData;
import org.prosolo.services.nodes.data.InstructorData;
import org.prosolo.services.nodes.data.UserData;
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
			boolean returnNumberOfCurrentlyAssignedStudents) throws DbConnectionException {
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
						result, result.getUser(), numberOfAssigned);			
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
					"HAVING count(student) < instructor.maxNumberOfStudents ");
			
			if(instructorToExcludeId > 0) {
				queryBuilder.append("AND instructor.id != :excludeId ");
			}
			queryBuilder.append("ORDER BY count(student)");
			
			Query q = persistence
					.currentManager()
					.createQuery(queryBuilder.toString())
					.setLong("credId", credentialId);
			
			if(instructorToExcludeId > 0) {
				q.setLong("excludeId", instructorToExcludeId);
			}
			q.setFetchSize(numberOfInstructorsToReturn);
			
			@SuppressWarnings("unchecked")
			List<Object[]> result = q.list();
			
			if (result != null && !result.isEmpty()) {
				for(Object[] row : result) {
					if(row != null) {
						InstructorData instructor = new InstructorData();
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
    public Map<String, Object> assignStudentsToInstructorAutomatically(long credId, 
		List<Long> targetCredIds, long instructorToExcludeId) throws DbConnectionException {
        List<InstructorData> instructors = getCredentialInstructorsWithLowestNumberOfStudents(credId, 
        		targetCredIds.size(), instructorToExcludeId);
        Map<String, Object> resultMap = new HashMap<>();
        Map<Long, InstructorData> assigned = new HashMap<>();
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
                swapInstructorsIfNeeded(instructors);
                assigned.put(tCredId, instructorToAssign);
                iterator.remove();
            }
            if(!targetCredIdsCopy.isEmpty()) {
                resultMap.put("unassigned", targetCredIdsCopy);
            }
            resultMap.put("assigned", assigned);
            return resultMap;
        }
        return null;
    }

	private void swapInstructorsIfNeeded(List<InstructorData> instructors) {
		InstructorData i1 = instructors.get(0);
		if(instructors.size() > 1) {
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
        	if(id.getNumberOfAssignedStudents() < id.getMaxNumberOfStudents()) {
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