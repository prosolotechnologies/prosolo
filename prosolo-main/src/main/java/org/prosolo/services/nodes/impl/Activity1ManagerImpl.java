package org.prosolo.services.nodes.impl;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import javax.inject.Inject;

import org.hibernate.Query;
import org.hibernate.Session;
import org.prosolo.bigdata.common.exceptions.DbConnectionException;
import org.prosolo.common.domainmodel.activities.events.EventType;
import org.prosolo.common.domainmodel.credential.Activity1;
import org.prosolo.common.domainmodel.credential.CommentedResourceType;
import org.prosolo.common.domainmodel.credential.Competence1;
import org.prosolo.common.domainmodel.credential.CompetenceActivity1;
import org.prosolo.common.domainmodel.credential.ExternalToolActivity1;
import org.prosolo.common.domainmodel.credential.ExternalToolTargetActivity1;
import org.prosolo.common.domainmodel.credential.LearningResourceType;
import org.prosolo.common.domainmodel.credential.ResourceLink;
import org.prosolo.common.domainmodel.credential.TargetActivity1;
import org.prosolo.common.domainmodel.credential.TargetCompetence1;
import org.prosolo.common.domainmodel.credential.TextActivity1;
import org.prosolo.common.domainmodel.credential.TextTargetActivity1;
import org.prosolo.common.domainmodel.credential.UrlActivity1;
import org.prosolo.common.domainmodel.credential.UrlActivityType;
import org.prosolo.common.domainmodel.credential.UrlTargetActivity1;
import org.prosolo.common.domainmodel.outcomes.Outcome;
import org.prosolo.common.domainmodel.user.User;
import org.prosolo.common.event.context.data.LearningContextData;
import org.prosolo.core.hibernate.HibernateUtil;
import org.prosolo.services.data.Result;
import org.prosolo.services.event.EventData;
import org.prosolo.services.event.EventException;
import org.prosolo.services.event.EventFactory;
import org.prosolo.services.general.impl.AbstractManagerImpl;
import org.prosolo.services.interaction.CommentManager;
import org.prosolo.services.interaction.data.CommentData;
import org.prosolo.services.interaction.data.CommentReplyFetchMode;
import org.prosolo.services.interaction.data.CommentsData;
import org.prosolo.services.interaction.data.ResultCommentInfo;
import org.prosolo.services.nodes.Activity1Manager;
import org.prosolo.services.nodes.Competence1Manager;
import org.prosolo.services.nodes.CredentialManager;
import org.prosolo.services.nodes.ResourceFactory;
import org.prosolo.services.nodes.data.ActivityAssessmentData;
import org.prosolo.services.nodes.data.ActivityData;
import org.prosolo.services.nodes.data.ActivityResultData;
import org.prosolo.services.nodes.data.ActivityResultType;
import org.prosolo.services.nodes.data.ActivityType;
import org.prosolo.services.nodes.data.CompetenceData1;
import org.prosolo.services.nodes.data.GradeData;
import org.prosolo.services.nodes.data.LearningResourceReturnResultType;
import org.prosolo.services.nodes.data.ObjectStatus;
import org.prosolo.services.nodes.data.Operation;
import org.prosolo.services.nodes.data.ResourceLinkData;
import org.prosolo.services.nodes.data.Role;
import org.prosolo.services.nodes.data.StudentAssessedFilter;
import org.prosolo.services.nodes.factory.ActivityDataFactory;
import org.prosolo.services.nodes.impl.util.EntityPublishTransition;
import org.prosolo.services.nodes.observers.learningResources.ActivityChangeTracker;
import org.prosolo.services.urlencoding.UrlIdEncoder;
import org.prosolo.web.useractions.CommentBean;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

@Service("org.prosolo.services.nodes.Activity1Manager")
public class Activity1ManagerImpl extends AbstractManagerImpl implements Activity1Manager {

	private static final long serialVersionUID = -2783669846949034832L;

//	private static Logger logger = Logger.getLogger(Activity1ManagerImpl.class);
	
	@Inject private ActivityDataFactory activityFactory;
	@Inject private Competence1Manager compManager;
	@Inject private CommentManager commentManager;
	@Inject private EventFactory eventFactory;
	@Inject private ResourceFactory resourceFactory;
	@Inject private CredentialManager credManager;
	@Inject private UrlIdEncoder idEncoder;
	
	@Override
	@Transactional(readOnly = false)
	public Activity1 saveNewActivity(ActivityData data, long userId, LearningContextData context) 
			throws DbConnectionException {
		try {
			Result<Activity1> res = resourceFactory.createActivity(data, userId);
			
			Activity1 act = res.getResult();

			String page = context != null ? context.getPage() : null; 
			String lContext = context != null ? context.getLearningContext() : null; 
			String service = context != null ? context.getService() : null; 
			for(EventData ev : res.getEvents()) {
				ev.setPage(page);
				ev.setContext(lContext);
				ev.setService(service);
				eventFactory.generateEvent(ev);
			}
			
			User user = new User();
			user.setId(userId);
			if(act.isPublished()) {
				eventFactory.generateEvent(EventType.Create, user.getId(), act, null, page, lContext,
						service, null);
			} else {
				eventFactory.generateEvent(EventType.Create_Draft, user.getId(), act, null, page, lContext,
						service, null);
			}

			return act;
		} catch (EventException e) {
			logger.error(e);
			e.printStackTrace();
			throw new DbConnectionException("Error while saving activity");
		} catch (DbConnectionException dbe) {
			logger.error(dbe);
			dbe.printStackTrace();
			throw dbe;
		}
	}

	@Override
	@Transactional(readOnly = false)
	public Activity1 deleteActivity(long originalActId, ActivityData data, long userId) 
			throws DbConnectionException {
		try {
			if(originalActId > 0) {
				Activity1 act = (Activity1) persistence.currentManager()
						.load(Activity1.class, originalActId);
				act.setDeleted(true);
				
				if(data.isDraft()) {
					Activity1 draftVersion = (Activity1) persistence.currentManager()
							.load(Activity1.class, data.getActivityId());
					act.setDraftVersion(null);
					delete(draftVersion);
				}
				
				deleteAllCompetenceActivitiesForActivity(act.getId());
	
				User user = new User();
				user.setId(userId);
				if(data.isPublished() || data.isDraft()) {
					eventFactory.generateEvent(EventType.Delete, user.getId(), act);
				} else {
					eventFactory.generateEvent(EventType.Delete_Draft, user.getId(), act);
				}
				
				return act;
			}
			return null;
		} catch (Exception e) {
			logger.error(e);
			e.printStackTrace();
			throw new DbConnectionException("Error while deleting credential");
		}
	}
	
	private void deleteAllCompetenceActivitiesForActivity(long actId) {
		Activity1 act = (Activity1) persistence.currentManager()
				.load(Activity1.class, actId);
		long duration = act.getDuration();
		
		String query = "SELECT compAct " +
			       	   "FROM CompetenceActivity1 compAct " + 
			       	   "WHERE compAct.activity = :act";

		@SuppressWarnings("unchecked")
		List<CompetenceActivity1> res = persistence.currentManager()
			.createQuery(query)
			.setEntity("act", act)
			.list();
		
		if(res != null) {
			for(CompetenceActivity1 ca : res) {
				if(ca != null) {
					long competenceId = ca.getCompetence().getId();
					shiftOrderOfActivitiesUp(competenceId, ca.getOrder());
					delete(ca);
				}
			}
		}
		
		if(duration != 0) {
			compManager.updateDurationForCompetencesWithActivity(actId, duration, Operation.Subtract);
		}
		
	}

	private void shiftOrderOfActivitiesUp(long id, int order) {
		List<CompetenceActivity1> activitiesAfterDeleted = getAllCompetenceActivitiesAfterSpecified(
				id, order);
		for(CompetenceActivity1 ca : activitiesAfterDeleted) {
			//TODO use hql update to avoid select + update
			ca.setOrder(ca.getOrder() - 1);
		}
	}

	private List<CompetenceActivity1> getAllCompetenceActivitiesAfterSpecified(long compId, int order) {
		Competence1 comp = (Competence1) persistence.currentManager().load(Competence1.class, compId);
		String query = "SELECT compAct " +
		       	   "FROM Competence1 comp " +
		       	   "LEFT JOIN comp.activities compAct " +
		       	   "WHERE comp = :comp " +
		       	   "AND compAct.order > :order";
		
		@SuppressWarnings("unchecked")
		List<CompetenceActivity1> res = persistence.currentManager()
			.createQuery(query)
			.setEntity("comp", comp)
			.setInteger("order", order)
			.list();
		
		if(res == null) {
			return new ArrayList<>();
		}
		
		return res;
	}
	
	@Override
	@Transactional(readOnly = true)
	public List<ActivityData> getCompetenceActivitiesData(long competenceId) 
			throws DbConnectionException {
		List<ActivityData> result = new ArrayList<>();
		try {
			List<CompetenceActivity1> res = getCompetenceActivities(competenceId, false);

			if (res != null) {
				for (CompetenceActivity1 act : res) {
					ActivityData bad = activityFactory.getBasicActivityData(act, true);
					result.add(bad);
				}
			}
			return result;
		} catch (Exception e) {
			logger.error(e);
			e.printStackTrace();
			throw new DbConnectionException("Error while loading activity data");
		}
	}
	
	@Override
	@Transactional(readOnly = true)
	public List<CompetenceActivity1> getCompetenceActivities(long competenceId, boolean loadResourceLinks) 
			throws DbConnectionException {
		try {
			Competence1 comp = (Competence1) persistence.currentManager().load(Competence1.class, competenceId);
			StringBuilder builder = new StringBuilder();
			builder.append("SELECT distinct compAct " +
				      	   "FROM CompetenceActivity1 compAct " + 
				       	   "INNER JOIN fetch compAct.activity act ");
			
			if(loadResourceLinks) {
				builder.append("LEFT JOIN fetch act.links " +
					       	   "LEFT JOIN fetch act.files ");
			}
			
			builder.append("WHERE compAct.competence = :comp " +					       
				           "ORDER BY compAct.order");
			
//			String query = "SELECT distinct compAct " +
//					       "FROM CompetenceActivity1 compAct " + 
//					       "INNER JOIN fetch compAct.activity act " +
//					       "LEFT JOIN fetch act.links " +
//					       "LEFT JOIN fetch act.files " +
//					       "WHERE compAct.competence = :comp " +					       
//					       "ORDER BY compAct.order";

			@SuppressWarnings("unchecked")
			List<CompetenceActivity1> res = persistence.currentManager()
				.createQuery(builder.toString())
				.setEntity("comp", comp)
				.list();
			
			if(res == null) {
				return new ArrayList<>();
			}
			
			return res;
		} catch(Exception e) {
			logger.error(e);
			e.printStackTrace();
			throw new DbConnectionException("Error while loading activities");
		}
	}
	
	@Override
	@Transactional(readOnly = false) 
	public List<TargetActivity1> createTargetActivities(long compId, TargetCompetence1 targetComp) 
			throws DbConnectionException {
		try {
			List<CompetenceActivity1> compActivities = getCompetenceActivities(compId, true);
			List<TargetActivity1> targetActivities = new ArrayList<>();
			if(compActivities != null) {
				for(CompetenceActivity1 act : compActivities) {
					TargetActivity1 ta = createTargetActivity(targetComp, act);
					targetActivities.add(ta);
				}
			}
			return targetActivities;
		} catch(Exception e) {
			logger.error(e);
			e.printStackTrace();
			throw new DbConnectionException("Error while enrolling activities");
		}
	}

	@Transactional(readOnly = false)
	private TargetActivity1 createTargetActivity(TargetCompetence1 targetComp, 
			CompetenceActivity1 compActivity) throws DbConnectionException {
		try {
			Activity1 act = compActivity.getActivity();
			TargetActivity1 targetAct = null;
			if(act instanceof TextActivity1) {
				TextActivity1 ta = (TextActivity1) act;
				TextTargetActivity1 tta = new TextTargetActivity1();
				tta.setText(ta.getText());
				targetAct = tta;
			} else if(act instanceof UrlActivity1) {
				UrlActivity1 urlAct = (UrlActivity1) act;
				UrlTargetActivity1 urlTargetActivity = new UrlTargetActivity1();
				switch(urlAct.getUrlType()) {
					case Video:
						urlTargetActivity.setType(UrlActivityType.Video);
						break;
					case Slides:
						urlTargetActivity.setType(UrlActivityType.Slides);
						break;
				}
				urlTargetActivity.setUrl(urlAct.getUrl());
				urlTargetActivity.setLinkName(urlAct.getLinkName());
				targetAct = urlTargetActivity;
			} else if(act instanceof ExternalToolActivity1) {
				ExternalToolActivity1 extAct = (ExternalToolActivity1) act;
				ExternalToolTargetActivity1 extTargetAct = new ExternalToolTargetActivity1();
				extTargetAct.setLaunchUrl(extAct.getLaunchUrl());
				extTargetAct.setSharedSecret(extAct.getSharedSecret());
				extTargetAct.setConsumerKey(extAct.getConsumerKey());
				extTargetAct.setOpenInNewWindow(extAct.isOpenInNewWindow());
				targetAct = extTargetAct;
			}
			
			targetAct.setTargetCompetence(targetComp);
			targetAct.setTitle(act.getTitle());
			targetAct.setDescription(act.getDescription());
			targetAct.setActivity(act);
			targetAct.setOrder(compActivity.getOrder());
			targetAct.setDuration(act.getDuration());
			targetAct.setResultType(act.getResultType());
			//targetAct.setUploadAssignment(act.isUploadAssignment());
			targetAct.setCreatedBy(act.getCreatedBy());
			targetAct.setLearningResourceType(act.getType());
			
			if(act.getLinks() != null) {
    			Set<ResourceLink> activityLinks = new HashSet<>();
    			for(ResourceLink rl : act.getLinks()) {
    				ResourceLink link = new ResourceLink();
    				link.setLinkName(rl.getLinkName());
    				link.setUrl(rl.getUrl());
    				if (rl.getIdParameterName() != null && !rl.getIdParameterName().isEmpty()) {
    					link.setIdParameterName(rl.getIdParameterName());
    				}
    				saveEntity(link);
    				activityLinks.add(link);
    			}
    			targetAct.setLinks(activityLinks);
    		}
    		
    		Set<ResourceLink> activityFiles = new HashSet<>();
    		if(act.getFiles() != null) {		
    			for(ResourceLink rl : act.getFiles()) {
    				ResourceLink link = new ResourceLink();
    				link.setLinkName(rl.getLinkName());
    				link.setUrl(rl.getUrl());
    				saveEntity(link);
    				activityFiles.add(link);
    			}
    			targetAct.setFiles(activityFiles);
    		}
			
			return saveEntity(targetAct);
		} catch(Exception e) {
			logger.error(e);
			e.printStackTrace();
			throw new DbConnectionException("Error while creating target activity");
		}
	}
	
	@Override
	@Transactional(readOnly = true)
	public List<ActivityData> getTargetActivitiesData(long targetCompId) 
			throws DbConnectionException {
		List<ActivityData> result = new ArrayList<>();
		try {
			List<TargetActivity1> res = getTargetActivities(targetCompId);

			if (res != null) {
				for (TargetActivity1 targetAct : res) {
					ActivityData actData = activityFactory.getBasicActivityData(targetAct, true);
					result.add(actData);
				}
			}
			return result;
		} catch (Exception e) {
			logger.error(e);
			e.printStackTrace();
			throw new DbConnectionException("Error while loading activities data");
		}
	}
	
	@Override
	@Transactional(readOnly = true)
	public List<TargetActivity1> getTargetActivities(long targetCompId) 
			throws DbConnectionException {
		try {
			TargetCompetence1 targetComp = (TargetCompetence1) persistence.currentManager().load(
					TargetCompetence1.class, targetCompId);
			
			String query = "SELECT targetAct " +
					       "FROM TargetActivity1 targetAct " +
					       "WHERE targetAct.targetCompetence = :targetComp " +
					       "ORDER BY targetAct.order";

			@SuppressWarnings("unchecked")
			List<TargetActivity1> res = persistence.currentManager()
				.createQuery(query)
				.setEntity("targetComp", targetComp)
				.list();
			
			if(res == null) {
				return new ArrayList<>();
			}
			return res;
		} catch(Exception e) {
			logger.error(e);
			e.printStackTrace();
			throw new DbConnectionException("Error while loading activities");
		}
	}

//	@Deprecated
//	@Override
//	@Transactional(readOnly = false)
//	public void publishDraftActivitiesWithoutDraftVersion(List<Long> actIds) 
//			throws DbConnectionException {
//		try {
//			if(actIds == null || actIds.isEmpty()) {
//				return;
//			}
//			
//			String query = "UPDATE Activity1 act " +
//						   "SET act.published = :published " + 
//						   "WHERE act.hasDraft = :hasDraft " +
//						   "AND act.id IN :actIds";
//			persistence.currentManager()
//				.createQuery(query)
//				.setBoolean("published", true)
//				.setBoolean("hasDraft", false)
//				.setParameterList("actIds", actIds)
//				.executeUpdate();
//		} catch(Exception e) {
//			logger.error(e);
//			e.printStackTrace();
//			throw new DbConnectionException("Error while updating activities");
//		}
//	}

//	private List<Long> getAllCompetenceActivitiesIds(Long compId) {
//		try {
//			String query = "Select act.id " +
//						   "FROM CompetenceActivity1 compAct " + 
//						   "INNER JOIN compAct.competence comp " +
//						   "INNER JOIN compAct.activity act " +
//						   "WHERE act.deleted = :deleted " +
//						   "AND comp.id = :compId";
//			
//			@SuppressWarnings("unchecked")
//			List<Long> actIds = persistence.currentManager()
//				.createQuery(query)
//				.setBoolean("deleted", false)
//				.setLong("compId", compId)
//				.list();
//			
//			if(actIds == null) {
//				return new ArrayList<>();
//			}
//			return actIds;
//		} catch(Exception e) {
//			logger.error(e);
//			e.printStackTrace();
//			throw new DbConnectionException("Error while updating activities");
//		}
//	}
	
	@Override
	@Transactional(readOnly = true)
	public ActivityData getActivityDataForEdit(long credId, long competenceId, long activityId, 
			long creatorId) throws DbConnectionException {
		return getCurrentVersionOfActivityBasedOnRole(credId, competenceId, activityId, creatorId, Role.User);
	}
	
	@Transactional(readOnly = true)
	private ActivityData getCurrentVersionOfActivityBasedOnRole(long credId, long competenceId, 
			long activityId, long creatorId, Role role) throws DbConnectionException {
		try {
			/*
			 * first check if passed credential has specified competence
			 */
			if(credId > 0) {
				List<Long> ids = new ArrayList<>();
				ids.add(credId);
				Optional<Long> draftVersionCredId = credManager
						.getDraftVersionIdIfExists(credId);
				if(draftVersionCredId.isPresent()) {
					ids.add(draftVersionCredId.get());
				}
				String query1 = "SELECT credComp.id " +
								"FROM CredentialCompetence1 credComp " +
								"WHERE credComp.credential.id IN (:credIds) " +
								"AND credComp.competence.id = :compId";
				
				@SuppressWarnings("unchecked")
				List<Long> res1 = persistence.currentManager()
						.createQuery(query1)
						.setParameterList("credIds", ids)
						.setLong("compId", competenceId)
						.list();
				
				if(res1 == null || res1.isEmpty()) {
					return null;
				}
			}
			/*
			 * we need to make sure that activity is bound to competence with passed id or its draft version
			 */
			Optional<Long> draftVersionCompId = compManager
					.getCompetenceDraftVersionIdForOriginal(competenceId);
			List<Long> compIds = new ArrayList<>();
			compIds.add(competenceId);
			if(draftVersionCompId.isPresent()) {
				compIds.add(draftVersionCompId.get());
			}
			String query = "SELECT act " +
					   "FROM CompetenceActivity1 compAct " +
					   "INNER JOIN compAct.activity act " +
					   "LEFT JOIN fetch act.links link " +
					   "LEFT JOIN fetch act.files file ";
			
			StringBuilder queryBuilder1 = new StringBuilder(query);
			queryBuilder1.append("WHERE act.id = :actId " +
					"AND act.deleted = :deleted " +
					"AND act.draft = :draft " +
					"AND compAct.competence.id IN (:compIds)");
			
			if(role == Role.User) {
				queryBuilder1.append("AND act.type = :type " +
									 "AND act.createdBy.id = :user");
			} else {
				queryBuilder1.append("AND act.type = :type ");
			}
				   
			Query q = persistence.currentManager()
					.createQuery(queryBuilder1.toString())
					.setLong("actId", activityId)
					.setParameterList("compIds", compIds)
					.setBoolean("deleted", false)
					.setBoolean("draft", false);
			
			if(role == Role.User) {
				q.setParameter("type", LearningResourceType.USER_CREATED);
				q.setLong("user", creatorId);
			} else {
				q.setParameter("type", LearningResourceType.UNIVERSITY_CREATED);
			}
			
			Activity1 res = (Activity1) q.uniqueResult();

			if(res != null) {
				ActivityData actData = null;
				if(res.isHasDraft()) {
					long draftVersionId = res.getDraftVersion().getId();
					/*
					 * remove proxy object from session to be able to retrieve real
					 * object with hql so instanceof will give expected result
					 */
					persistence.currentManager().evict(res.getDraftVersion());
					String query2 = "SELECT act " +
							   		"FROM Activity1 act " +
							   		"LEFT JOIN fetch act.links link " +
							   		"LEFT JOIN fetch act.files file " +
							   		"WHERE act.id = :draftVersion";
					Activity1 draftAct = (Activity1) persistence.currentManager()
							.createQuery(query2)
							.setLong("draftVersion", draftVersionId)
							.uniqueResult();
					//Activity1 draftAct = getActivity(draftVersionId, true);
					if(draftAct != null) {
						actData = activityFactory.getActivityData(draftAct, 0, 0,
								draftAct.getLinks(), draftAct.getFiles(), true);
					}	
				} else {
					actData = activityFactory.getActivityData(res, 0, 0, res.getLinks(),
							res.getFiles(), true);
				}
				return actData;
			}
			return null;
		} catch (Exception e) {
			logger.error(e);
			e.printStackTrace();
			throw new DbConnectionException("Error while loading activity data");
		}
	}
	
	/**
	 * Returns most current version of activity (draft if exists) and competence activity from 
     * draft competence if exists(otherwise from original version)
	 * @param activityId
	 * @param creatorId
	 * @param role
	 * @return
	 * @throws DbConnectionException
	 */
	@Transactional(readOnly = true)
	private ActivityData getCompetenceActivityCurrentVersionData(long credId, long compId, 
			long activityId, long creatorId, Role role) throws DbConnectionException {
		try {			   
			CompetenceActivity1 res = getCompetenceActivityBasedOnRole(credId, compId, activityId, 
					creatorId, true, role);
					//getCompetenceActivityForCreator(activityId, creatorId, true);
			
			if(res != null) {
				ActivityData actData = null;
				Activity1 act = res.getActivity();
				if(act.isHasDraft()) {
					long draftVersionId = act.getDraftVersion().getId();
					/*
					 * remove proxy object from session to be able to retrieve real
					 * object with hql so instanceof will give expected result
					 */
					persistence.currentManager().evict(act.getDraftVersion());
					String query = "SELECT distinct act " +
							   "FROM Activity1 act " + 
							   "LEFT JOIN fetch act.links link " +
							   "LEFT JOIN fetch act.files file " +
							   "WHERE act.id = :draftVersion";
				
					Activity1 draftAct = (Activity1) persistence.currentManager()
							.createQuery(query)
							.setLong("draftVersion", draftVersionId)
							.uniqueResult();
					if(draftAct != null) {
						actData = activityFactory.getActivityData(draftAct, res.getCompetence().getId(),
								res.getOrder(), draftAct.getLinks(), 
								draftAct.getFiles(), true);
					}	
				} else {
					actData = activityFactory.getActivityData(res, res.getActivity().getLinks(), 
							res.getActivity().getFiles(), true);
				}
				return actData;
			}
			
			return null;
		} catch (Exception e) {
			logger.error(e);
			e.printStackTrace();
			throw new DbConnectionException("Error while loading activity data");
		}
	}

//	/**
//	 * It will return competence activity from draft version of a competence if it exists and from
//	 * original version otherwise.
//	 * @param activityId
//	 * @param userId
//	 * @param loadLinks
//	 * @return
//	 * @throws DbConnectionException
//	 */
//	@Transactional(readOnly = true)
//	private CompetenceActivity1 getCompetenceActivityForCreator(long activityId, long userId, 
//			boolean loadLinks) throws DbConnectionException {
//		try {
//			User user = (User) persistence.currentManager().load(User.class, userId);
//				
//			StringBuilder builder = new StringBuilder();
//			builder.append("SELECT compAct " +
//					   	   "FROM CompetenceActivity1 compAct " + 
//					       "INNER JOIN fetch compAct.activity act " +
//					   	   "INNER JOIN compAct.competence comp ");
//			
//			if(loadLinks) {
//				builder.append("LEFT JOIN fetch act.links link " +
//						       "LEFT JOIN fetch act.files file ");
//			}
//			builder.append("WHERE act.id = :actId " +
//					       "AND act.deleted = :boolFalse " +
//						   "AND comp.hasDraft = :boolFalse ");
//			
//			if(userId > 0) {
//				builder.append("AND act.createdBy = :user");
//			}
//						
//			Query q = persistence.currentManager()
//					.createQuery(builder.toString())
//					.setLong("actId", activityId)
//					.setBoolean("boolFalse", false);
//			if(userId > 0) {
//				q.setEntity("user", user);
//			}
//			return (CompetenceActivity1) q.uniqueResult();
//		} catch (Exception e) {
//			logger.error(e);
//			e.printStackTrace();
//			throw new DbConnectionException("Error while loading activity");
//		}
//	}
	
	/**
	 * It will return competence activity from draft version of a competence if it exists and from
	 * original version otherwise.
	 * @param activityId
	 * @param userId
	 * @param loadLinks
	 * @param role
	 * @return
	 * @throws DbConnectionException
	 */
	@Transactional(readOnly = true)
	private CompetenceActivity1 getCompetenceActivityBasedOnRole(long credId, long compId, 
			long activityId, long creatorId, boolean loadLinks, Role role) throws DbConnectionException {
		try {
			long competenceId = compId;
			Optional<Long> draftVersionCompId = compManager
					.getCompetenceDraftVersionIdForOriginal(compId);
			if(draftVersionCompId.isPresent()) {
				competenceId = draftVersionCompId.get();
			}
			StringBuilder builder = new StringBuilder();
			builder.append("SELECT compAct ");
			if(credId > 0) {
				builder.append("FROM CredentialCompetence1 credComp ");
				if(competenceId == compId) {
					builder.append("INNER JOIN credComp.competence comp " +
							   			"WITH comp.id = :compId ");
				} else {
					builder.append("INNER JOIN credComp.competence originalComp " +
					   					"WITH originalComp.id = :compId " +
								   "INNER JOIN originalComp.draftVersion comp ");
				}
				builder.append("INNER JOIN comp.activities compAct " +
					   	   	  		"WITH compAct.activity.id = :actId " +
							   "INNER JOIN fetch compAct.activity act ");
			} else {
				builder.append("FROM CompetenceActivity1 compAct " +
						      "INNER JOIN fetch compAct.activity act " +				   	 
					   	   	  "INNER JOIN compAct.competence comp " +
					   	   			"WITH comp.id = :compId ");
			}
			
//			builder.append("SELECT compAct " +
//					   	   "FROM CompetenceActivity1 compAct " + 
//					       "INNER JOIN fetch compAct.activity act " +
//					   	   "INNER JOIN compAct.competence comp ");
			
			if(loadLinks) {
				builder.append("LEFT JOIN fetch act.links link " +
						       "LEFT JOIN fetch act.files file ");
			}
			
			if(credId > 0) {
				builder.append("WHERE credComp.credential.id IN (:credIds) " +
							   "AND act.deleted = :boolFalse ");
			} else {
				builder.append("WHERE act.id = :actId " +
						       "AND act.deleted = :boolFalse ");
			}
			
			if(role == Role.User) {
				builder.append("AND act.createdBy.id = :user");
			} else {
				builder.append("AND act.type = :type ");
			}
						
			Query q = persistence.currentManager()
					.createQuery(builder.toString())
					.setLong("actId", activityId)
					.setBoolean("boolFalse", false);
			if(credId > 0 && competenceId != compId) {
				q.setLong("compId", compId);
			} else {
				q.setLong("compId", competenceId);
			}
			
			if(credId > 0) {
				List<Long> ids = new ArrayList<>();
				ids.add(credId);
				Optional<Long> draftVersionCredId = credManager
						.getDraftVersionIdIfExists(credId);
				if(draftVersionCredId.isPresent()) {
					ids.add(draftVersionCredId.get());
				}
				q.setParameterList("credIds", ids);
			}
			
			if(role == Role.User) {
				q.setLong("user", creatorId);
			} else {
				q.setParameter("type", LearningResourceType.UNIVERSITY_CREATED);
			}
			return (CompetenceActivity1) q.uniqueResult();
		} catch (Exception e) {
			logger.error(e);
			e.printStackTrace();
			throw new DbConnectionException("Error while loading activity");
		}
	}
	
	@Override
	@Transactional(readOnly = false)
	public Activity1 updateActivity(long originalActivityId, ActivityData data, long userId,
			LearningContextData context) throws DbConnectionException {
		try {
			Activity1 act = resourceFactory.updateActivity(data, userId);
			Class<? extends Activity1> actClass = null;
			if(act instanceof TextActivity1) {
				actClass = TextActivity1.class;
			} else if(act instanceof UrlActivity1) {
				actClass = UrlActivity1.class;
			} else if(act instanceof ExternalToolActivity1) {
				actClass = ExternalToolActivity1.class;
			}
			
			String page = context != null ? context.getPage() : null; 
			String lContext = context != null ? context.getLearningContext() : null; 
			String service = context != null ? context.getService() : null; 
			User user = new User();
			user.setId(userId);
			if(data.isPublished()) {
				//activity remains published
				if(!data.isPublishedChanged()) {
					fireSameVersionActivityEditEvent(actClass, data, user, act, 0, page, lContext, service);
				} 
				/*
				 * this means that activity is published for the first time
				 */
				else if(!data.isDraft()) {
					EventData ev = fireFirstTimePublishActivityEvent(user, act);
					ev.setPage(page);
					ev.setContext(lContext);
					ev.setService(service);
					eventFactory.generateEvent(ev);
				}
				/*
				 * Activity becomes published again. Because data can show what has changed
				 * based on draft version, we can't use that. We need to know what has changed based on
				 * original activity, so all fields are treated as changed.
				 */
				else {
					EventData ev = fireActivityPublishedAgainEditEvent(actClass, user, act, 
				    		data.getActivityId());
					ev.setPage(page);
					ev.setContext(lContext);
					ev.setService(service);
				    eventFactory.generateEvent(ev);
				}
			} else {
				/*
				 * if activity remains draft
				 */
				if(!data.isPublishedChanged()) {
					long originalVersionId = 0;
					if(data.isDraft()) {
						originalVersionId = originalActivityId;
					}
					fireSameVersionActivityEditEvent(actClass, data, user, act, originalVersionId, page,
							lContext, service);
				} 
				/*
				 * This means that activity was published before so draft version is created.
				 */
				else {
					Map<String, String> params = new HashMap<>();
					params.put("originalVersionId", data.getActivityId() + "");
					eventFactory.generateEvent(EventType.Create_Draft, user.getId(), act, null, page,
							lContext, service, params);
				}
			}

		    return act;
		} catch (EventException e) {
			logger.error(e);
			e.printStackTrace();
			throw new DbConnectionException("Error while saving credential");
		} catch (DbConnectionException dbe) {
			logger.error(dbe);
			dbe.printStackTrace();
			throw dbe;
		}
	}
	
	private void fireSameVersionActivityEditEvent(Class<? extends Activity1> actClass, 
			ActivityData data, User user, Activity1 act, long originalVersionId, String page, 
			String context, String service) throws EventException {
		Map<String, String> params = new HashMap<>();
		boolean linksChanged = false;
		for(ResourceLinkData rl : data.getLinks()) {
			if(rl.getStatus() != ObjectStatus.UP_TO_DATE) {
				linksChanged = true;
				break;
			}
		}
		boolean filesChanged = false;
		for(ResourceLinkData rl : data.getFiles()) {
			if(rl.getStatus() != ObjectStatus.UP_TO_DATE) {
				filesChanged = true;
				break;
			}
		}
	    ActivityChangeTracker changeTracker = new ActivityChangeTracker(
	    		actClass, 
	    		data.isPublished(), 
	    		false, 
	    		data.isTitleChanged(), 
	    		data.isDescriptionChanged(), 
	    		data.isDurationHoursChanged() || data.isDurationMinutesChanged(), 
	    		linksChanged, 
	    		filesChanged, 
	    		data.isUploadAssignmentChanged(), 
	    		data.isTextChanged(), 
	    		data.isLinkChanged(), 
	    		data.isLaunchUrlChanged(), 
	    		data.isConsumerKeyChanged(), 
	    		data.isSharedSecretChanged(),
	    		data.isOpenInNewWindowChanged());
	    Gson gson = new GsonBuilder().create();
	    String jsonChangeTracker = gson.toJson(changeTracker);
	    params.put("changes", jsonChangeTracker);
	  
	    if(originalVersionId > 0) {
	    	params.put("originalVersionId", originalVersionId + "");
	    }
	    EventType event = data.isPublished() ? EventType.Edit : EventType.Edit_Draft;
	    eventFactory.generateEvent(event, user.getId(), act, null, page, context, service, params);
	}
	
	private EventData fireFirstTimePublishActivityEvent(User user, Activity1 act) {
		EventData ev = new EventData();
		ev.setEventType(EventType.Create);
		ev.setActorId(user.getId());
		ev.setObject(act);
		return ev;
	}
	
	private EventData fireActivityPublishedAgainEditEvent(Class<? extends Activity1> actClass, User user, 
			Activity1 act, long draftVersionId) {
		Map<String, String> params = new HashMap<>();
	    ActivityChangeTracker changeTracker = new ActivityChangeTracker(
	    		actClass, true, true, true, true, true, true, true, true, true, true, 
	    		true, true, true, true);
	    Gson gson = new GsonBuilder().create();
	    String jsonChangeTracker = gson.toJson(changeTracker);
	    params.put("changes", jsonChangeTracker);
	    params.put("draftVersionId", draftVersionId + "");
	    
	    EventData ev = new EventData();
	    ev.setEventType(EventType.Edit);
	    ev.setActorId(user.getId());
	    ev.setObject(act);
	    ev.setParameters(params);
	    return ev;
	}
	
	@Override
	@Transactional(readOnly = false)
	public Activity1 updateActivityData(ActivityData data, long userId) {
		Activity1 act = null;
		Class<? extends Activity1> activityClass = null;
		
		/*
		 * If activity type is changed it should mean that activity is first time draft and only way
		 * to change activity type is to delete old activity from database and insert as new activity
		 * with different type.
		 */
		if(data.isActivityTypeChanged()) {
			boolean firstTimeDraft = ((!data.isPublished() && !data.isPublishedChanged())
					|| (data.isPublished() && data.isPublishedChanged())) && !data.isDraft();
			
			if(firstTimeDraft) {
				updateActivityType(data.getActivityId(), data.getActivityType());
			} else {
				throw new RuntimeException("You can't change type for activity that was published once");
			}
			
		}
		/*
		 * draft should be created if something changed, draft option is chosen 
		 * and activity was published before this update
		*/
		EntityPublishTransition publishTransition = (!data.isPublished() && data.isPublishedChanged()) ? 
				EntityPublishTransition.FROM_PUBLISHED_TO_DRAFT_VERSION :
				EntityPublishTransition.NO_TRANSITION;
		/*
		 * check if published option was chosen, it was draft before update and 
		 * draft version (and not original activity) of activity is updated.
		 * Last check added because it is possible for original activity to be
		 * draft and that it doesn't have draft version because it was never published.
		*/
		if(publishTransition == EntityPublishTransition.NO_TRANSITION) {
			publishTransition = (data.isPublished() && data.isPublishedChanged() && data.isDraft()) ? 
					publishTransition = EntityPublishTransition.FROM_DRAFT_VERSION_TO_PUBLISHED :
					publishTransition;
		}
		
		switch(data.getActivityType()) {
			case TEXT:
				act = (TextActivity1) persistence.currentManager().load(TextActivity1.class, 
						data.getActivityId());
				activityClass = TextActivity1.class;
				break;
			case VIDEO:
			case SLIDESHARE:
				act = (UrlActivity1) persistence.currentManager().load(UrlActivity1.class, 
						data.getActivityId());
				activityClass = UrlActivity1.class;
				break;
			case EXTERNAL_TOOL:
				act = (ExternalToolActivity1) persistence.currentManager().load(
						ExternalToolActivity1.class, data.getActivityId());
				activityClass = ExternalToolActivity1.class;
				break;
		}
		
		return updateActivityData(act, activityClass, publishTransition, data);
		
	}
	
	private void updateActivityType(long activityId, ActivityType activityType) {
		Activity1 act = activityFactory.getObjectForActivityType(activityType);
		boolean isUrlActivity = act instanceof UrlActivity1;
		StringBuilder builder = new StringBuilder();
		builder.append("UPDATE activity1 SET dtype = :type ");
		if(isUrlActivity) {
			builder.append(", url_type = :urlType ");
		}
		builder.append("WHERE id = :id");

		Query q = persistence.currentManager()
			.createSQLQuery(builder.toString())
			.setString("type", act.getClass().getSimpleName())
			.setLong("id", activityId);
		if(isUrlActivity) {
			q.setString("urlType", ((UrlActivity1) act).getUrlType().toString());
		}
		
		q.executeUpdate();
	}

//	@Transactional(readOnly = false)
//	private void deleteActivity(long activityId) {
//		String query1 = "DELETE FROM activity1_links where activity1 = :activityId";
//		persistence.currentManager()
//			.createSQLQuery(query1)
//			.setLong("activityId", activityId)
//			.executeUpdate();
//		
//		String query2 = "DELETE FROM activity1_files where activity1 = :activityId";
//		persistence.currentManager()
//			.createSQLQuery(query2)
//			.setLong("activityId", activityId)
//			.executeUpdate();
//		
//		String query3 = "DELETE FROM activity1 where id = :activityId";
//		persistence.currentManager()
//			.createSQLQuery(query3)
//			.setLong("activityId", activityId)
//			.executeUpdate();
//	}
	
	/**
	 * Updates all activity updatable fields and takes into
	 * account transitions between activity publish status
	 * so if status is changed from published to draft, draft
	 * version is inserted and original activity is referencing
	 * this version. If status is changed from draft to published,
	 * it is checked if activity has been published once and if 
	 * it has, that means that draft version exists and this version
	 * is deleted.
	 * @param act
	 * @param publishTransition
	 * @param data
	 */
	@Transactional(readOnly = false)
	private Activity1 updateActivityData(Activity1 act, Class<? extends Activity1> activityClass, 
			EntityPublishTransition publishTransition, ActivityData data) {
		try {
			Activity1 actToUpdate = null;
			switch(publishTransition) {
				case FROM_PUBLISHED_TO_DRAFT_VERSION:
					actToUpdate = activityClass.newInstance();
					actToUpdate.setDraft(true);
					actToUpdate.setCreatedBy(act.getCreatedBy());
					actToUpdate.setDateCreated(act.getDateCreated());
					act.setHasDraft(true);
					act.setPublished(false);
					break;
				case FROM_DRAFT_VERSION_TO_PUBLISHED:
					actToUpdate = getOriginalActivityForDraft(act.getId());
					actToUpdate.setHasDraft(false);
					actToUpdate.setDraftVersion(null);
			    	delete(act);
			    	long newDuration = data.getDurationHours() * 60 + data.getDurationMinutes();
			    	updateCompDuration(actToUpdate.getId(), newDuration, actToUpdate.getDuration());
			    	break;
				case NO_TRANSITION:
					actToUpdate = act;
					//update duration for competence and credentials if it is original version
					if(!data.isDraft() && 
							(data.isDurationHoursChanged() || data.isDurationMinutesChanged())) {
						int oldHours = 0;
						int oldMinutes = 0;
						Optional<Integer> optDurationHours = data.getDurationHoursBeforeUpdate();
						if(optDurationHours.isPresent()) {
							oldHours = optDurationHours.get().intValue();
						} else {
							oldHours = data.getDurationHours();
						}
						Optional<Integer> optDurationMinutes = data.getDurationMinutesBeforeUpdate();
						if(optDurationMinutes.isPresent()) {
							oldMinutes = optDurationMinutes.get().intValue();
						} else {
							oldMinutes = data.getDurationMinutes();
						}
						long oldDuration = oldHours * 60 + 
								oldMinutes;
						long newDuration1 = data.getDurationHours() * 60 + data.getDurationMinutes();
						updateCompDuration(actToUpdate.getId(), newDuration1, oldDuration);
					}
					break;
			}
			
			actToUpdate.setTitle(data.getTitle());
			actToUpdate.setDescription(data.getDescription());
			actToUpdate.setDuration(data.getDurationHours() * 60 + data.getDurationMinutes());
			actToUpdate.setPublished(data.isPublished());
			actToUpdate.setMaxPoints(data.getMaxPointsString().isEmpty() ? 0 : Integer.parseInt(data.getMaxPointsString()));
			actToUpdate.setStudentCanSeeOtherResponses(data.isStudentCanSeeOtherResponses());
			actToUpdate.setStudentCanEditResponse(data.isStudentCanEditResponse());
			actToUpdate.setResultType(activityFactory.getResultType(data.getResultData().getResultType()));
			//actToUpdate.setUploadAssignment(data.isUploadAssignment());

			updateResourceLinks(data.getLinks(), actToUpdate.getLinks(), 
					publishTransition);
			
			updateResourceLinks(data.getFiles(), actToUpdate.getFiles(), 
					publishTransition);
			
			if(data.getActivityType() == ActivityType.VIDEO) {
				updateResourceLinks(data.getCaptions(), ((UrlActivity1) actToUpdate).getCaptions(),
					publishTransition);
			}
			
			if(actToUpdate instanceof TextActivity1) {
				TextActivity1 ta = (TextActivity1) actToUpdate;
				ta.setText(data.getText());
			} else if(actToUpdate instanceof UrlActivity1) {
				UrlActivity1 urlAct = (UrlActivity1) actToUpdate;
				if(data.getActivityType() == ActivityType.VIDEO) {
					urlAct.setUrlType(UrlActivityType.Video);
				} else {
					urlAct.setUrlType(UrlActivityType.Slides);
				}
				urlAct.setUrl(data.getLink());
				urlAct.setLinkName(data.getLinkName());
			} else if(actToUpdate instanceof ExternalToolActivity1) {
				ExternalToolActivity1 extAct = (ExternalToolActivity1) actToUpdate;
				extAct.setLaunchUrl(data.getLaunchUrl());
				extAct.setSharedSecret(data.getSharedSecret());
				extAct.setConsumerKey(data.getConsumerKey());
				extAct.setAcceptGrades(data.isAcceptGrades());
				extAct.setOpenInNewWindow(data.isOpenInNewWindow());
			}
		    
			if(publishTransition == EntityPublishTransition.FROM_PUBLISHED_TO_DRAFT_VERSION) {
				saveEntity(actToUpdate);
				act.setDraftVersion(actToUpdate);
			}
		    
		    return actToUpdate;
		} catch(Exception e) {
			logger.error(e);
			e.printStackTrace();
			return null;
		}
	}
	
	private void updateCompDuration(long actId, long newDuration, long oldDuration) {
		long durationChange = newDuration - oldDuration;
    	Operation op = null;
    	if(durationChange > 0) {
    		op = Operation.Add;
    	} else {
    		durationChange = -durationChange;
    		op = Operation.Subtract;
    	}
    	compManager.updateDurationForCompetencesWithActivity(actId, durationChange, op);
	}

	/**
	 * This method will save resource link (insert or update), add or remove it when needed from
	 * activity, or even set new set of resource links to activity.
	 * @param resLinksData
	 * @param resLinks
	 * @param actToUpdate
	 * @param linkSetter
	 * @param publishTransition
	 */
	public void updateResourceLinks(List<ResourceLinkData> resLinksData, Set<ResourceLink> resLinks, 
			EntityPublishTransition publishTransition) {
		
		if (resLinksData != null) {
			if (publishTransition == EntityPublishTransition.NO_TRANSITION) {
				for (ResourceLinkData rl : resLinksData) {
					switch (rl.getStatus()) {
						case CREATED:
							ResourceLink link = new ResourceLink();
							link.setLinkName(rl.getLinkName());
							link.setUrl(rl.getUrl());
							if (rl.getIdParamName() != null && !rl.getIdParamName().isEmpty()) {
		    					link.setIdParameterName(rl.getIdParamName());
		    				}
							saveEntity(link);
							resLinks.add(link);
							break;
						case CHANGED:
							ResourceLink updatedLink = (ResourceLink) persistence.currentManager()
								.load(ResourceLink.class, rl.getId());
							updatedLink.setLinkName(rl.getLinkName());
							updatedLink.setUrl(rl.getUrl());
							if (rl.getIdParamName() != null && !rl.getIdParamName().isEmpty()) {
								updatedLink.setIdParameterName(rl.getIdParamName());
		    				}
							break;
						case REMOVED:
							ResourceLink removedLink = (ResourceLink) persistence.currentManager()
								.load(ResourceLink.class, rl.getId());
							resLinks.remove(removedLink);
							break;
						default:
							break;
					}
				}
			} else {
				//Set<ResourceLink> activityLinks = new HashSet<>();
				resLinks.clear();
				for (ResourceLinkData rl : resLinksData) {
					if (rl.getStatus() != ObjectStatus.REMOVED) {
						ResourceLink link = new ResourceLink();
						link.setLinkName(rl.getLinkName());
						link.setUrl(rl.getUrl());
						if (rl.getIdParamName() != null && !rl.getIdParamName().isEmpty()) {
							link.setIdParameterName(rl.getIdParamName());
	    				}
						saveEntity(link);
						resLinks.add(link);
						//activityLinks.add(link);
					}
				}
				//linkSetter.accept(actToUpdate, activityLinks);
			}
		}
	}

	@Override
	@Transactional(readOnly = true)
	public Activity1 getOriginalActivityForDraft(long draftActivityId) throws DbConnectionException {  
		try {
			Activity1 draftAct = (Activity1) persistence.currentManager().load(
					Activity1.class, draftActivityId);
			String query = "SELECT act " +
					   "FROM Activity1 act " + 
					   "WHERE act.draftVersion = :draft";
			
			Activity1 original = (Activity1) persistence.currentManager()
					.createQuery(query)
					.setEntity("draft", draftAct)
					.uniqueResult();
			return original;
		} catch(Exception e) {
			logger.error(e);
			e.printStackTrace();
			throw new DbConnectionException("Error while retrieving activity data");
		}
	}
	
//	@Override
//	@Transactional(readOnly = true)
//	public CompetenceData1 getTargetCompetenceActivitiesWithSpecifiedActivityInFocus(
//			long targetActivityId) throws DbConnectionException {
//		CompetenceData1 compData = null;
//		try {			
//			ActivityData activityWithDetails = getTargetActivityData(targetActivityId, true);
//
//			if (activityWithDetails != null) {
//				String query1 = "SELECT comp.id, targetComp.id, targetComp.title, cred.id, targetCred.title, targetComp.createdBy.id " +
//								"FROM TargetCompetence1 targetComp " +
//								"INNER JOIN targetComp.competence comp " +
//								"INNER JOIN targetComp.targetCredential targetCred " +
//								"INNER JOIN targetCred.credential cred " +
//								"WHERE targetComp.id = :compId";
//				Object[] res1 = (Object[]) persistence.currentManager()
//						.createQuery(query1)
//						.setLong("compId", activityWithDetails.getCompetenceId())
//						.uniqueResult();
//				
//				if(res1 != null) {
//					compData = new CompetenceData1(false);
//					compData.setCompetenceId((long) res1[0]);
//					compData.setTargetCompId((long) res1[1]);
//					compData.setTitle((String) res1[2]);
//					compData.setCredentialId((long) res1[3]);
//					compData.setCredentialTitle((String) res1[4]);
//					/*
//					 * competence creator id is set because creator of a competence
//					 * is also a creator of all activities for that competence.
//					 */
//					ResourceCreator rc = new ResourceCreator();
//					rc.setId((long) res1[5]);
//					compData.setCreator(rc);
//					
//					compData.setActivityToShowWithDetails(activityWithDetails);
//					List<ActivityData> activities = getTargetActivitiesData(activityWithDetails
//							.getCompetenceId());
//					compData.setActivities(activities);
//					return compData;
//				}
//			}
//			return null;
//		} catch (Exception e) {
//			logger.error(e);
//			e.printStackTrace();
//			throw new DbConnectionException("Error while loading competence data");
//		}
//	}
	 
//	@Transactional(readOnly = true)
//	private ActivityData getTargetActivityData(long targetActivityId, boolean loadResourceLinks) 
//			throws DbConnectionException {
//		try {	
//			StringBuilder builder = new StringBuilder();
//			builder.append("SELECT targetAct " +
//					   	   "FROM TargetActivity1 targetAct ");
//			if(loadResourceLinks) {
//				builder.append("LEFT JOIN fetch targetAct.links link " + 
//						       "LEFT JOIN fetch targetAct.files files ");
//			}
//			builder.append("WHERE targetAct.id = :id");
////			String query = "SELECT targetAct " +
////						   "FROM TargetActivity1 targetAct " + 
////						   "LEFT JOIN fetch targetAct.links link " + 
////						   "LEFT JOIN fetch targetAct.files files " +
////						   "WHERE targetAct.id = :id";
//
//			TargetActivity1 res = (TargetActivity1) persistence.currentManager()
//					.createQuery(builder.toString())
//					.setLong("id", targetActivityId)
//					.uniqueResult();
//
//			if (res != null) {
//					Set<ResourceLink> links = loadResourceLinks ? res.getLinks() : null;
//					Set<ResourceLink> files = loadResourceLinks ? res.getFiles() : null;
//					ActivityData activityWithDetails = activityFactory.getActivityData(res, links, 
//							files, false);
//					return activityWithDetails;
//			}
//			return null;
//		} catch (Exception e) {
//			logger.error(e);
//			e.printStackTrace();
//			throw new DbConnectionException("Error while loading activity data");
//		}
//	}
	
	@Override
	@Transactional(readOnly = true)
	public CompetenceData1 getCompetenceActivitiesWithSpecifiedActivityInFocus(long credId, 
			long compId, long activityId, long creatorId, boolean shouldReturnDraft, Role role, 
			LearningResourceReturnResultType returnType) throws DbConnectionException {
		CompetenceData1 compData = null;
		try {
			ActivityData activityWithDetails = null;
			if(shouldReturnDraft) {
				activityWithDetails = getCompetenceActivityCurrentVersionData(credId, compId, activityId, 
						creatorId, role);
						//getActivityDataForEdit(activityId, creatorId);
			} else {
				activityWithDetails = getCompetenceActivityData(credId, compId, activityId, true, 
						creatorId, returnType);
			}

			if (activityWithDetails != null) {
					compData = new CompetenceData1(false);
					compData.setActivityToShowWithDetails(activityWithDetails);
					List<ActivityData> activities = getCompetenceActivitiesData(
							activityWithDetails.getCompetenceId());
					compData.setActivities(activities);
					return compData;
			}
			return null;
		} catch (Exception e) {
			logger.error(e);
			e.printStackTrace();
			throw new DbConnectionException("Error while loading competence data");
		}
	}
	
	@Override
	@Transactional(readOnly = true)
	public CompetenceData1 getCompetenceActivitiesWithSpecifiedActivityInFocusForUser(long credId,
			long compId, long activityId, long creatorId, boolean shouldReturnDraft) 
					throws DbConnectionException {
		return getCompetenceActivitiesWithSpecifiedActivityInFocus(credId, compId, activityId, creatorId, 
				shouldReturnDraft, Role.User, LearningResourceReturnResultType.FIRST_TIME_DRAFT_FOR_USER);
	}
	
	@Override
	@Transactional(readOnly = true)
	public CompetenceData1 getCompetenceActivitiesWithSpecifiedActivityInFocusForManager(long credId, 
			long compId, long activityId, boolean shouldReturnDraft) throws DbConnectionException {
		return getCompetenceActivitiesWithSpecifiedActivityInFocus(credId, compId, activityId, 0, 
				shouldReturnDraft, Role.Manager, 
				LearningResourceReturnResultType.FIRST_TIME_DRAFT_FOR_MANAGER);
	}
	 
	/**
	 * Returns competence activity from original version of competence
	 * @param activityId
	 * @param loadResourceLinks
	 * @return
	 * @throws DbConnectionException
	 */
	@Transactional(readOnly = true)
	private ActivityData getCompetenceActivityData(long credId, long compId, long activityId, 
			boolean loadResourceLinks, long userId, LearningResourceReturnResultType returnType) 
					throws DbConnectionException {
		try {	
			StringBuilder builder = new StringBuilder();
			builder.append("SELECT compAct ");
			if(credId > 0) {
				builder.append("FROM CredentialCompetence1 credComp " +
							   "INNER JOIN credComp.competence comp " +
							  		"WITH comp.id = :compId " +
							   "AND comp.draft = :draft ");
			} else {
				builder.append("FROM CompetenceActivity1 compAct " +
						   	   "INNER JOIN fetch compAct.activity act " +				   	 
						   	   "INNER JOIN compAct.competence comp " +
					   	   			"WITH comp.id = :compId " +
						   			"AND comp.draft = :draft ");
			}
			
//			builder.append("FROM CompetenceActivity1 compAct " +
//						   "INNER JOIN fetch compAct.activity act " +				   	 
//					   	   "INNER JOIN compAct.competence comp " +
//						   		"WITH comp.draft = :draft ");
			
			String compCondition = "AND (comp.published = :published " +
							       "OR comp.hasDraft = :hasDraft ";
			String actCondition = "AND (act.published = :published " +
						          "OR act.hasDraft = :hasDraft ";
			switch(returnType) {
				case ANY:
					compCondition = "";
					actCondition = "";
					break;
				case PUBLISHED_VERSION:
					compCondition += ")";
					actCondition += ")";
					break;
				case FIRST_TIME_DRAFT_FOR_USER:
					compCondition += "OR comp.createdBy.id = :userId) ";
					actCondition += "OR act.createdBy.id = :userId) ";
					break;
				case FIRST_TIME_DRAFT_FOR_MANAGER:
					compCondition += "OR comp.type = :type) ";
					actCondition += "OR act.type = :type) ";
					break;
			}
			
			builder.append(compCondition);
			
			if(credId > 0) {
				builder.append("INNER JOIN comp.activities compAct " +
						"WITH compAct.activity.id = :actId " +
						"INNER JOIN fetch compAct.activity act ");
			}
			if(loadResourceLinks) {
				builder.append("LEFT JOIN fetch act.links link " + 
						       "LEFT JOIN fetch act.files files ");
			}
			if(credId > 0) {
				builder.append("WHERE credComp.credential.id = :credId " +
							   "AND act.deleted = :deleted ");
			} else {
				builder.append("WHERE act.id = :actId " +
						   	   "AND act.deleted = :deleted ");
			}
			
			builder.append(actCondition);

			Query q = persistence.currentManager()
					.createQuery(builder.toString())
					.setLong("actId", activityId)
					.setLong("compId", compId)
					.setBoolean("deleted", false)
					.setBoolean("draft", false);
			
			if(credId > 0) {
				q.setLong("credId", credId);
			}
			
			if(returnType != LearningResourceReturnResultType.ANY) {
				q.setBoolean("published", true)
				 .setBoolean("hasDraft", true);
			}
			if(returnType == LearningResourceReturnResultType.FIRST_TIME_DRAFT_FOR_USER) {
				q.setLong("userId", userId);
			} else if(returnType == LearningResourceReturnResultType.FIRST_TIME_DRAFT_FOR_MANAGER) {
				q.setParameter("type", LearningResourceType.UNIVERSITY_CREATED);
			}
			
			CompetenceActivity1 res = (CompetenceActivity1) q.uniqueResult();

			if (res != null) {
					Set<ResourceLink> links = loadResourceLinks ? res.getActivity().getLinks() : null;
					Set<ResourceLink> files = loadResourceLinks ? res.getActivity().getFiles() : null;
					ActivityData activityWithDetails = activityFactory.getActivityData(res, links, 
							files, false);
					return activityWithDetails;
			}
			return null;
		} catch (Exception e) {
			logger.error(e);
			e.printStackTrace();
			throw new DbConnectionException("Error while loading activity data");
		}
	}
	
	@Override
	@Transactional(readOnly = false)
	public void saveResponse(long targetActId, String path, Date postDate, long userId, 
			ActivityResultType resType, LearningContextData context) throws DbConnectionException {
		try {
			String query = "UPDATE TargetActivity1 act SET " +
						   "act.result = :path, " +
						   "act.resultPostDate = :date " +
						   "WHERE act.id = :actId";
			
			persistence.currentManager()
				.createQuery(query)
				.setLong("actId", targetActId)
				.setString("path", path)
				.setTimestamp("date", postDate)
				.executeUpdate();
			
			User user = new User();
			user.setId(userId);
			TargetActivity1 tAct = new TargetActivity1();
			tAct.setId(targetActId);
			String lcPage = context != null ? context.getPage() : null; 
			String lcContext = context != null ? context.getLearningContext() : null; 
			String lcService = context != null ? context.getService() : null;
			EventType evType = resType == ActivityResultType.FILE_UPLOAD
					? EventType.AssignmentUploaded : EventType.Typed_Response_Posted;
			eventFactory.generateEvent(evType, user.getId(), tAct, null,
					lcPage, lcContext, lcService, null);
		} catch (Exception e) {
			logger.error(e);
			e.printStackTrace();
			throw new DbConnectionException("Error while saving assignment");
		}
	}
	
	@Override
	@Transactional(readOnly = false)
	public void updateTextResponse(long targetActId, String path, long userId, 
			LearningContextData context) throws DbConnectionException {
		try {
			String query = "UPDATE TargetActivity1 act SET " +
						   "act.result = :path " +
						   "WHERE act.id = :actId";
			
			persistence.currentManager()
				.createQuery(query)
				.setLong("actId", targetActId)
				.setString("path", path)
				.executeUpdate();
			
			User user = new User();
			user.setId(userId);
			TargetActivity1 tAct = new TargetActivity1();
			tAct.setId(targetActId);
			String lcPage = context != null ? context.getPage() : null; 
			String lcContext = context != null ? context.getLearningContext() : null; 
			String lcService = context != null ? context.getService() : null;
			eventFactory.generateEvent(EventType.Typed_Response_Edit, user.getId(), tAct, null,
					lcPage, lcContext, lcService, null);
		} catch (Exception e) {
			logger.error(e);
			e.printStackTrace();
			throw new DbConnectionException("Error while editing response");
		}
	}
	
	@Override
	@Transactional(readOnly = false)
	public void completeActivity(long targetActId, long targetCompId, long credId, long userId,
			LearningContextData contextData) throws DbConnectionException {
		try {
			String query = "UPDATE TargetActivity1 act SET " +
						   "act.completed = :completed, " +
						   "act.dateCompleted = :date " +
						   "WHERE act.id = :actId";
			
			persistence.currentManager()
				.createQuery(query)
				.setLong("actId", targetActId)
				.setBoolean("completed", true)
				.setDate("date", new Date())
				.executeUpdate();
			
			credManager.updateCredentialAndCompetenceProgressAndNextActivityToLearn(credId, 
					targetCompId, targetActId, userId, contextData);
			
			TargetActivity1 tAct = new TargetActivity1();
			tAct.setId(targetActId);
			String lcPage = contextData != null ? contextData.getPage() : null; 
			String lcContext = contextData != null ? contextData.getLearningContext() : null; 
			String lcService = contextData != null ? contextData.getService() : null;
			eventFactory.generateEvent(EventType.Completion, userId, tAct, null,
					lcPage, lcContext, lcService, null);
		} catch (Exception e) {
			logger.error(e);
			e.printStackTrace();
			throw new DbConnectionException("Error while updating activity progress");
		}
	}
	
	@Override
	@Transactional(readOnly = true)
	public CompetenceData1 getFullTargetActivityOrActivityData(long credId, long compId, 
			long actId, long userId, boolean shouldReturnDraft) throws DbConnectionException {
		CompetenceData1 compData = null;
		try {
			compData = getTargetCompetenceActivitiesWithSpecifiedActivityInFocus(credId, 
					compId, actId, userId);
			if (compData == null) {
				compData = getCompetenceActivitiesWithSpecifiedActivityInFocus(credId, compId, actId, 
						userId, shouldReturnDraft, Role.User, 
						LearningResourceReturnResultType.FIRST_TIME_DRAFT_FOR_USER);
			}
				
			return compData;
		} catch (Exception e) {
			logger.error(e);
			e.printStackTrace();
			throw new DbConnectionException("Error while loading competence data");
		}
	}
	
	@Transactional(readOnly = true)
	private CompetenceData1 getTargetCompetenceActivitiesWithSpecifiedActivityInFocus(
			long credId, long compId, long actId, long userId) 
					throws DbConnectionException {
		//TODO test if EDIT activity button is enabled, if creator id is set in data object
		CompetenceData1 compData = null;
		try {			
			ActivityData activityWithDetails = getTargetActivityData(credId, compId, actId, userId, true);

			if (activityWithDetails != null) {
//				String query1 = "SELECT targetComp.title, targetCred.title, targetComp.createdBy.id " +
//								"FROM TargetCompetence1 targetComp " +
//								"INNER JOIN targetComp.targetCredential targetCred " +
//								"WHERE targetComp.id = :compId";
//				Object[] res1 = (Object[]) persistence.currentManager()
//						.createQuery(query1)
//						.setLong("compId", activityWithDetails.getCompetenceId())
//						.uniqueResult();
//				
//				if(res1 != null) {
//					compData = new CompetenceData1(false);
//					compData.setCompetenceId(compId);
//					compData.setTitle((String) res1[0]);
//					compData.setCredentialId(credId);
//					compData.setCredentialTitle((String) res1[1]);
//					/*
//					 * competence creator id is set because creator of a competence
//					 * is also a creator of all activities for that competence.
//					 */
//					ResourceCreator rc = new ResourceCreator();
//					rc.setId((long) res1[2]);
//					compData.setCreator(rc);
				compData = new CompetenceData1(false);
				compData.setActivityToShowWithDetails(activityWithDetails);
				//compId is id of a competence and activityWithDetails.getCompetenceId() is id of a target competence
				List<ActivityData> activities = getTargetActivitiesData(activityWithDetails.getCompetenceId());
				compData.setActivities(activities);
				return compData;
				//}
			}
			return null;
		} catch (Exception e) {
			logger.error(e);
			e.printStackTrace();
			throw new DbConnectionException("Error while loading competence data");
		}
	}
	
	/**
	 * Returns full target activity data when id of a target activity is not known.
	 * @param credId
	 * @param compId
	 * @param actId
	 * @param userId
	 * @return
	 * @throws DbConnectionException
	 */
	@Transactional(readOnly = true)
	private ActivityData getTargetActivityData(long credId, long compId, long actId, long userId,
			boolean loadResourceLinks) 
			throws DbConnectionException {
		try {		
			StringBuilder query = new StringBuilder("SELECT targetAct " +
					   "FROM TargetActivity1 targetAct " +
					   "INNER JOIN targetAct.targetCompetence targetComp " +
					   		"WITH targetComp.competence.id = :compId " +
					   "INNER JOIN targetComp.targetCredential targetCred " +
					   		"WITH targetCred.credential.id = :credId " +
					   		"AND targetCred.user.id = :userId ");
			if(loadResourceLinks) {
				query.append("LEFT JOIN fetch targetAct.links link " + 
							 "LEFT JOIN fetch targetAct.files files ");
			}
			query.append("WHERE targetAct.activity.id = :actId");

			TargetActivity1 res = (TargetActivity1) persistence.currentManager()
					.createQuery(query.toString())
					.setLong("userId", userId)
					.setLong("credId", credId)
					.setLong("compId", compId)
					.setLong("actId", actId)
					.uniqueResult();

			if (res != null) {
				Set<ResourceLink> links = loadResourceLinks ? res.getLinks() : null;
				Set<ResourceLink> files = loadResourceLinks ? res.getFiles() : null;
				ActivityData activity = activityFactory.getActivityData(res, links, 
						files, true);
				return activity;
			}
			return null;
		} catch (Exception e) {
			logger.error(e);
			e.printStackTrace();
			throw new DbConnectionException("Error while loading activity data");
		}
	}
	
	@Override
	@Transactional(readOnly = false)
	public void deleteAssignment(long targetActivityId, long userId, LearningContextData context) 
			throws DbConnectionException {
		try {
			String query = "UPDATE TargetActivity1 act SET " +
						   "act.assignmentLink = :nullString " +
						   "WHERE act.id = :id";
			persistence.currentManager()
				.createQuery(query)
				.setLong("id", targetActivityId)
				.setString("nullString", null)
				.executeUpdate();
			
			User user = new User();
			user.setId(userId);
			TargetActivity1 tAct = new TargetActivity1();
			tAct.setId(targetActivityId);
			String lcPage = context != null ? context.getPage() : null; 
			String lcContext = context != null ? context.getLearningContext() : null; 
			String lcService = context != null ? context.getService() : null;
			eventFactory.generateEvent(EventType.AssignmentRemoved, user.getId(), tAct, null,
					lcPage, lcContext, lcService, null);
		} catch (Exception e) {
			logger.error(e);
			e.printStackTrace();
			throw new DbConnectionException("Error while removing assignment");
		}
	}
	
	@Override
	@Transactional(readOnly = false)
	public void updateTargetActivitiesWithChangedData(long actId, ActivityChangeTracker changeTracker) 
			throws DbConnectionException {
		if(changeTracker.isPublished()) {
			if(changeTracker.isLinksChanged()) {
				updateTargetActivityLinks(actId);
			}
			if(changeTracker.isFilesChanged()) {
				updateTargetActivityFiles(actId);
			}
			if(changeTracker.isTitleChanged() || changeTracker.isDescriptionChanged()
					|| changeTracker.isDurationChanged() || changeTracker.isUploadResultChanged()
					|| changeTracker.isTextChanged() || changeTracker.isUrlChanged() 
					|| changeTracker.isLaunchUrlChanged() || changeTracker.isConsumerKeyChanged() 
					|| changeTracker.isSharedSecretChanged() || changeTracker.isOpenInNewWindowChanged()) {
				updateTargetActivityBasicDataForUncompletedCredentials(actId, 
						changeTracker.getActivityClass());
				
				if(changeTracker.isDurationChanged()) {		
					recalculateTargetCompetencesAndCredentialsDuration(actId);
				}
			}
		}
	}
	
	@Transactional(readOnly = true)
	private void recalculateTargetCompetencesAndCredentialsDuration(long actId) {
		try {
			String query1 = "SELECT cred.id " +
					"FROM TargetActivity1 act " +
					"INNER JOIN act.targetCompetence comp " +
					"INNER JOIN comp.targetCredential cred " +
						"WITH cred.progress != :progress " +
					"WHERE act.activity.id = :actId";
	
			@SuppressWarnings("unchecked")
			List<Long> credIds = persistence.currentManager()
					.createQuery(query1)
					.setInteger("progress", 100)
					.setLong("actId", actId)
					.list();
			if(!credIds.isEmpty()) {
				String query2 = "SELECT cred.id, comp.id, act.id, act.duration " +
							    "FROM TargetCredential1 cred " +
							    "LEFT JOIN cred.targetCompetences comp " +
							    "LEFT JOIN comp.targetActivities tAct " +
							    "INNER JOIN tAct.activity act " +
							    "WHERE cred.id IN (:credIds) " +									    
							    "ORDER BY cred.id, comp.id";
			
				@SuppressWarnings("unchecked")
				List<Object[]> res = persistence.currentManager()
						.createQuery(query2)
						.setParameterList("credIds", credIds)
						.list();
				
				if(res != null) {
					long currentCredId = 0;
					long currentCompId = 0;
					long cumulativeCredDuration = 0;
					long cumulativeCompDuration = 0;
					boolean foundCompToUpdate = false;
					for(Object[] row : res) {
						long credId = (long) row[0];
						long compId = (long) row[1];
						long activityId = (long) row[2];
						long duration = (long) row[3];
						
						if(credId == currentCredId) {
							cumulativeCredDuration += duration;
						} else {
							if(currentCredId != 0) {
								credManager.updateTargetCredentialDuration(currentCredId, 
										cumulativeCredDuration);
							}
							currentCredId = credId;
							cumulativeCredDuration = duration;
						}
						
						if(compId == currentCompId) {
							//if other than first activity in a competence is changed
							if(activityId == actId) {
								foundCompToUpdate = true;
							}
							cumulativeCompDuration += duration;
						} else {
							if(foundCompToUpdate) {
								compManager.updateTargetCompetenceDuration(currentCompId, 
										cumulativeCompDuration);
								foundCompToUpdate = false;
							} 
							currentCompId = compId;
							cumulativeCompDuration = duration;
							//if first activity in a competence is changed
							if(activityId == actId) {
								foundCompToUpdate = true;
							}
						}
					}
					if(currentCredId != 0) {
						credManager.updateTargetCredentialDuration(currentCredId, 
								cumulativeCredDuration);
					}
					if(foundCompToUpdate) {
						compManager.updateTargetCompetenceDuration(currentCompId, 
								cumulativeCompDuration);
						foundCompToUpdate = false;
					} 
				}
			}
		} catch(Exception e) {
			logger.error(e);
			e.printStackTrace();
			throw new DbConnectionException("Error while recalculating duration");
		}
	}

	@Transactional(readOnly = false)
	private void updateTargetActivityLinks(long actId) 
			throws DbConnectionException {
		try {
			List<TargetActivity1> targetActivities = getTargetActivitiesForActivity(actId, true);
			List<ResourceLink> links = getActivityLinks(actId);
			for(TargetActivity1 ta : targetActivities) {
				ta.getLinks().clear();
				for(ResourceLink rl : links) {
					ResourceLink rl1 = new ResourceLink();
					rl1.setUrl(rl.getUrl());
					rl1.setLinkName(rl.getLinkName());
					if (rl.getIdParameterName() != null && !rl.getIdParameterName().isEmpty()) {
						rl1.setIdParameterName(rl.getIdParameterName());
    				}
					saveEntity(rl1);
					ta.getLinks().add(rl1);
				}
			}
		} catch(Exception e) {
			logger.error(e);
			e.printStackTrace();
			throw new DbConnectionException("Error while updating user activities");
		}
	}
	
	@Transactional(readOnly = false)
	private void updateTargetActivityFiles(long actId) 
			throws DbConnectionException {
		try {
			List<TargetActivity1> targetActivities = getTargetActivitiesForActivity(actId, true);
			List<ResourceLink> files = getActivityFiles(actId);
			for(TargetActivity1 ta : targetActivities) {
				ta.getFiles().clear();
				for(ResourceLink rl : files) {
					ResourceLink rl1 = new ResourceLink();
					rl1.setUrl(rl.getUrl());
					rl1.setLinkName(rl.getLinkName());
					saveEntity(rl1);
					ta.getFiles().add(rl1);
				}
			}
		} catch(Exception e) {
			logger.error(e);
			e.printStackTrace();
			throw new DbConnectionException("Error while updating user activities");
		}
	}
	
	@Transactional(readOnly = true)
	private List<ResourceLink> getActivityLinks(long actId) 
			throws DbConnectionException {
		try {		
			//if left join is used list with null element would be returned.
			String query = "SELECT link " +
					       "FROM Activity1 act " +
					       "INNER JOIN act.links link " +
					       "WHERE act.id = :actId";					    
			@SuppressWarnings("unchecked")
			List<ResourceLink> res = persistence.currentManager()
				.createQuery(query)
				.setLong("actId", actId)
				.list();
			if(res == null) {
				return new ArrayList<>();
			}
			
			return res;
		} catch(Exception e) {
			logger.error(e);
			e.printStackTrace();
			throw new DbConnectionException("Error while loading activity links");
		}
	}
	
	@Transactional(readOnly = true)
	private List<ResourceLink> getActivityFiles(long actId) 
			throws DbConnectionException {
		try {			
			//if left join is used list with null element would be returned.
			String query = "SELECT file " +
					       "FROM Activity1 act " +
					       "INNER JOIN act.files file " +
					       "WHERE act.id = :actId";					    
			@SuppressWarnings("unchecked")
			List<ResourceLink> res = persistence.currentManager()
				.createQuery(query)
				.setLong("actId", actId)
				.list();
			if(res == null) {
				return new ArrayList<>();
			}
			
			return res;
		} catch(Exception e) {
			logger.error(e);
			e.printStackTrace();
			throw new DbConnectionException("Error while loading activity files");
		}
	}
	
	@Transactional(readOnly = false)
	private void updateTargetActivityBasicDataForUncompletedCredentials(long actId, 
			Class<? extends Activity1> activityClass) 
			throws DbConnectionException {
		try {			
			Activity1 act = (Activity1) persistence.currentManager().load(activityClass, 
					actId);
			List<Long> ids = getTargetActivityIdsForUncompletedCredentials(actId);
			
			if(!ids.isEmpty()) {
				StringBuilder builder = new StringBuilder();
				builder.append("UPDATE TargetActivity1 act SET " +
						       "act.title = :title, " +
						       "act.description = :description, " +
						       "act.duration = :duration, " +
						       "act.uploadAssignment = :uploadAssignment ");
				if(activityClass == TextActivity1.class) {
					builder.append(",act.text = :text ");
				} else if(activityClass == UrlActivity1.class) {
					builder.append(",act.url = :url ");
				} else if(activityClass == ExternalToolActivity1.class) {
					builder.append(",act.launchUrl = :launchUrl " +
							       ",act.sharedSecret = :sharedSecret " +
							       ",act.consumerKey = :consumerKey " +
								   ",act.openInNewWindow = :openInNewWindow ");
				}
				builder.append("WHERE act.id IN (:actIds)");				    
	
				Query q = persistence.currentManager()
					.createQuery(builder.toString())
					.setString("title", act.getTitle())
					.setString("description", act.getDescription())
					.setBoolean("uploadAssignment", act.isUploadAssignment())
					.setLong("duration", act.getDuration())
					.setParameterList("actIds", ids);
				if(activityClass == TextActivity1.class) {
					TextActivity1 ta = (TextActivity1) act;
					q.setString("text", ta.getText());
				} else if(activityClass == UrlActivity1.class) {
					UrlActivity1 urlAct = (UrlActivity1) act;
					q.setString("url", urlAct.getUrl());
				} else if(activityClass == ExternalToolActivity1.class) {
					ExternalToolActivity1 extAct = (ExternalToolActivity1) act;
					q.setString("launchUrl", extAct.getLaunchUrl())
					 .setString("sharedSecret", extAct.getSharedSecret())
					 .setString("consumerKey", extAct.getConsumerKey())
					 .setBoolean("openInNewWindow", extAct.isOpenInNewWindow());
				}
					
				q.executeUpdate();
			}
		} catch(Exception e) {
			logger.error(e);
			e.printStackTrace();
			throw new DbConnectionException("Error while updating user activities");
		}
	}
	
	@Transactional(readOnly = true)
	private List<Long> getTargetActivityIdsForUncompletedCredentials(long actId) {
		try {	
			Activity1 act = (Activity1) persistence.currentManager().load(Activity1.class, 
					actId);
			
			String query = "SELECT act.id FROM TargetActivity1 act " +
						   "INNER JOIN act.targetCompetence comp " +
				       	   "INNER JOIN comp.targetCredential cred " +
				       			"WITH cred.progress != :progress " +
				       	   "WHERE act.activity = :act";					    

			@SuppressWarnings("unchecked")
			List<Long> res = persistence.currentManager()
				.createQuery(query)
				.setEntity("act", act)
				.setInteger("progress", 100)
				.list();
			
			if(res == null) {
				return new ArrayList<>();
			}
			return res;
		} catch(Exception e) {
			logger.error(e);
			e.printStackTrace();
			throw new DbConnectionException("Error while retrieving activity ids");
		}
	}

	@Transactional(readOnly = true)
	private List<TargetActivity1> getTargetActivitiesForActivity(long actId, 
			boolean justUncompleted) 
			throws DbConnectionException {
		try {		
			Activity1 act = (Activity1) persistence.currentManager().load(Activity1.class, 
					actId);
			StringBuilder builder = new StringBuilder();
			builder.append("SELECT act " +
				           "FROM TargetActivity1 act ");
			if(justUncompleted) {
				builder.append("INNER JOIN act.targetCompetence comp " +
			       		       "INNER JOIN comp.targetCredential cred " +
									"WITH cred.progress != :progress ");
			}
			builder.append("WHERE act.activity = :act");
				    
			Query q = persistence.currentManager()
				.createQuery(builder.toString())
				.setEntity("act", act);
			if(justUncompleted) {
				q.setInteger("progress", 100);
			}
			@SuppressWarnings("unchecked")
			List<TargetActivity1> res = q.list();
			if(res == null) {
				return new ArrayList<>();
			}
			
			return res;
		} catch(Exception e) {
			logger.error(e);
			e.printStackTrace();
			throw new DbConnectionException("Error while loading user activities");
		}
	}
	
	@Override
	@Transactional(readOnly = true)
	public Long getCompetenceIdForActivity(long actId) throws DbConnectionException {
		try {	
			String query = "SELECT comp.id " +
						   "FROM CompetenceActivity1 compActivity " +
						   "INNER JOIN compActivity.competence comp " +
						   		"WITH comp.draft = :draft " +
				       	   "WHERE compActivity.activity.id = :actId";					    

			Long id = (Long) persistence.currentManager()
				.createQuery(query)
				.setLong("actId", actId)
				.setBoolean("draft", false)
				.uniqueResult();
			
			return id;
		} catch(Exception e) {
			logger.error(e);
			e.printStackTrace();
			throw new DbConnectionException("Error while retrieving competence id");
		}
	}
	
	@Override
	@Transactional(readOnly = true)
	public ActivityData getCurrentVersionOfActivityForManager(long credId, long competenceId, 
			long activityId) throws DbConnectionException {
			return getCurrentVersionOfActivityBasedOnRole(credId, competenceId, activityId, 0, Role.Manager);
	}
	
	@Override
	@Transactional(readOnly = false)
	public List<EventData> publishActivitiesFromCompetences(List<Long> compIds) 
			throws DbConnectionException {
		try {
			List<Activity1> acts = getDraftActivitiesFromCompetences(compIds);
			//publishDraftActivitiesWithoutDraftVersion(actIds);
			return publishDraftActivitiesFromList(acts);
		} catch(Exception e) {
			logger.error(e);
			e.printStackTrace();
			throw new DbConnectionException("Error while publishing activities");
		}
	}
	
	@Override
	@Transactional(readOnly = false)
	public List<EventData> publishDraftActivities(List<Long> actIds) 
			throws DbConnectionException {
		try {
			//get all draft activities
			List<Activity1> acts = getDraftActivitiesFromList(actIds);
			return publishDraftActivitiesFromList(acts);
		} catch(Exception e) {
			logger.error(e);
			e.printStackTrace();
			throw new DbConnectionException("Error while publishing activities");
		}
	}
	
	/**
	 * expecting list of draft activities as parameter
	 * @param activities
	 * @throws DbConnectionException
	 */
	@Transactional(readOnly = false)
	private List<EventData> publishDraftActivitiesFromList(List<Activity1> activities) 
			throws DbConnectionException {
		//iterate through list and if activity does not have draft version set to published, 
		//if it has, copy data from draft version to original and set published to true
		List<EventData> events = new ArrayList<>();
		for(Activity1 a : activities) {
			User user = new User();
			user.setId(a.getId());
			if(a.isHasDraft()) {
				Class<? extends Activity1> actClass = null;
				if(a instanceof TextActivity1) {
					actClass = TextActivity1.class;
				} else if(a instanceof UrlActivity1) {
					actClass = UrlActivity1.class;
				} else if(a instanceof ExternalToolActivity1) {
					actClass = ExternalToolActivity1.class;
				}
				long draftVersionId = a.getDraftVersion().getId();
				//persistence.currentManager().evict(a.getDraftVersion());
				Activity1 draftA = getActivity(draftVersionId, true);
				draftA = HibernateUtil.initializeAndUnproxy(draftA);
				publishDraftVersion(a, draftA);
			    events.add(fireActivityPublishedAgainEditEvent(actClass, user, a, 
			    		draftVersionId));
			} else {
				a.setPublished(true);
				events.add(fireFirstTimePublishActivityEvent(user, a));
			}
		}
		return events;
	}
	
	@Transactional(readOnly = true)
	private List<Activity1> getDraftActivitiesFromCompetences(List<Long> compIds) {
		String query = "SELECT act FROM CompetenceActivity1 cAct " +
					   "INNER JOIN cAct.activity act " +
					   "WHERE cAct.competence.id IN (:compIds) " +
					   "AND act.published = :published";
		
		Query q = persistence.currentManager()
				.createQuery(query)
				.setParameterList("compIds", compIds)
				.setBoolean("published", false);
		
		@SuppressWarnings("unchecked")
		List<Activity1> activities = q.list();
		if(activities == null) {
			return new ArrayList<>();
		}
		return activities;
	}
	
	@Transactional(readOnly = true)
	private List<Activity1> getDraftActivitiesFromList(List<Long> actIds) {
		if(actIds == null || actIds.isEmpty()) {
			return new ArrayList<>();
		}
		String query = "SELECT act FROM Activity1 act " +
					    "WHERE act.id IN (:ids) " +
					    "AND act.published = :published ";
		
		Query q = persistence.currentManager()
				.createQuery(query)
				.setParameterList("ids", actIds)
				.setBoolean("published", false);
		
		@SuppressWarnings("unchecked")
		List<Activity1> activities = q.list();
		if(activities == null) {
			return new ArrayList<>();
		}
		return activities;
	}
	
	private Activity1 publishDraftVersion(Activity1 originalActivity, Activity1 draftActivity) {
		originalActivity.setTitle(draftActivity.getTitle());
		originalActivity.setDescription(draftActivity.getDescription());
		originalActivity.setDuration(draftActivity.getDuration());
		originalActivity.setUploadAssignment(draftActivity.isUploadAssignment());	
	    
	    originalActivity.setHasDraft(false);
		originalActivity.setDraftVersion(null);
		originalActivity.setPublished(true);

    	updateCompDuration(originalActivity.getId(), draftActivity.getDuration(), 
    			originalActivity.getDuration());
    	
    	Set<ResourceLink> links = draftActivity.getLinks();
    	originalActivity.getLinks().clear();
    	if(links != null) {
    		for(ResourceLink rl : links) {
    			ResourceLink link = new ResourceLink();
				link.setLinkName(rl.getLinkName());
				link.setUrl(rl.getUrl());
				if (rl.getIdParameterName() != null && !rl.getIdParameterName().isEmpty()) {
					link.setIdParameterName(rl.getIdParameterName());
				}
				saveEntity(link);
				originalActivity.getLinks().add(link);
    		}
    	}
    	
    	Set<ResourceLink> files = draftActivity.getFiles();
    	originalActivity.getFiles().clear();
    	if(files != null) {
    		for(ResourceLink rl : files) {
    			ResourceLink file = new ResourceLink();
				file.setLinkName(rl.getLinkName());
				file.setUrl(rl.getUrl());
				saveEntity(file);
				originalActivity.getFiles().add(file);
    		}
    	}
		
		if(originalActivity instanceof TextActivity1) {
			TextActivity1 ta = (TextActivity1) originalActivity;
			ta.setText(((TextActivity1) draftActivity).getText());
		} else if(originalActivity instanceof UrlActivity1) {
			UrlActivity1 urlAct = (UrlActivity1) originalActivity;
			urlAct.setUrl(((UrlActivity1) draftActivity).getUrl());
			urlAct.setLinkName(((UrlActivity1) draftActivity).getLinkName());
		} else if(originalActivity instanceof ExternalToolActivity1) {
			ExternalToolActivity1 extAct = (ExternalToolActivity1) originalActivity;
			ExternalToolActivity1 extDraftAct = (ExternalToolActivity1) draftActivity;
			extAct.setLaunchUrl(extDraftAct.getLaunchUrl());
			extAct.setSharedSecret(extDraftAct.getSharedSecret());
			extAct.setConsumerKey(extDraftAct.getConsumerKey());
			extAct.setAcceptGrades(extDraftAct.isAcceptGrades());
			extAct.setOpenInNewWindow(extDraftAct.isOpenInNewWindow());
		}
		delete(draftActivity);
		return originalActivity;
	}
	
	/**
	 * Returns activity with specified id and no other condition. It will return activity
	 * no matter if it is draft, draft version, published or deleted.
	 * @param actId
	 * @param loadResourceLinks
	 * @return
	 * @throws DbConnectionException
	 */
	@Transactional(readOnly = true)
	private Activity1 getActivity(long actId, boolean loadResourceLinks) 
			throws DbConnectionException {
		try {	
			StringBuilder builder = new StringBuilder();
			builder.append("SELECT act " +
					   	   "FROM Activity1 act ");
			
			if(loadResourceLinks) {
				builder.append("LEFT JOIN fetch act.links link " + 
						       "LEFT JOIN fetch act.files files ");
			}
			builder.append("WHERE act.id = :id ");

			Activity1 act = (Activity1) persistence.currentManager()
					.createQuery(builder.toString())
					.setLong("id", actId)
					.uniqueResult();

			return act;
		} catch (Exception e) {
			logger.error(e);
			e.printStackTrace();
			throw new DbConnectionException("Error while loading activity");
		}
	}
	
	@Override
	@Transactional(readOnly = true)
	public CompetenceData1 getTargetCompetenceActivitiesWithResultsForSpecifiedActivity(
			long credId, long compId, long actId, long userId) 
					throws DbConnectionException {
		CompetenceData1 compData = null;
		try {			
			ActivityData activityWithDetails = getTargetActivityData(credId, compId, actId, userId, false);
			
			if (activityWithDetails != null) {
				/*
				 * if user hasn't submitted result, null should be returned
				 */
				String result = activityWithDetails.getResultData().getResult();
				if(result == null || result.isEmpty()) {
					return null;
				}
				
				activityWithDetails.setStudentResults(getStudentsResults(credId, compId, actId, userId, 
						false, false, false, 0, 0, null));
				
				compData = new CompetenceData1(false);
				compData.setActivityToShowWithDetails(activityWithDetails);
				//compId is id of a competence and activityWithDetails.getCompetenceId() is id of a target competence
				List<ActivityData> activities = getTargetActivitiesData(activityWithDetails.getCompetenceId());
				compData.setActivities(activities);
				return compData;
				//}
			}
			return null;
		} catch (Exception e) {
			logger.error(e);
			e.printStackTrace();
			throw new DbConnectionException("Error while loading activity results");
		}
	}
	
	@Override
	@Transactional(readOnly = true)
	public Long countStudentsResults(long credId, long compId, long actId, StudentAssessedFilter filter) 
			throws DbConnectionException {
		try {
			//TODO change when we upgrade to Hibernate 5.1 - it supports ad hoc joins for unmapped tables
			StringBuilder query = new StringBuilder(
			   "SELECT COUNT(targetAct.id) ");
			query.append("FROM target_activity1 targetAct " +
				   	     "INNER JOIN target_competence1 targetComp " +
				   	   		"ON (targetAct.target_competence = targetComp.id " +
				   			"AND targetComp.competence = :compId) " +
				   	     "INNER JOIN target_credential1 targetCred " +
				   		   "ON (targetComp.target_credential = targetCred.id " +
					   	   "AND targetCred.credential = :credId) ");
			
			if(filter != null) {
				query.append("LEFT JOIN activity_discussion ad " +
							 "ON targetAct.id = ad.target_activity AND ad.default_assessment = :boolTrue " +
							 "LEFT JOIN activity_grade grade " +
							 "ON ad.grade = grade.id ");
			}

			query.append("WHERE targetAct.activity = :actId " +
				   		 "AND targetAct.result is not NULL "); 
			
			if(filter != null) {
				if(filter == StudentAssessedFilter.Assessed) {
					query.append("AND grade.value IS NOT NULL ");
				} else {
					query.append("AND grade.value IS NULL ");
				}
			}
				
			Query q = persistence.currentManager()
						.createSQLQuery(query.toString())
						.setLong("credId", credId)
						.setLong("compId", compId)
						.setLong("actId", actId);
			
			if(filter != null) {
				q.setBoolean("boolTrue", true);
			}
						
			BigInteger res = (BigInteger) q.uniqueResult();
			
			if(res == null) {
				return 0L;
			}
			return res.longValue();
		} catch(Exception e) {
			logger.error(e);
			e.printStackTrace();
			throw new DbConnectionException("Error while retrieving number of students results");
		}
	}

	@Override
	@Transactional(readOnly = true)
	public List<ActivityResultData> getStudentsResults(long credId, long compId, long actId, 
			long userToExclude, boolean isInstructor, boolean returnAssessmentData, boolean paginate,
			int page, int limit, StudentAssessedFilter filter)  throws DbConnectionException {
		try {
			//TODO change when we upgrade to Hibernate 5.1 - it supports ad hoc joins for unmapped tables
			StringBuilder query = new StringBuilder(
			   "SELECT targetAct.id as tActId, targetAct.result_type, targetAct.result, targetAct.result_post_date, " +
			   "u.id as uId, u.name, u.lastname, u.avatar_url, " +
		   	   "COUNT(distinct com.id) ");
			
			if (returnAssessmentData) {
				query.append(", ad.id as adId, COUNT(distinct msg.id), p.is_read, act.max_points ");
			}
			
			if (returnAssessmentData || filter != null) {
				query.append(", ad.points ");
			}
			
			query.append("FROM target_activity1 targetAct " +
				   	     "INNER JOIN target_competence1 targetComp " +
				   	   		"ON (targetAct.target_competence = targetComp.id " +
				   			"AND targetComp.competence = :compId) " +
			   			"INNER JOIN activity1 act " +
				   			"ON (targetAct.activity = act.id) " +
				   	     "INNER JOIN target_credential1 targetCred " +
				   		   "ON (targetComp.target_credential = targetCred.id " +
					   	   "AND targetCred.credential = :credId ");
			
			if (userToExclude > 0) {
				query.append("AND targetCred.user != :userId) ");
			} else {
				query.append(") ");
			}
			
			if (returnAssessmentData || filter != null) {
				query.append("LEFT JOIN activity_discussion ad " +
							"ON targetAct.id = ad.target_activity AND ad.default_assessment = :boolTrue ");
			}
			
			if (returnAssessmentData) {
				query.append("LEFT JOIN activity_discussion_participant p " +
						 		"ON ad.id = p.activity_discussion AND p.participant = targetCred.user " +
						 	 "LEFT JOIN activity_discussion_message msg " +
						 		"ON ad.id = msg.discussion ");
			}
		   	   
			query.append("INNER JOIN user u " +
				   			"ON (targetCred.user = u.id) " +
				   		 "LEFT JOIN comment1 com " +
				   			"ON (targetAct.id = com.commented_resource_id " +
				   			"AND com.resource_type = :resType " +
				   			"AND com.parent_comment is NULL) " +
				   		 "WHERE targetAct.activity = :actId " +
				   		 "AND targetAct.result is not NULL ");
			
			if (filter != null) {
				if (filter == StudentAssessedFilter.Assessed) {
					query.append("AND ad.points IS NOT NULL ");
				} else {
					query.append("AND ad.points IS NULL ");
				}
			}
				   		
			query.append("GROUP BY targetAct.id "); 
			
			if (returnAssessmentData) {
				query.append(", ad.id ");
			}
			query.append("ORDER BY targetAct.result_post_date ");
			
			if (paginate) {
				query.append("LIMIT " + limit + " ");
				query.append("OFFSET " + page * limit);
			}
				
			Query q = persistence.currentManager()
						.createSQLQuery(query.toString())
						.setLong("credId", credId)
						.setLong("compId", compId)
						.setLong("actId", actId)
						.setString("resType", CommentedResourceType.ActivityResult.name());
			
			if (userToExclude > 0) {
				q.setLong("userId", userToExclude);
			}
			if (returnAssessmentData || filter != null) {
				q.setBoolean("boolTrue", true);
			}
	
			@SuppressWarnings("unchecked")
			List<Object[]> res = q.list();
			
			List<ActivityResultData> results = new ArrayList<>();
			if (res != null) {
				for (Object[] row : res) {
					long tActId = ((BigInteger) row[0]).longValue();
					org.prosolo.common.domainmodel.credential.ActivityResultType type = 
							org.prosolo.common.domainmodel.credential.ActivityResultType.valueOf((String) row[1]);
					String result = (String) row[2];
					Date date = (Date) row[3];
					long userId = ((BigInteger) row[4]).longValue();
					String firstName = (String) row[5];
					String lastName = (String) row[6];
					String avatar = (String) row[7];
					User user = new User();
					user.setId(userId);
					user.setName(firstName);
					user.setLastname(lastName);
					user.setAvatarUrl(avatar);
					int commentsNo = ((BigInteger) row[8]).intValue();
					
					ActivityResultData ard = activityFactory.getActivityResultData(tActId, type, result, 
							date, user, commentsNo, isInstructor);
					
					ard.setOtherResultsComments(getCommentsOnOtherResults(userId, tActId));
					results.add(ard); 
					
					if (returnAssessmentData) {
						BigInteger assessmentId = (BigInteger) row[9];
						if(assessmentId != null) {
							ActivityAssessmentData ad = new ActivityAssessmentData();
							ad.setEncodedDiscussionId(idEncoder.encodeId(assessmentId.longValue()));
							ad.setNumberOfMessages(((BigInteger) row[10]).intValue());
							ad.setAllRead(((Character) row[11]).charValue() == 'T');
							GradeData gd = new GradeData();
							gd.setMinGrade(0);
							gd.setMaxGrade((Integer) row[12]);
							gd.setValue((Integer) row[13]);
							ad.setGrade(gd);
							ard.setAssessment(ad);							
						} else {
							// there is no activity assessment created yet
							ActivityAssessmentData ad = new ActivityAssessmentData();
							GradeData gd = new GradeData();
							gd.setMinGrade(0);
							gd.setMaxGrade((Integer) row[12]);
							gd.setValue(0);
							ad.setGrade(gd);
							ard.setAssessment(ad);
						}
					}
				}
			}
			return results;
		} catch(Exception e) {
			logger.error(e);
			e.printStackTrace();
			throw new DbConnectionException("Error while retrieving results for students");
		}
	}
	
	private List<ResultCommentInfo> getCommentsOnOtherResults(long userId, long targetActivityId) {
		String query = 
				"SELECT targetAct.id, user.name, user.lastname, COUNT(DISTINCT com.id) AS noOfComments, MIN(com.post_date) AS firstCommentDate " +
				"FROM target_activity1 targetAct " +
				"INNER JOIN activity1 act ON (targetAct.activity = act.id) " +
				"INNER JOIN target_competence1 targetComp ON (targetAct.target_competence = targetComp.id) " +
				"INNER JOIN target_credential1 targetCred ON (targetComp.target_credential = targetCred.id) " +
				"INNER JOIN user user ON (targetCred.user = user.id) " +
				"LEFT JOIN comment1 com ON (targetAct.id = com.commented_resource_id AND com.resource_type = 'ActivityResult') " +
				"WHERE act.id IN ( " +
						"SELECT act1.id " +
						"FROM target_activity1 targetAct1 " +
						"INNER JOIN activity1 act1 ON (targetAct1.activity = act1.id) " +
						"WHERE targetAct1.id = :targetActivityId " +
					") " +
					"AND targetAct.id != :targetActivityId " +
					"AND targetAct.result IS NOT NULL " +
					"AND com.user = :userId " +
					"AND user.id != :userId " +
				"GROUP BY targetAct.id ";
		
		@SuppressWarnings("unchecked")
		List<Object[]> rows = persistence.currentManager().createSQLQuery(query)
				.setLong("targetActivityId", targetActivityId)
				.setLong("userId", userId)
				.list();
		
		List<ResultCommentInfo> result = new LinkedList<>();
		
		for (Object[] row : rows) {
			long tActId = ((BigInteger) row[0]).longValue();
			String resultCreator = ((String) row[1]) + " " + ((String) row[2]);
			int noOfComments = ((BigInteger) row[3]).intValue();
			Date firstCommentDate = (Date) row[4];
			
			result.add(new ResultCommentInfo(noOfComments, resultCreator, firstCommentDate, tActId));
		}
		
		return result;
	}

	@Override
	@Transactional(readOnly = true)
	public ActivityData getActivityDataWithStudentResultsForManager(long credId, long compId, long actId, 
			boolean isInstructor, boolean paginate, int page, int limit, StudentAssessedFilter filter) 
					throws DbConnectionException {
		try {			
			Activity1 activity = (Activity1) persistence.currentManager().get(Activity1.class, actId);
			if (activity == null) {
				return null;
			}
			ActivityData data = new ActivityData(false);
			data.setTitle(activity.getTitle());
			data.setType(activity.getType());
			data.setActivityId(actId);
//			data.getGradeOptions().setMinGrade(activity.getGradingOptions().getMinGrade());
//			data.getGradeOptions().setMaxGrade(activity.getGradingOptions().getMaxGrade());
			data.setMaxPointsString(activity.getMaxPoints() == 0 ? "" : String.valueOf(activity.getMaxPoints()));
			data.setStudentCanSeeOtherResponses(activity.isStudentCanSeeOtherResponses());
			data.setStudentCanEditResponse(activity.isStudentCanEditResponse());
			data.setStudentResults(getStudentsResults(credId, compId, actId, 0, isInstructor, true,
					paginate, page, limit, filter));
			
			return data;
		} catch (Exception e) {
			logger.error(e);
			e.printStackTrace();
			throw new DbConnectionException("Error while loading activity results");
		}
	}

	@Override
	@Transactional (readOnly = true)
	public ActivityResultData getActivityResultData(long targetActivityId, boolean loadComments, boolean instructor, long loggedUserId) {
		String query = 
			"SELECT targetAct, targetCred.user " +
			"FROM TargetActivity1 targetAct " +
			"INNER JOIN targetAct.targetCompetence targetComp " + 
			"INNER JOIN targetComp.targetCredential targetCred " + 
			"WHERE targetAct.id = :targetActivityId";
		
		Object[] result = (Object[]) persistence.currentManager()
			.createQuery(query.toString())
			.setLong("targetActivityId", targetActivityId)
			.uniqueResult();
		
		if (result != null) {
			TargetActivity1 targetActivity = (TargetActivity1) result[0];
			User user = (User) result[1];
			
			ActivityResultData activityResult = activityFactory.getActivityResultData(targetActivity.getId(), targetActivity.getResultType(), targetActivity.getResult(), 
					targetActivity.getResultPostDate(), user, 0, false);
			
			if (loadComments) {
				CommentsData commentsData = new CommentsData(CommentedResourceType.ActivityResult, 
						targetActivityId, 
						instructor);
				
				List<CommentData> comments = commentManager.getComments(
						commentsData.getResourceType(), 
						commentsData.getResourceId(), false, 0, 
						CommentBean.getCommentSortData(commentsData), 
						CommentReplyFetchMode.FetchReplies, 
						loggedUserId);
				
				Collections.reverse(comments);
				
				commentsData.setComments(comments);
				
				activityResult.setResultComments(commentsData);
			}
			
			return activityResult;
		} 
		return null;
	}
	
//	@Override
//	@Transactional(readOnly = true)
//	public ActivityData getActivityForManager(long activityId, Mode mode) 
//			throws DbConnectionException {
//		try {
//			String query = "SELECT act " +
//					   "FROM Activity1 act " + 
//					   "LEFT JOIN fetch act.links link " +
//					   "LEFT JOIN fetch act.files file ";
//			
//			StringBuilder queryBuilder1 = new StringBuilder(query);
//			queryBuilder1.append("WHERE act.id = :actId " +
//					"AND act.deleted = :deleted " +
//					"AND act.draft = :draft ");
//			
//			if(mode == Mode.Edit) {
//				queryBuilder1.append("AND act.type = :type ");
//			} else {
//				queryBuilder1.append("AND (act.type = :type  OR (act.published = :published " +
//					"OR act.hasDraft = :hasDraft))");
//			}
//						   
//			Query q = persistence.currentManager()
//					.createQuery(queryBuilder1.toString())
//					.setLong("actId", activityId)
//					.setBoolean("deleted", false)
//					.setParameter("type", LearningResourceType.UNIVERSITY_CREATED)
//					.setBoolean("draft", false);
//			
//			if(mode == Mode.View) {
//				q.setBoolean("published", true)
//				 .setBoolean("hasDraft", true);
//			}
//			
//			Activity1 res = (Activity1) q.uniqueResult();
//
//			if(res != null) {
//				ActivityData actData = null;
//				if(res.isHasDraft() && (mode == Mode.Edit  || (mode == Mode.View 
//						&& res.getType() == LearningResourceType.UNIVERSITY_CREATED))) {
//					long draftVersionId = res.getDraftVersion().getId();
//					/*
//					 * remove proxy object from session to be able to retrieve real
//					 * object with hql so instanceof will give expected result
//					 */
//					persistence.currentManager().evict(res.getDraftVersion());
//					String query2 = query + 
//							" WHERE act.id = :draftVersion";
//					Activity1 draftAct = (Activity1) persistence.currentManager()
//							.createQuery(query2)
//							.setLong("draftVersion", draftVersionId)
//							.uniqueResult();
//					if(draftAct != null) {
//						actData = activityFactory.getActivityData(draftAct, 0, 0,
//								draftAct.getLinks(), draftAct.getFiles(), true);
//					}	
//				} else {
//					actData = activityFactory.getActivityData(res, 0, 0, res.getLinks(),
//							res.getFiles(), true);
//				}
//				return actData;
//			}
//			return null;
//		} catch (Exception e) {
//			logger.error(e);
//			e.printStackTrace();
//			throw new DbConnectionException("Error while loading activity data");
//		}
//	}
	
//	@Transactional(readOnly = true)
//	private ActivityData getCompetenceActivityDataForManager(long activityId, Mode mode) 
//			throws DbConnectionException {
//		try {
//			StringBuilder queryBuilder = new StringBuilder("SELECT compAct " +
//					   "FROM CompetenceActivity1 compAct " +
//					   "INNER JOIN fetch compAct.activity act " +
//					   "INNER JOIN compAct.competence comp " +
//					   		"WITH comp.hasDraft = :boolFalse " +
//					   "LEFT JOIN fetch act.links link " +
//					   "LEFT JOIN fetch act.files file " +
//					   "WHERE act.id = :actId " +
//					   "AND act.deleted = :boolFalse " +
//					   "AND act.draft = :boolFalse");
//			
//			if(mode == Mode.Edit) {
//				queryBuilder.append("AND act.type = :type ");
//			} else {
//				queryBuilder.append("AND (act.type = :type  OR (act.published = :boolTrue " +
//					"OR act.hasDraft = :boolTrue))");
//			}
//						   
//			Query q = persistence.currentManager()
//					.createQuery(queryBuilder.toString())
//					.setLong("actId", activityId)
//					.setBoolean("boolFalse", false)
//					.setParameter("type", LearningResourceType.UNIVERSITY_CREATED);
//			
//			if(mode == Mode.View) {
//				q.setBoolean("boolTrue", true);
//			}
//			
//			CompetenceActivity1 res = (CompetenceActivity1) q.uniqueResult();
//
//			if(res != null) {
//				ActivityData actData = null;
//				Activity1 act = res.getActivity();
//				if(act.isHasDraft() && (mode == Mode.Edit  || (mode == Mode.View 
//						&& act.getType() == LearningResourceType.UNIVERSITY_CREATED))) {
//					long draftVersionId = act.getDraftVersion().getId();
//					/*
//					 * remove proxy object from session to be able to retrieve real
//					 * object with hql so instanceof will give expected result
//					 */
//					persistence.currentManager().evict(act.getDraftVersion());
//					String query2 = "SELECT act " +
//							   "FROM Activity1 act " + 
//							   "LEFT JOIN fetch act.links link " +
//							   "LEFT JOIN fetch act.files file " + 
//							   "WHERE act.id = :draftVersion";
//					Activity1 draftAct = (Activity1) persistence.currentManager()
//							.createQuery(query2)
//							.setLong("draftVersion", draftVersionId)
//							.uniqueResult();
//					if(draftAct != null) {
//						actData = activityFactory.getActivityData(draftAct, res.getCompetence().getId(),
//								res.getOrder(), draftAct.getLinks(), 
//								draftAct.getFiles(), true);
//					}	
//				} else {
//					actData = activityFactory.getActivityData(res, res.getActivity().getLinks(), 
//							res.getActivity().getFiles(), true);
//				}
//				return actData;
//			}
//			return null;
//		} catch (Exception e) {
//			logger.error(e);
//			e.printStackTrace();
//			throw new DbConnectionException("Error while loading activity data");
//		}
//	}
//	
//	@Override
//	@Transactional(readOnly = true)
//	public CompetenceData1 getCompetenceActivitiesWithActivityDetailsForManager(
//			long activityId, Mode mode) throws DbConnectionException {
//		CompetenceData1 compData = null;
//		try {
//			ActivityData activityWithDetails = getCompetenceActivityDataForManager(activityId, mode);
//
//			if (activityWithDetails != null) {
//					compData = new CompetenceData1(false);
//					compData.setActivityToShowWithDetails(activityWithDetails);
//					//for now
//					List<ActivityData> activities = getCompetenceActivitiesData(
//							activityWithDetails.getCompetenceId());
//					compData.setActivities(activities);
//					return compData;
//			}
//			return null;
//		} catch (Exception e) {
//			logger.error(e);
//			e.printStackTrace();
//			throw new DbConnectionException("Error while loading competence data");
//		}
//	}

	@Override
	public TargetActivity1 replaceTargetActivityOutcome(long targetActivityId, Outcome outcome, Session session){
		TargetActivity1 targetActivity = (TargetActivity1) session.load(TargetActivity1.class, targetActivityId);
		System.out.println("REPLACE OUTCOME SHOULD BE PROCESSED HERE...");
		/*List<Outcome> oldOutcomes = targetActivity.getOutcomes();
		List<Outcome> newOutcomes = new ArrayList<Outcome>();
		newOutcomes.add(outcome);
		targetActivity.setOutcomes(newOutcomes);
		targetActivity.setCompleted(true);
		targetActivity.setDateCompleted(new Date());
		session.save(targetActivity);
		for (Outcome oldOutcome : oldOutcomes) {
			try {
				this.deleteById(SimpleOutcome.class, oldOutcome.getId(), session);
			} catch (ResourceCouldNotBeLoadedException e) {
				e.printStackTrace();
			}
		}*/
		return targetActivity;
	}
	
}
