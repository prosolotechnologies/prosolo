package org.prosolo.services.nodes.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.apache.log4j.Logger;
import org.hibernate.Query;
import org.prosolo.bigdata.common.exceptions.DbConnectionException;
import org.prosolo.common.domainmodel.activities.events.EventType;
import org.prosolo.common.domainmodel.credential.Credential1;
import org.prosolo.common.domainmodel.credential.CredentialInstructor;
import org.prosolo.common.domainmodel.credential.TargetCredential1;
import org.prosolo.common.domainmodel.user.User;
import org.prosolo.common.domainmodel.user.UserGroupPrivilege;
import org.prosolo.common.event.context.data.LearningContextData;
import org.prosolo.services.data.Result;
import org.prosolo.services.event.EventData;
import org.prosolo.services.event.EventException;
import org.prosolo.services.event.EventFactory;
import org.prosolo.services.general.impl.AbstractManagerImpl;
import org.prosolo.services.indexing.utils.ElasticsearchUtil;
import org.prosolo.services.nodes.AssessmentManager;
import org.prosolo.services.nodes.CredentialInstructorManager;
import org.prosolo.services.nodes.CredentialManager;
import org.prosolo.services.nodes.UserGroupManager;
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
			throw new DbConnectionException("Error while loading credential instructor data");
		}
	}
	
	//not transactional
	@Override
	public void assignStudentToInstructor(long studentId, long instructorId, long credId, long formerInstructorUserId,
			long actorId, LearningContextData context) throws DbConnectionException, EventException {
		Result<Void> res = credInstructorManager.assignStudentToInstructorAndGetEvents(studentId, instructorId, credId, 
				formerInstructorUserId, actorId, context);
		for (EventData ev : res.getEvents()) {
			eventFactory.generateEvent(ev);
		}
	}
	
	@Override
	@Transactional(readOnly = false) 
	public Result<Void> assignStudentToInstructorAndGetEvents(long studentId, long instructorId, long credId, 
			long formerInstructorUserId, long actorId, LearningContextData context) throws DbConnectionException {
		try {
			TargetCredential1 targetCred = credManager.getTargetCredential(credId, studentId, false, false, false);
			return assignStudentToInstructor(instructorId, targetCred, formerInstructorUserId, true, actorId, context);
		} catch (DbConnectionException e) {
			logger.error(e);
			e.printStackTrace();
			throw e;
		} catch (Exception e) {
			logger.error(e);
			e.printStackTrace();
			throw new DbConnectionException("Error while assigning student to an instructor");
		}
	}
	
	//not transactional
	@Override
	public void assignStudentToInstructor(long instructorId, long targetCredId, long formerInstructorId, 
			long actorId, LearningContextData context) throws DbConnectionException, EventException {
		Result<Void> res = credInstructorManager.assignStudentToInstructorAndGetEvents(
				instructorId, targetCredId, formerInstructorId, actorId, context);
		for (EventData ev : res.getEvents()) {
			eventFactory.generateEvent(ev);
		}
	}
	
	@Override
	@Transactional(readOnly = false) 
	public Result<Void> assignStudentToInstructorAndGetEvents(long instructorId, long targetCredId, long formerInstructorId, 
			long actorId, LearningContextData context) throws DbConnectionException {
		return assignStudentToInstructor(instructorId, targetCredId, formerInstructorId, true, actorId, context);
	}
	
	@Transactional(readOnly = false) 
	private Result<Void> assignStudentToInstructor(long instructorId, long targetCredId, long formerInstructorUserId, 
			boolean updateAsessor, long actorId, LearningContextData context) throws DbConnectionException {
		TargetCredential1 targetCred = (TargetCredential1) persistence.currentManager().load(
				TargetCredential1.class, targetCredId);
		return assignStudentToInstructor(instructorId, targetCred, formerInstructorUserId, updateAsessor,
				actorId, context);
	}
	
	@Transactional(readOnly = false) 
	private Result<Void> assignStudentToInstructor(long instructorId, TargetCredential1 targetCred,
			long formerInstructorUserId, boolean updateAssessor, long actorId, LearningContextData context) 
					throws DbConnectionException {
		try {
			CredentialInstructor instructor = (CredentialInstructor) persistence
					.currentManager().load(CredentialInstructor.class, instructorId);
			
			return setInstructorForStudent(targetCred, instructor, formerInstructorUserId, updateAssessor, 
					actorId, context);
		} catch(Exception e) {
			logger.error(e);
			e.printStackTrace();
			throw new DbConnectionException("Error while assigning student to an instructor");
		}
	}
	
	@Transactional(readOnly = false) 
	private Result<Void> setInstructorForStudent(TargetCredential1 targetCred, CredentialInstructor instructor,
			long formerInstructorUserId, boolean updateAssessor, long actorId, LearningContextData context) {
		Result<Void> result = new Result<>();
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
				if (formerInstructorUserId > 0) {
					eventType = EventType.STUDENT_REASSIGNED_TO_INSTRUCTOR;
					params.put("reassignedFromInstructorUserId", formerInstructorUserId + "");
				} else {
					eventType = EventType.STUDENT_ASSIGNED_TO_INSTRUCTOR;
				}
			} else if (formerInstructorUserId > 0) {
				eventType = EventType.STUDENT_UNASSIGNED_FROM_INSTRUCTOR;
				targetInstructorUserId = formerInstructorUserId;
				params = new HashMap<>();
				params.put("credId", targetCred.getCredential().getId() + "");
			}
			User target = new User();
			target.setId(targetInstructorUserId);
			User object = new User();
			object.setId(targetCred.getUser().getId());
			result.addEvent(eventFactory.generateEventData(eventType, actorId, object, target, context, params));
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
    public Result<StudentAssignData> assignStudentsToInstructorAutomatically(long credId, 
		List<TargetCredential1> targetCreds, long formerInstructorId, long actorId, LearningContextData context) 
				throws DbConnectionException {
        return assignStudentsToInstructorAutomatically(credId, targetCreds, formerInstructorId, true, actorId, context);
    }
	
	@Override
    @Transactional(readOnly = false)
    public Result<StudentAssignData> assignStudentsToInstructorAutomatically(long credId, 
    		List<TargetCredential1> targetCreds, long formerInstructorId, boolean updateAssessor, 
    		long actorId, LearningContextData context) throws DbConnectionException {
		Result<StudentAssignData> result = new Result<>();
        List<InstructorData> instructors = getCredentialInstructorsWithLowestNumberOfStudents(credId, 
        		formerInstructorId);
        StudentAssignData data = new StudentAssignData();
        if (instructors != null && targetCreds != null) {
            List<TargetCredential1> targetCredsCopy = new ArrayList<>(targetCreds);
            Iterator<TargetCredential1> iterator = targetCredsCopy.iterator();
            while (iterator.hasNext()) {
                TargetCredential1 tCred = iterator.next();
                InstructorData instructorToAssign = getInstructorWithLowestNumberOfStudents(instructors, 
                		tCred.getUser().getId());
                if (instructorToAssign != null) {
	                result.addEvents(assignStudentToInstructor(instructorToAssign.getInstructorId(), tCred.getId(), 
	                		formerInstructorId, updateAssessor, actorId, context)
	                			.getEvents());
	                /*
	                this is currently needed only for enrollincredential method to be able to get instructor user id
	                 */
	                //TODO observer refactor - when observers are refactored remove this if not needed
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
	                	if (instructors.isEmpty()) {
		                	break;
		                }
	                }
	                
	                iterator.remove();
                }
            }
            if (!targetCredsCopy.isEmpty()) {
            	for(TargetCredential1 tc : targetCredsCopy) {
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
    
    //not transactional
    @Override
    public void unassignStudentFromInstructor(long userId, long credId, long actorId, LearningContextData context) 
    		throws DbConnectionException, EventException {
		Result<Void> res = credInstructorManager.unassignStudentFromInstructorAndGetEvents(
				userId, credId, actorId, context);
		for (EventData ev : res.getEvents()) {
			eventFactory.generateEvent(ev);
		}
    }
    
    @Override
    @Transactional(readOnly = false)
    public Result<Void> unassignStudentFromInstructorAndGetEvents(long userId, long credId, long actorId, LearningContextData context) 
    		throws DbConnectionException {
    	try {
    		TargetCredential1 targetCred = credManager.getTargetCredential(credId, userId, false, false, true);
    		return setInstructorForStudent(targetCred, null, targetCred.getInstructor().getUser().getId(), true, 
    				actorId, context);
		} catch(Exception e) {
			logger.error(e);
			e.printStackTrace();
			throw new DbConnectionException("Error while loading credential ids");
		}
    }
    
    @Transactional(readOnly = false)
    private Result<Void> assignStudentsAutomatically(long credId, List<TargetCredential1> targetCreds, 
    		long instructorId, long actorId, LearningContextData context) {
    	Result<Void> res = new Result<>();
		Result<StudentAssignData> result = assignStudentsToInstructorAutomatically(credId, 
				targetCreds, instructorId, actorId, context);
		res.addEvents(result.getEvents());
		List<TargetCredential1> unassigned = result.getResult().getUnassigned();
		if(!unassigned.isEmpty()) {
//			CredentialInstructor instructor = (CredentialInstructor) persistence.currentManager().
//					load(CredentialInstructor.class, instructorId);
			res.addEvents(updateStudentsAssigned(
					null, null, unassigned, actorId, context)
						.getEvents());
		}
		return res;
	}
	
	@Override
	@Transactional(readOnly = false)
	public Result<Void> reassignStudentsAutomatically(long instructorId, long credId, long actorId,
			LearningContextData context) throws DbConnectionException {
		try {
			List<TargetCredential1> targetCreds = credManager.getTargetCredentialsForInstructor(instructorId);
			return assignStudentsAutomatically(credId, targetCreds, instructorId, actorId, context);
		} catch(Exception e) {
			throw new DbConnectionException("Error while reassigning students");
		}
	}
	
	//not transactional
	@Override
	public void removeInstructorFromCredential(long instructorId, long credId, 
			boolean reassignAutomatically, long actorId, LearningContextData context) 
					throws DbConnectionException, EventException {
		Result<Void> res = credInstructorManager.removeInstructorFromCredentialAndGetEvents(
				instructorId, credId, reassignAutomatically, actorId, context);
		for (EventData ev : res.getEvents()) {
			eventFactory.generateEvent(ev);
		}
	}
	
	
	@Override
	@Transactional(readOnly = false)
	public Result<Void> removeInstructorFromCredentialAndGetEvents(long instructorId, long credId, 
			boolean reassignAutomatically, long actorId, LearningContextData context) throws DbConnectionException {
		try {
			Result<Void> res = new Result<>();
			CredentialInstructor instructor = (CredentialInstructor) persistence.currentManager().
					load(CredentialInstructor.class, instructorId);
			List<TargetCredential1> targetCreds = credManager.getTargetCredentialsForInstructor(instructorId);
			if (reassignAutomatically) {
				res.addEvents(assignStudentsAutomatically(
						credId, targetCreds, instructorId, actorId, context).getEvents());
			} else {
				List<Long> unassignedCreds = new ArrayList<>();
            	for(TargetCredential1 tc : targetCreds) {
            		unassignedCreds.add(tc.getId());
            	}
				res.addEvents(updateStudentsAssigned(null, null, targetCreds, actorId, context)
						.getEvents());
			}
			res.addEvents(userGroupManager.removeUserFromDefaultCredentialGroupAndGetEvents(
					instructor.getUser().getId(), credId, UserGroupPrivilege.Instruct, actorId, context).getEvents());
			
			persistence.currentManager().delete(instructor);
			
			Credential1 cred = new Credential1();
			cred.setId(credId);
			User instr = new User();
			instr.setId(instructor.getUser().getId());
			res.addEvent(eventFactory.generateEventData(
				EventType.INSTRUCTOR_REMOVED_FROM_CREDENTIAL, actorId, instr, cred, context, null));
				
			return res;
		} catch(Exception e) {
			logger.error(e);
			e.printStackTrace();
			throw new DbConnectionException("Error while removing instructor from credential");
		}
	}
	
	private Result<Void> updateStudentsAssigned(CredentialInstructor instructor, 
			List<TargetCredential1> targetCredsToAssign, List<TargetCredential1> targetCredsToUnassign, 
			long actorId, LearningContextData context) throws DbConnectionException {
		Result<Void> result = new Result<>();
		try {
			String query = 
					"UPDATE " +
					"TargetCredential1 cred " +
				    "set cred.instructor = :instructor, " +
					"cred.assignedToInstructor = :assigned " +
				    "WHERE cred.id IN " +
						"(:ids)";
			
			if (targetCredsToAssign != null && !targetCredsToAssign.isEmpty()) {
				/*
				 * go through all credentials and generate appropriate event
				 */
				EventType eventType = null;
				Map<String, String> params = null;
			    for (TargetCredential1 tc : targetCredsToAssign) {
					if (instructor != null) {
						params = new HashMap<>();
						params.put("credId", tc.getCredential().getId() + "");
						//if student already had instructor assigned, reassign event should be generated
						if (tc.getInstructor() != null) {
							eventType = EventType.STUDENT_REASSIGNED_TO_INSTRUCTOR;
							params.put("reassignedFromInstructorUserId", tc.getInstructor().getUser().getId() + "");
						} else {
							eventType = EventType.STUDENT_ASSIGNED_TO_INSTRUCTOR;
						}
					} 
					User target = new User();
					target.setId(instructor.getUser().getId());
					User object = new User();
					object.setId(tc.getUser().getId());
					result.addEvent(eventFactory.generateEventData(eventType, actorId, object, target, context, params));
				}
			    
				List<Long> targetCredIdsToAssign = targetCredsToAssign.stream().map(tc -> tc.getId())
						.collect(Collectors.toList());
				persistence.currentManager().createQuery(query)
								.setParameter("instructor", instructor)
								.setBoolean("assigned", true)
								.setParameterList("ids", targetCredIdsToAssign)
								.executeUpdate();
				
				assessmentManager.updateDefaultAssessmentsAssessor(targetCredIdsToAssign, 
						instructor.getUser().getId());
			}
			if (targetCredsToUnassign != null && !targetCredsToUnassign.isEmpty()) {	
				for (TargetCredential1 tc : targetCredsToUnassign) {
					Map<String, String> params = new HashMap<>();
					params.put("credId", tc.getCredential().getId() + "");
					User target = new User();
					target.setId(tc.getInstructor().getUser().getId());
					User object = new User();
					object.setId(tc.getUser().getId());
					result.addEvent(eventFactory.generateEventData(
							EventType.STUDENT_UNASSIGNED_FROM_INSTRUCTOR, actorId, object, target, context, params));
				}
				
				List<Long> targetCredIdsToUnassign = targetCredsToUnassign.stream().map(tc -> tc.getId())
						.collect(Collectors.toList());
				persistence.currentManager().createQuery(query)
								.setParameter("instructor", null)
								.setBoolean("assigned", false)
								.setParameterList("ids", targetCredIdsToUnassign)
								.executeUpdate();
				
				assessmentManager.updateDefaultAssessmentsAssessor(targetCredIdsToUnassign, 0);
			}
			return result;	
		} catch(Exception e) {
			logger.error(e);
			e.printStackTrace();
			throw new DbConnectionException("Error while updating instructor");
		}
	}
	
	//not transactional
	@Override
	public void addInstructorToCredential(long credId, long userId, 
			int maxNumberOfStudents, long actorId, LearningContextData context) throws DbConnectionException, 
				EventException {
		//self invocation to trigger spring interception and transaction start
		for (EventData ev : credInstructorManager.addInstructorToCredentialAndGetEvents(
				credId, userId, maxNumberOfStudents, actorId, context)
					.getEvents()) {
			eventFactory.generateEvent(ev);
		}
	}
	
	@Override
	@Transactional(readOnly = false)
	public Result<CredentialInstructor> addInstructorToCredentialAndGetEvents(long credId, long userId, 
			int maxNumberOfStudents, long actorId, LearningContextData context) throws DbConnectionException {
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
			res.addEvents(userGroupManager.saveUserToDefaultCredentialGroupAndGetEvents(
					userId, credId, UserGroupPrivilege.Instruct, actorId, context).getEvents());
			
			Credential1 credential = new Credential1();
			credential.setId(credId);
			User instr = new User();
			instr.setId(userId);
			Map<String, String> params = new HashMap<>();
			String dateString = null;
			if(instructor.getDateAssigned() != null) {
				dateString = ElasticsearchUtil.getDateStringRepresentation(instructor.getDateAssigned());
			}
			params.put("dateAssigned", dateString);
			res.addEvent(eventFactory.generateEventData(
					EventType.INSTRUCTOR_ASSIGNED_TO_CREDENTIAL, actorId, instr, credential, context, params));
				
			return res;
		} catch(Exception e) {
			logger.error(e);
			e.printStackTrace();
			throw new DbConnectionException("Error while assigning instructor to a credential");
		}
	}
	
	//not transactional
	@Override
	public void updateInstructorAndStudentsAssigned(long credId, InstructorData id, 
			List<Long> studentsToAssign, List<Long> studentsToUnassign, long actorId,
			LearningContextData context) throws DbConnectionException, EventException {
		Result<Void> res = credInstructorManager.updateInstructorAndStudentsAssignedAndGetEvents(
				credId, id, studentsToAssign, studentsToUnassign, actorId, context);
		for (EventData ev : res.getEvents()) {
			eventFactory.generateEvent(ev);
		}
	}
	
	@Override
	@Transactional(readOnly = false)
	public Result<Void> updateInstructorAndStudentsAssignedAndGetEvents(long credId, InstructorData id, 
			List<Long> studentsToAssign, List<Long> studentsToUnassign, long actorId,
			LearningContextData context) throws DbConnectionException {
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
			
			return updateStudentsAssignedToInstructor(id.getInstructorId(), credId, studentsToAssign, 
					studentsToUnassign, actorId, context);
				
		} catch(Exception e) {
			logger.error(e);
			e.printStackTrace();
			throw new DbConnectionException("Error while updating instructor");
		}
	}
	
	@Override
	@Transactional(readOnly = false)
	public Result<Void> updateStudentsAssignedToInstructor(long instructorId, long credId, 
			List<Long> studentsToAssign, List<Long> studentsToUnassign, long actorId,
			LearningContextData context) throws DbConnectionException {
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
			
			return updateStudentsAssigned(instructor, studentCredsToAssign, studentCredsToUnassign, actorId, context);
				
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
			
			return result != null ? result : new ArrayList<>();
		} catch(Exception e) {
			logger.error(e);
			e.printStackTrace();
			throw new DbConnectionException("Error while retrieving credential instructors user ids");
		}
	}
	
}