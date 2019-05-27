package org.prosolo.services.nodes.impl;

import org.apache.log4j.Logger;
import org.hibernate.Query;
import org.hibernate.exception.ConstraintViolationException;
import org.prosolo.bigdata.common.exceptions.DbConnectionException;
import org.prosolo.bigdata.common.exceptions.IllegalDataStateException;
import org.prosolo.common.domainmodel.assessment.AssessmentType;
import org.prosolo.common.domainmodel.assessment.AssessorAssignmentMethod;
import org.prosolo.common.domainmodel.credential.Credential1;
import org.prosolo.common.domainmodel.credential.CredentialInstructor;
import org.prosolo.common.domainmodel.credential.InstructorWithdrawal;
import org.prosolo.common.domainmodel.credential.TargetCredential1;
import org.prosolo.common.domainmodel.events.EventType;
import org.prosolo.common.domainmodel.user.User;
import org.prosolo.common.domainmodel.user.UserGroupPrivilege;
import org.prosolo.common.event.context.data.UserContextData;
import org.prosolo.common.util.date.DateUtil;
import org.prosolo.services.assessment.AssessmentManager;
import org.prosolo.services.data.Result;
import org.prosolo.services.event.EventFactory;
import org.prosolo.services.general.impl.AbstractManagerImpl;
import org.prosolo.services.nodes.CredentialInstructorManager;
import org.prosolo.services.nodes.CredentialManager;
import org.prosolo.services.nodes.config.credential.CredentialLoadConfig;
import org.prosolo.services.nodes.data.credential.CredentialData;
import org.prosolo.services.nodes.data.instructor.InstructorData;
import org.prosolo.services.nodes.data.instructor.StudentAssignData;
import org.prosolo.services.nodes.data.instructor.StudentInstructorPair;
import org.prosolo.services.nodes.factory.CredentialInstructorDataFactory;
import org.prosolo.services.user.UserGroupManager;
import org.prosolo.services.user.data.UserBasicData;
import org.prosolo.services.user.data.UserData;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import java.util.*;

@Service("org.prosolo.services.nodes.CredentialInstructorManager")
public class CredentialInstructorManagerImpl extends AbstractManagerImpl implements CredentialInstructorManager {

	private static final long serialVersionUID = 394489300560570371L;

	private static Logger logger = Logger.getLogger(CredentialInstructorManagerImpl.class);
	
	@Inject private CredentialManager credManager;
	@Inject private CredentialInstructorDataFactory credInstructorFactory;
	@Inject private AssessmentManager assessmentManager;
	@Inject private UserGroupManager userGroupManager;
	@Inject private EventFactory eventFactory;
	@Inject private CredentialInstructorManager credInstructorManager;
	
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
			throw new DbConnectionException("Error loading credential instructor data");
		}
	}
	
	//not transactional
	@Override
	public void assignStudentToInstructor(long studentId, long instructorId, long credId,
			UserContextData context) throws DbConnectionException {
		Result<Void> res = credInstructorManager.assignStudentToInstructorAndGetEvents(studentId, instructorId, credId, context);
		eventFactory.generateAndPublishEvents(res.getEventQueue());
	}
	
	@Override
	@Transactional(readOnly = false) 
	public Result<Void> assignStudentToInstructorAndGetEvents(long studentId, long instructorId, long credId, UserContextData context) throws DbConnectionException {
		try {
			TargetCredential1 targetCred = credManager.getTargetCredential(credId, studentId, CredentialLoadConfig.builder().create());
			return assignStudentToInstructor(instructorId, targetCred, context);
		} catch (DbConnectionException e) {
			logger.error("Error", e);
			throw e;
		} catch (Exception e) {
			logger.error("Error", e);
			throw new DbConnectionException("Error assigning student to an instructor");
		}
	}
	
	//not transactional
//	@Override
//	public void assignStudentToInstructor(long instructorId, long targetCredId, long formerInstructorId,
//			UserContextData context) throws DbConnectionException {
//		Result<Void> res = credInstructorManager.assignStudentToInstructorAndGetEvents(
//				instructorId, targetCredId, formerInstructorId, context);
//		eventFactory.generateEvents(res.getEventQueue());
//	}
//
//	@Override
//	@Transactional(readOnly = false)
//	public Result<Void> assignStudentToInstructorAndGetEvents(long instructorId, long targetCredId, long formerInstructorId,
//			UserContextData context) throws DbConnectionException {
//		return assignStudentToInstructor(instructorId, targetCredId, formerInstructorId, true, context);
//	}
	
	private Result<Void> assignStudentToInstructor(long instructorId, long targetCredId,
                                                   UserContextData context) throws DbConnectionException {
		TargetCredential1 targetCred = (TargetCredential1) persistence.currentManager().load(
				TargetCredential1.class, targetCredId);
		return assignStudentToInstructor(instructorId, targetCred, context);
	}
	
	private Result<Void> assignStudentToInstructor(long instructorId, TargetCredential1 targetCred, UserContextData context)
					throws DbConnectionException {
		try {
			CredentialInstructor instructor = (CredentialInstructor) persistence
					.currentManager().load(CredentialInstructor.class, instructorId);
			
			return setInstructorForStudent(targetCred, instructor, context);
		} catch(Exception e) {
			logger.error("Error", e);
			throw new DbConnectionException("Error assigning student to an instructor");
		}
	}
	
	private Result<Void> setInstructorForStudent(TargetCredential1 targetCred, CredentialInstructor instructor, UserContextData context) throws IllegalDataStateException {
		Result<Void> result = new Result<>();
		if (targetCred != null) {
		    CredentialInstructor formerInstructor = targetCred.getInstructor();
		    long formerInstructorId = formerInstructor == null ? 0 : formerInstructor.getId();
		    long newInstructorId = instructor == null ? 0 : instructor.getId();
		    if (formerInstructorId == newInstructorId) {
		        return result;
            }
			boolean assigned = instructor != null;
			targetCred.setAssignedToInstructor(assigned);
			targetCred.setInstructor(instructor);

			//decline assessment by previous instructor if existed and create new if new assessor if not null
			if (formerInstructor != null) {
				assessmentManager.declineCredentialAssessmentIfActive(targetCred.getCredential().getId(), targetCred.getUser().getId(), formerInstructor.getUser().getId(), AssessmentType.INSTRUCTOR_ASSESSMENT);
			}
			if (instructor != null) {
				result.appendEvents(assessmentManager.createInstructorAssessmentAndGetEvents(targetCred, instructor.getUser().getId(), context).getEventQueue());
			}
			/*
			 * if student is assigned to instructor and id of former instructor passed is 0, then student
			 * assigned to instructor event is generated, if id of former instructor is greater than 0,
			 * student reassigned to instructor event is generated and if instructor is null and id of
			 * former instructor is greater than 0, student unassigned from instructor event is generated
			 */ 
			long targetInstructorUserId = 0;
			EventType eventType = null;
			Map<String, String> params = null;
			if (instructor != null) {
				targetInstructorUserId = instructor.getUser().getId();
				params = new HashMap<>();
				params.put("credId", targetCred.getCredential().getId() + "");
				if (formerInstructor != null) {
					eventType = EventType.STUDENT_REASSIGNED_TO_INSTRUCTOR;
					params.put("reassignedFromInstructorUserId", formerInstructor.getUser().getId() + "");
				} else {
					eventType = EventType.STUDENT_ASSIGNED_TO_INSTRUCTOR;
				}
			} else if (formerInstructor != null) {
				eventType = EventType.STUDENT_UNASSIGNED_FROM_INSTRUCTOR;
				targetInstructorUserId = formerInstructor.getUser().getId();
				params = new HashMap<>();
				params.put("credId", targetCred.getCredential().getId() + "");
			}
			User target = new User();
			target.setId(targetInstructorUserId);
			User object = new User();
			object.setId(targetCred.getUser().getId());
			result.appendEvent(eventFactory.generateEventData(eventType, context, object, target, null, params));
		}	
		return result;
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
					"GROUP BY instructor.id, instructor.maxNumberOfStudents, instructor.user.id " +
					"HAVING instructor.maxNumberOfStudents = :unlimitedNo " +
					"OR count(student) < instructor.maxNumberOfStudents ");
			
			queryBuilder.append("ORDER BY count(student)");
			
			Query q = persistence
					.currentManager()
					.createQuery(queryBuilder.toString())
					.setLong("credId", credentialId)
					.setInteger("unlimitedNo", 0);

			if (instructorToExcludeId > 0) {
				q.setLong("excludeId", instructorToExcludeId);
			}
			
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
						instructor.setWithdrawList(getStudentIdsOnInstructorsWithdrawnList(user.getId(), credentialId));
						instructors.add(instructor);
					}
				}
		    }
					
			return instructors;
		} catch(Exception e) {
			logger.error(e);
			e.printStackTrace();
			throw new DbConnectionException("Error loading credential instructor data");
		}
	}

	private List<Long> getStudentIdsOnInstructorsWithdrawnList(long instructorUserId, long credentialId) {
		String q =
				"SELECT iw.targetCredential.user.id FROM InstructorWithdrawal iw " +
				"WHERE iw.targetCredential.credential.id = :credId " +
				"AND iw.instructor.id = :instructorUserId";
		return (List<Long>) persistence.currentManager().createQuery(q)
				.setLong("credId", credentialId)
				.setLong("instructorUserId", instructorUserId)
				.list();
	}
	
	@Override
    @Transactional
    public Result<StudentAssignData> assignStudentsToInstructorAutomatically(long credId, 
    		List<TargetCredential1> targetCreds, long instructorIdToExclude, UserContextData context) throws DbConnectionException, IllegalDataStateException {
		Result<StudentAssignData> result = new Result<>();
        List<InstructorData> instructors = getCredentialInstructorsWithLowestNumberOfStudents(credId,
                instructorIdToExclude);
        StudentAssignData data = new StudentAssignData();
        if (instructors != null && targetCreds != null) {
            List<TargetCredential1> targetCredsCopy = new ArrayList<>(targetCreds);
            Iterator<TargetCredential1> iterator = targetCredsCopy.iterator();
            while (iterator.hasNext()) {
                TargetCredential1 tCred = iterator.next();
                InstructorData instructorToAssign = getInstructorWithLowestNumberOfStudents(instructors, 
                		tCred.getUser().getId());
                if (instructorToAssign != null) {
	                result.appendEvents(assignStudentToInstructor(instructorToAssign.getInstructorId(), tCred.getId(), context).getEventQueue());

	                CredentialInstructor ci = new CredentialInstructor();
	                User instUser = new User();
	                instUser.setId(instructorToAssign.getUser().getId());
	                ci.setId(instructorToAssign.getInstructorId());
	                ci.setUser(instUser);
	                data.addAssignedPair(new StudentInstructorPair(tCred, ci));
	                instructorToAssign.setNumberOfAssignedStudents(instructorToAssign
	                		.getNumberOfAssignedStudents() + 1);
	                if (instructorToAssign.isFull()) {
	                	instructors.remove(instructorToAssign);
	                }
	                
	                iterator.remove();
                    if (instructors.isEmpty()) {
                        break;
                    }
                }
            }
            if (!targetCredsCopy.isEmpty()) {
            	for(TargetCredential1 tc : targetCredsCopy) {
            		result.appendEvents(setInstructorForStudent(tc, null, context).getEventQueue());
            		data.addUnassigned(tc);
            	}
            }
        }
        result.setResult(data);
        return result;
    }
	
	/**
	 * Returns instructor with lowest number of students who has available spots from list of instructors
	 * @param instructors
	 * @return
	 */
    private InstructorData getInstructorWithLowestNumberOfStudents(List<InstructorData> instructors, long studentUserId) {
        InstructorData instructorWithLowestNumberOfStudents = null;
        int minNumberOfAssignedStudents = Integer.MAX_VALUE;
        for (InstructorData id : instructors) {
            boolean isStudentOnWithdrawnList = id.getWithdrawList()
                    .stream()
                    .anyMatch(studentId -> studentId == studentUserId);
        	if (!isStudentOnWithdrawnList && id.getUser().getId() != studentUserId && (id.getMaxNumberOfStudents() == 0 ||
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
						cd.getIdData().setId((long) row[0]);
						cd.setDate((Date) row[1]);
						data.add(cd);
					}
				}
			}
					
			return data;
		} catch(Exception e) {
			logger.error(e);
			e.printStackTrace();
			throw new DbConnectionException("Error loading credential ids");
		}
	}
    
    //not transactional
    @Override
    public void unassignStudentFromInstructor(long userId, long credId, UserContextData context)
    		throws DbConnectionException {
		Result<Void> res = credInstructorManager.unassignStudentFromInstructorAndGetEvents(
				userId, credId, context);
		eventFactory.generateAndPublishEvents(res.getEventQueue());
    }
    
    @Override
    @Transactional(readOnly = false)
    public Result<Void> unassignStudentFromInstructorAndGetEvents(long userId, long credId, UserContextData context)
    		throws DbConnectionException {
    	try {
    		TargetCredential1 targetCred = credManager.getTargetCredential(credId, userId, CredentialLoadConfig.builder().setLoadInstructor(true).create());
    		return setInstructorForStudent(targetCred, null, context);
		} catch(Exception e) {
			logger.error("Error", e);
			throw new DbConnectionException("Error loading credential ids");
		}
    }
	
	@Override
	@Transactional
	public Result<Void> reassignStudentsAutomatically(long instructorId, long credId, UserContextData context)
			throws DbConnectionException {
		try {
			List<TargetCredential1> targetCreds = credManager.getTargetCredentialsForInstructor(instructorId);
			Result<StudentAssignData> res = assignStudentsToInstructorAutomatically(credId, targetCreds, instructorId, context);
			return Result.of(res.getEventQueue());
		} catch(Exception e) {
			throw new DbConnectionException("Error reassigning students");
		}
	}
	
	//not transactional
	@Override
	public void removeInstructorFromCredential(long instructorId, long credId, 
			boolean reassignAutomatically, UserContextData context)
					throws DbConnectionException {
		Result<Void> res = credInstructorManager.removeInstructorFromCredentialAndGetEvents(
				instructorId, credId, reassignAutomatically, context);
		eventFactory.generateAndPublishEvents(res.getEventQueue());
	}
	
	@Override
	@Transactional(readOnly = false)
	public Result<Void> removeInstructorFromCredentialAndGetEvents(long instructorId, long credId, 
			boolean reassignAutomatically, UserContextData context) throws DbConnectionException {
		try {
			Result<Void> res = new Result<>();
			CredentialInstructor instructor = (CredentialInstructor) persistence.currentManager().
					load(CredentialInstructor.class, instructorId);
			List<TargetCredential1> targetCreds = credManager.getTargetCredentialsForInstructor(instructorId);
			if (reassignAutomatically) {
				res.appendEvents(assignStudentsToInstructorAutomatically(
						credId, targetCreds, instructorId, context).getEventQueue());
			} else {
            	for(TargetCredential1 tc : targetCreds) {
					res.appendEvents(setInstructorForStudent(tc, null, context).getEventQueue());
            	}
			}
			res.appendEvents(userGroupManager.removeUserFromDefaultCredentialGroupAndGetEvents(
					instructor.getUser().getId(), credId, UserGroupPrivilege.Instruct, context).getEventQueue());
			
			persistence.currentManager().delete(instructor);
			
			Credential1 cred = new Credential1();
			cred.setId(credId);
			User instr = new User();
			instr.setId(instructor.getUser().getId());
			res.appendEvent(eventFactory.generateEventData(
				EventType.INSTRUCTOR_REMOVED_FROM_CREDENTIAL, context, instr, cred,null,null));
				
			return res;
		} catch(Exception e) {
			logger.error("Error", e);
			throw new DbConnectionException("Error removing instructor from credential");
		}
	}
	
	private Result<Void> updateStudentsAssigned(CredentialInstructor instructor, 
			List<TargetCredential1> targetCredsToAssign, List<TargetCredential1> targetCredsToUnassign, UserContextData context)
			throws DbConnectionException {
		Result<Void> result = new Result<>();
		try {
			if (targetCredsToAssign != null && !targetCredsToAssign.isEmpty()) {
			    for (TargetCredential1 tc : targetCredsToAssign) {
					result.appendEvents(setInstructorForStudent(tc, instructor, context).getEventQueue());
			    }
			}
			if (targetCredsToUnassign != null && !targetCredsToUnassign.isEmpty()) {	
				for (TargetCredential1 tc : targetCredsToUnassign) {
					result.appendEvents(setInstructorForStudent(tc, null, context).getEventQueue());
				}
			}
			return result;	
		} catch(Exception e) {
			logger.error("Error", e);
			throw new DbConnectionException("Error updating instructor");
		}
	}
	
	//not transactional
	@Override
	public void addInstructorToCredential(long credId, long userId, 
			int maxNumberOfStudents, UserContextData context) throws DbConnectionException {
		//self invocation to trigger spring interception and transaction start
		eventFactory.generateAndPublishEvents(credInstructorManager.addInstructorToCredentialAndGetEvents(
				credId, userId, maxNumberOfStudents, context).getEventQueue());
	}
	
	@Override
	@Transactional(readOnly = false)
	public Result<CredentialInstructor> addInstructorToCredentialAndGetEvents(long credId, long userId, 
			int maxNumberOfStudents, UserContextData context) throws DbConnectionException {
		try {
			Result<CredentialInstructor> res = new Result<>();
			
			Credential1 cred = (Credential1) persistence
					.currentManager().load(Credential1.class, credId);
			
			User user = (User) persistence
					.currentManager().load(User.class, userId);
			
			CredentialInstructor instructor = new CredentialInstructor();
			instructor.setUser(user);
			instructor.setCredential(cred);
			instructor.setMaxNumberOfStudents(maxNumberOfStudents);
			instructor.setDateAssigned(new Date());
			
			saveEntity(instructor);
			
			//assign instruct privilege for newly added instructor
			res.appendEvents(userGroupManager.saveUserToDefaultCredentialGroupAndGetEvents(
					userId, credId, UserGroupPrivilege.Instruct, context).getEventQueue());
			
			Credential1 credential = new Credential1();
			credential.setId(credId);
			User instr = new User();
			instr.setId(userId);
			Map<String, String> params = new HashMap<>();
			params.put("dateAssigned", DateUtil.getMillisFromDate(instructor.getDateAssigned()) + "");
			res.appendEvent(eventFactory.generateEventData(
					EventType.INSTRUCTOR_ASSIGNED_TO_CREDENTIAL, context, instr, credential, null, params));

			res.setResult(instructor);
			return res;
		} catch(Exception e) {
			logger.error("Error", e);
			throw new DbConnectionException("Error assigning instructor to a credential");
		}
	}
	
	//not transactional
	@Override
	public void updateInstructorAndStudentsAssigned(long credId, InstructorData id, 
			List<Long> studentsToAssign, List<Long> studentsToUnassign, UserContextData context)
			throws DbConnectionException {
		Result<Void> res = credInstructorManager.updateInstructorAndStudentsAssignedAndGetEvents(
				credId, id, studentsToAssign, studentsToUnassign, context);
		eventFactory.generateAndPublishEvents(res.getEventQueue());
	}
	
	@Override
	@Transactional(readOnly = false)
	public Result<Void> updateInstructorAndStudentsAssignedAndGetEvents(long credId, InstructorData id,
			List<Long> studentsToAssign, List<Long> studentsToUnassign, UserContextData context)
			throws DbConnectionException {
		try {
			if (id.hasObjectChanged()) {
				String query = "UPDATE CredentialInstructor instructor SET " +
							   "instructor.maxNumberOfStudents = :maxNo " +
							   "WHERE instructor.id = :instructorId";
				
				persistence.currentManager()
					.createQuery(query)
					.setInteger("maxNo", id.getMaxNumberOfStudents())
					.setLong("instructorId", id.getInstructorId())
					.executeUpdate();
			}
			
			return updateStudentsAssignedToInstructor(id.getInstructorId(), credId, studentsToAssign, studentsToUnassign, context);
				
		} catch(Exception e) {
			logger.error("Error", e);
			throw new DbConnectionException("Error updating instructor");
		}
	}
	
	@Override
	@Transactional(readOnly = false)
	public Result<Void> updateStudentsAssignedToInstructor(long instructorId, long credId, 
			List<Long> studentsToAssign, List<Long> studentsToUnassign, UserContextData context) throws DbConnectionException {
		try {
			CredentialInstructor instructor = (CredentialInstructor) persistence.currentManager()
					.load(CredentialInstructor.class, instructorId);
			
			List<TargetCredential1> studentCredsToAssign = null;
			if (studentsToAssign != null && !studentsToAssign.isEmpty()) {
				studentCredsToAssign = credManager.getTargetCredentialsForUsers(studentsToAssign, credId);
			}
			List<TargetCredential1> studentCredsToUnassign = null;
			if (studentsToUnassign != null && !studentsToUnassign.isEmpty()) {		
				studentCredsToUnassign = credManager.getTargetCredentialsForUsers(studentsToUnassign, credId);
			}
			
			return updateStudentsAssigned(instructor, studentCredsToAssign, studentCredsToUnassign, context);
				
		} catch(Exception e) {
			logger.error("Error", e);
			throw new DbConnectionException("Error updating instructor");
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
			throw new DbConnectionException("Error loading credential instructors");
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
			throw new DbConnectionException("Error loading credential instructors number");
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
			
			return result != null ? result : new ArrayList<>();
		} catch(Exception e) {
			logger.error(e);
			e.printStackTrace();
			throw new DbConnectionException("Error retrieving credential instructors user ids");
		}
	}

	@Override
	@Transactional(readOnly = true)
	public List<UserBasicData> getCredentialInstructorsBasicUserData(long credentialId, boolean returnInstructorsWithoutAssignedStudents) {
		try {
			StringBuilder query = new StringBuilder(
					"SELECT DISTINCT user " +
					"FROM CredentialInstructor credInstructor " +
					"INNER JOIN credInstructor.user user ");
			if (!returnInstructorsWithoutAssignedStudents) {
				query.append("INNER JOIN credInstructor.assignedStudents ");
			}
			query.append(
					"WHERE credInstructor.credential.id = :credId " +
					"ORDER BY user.name, user.lastname");

			List<User> result = (List<User>) persistence
					.currentManager()
					.createQuery(query.toString())
					.setLong("credId", credentialId)
					.list();

			List<UserBasicData> users = new ArrayList<>();
			for (User user : result) {
				users.add(new UserBasicData(user.getId(), user.getFullName()));
			}
			return users;
		} catch(Exception e) {
			logger.error("Error", e);
			throw new DbConnectionException("Error loading credential instructors");
		}
	}

	@Override
	public void withdrawFromBeingInstructor(long credentialId, long studentUserId, UserContextData context) throws IllegalDataStateException {
		Result<Void> res = credInstructorManager.withdrawFromBeingInstructorAndGetEvents(credentialId, studentUserId, context);
		eventFactory.generateAndPublishEvents(res.getEventQueue());
	}

	@Override
	@Transactional
	public Result<Void> withdrawFromBeingInstructorAndGetEvents(long credentialId, long studentUserId, UserContextData context) throws IllegalDataStateException {
		long targetCredentialId = credManager.getTargetCredentialId(credentialId, studentUserId);
		if (targetCredentialId > 0) {
			return withdrawFromBeingInstructorAndGetEvents(targetCredentialId, context);
		} else {
			throw new IllegalDataStateException("Student is not enrolled in a credential");
		}
	}

	@Override
	public void withdrawFromBeingInstructor(long targetCredentialId, UserContextData context) throws IllegalDataStateException {
    	Result<Void> res = credInstructorManager.withdrawFromBeingInstructorAndGetEvents(targetCredentialId, context);
    	eventFactory.generateAndPublishEvents(res.getEventQueue());
	}

	@Override
	@Transactional
	public Result<Void> withdrawFromBeingInstructorAndGetEvents(long targetCredentialId, UserContextData context) throws IllegalDataStateException {
		try {
            TargetCredential1 tc = (TargetCredential1) persistence.currentManager().load(TargetCredential1.class, targetCredentialId);
            if (tc.getInstructor() == null || tc.getInstructor().getUser().getId() != context.getActorId()) {
                //if user that triggered withdrawal is not actually assigned instructor throw exception
                throw new IllegalDataStateException("This user is not currently assigned as instructor so he can't withdraw");
            }
            Result<Void> result = new Result<>();
            saveInstructorWithdrawal(tc, context.getActorId());
            if (tc.getCredential().getAssessorAssignmentMethod() != null &&
                    tc.getCredential().getAssessorAssignmentMethod().equals(AssessorAssignmentMethod.AUTOMATIC)) {
                Result<StudentAssignData> res = assignStudentsToInstructorAutomatically(
                        tc.getCredential().getId(), List.of(tc), tc.getInstructor().getId(), context);
                result.appendEvents(res.getEventQueue());
            } else {
                result.appendEvents(setInstructorForStudent(tc, null, context).getEventQueue());
            }
            return result;
        } catch (IllegalDataStateException e) {
		    logger.error("Error", e);
		    throw e;
		} catch (Exception e) {
		    logger.error("Error", e);
		    throw new DbConnectionException("Error withdrawing frm being instructor");
        }
    }

    private void saveInstructorWithdrawal(TargetCredential1 targetCredential, long instructorUserId) {
    	try {
    		InstructorWithdrawal withdrawal = new InstructorWithdrawal();
			withdrawal.setTargetCredential(targetCredential);
			withdrawal.setInstructor((User) persistence.currentManager().load(User.class, instructorUserId));
			saveEntity(withdrawal);
		} catch (ConstraintViolationException | DataIntegrityViolationException e) {
			//it means that instructor is already on withdrawn list so he can't be added again but that is fine
			logger.info("User with id: " + instructorUserId + " not added to withdrawn list because he is already on this list");
		}
	}
	
}