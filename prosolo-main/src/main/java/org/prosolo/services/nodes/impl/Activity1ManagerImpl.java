package org.prosolo.services.nodes.impl;

import org.hibernate.LockOptions;
import org.hibernate.Query;
import org.prosolo.bigdata.common.exceptions.*;
import org.prosolo.common.domainmodel.annotation.Tag;
import org.prosolo.common.domainmodel.assessment.AssessmentType;
import org.prosolo.common.domainmodel.credential.*;
import org.prosolo.common.domainmodel.credential.visitor.ActivityVisitor;
import org.prosolo.common.domainmodel.events.EventType;
import org.prosolo.common.domainmodel.rubric.Rubric;
import org.prosolo.common.domainmodel.rubric.RubricType;
import org.prosolo.common.domainmodel.user.User;
import org.prosolo.common.event.context.data.UserContextData;
import org.prosolo.common.util.ImageFormat;
import org.prosolo.services.annotation.TagManager;
import org.prosolo.services.assessment.AssessmentManager;
import org.prosolo.services.assessment.RubricManager;
import org.prosolo.services.assessment.data.ActivityAssessmentData;
import org.prosolo.services.assessment.data.ActivityAssessmentsSummaryData;
import org.prosolo.services.assessment.data.AssessmentBasicData;
import org.prosolo.services.assessment.data.GradeDataFactory;
import org.prosolo.services.assessment.data.factory.AssessmentDataFactory;
import org.prosolo.services.assessment.data.grading.RubricAssessmentGradeSummary;
import org.prosolo.services.data.Result;
import org.prosolo.services.event.EventData;
import org.prosolo.services.event.EventFactory;
import org.prosolo.services.general.impl.AbstractManagerImpl;
import org.prosolo.services.interaction.CommentManager;
import org.prosolo.services.interaction.data.CommentData;
import org.prosolo.services.interaction.data.CommentReplyFetchMode;
import org.prosolo.services.interaction.data.CommentsData;
import org.prosolo.services.interaction.data.ResultCommentInfo;
import org.prosolo.services.interaction.data.factory.CommentDataFactory;
import org.prosolo.services.nodes.Activity1Manager;
import org.prosolo.services.nodes.Competence1Manager;
import org.prosolo.services.nodes.CredentialManager;
import org.prosolo.services.nodes.ResourceFactory;
import org.prosolo.services.nodes.data.*;
import org.prosolo.services.nodes.data.ActivityResultType;
import org.prosolo.services.nodes.data.competence.CompetenceData1;
import org.prosolo.services.nodes.data.resourceAccess.ResourceAccessData;
import org.prosolo.services.nodes.factory.ActivityDataFactory;
import org.prosolo.services.urlencoding.UrlIdEncoder;
import org.prosolo.services.user.data.UserData;
import org.prosolo.web.util.AvatarUtils;
import org.springframework.orm.hibernate4.HibernateOptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import java.math.BigInteger;
import java.util.*;

@Service("org.prosolo.services.nodes.Activity1Manager")
public class Activity1ManagerImpl extends AbstractManagerImpl implements Activity1Manager {

	private static final long serialVersionUID = -2783669846949034832L;

//	private static Logger logger = Logger.getLogger(Activity1ManagerImpl.class);
	
	@Inject private ActivityDataFactory activityFactory;
	@Inject private Competence1Manager compManager;
	@Inject private CommentManager commentManager;
	@Inject private AssessmentManager assessmentManager;
	@Inject private EventFactory eventFactory;
	@Inject private ResourceFactory resourceFactory;
	@Inject private CredentialManager credManager;
	@Inject private UrlIdEncoder idEncoder;
	@Inject private CommentDataFactory commentDataFactory;
	@Inject private Activity1Manager self;
	@Inject private TagManager tagManager;
	@Inject private AssessmentDataFactory assessmentDataFactory;
	@Inject private RubricManager rubricManager;

	@Override
	//nt
	public Activity1 saveNewActivity(ActivityData data, UserContextData context)
			throws DbConnectionException, IllegalDataStateException {
		try {
			Result<Activity1> res = self.createActivity(data, context);

			eventFactory.generateEvents(res.getEventQueue());

			return res.getResult();
		} catch (IllegalDataStateException idse) {
			throw idse;
		} catch (DbConnectionException dbe) {
			logger.error(dbe);
			dbe.printStackTrace();
			throw dbe;
		}
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public Result<Activity1> createActivity(org.prosolo.services.nodes.data.ActivityData data, UserContextData context)
			throws DbConnectionException, IllegalDataStateException {
		try {
			Result<Activity1> result = new Result<>();
			Activity1 activity = activityFactory.getActivityFromActivityData(data);

			setAssessmentRelatedData(activity, data, true);

			if (data.getLinks() != null) {
				Set<ResourceLink> activityLinks = new HashSet<>();

				for (ResourceLinkData rl : data.getLinks()) {
					ResourceLink link = new ResourceLink();
					link.setLinkName(rl.getLinkName());
					link.setUrl(rl.getUrl());
					if (rl.getIdParamName() != null && !rl.getIdParamName().isEmpty()) {
						link.setIdParameterName(rl.getIdParamName());
					}
					saveEntity(link);
					activityLinks.add(link);
				}
				activity.setLinks(activityLinks);
			}

			Set<ResourceLink> activityFiles = new HashSet<>();

			if (data.getFiles() != null) {
				for (ResourceLinkData rl : data.getFiles()) {
					ResourceLink link = new ResourceLink();
					link.setLinkName(rl.getLinkName());
					link.setUrl(rl.getUrl());
					saveEntity(link);
					activityFiles.add(link);
				}
				activity.setFiles(activityFiles);
			}

			if(data.getActivityType() == org.prosolo.services.nodes.data.ActivityType.VIDEO) {
				Set<ResourceLink> captions = new HashSet<>();

				if (data.getCaptions() != null) {
					for (ResourceLinkData rl : data.getCaptions()) {
						ResourceLink link = new ResourceLink();
						link.setLinkName(rl.getLinkName());
						link.setUrl(rl.getUrl());
						saveEntity(link);
						captions.add(link);
					}
					((UrlActivity1) activity).setCaptions(captions);
				}
			}

			User creator = (User) persistence.currentManager().load(User.class, context.getActorId());
			activity.setCreatedBy(creator);

			activity.setStudentCanSeeOtherResponses(data.isStudentCanSeeOtherResponses());
			activity.setStudentCanEditResponse(data.isStudentCanEditResponse());
			activity.setVisibleForUnenrolledStudents(data.isVisibleForUnenrolledStudents());

			activity.setTags(new HashSet<>(tagManager.parseCSVTagsAndSave(data.getTagsString())));

			saveEntity(activity);

			result.appendEvent(eventFactory.generateEventData(
					EventType.Create, context, activity, null, null, null));

			if(data.getCompetenceId() > 0) {
				EventData ev = compManager.addActivityToCompetence(data.getCompetenceId(),
						activity, context);
				result.appendEvent(ev);
			}

			result.setResult(activity);
			return result;
		} catch(IllegalDataStateException idse) {
			throw idse;
		} catch(DbConnectionException dce) {
			throw dce;
		} catch(Exception e) {
			logger.error(e);
			e.printStackTrace();
			throw new DbConnectionException("Error while saving activity");
		}
	}

	private void setAssessmentRelatedData(Activity1 activity, ActivityData data, boolean updateRubric) throws IllegalDataStateException {
		class ExternalActivityVisitor implements ActivityVisitor {

			boolean acceptGrades;

			ExternalActivityVisitor(boolean acceptGrades) {
				this.acceptGrades = acceptGrades;
			}

			@Override
			public void visit(TextActivity1 activity) {}

			@Override
			public void visit(UrlActivity1 activity) {}

			@Override
			public void visit(ExternalToolActivity1 activity) {
				activity.setAcceptGrades(acceptGrades);
			}
		}

		activity.setGradingMode(data.getAssessmentSettings().getGradingMode());
		switch (data.getAssessmentSettings().getGradingMode()) {
			case AUTOMATIC:
				activity.accept(new ExternalActivityVisitor(data.isAcceptGrades()));
				activity.setRubric(null);
				activity.setRubricVisibility(ActivityRubricVisibility.NEVER);
				break;
			case MANUAL:
				if (updateRubric) {
					activity.setRubric(rubricManager.getRubricForLearningResource(data.getAssessmentSettings()));
				}
				if (data.getAssessmentSettings().getRubricId() > 0) {
					activity.setRubricVisibility(data.getRubricVisibility());
				} else {
					activity.setRubricVisibility(ActivityRubricVisibility.NEVER);
				}

				activity.accept(new ExternalActivityVisitor(false));
				break;
			case NONGRADED:
				activity.setRubric(null);
				activity.setRubricVisibility(ActivityRubricVisibility.NEVER);
				activity.accept(new ExternalActivityVisitor(false));
				break;
		}
		activity.setMaxPoints(
				isPointBasedActivity(activity.getGradingMode(), activity.getRubric())
					? (data.getAssessmentSettings().getMaxPointsString().isEmpty() ? 0 : Integer.parseInt(data.getAssessmentSettings().getMaxPointsString()))
					: 0);
	}

	private boolean isPointBasedActivity(GradingMode gradingMode, Rubric rubric) {
		return gradingMode != GradingMode.NONGRADED && (rubric == null || rubric.getRubricType() == RubricType.POINT || rubric.getRubricType() == RubricType.POINT_RANGE);
	}

	@Override
	@Transactional(readOnly = false, rollbackFor = Exception.class)
	public Activity1 deleteActivity(long activityId, UserContextData context) throws DbConnectionException, IllegalDataStateException {
		try {
			if(activityId > 0) {
				Activity1 act = (Activity1) persistence.currentManager().load(Activity1.class, activityId);
				act.setDeleted(true);
				
				deleteCompetenceActivity(act.getId());

				Activity1 activity = new Activity1();
				activity.setId(activityId);
				eventFactory.generateEvent(EventType.Delete, context, activity,
						null, null, null);
				
				return act;
			}
			return null;
		} catch (IllegalDataStateException idse) {
			idse.printStackTrace();
			logger.error(idse);
			throw idse;
		} catch (Exception e) {
			logger.error(e);
			e.printStackTrace();
			throw new DbConnectionException("Error while deleting activity");
		}
	}
	
	private void deleteCompetenceActivity(long actId) throws IllegalDataStateException {
		Activity1 act = (Activity1) persistence.currentManager().load(Activity1.class, actId);
		long duration = act.getDuration();
		
		String query = "SELECT compAct " +
			       	   "FROM CompetenceActivity1 compAct " + 
			       	   "INNER JOIN fetch compAct.competence comp " +
			       	   "WHERE compAct.activity = :act";

		CompetenceActivity1 res = (CompetenceActivity1) persistence.currentManager()
			.createQuery(query)
			.setEntity("act", act)
			.setLockOptions(LockOptions.UPGRADE)
			.uniqueResult();
		
		//if competence is once published, activities can not be removed from it.
		if(res.getCompetence().getDatePublished() != null) {
			throw new IllegalDataStateException("After competence is first published, activities can not be removed.");
		}
		long competenceId = res.getCompetence().getId();
		shiftOrderOfActivitiesUp(competenceId, res.getOrder());
		delete(res);
		
		if(duration != 0) {
			compManager.updateDurationForCompetenceWithActivity(actId, duration, Operation.Subtract);
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
		
		return res;
	}
	
	@Override
	@Transactional(readOnly = true)
	public List<ActivityData> getCompetenceActivitiesData(long competenceId) throws DbConnectionException {
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
	public List<CompetenceActivity1> getCompetenceActivities(long competenceId, 
			boolean loadResourceLinks) throws DbConnectionException {
		try {
			Competence1 comp = (Competence1) persistence.currentManager().load(Competence1.class, competenceId);
			StringBuilder builder = new StringBuilder();
			builder.append("SELECT DISTINCT compAct " +
				      	   "FROM CompetenceActivity1 compAct " + 
				       	   "INNER JOIN fetch compAct.activity act ");
			
			if(loadResourceLinks) {
				builder.append("LEFT JOIN fetch act.links " +
					       	   "LEFT JOIN fetch act.files ");
			}
			
			builder.append("WHERE compAct.competence = :comp " +
						   "ORDER BY compAct.order");

			Query query = persistence.currentManager()
				.createQuery(builder.toString())
				.setEntity("comp", comp);
			
			@SuppressWarnings("unchecked")
			List<CompetenceActivity1> res = query.list();
			
			return res;
		} catch(Exception e) {
			logger.error(e);
			e.printStackTrace();
			throw new DbConnectionException("Error while loading activities");
		}
	}
	
	@Override
	@Transactional(readOnly = false) 
	public List<TargetActivity1> createTargetActivities(TargetCompetence1 targetComp) 
			throws DbConnectionException {
		try {
			//we should not create target activities for unpublished activities
			List<CompetenceActivity1> compActivities = getCompetenceActivities(
					targetComp.getCompetence().getId(), true);
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

	private TargetActivity1 createTargetActivity(TargetCompetence1 targetComp,
			CompetenceActivity1 activity) throws DbConnectionException {
		try {
			TargetActivity1 targetAct = new TargetActivity1();
			targetAct.setDateCreated(new Date());
			targetAct.setTargetCompetence(targetComp);
			targetAct.setActivity(activity.getActivity());
			targetAct.setOrder(activity.getOrder());
    		
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
		} catch (DbConnectionException dce) {
			throw dce;
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
					       "INNER JOIN fetch targetAct.activity act " +
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
	
	@Transactional(readOnly = true)
	@Override
	public ActivityData getActivityData(long credId, long competenceId, long activityId, boolean loadLinks, boolean loadTags)
					throws DbConnectionException, ResourceNotFoundException {
		try {
			CompetenceActivity1 res = getCompetenceActivity(credId, competenceId, activityId, 
					loadLinks, loadTags, true);

			if (res == null) {
				throw new ResourceNotFoundException();
			}

			Set<ResourceLink> links = loadLinks ? res.getActivity().getLinks() : null;
			Set<ResourceLink> files = loadLinks ? res.getActivity().getFiles() : null;
			Set<Tag> tags = loadTags ? res.getActivity().getTags() : null;
			return activityFactory.getActivityData(
					res, links, files, tags, true);
		} catch(ResourceNotFoundException rnfe) {
			throw rnfe;
		} catch (Exception e) {
			logger.error(e);
			e.printStackTrace();
			throw new DbConnectionException("Error while loading activity data");
		}
	}

	/**
	 * Checks if activity specified with {@code actId} id is part of a credential with {@code credId} id
	 * and if not throws {@link ResourceNotFoundException}.
	 *
	 * @param credId
	 * @param actId
	 * @throws ResourceNotFoundException
	 */
	private void checkIfActivityIsPartOfACredential(long credId, long actId)
			throws ResourceNotFoundException {
		if(credId > 0) {
			String query1 =
					"SELECT COUNT(compAct.id) " +
					"FROM CompetenceActivity1 compAct " +
					"INNER JOIN compAct.competence comp " +
					"INNER JOIN comp.credentialCompetences credComp " +
					"WITH credComp.credential.id = :credId " +
					"WHERE compAct.activity.id = :actId";

			@SuppressWarnings("unchecked")
			Long no = (Long) persistence.currentManager()
					.createQuery(query1)
					.setLong("credId", credId)
					.setLong("actId", actId)
					.uniqueResult();

			if (no == 0) {
				throw new ResourceNotFoundException();
			}
		}
	}
	
	private CompetenceActivity1 getCompetenceActivity(long credId, long competenceId, long activityId, 
			boolean loadLinks, boolean loadTags, boolean loadCompetence) {
		try {
			compManager.checkIfCompetenceIsPartOfACredential(credId, competenceId);
			/*
			 * we need to make sure that activity is bound to competence with passed id
			 */
			StringBuilder queryB = new StringBuilder("SELECT compAct " +
					   "FROM CompetenceActivity1 compAct " +
					   "INNER JOIN fetch compAct.activity act ");
			
			if (loadLinks) {
				queryB.append("LEFT JOIN fetch act.links link " +
							  "LEFT JOIN fetch act.files file ");
			}

			if (loadTags) {
				queryB.append("LEFT JOIN fetch act.tags ");
			}
			
			if (loadCompetence) {
				queryB.append("INNER JOIN fetch compAct.competence comp ");
			}
			
			queryB.append("WHERE act.id = :actId " +
					"AND act.deleted = :deleted " +
					"AND compAct.competence.id = :compId ");
				   
			Query q = persistence.currentManager()
					.createQuery(queryB.toString())
					.setLong("actId", activityId)
					.setLong("compId", competenceId)
					.setBoolean("deleted", false);
			
			return (CompetenceActivity1) q.uniqueResult();
		} catch(ResourceNotFoundException rnfe) {
			throw rnfe;
		} catch(Exception e) {
			e.printStackTrace();
			logger.error(e);
			throw new DbConnectionException("Error while retrieving competence activity data");
		}
	}
	
	@Override
	@Transactional(readOnly = false, rollbackFor = Exception.class)
	public Activity1 updateActivity(ActivityData data, UserContextData context)
			throws DbConnectionException, StaleDataException, IllegalDataStateException {
		Activity1 act = resourceFactory.updateActivity(data);

		eventFactory.generateEvent(EventType.Edit, context, act,
				null, null, null);

		return act;
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
	
	@Override
	@Transactional(readOnly = false, rollbackFor = Exception.class)
	public Activity1 updateActivityData(ActivityData data) throws DbConnectionException, StaleDataException, IllegalDataStateException {
		try {
			/*
			 * Lock the competence record so we can avoid integrity rule violation with concurrent updates.
			 * 
			 * This way, exclusive lock on a competence is acquired and publish date is retrieved.
			 */
			String query = "SELECT comp.datePublished " +
					"FROM Competence1 comp " +
					"WHERE comp.id = :compId";

			Date datePublished = (Date) persistence.currentManager()
					.createQuery(query)
					.setLong("compId", data.getCompetenceId())
					.setLockOptions(LockOptions.UPGRADE)
					.uniqueResult();
			
			/*
			 * if this crucial condition changed after data for editing activity is read, we should not
			 * allow update, because it would not be clear to end user what data changed and why. It would
			 * be possible that some changes are made and others not.
			 */
			boolean compOncePublished = datePublished != null;
			if (compOncePublished != data.isOncePublished()) {
				throw new StaleDataException("Data changed in the meantime. Please review changes and try again.");
			}
			
			/*
			 * handle the case where activity type is changed by client by mistake, but it shouldn't be changed 
			 * because it would violate integrity rule
			 */
			final ActivityType actType = compOncePublished && data.isActivityTypeChanged()
					? data.getActivityTypeBeforeUpdate().get()
					: data.getActivityType();

			//if competence is published activity type can't be changed
			if (!compOncePublished && data.isActivityTypeChanged()) {
				updateActivityType(data.getActivityId(), data.getActivityType());
			}

			Activity1 actToUpdate = (Activity1) persistence.currentManager().load(Activity1.class,
					data.getActivityId());
			
			/* this check is needed to find out if activity is changed from the moment activity data
			 * is loaded for edit to the moment update request is sent
			 */
			if (actToUpdate.getVersion() != data.getVersion()) {
				throw new StaleDataException("Activity changed in the meantime. Please review changes and try again.");
			}

			long oldDuration = getActivityDurationBeforeUpdate(data);
			long newDuration = data.getDurationHours() * 60 + data.getDurationMinutes();

			if (oldDuration != newDuration) {
				updateCompDuration(actToUpdate.getId(), newDuration, oldDuration);
			}

			actToUpdate.setTitle(data.getTitle());
			actToUpdate.setDescription(data.getDescription());
			actToUpdate.setDuration(data.getDurationHours() * 60 + data.getDurationMinutes());
			actToUpdate.setStudentCanSeeOtherResponses(data.isStudentCanSeeOtherResponses());
			actToUpdate.setStudentCanEditResponse(data.isStudentCanEditResponse());
			actToUpdate.setVisibleForUnenrolledStudents(data.isVisibleForUnenrolledStudents());
			actToUpdate.setDifficulty(data.getDifficulty());
			if (data.isTagsStringChanged()) {
				actToUpdate.setTags(new HashSet<Tag>(tagManager.parseCSVTagsAndSave(
						data.getTagsString())));
			}

			//changes which are not allowed if competence is once published
			if (!compOncePublished) {
				actToUpdate.setResultType(activityFactory.getResultType(data.getResultData().getResultType()));

				setAssessmentRelatedData(actToUpdate, data, data.getAssessmentSettings().isRubricChanged());
			}

			updateResourceLinks(data.getLinks(), actToUpdate.getLinks());

			updateResourceLinks(data.getFiles(), actToUpdate.getFiles());

			actToUpdate.accept(new ActivityVisitor() {

				@Override
				public void visit(ExternalToolActivity1 activity) {
					activity.setLaunchUrl(data.getLaunchUrl());
					activity.setSharedSecret(data.getSharedSecret());
					activity.setConsumerKey(data.getConsumerKey());
					activity.setOpenInNewWindow(data.isOpenInNewWindow());

					//changes which are not allowed if competence is published
					if (!compOncePublished) {
						activity.setAcceptGrades(data.isAcceptGrades());
						activity.setScoreCalculation(data.getScoreCalculation());
					}
				}

				@Override
				public void visit(UrlActivity1 activity) {
					if (actType == ActivityType.VIDEO) {
						activity.setUrlType(UrlActivityType.Video);
						activity.setUrl(data.getVideoLink());
						updateResourceLinks(data.getCaptions(), activity.getCaptions());
					} else {
						activity.setUrlType(UrlActivityType.Slides);
						activity.setUrl(data.getSlidesLink());
					}
					activity.setLinkName(data.getLinkName());
				}

				@Override
				public void visit(TextActivity1 activity) {
					activity.setText(data.getText());
				}
			});

			persistence.flush();
			return actToUpdate;
		} catch(HibernateOptimisticLockingFailureException e) {
			e.printStackTrace();
			logger.error(e);
			throw new StaleDataException("Activity changed in the meantime. Please review changes and try again.");
		} catch (StaleDataException|IllegalDataStateException ex) {
			logger.error(ex);
			ex.printStackTrace();
			throw ex;
		} catch (Exception e) {
			logger.error(e);
			e.printStackTrace();
			throw new DbConnectionException("Error while updating activity");
		}
	}
	
	private long getActivityDurationBeforeUpdate(ActivityData data) {
		long oldDuration = 0;
		if(data.isDurationHoursChanged() || data.isDurationMinutesChanged()) {
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
			oldDuration = oldHours * 60 + oldMinutes;
		} else {
			oldDuration = data.getDurationHours() * 60 + data.getDurationMinutes();
		}
		return oldDuration;
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
    	compManager.updateDurationForCompetenceWithActivity(actId, durationChange, op);
	}

	/**
	 * This method will save resource link (insert or update), add or remove it when needed from
	 * activity.
	 * @param resLinksData
	 * @param resLinks
	 */
	public void updateResourceLinks(List<ResourceLinkData> resLinksData, Set<ResourceLink> resLinks) {
		if (resLinksData != null) {
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
		}
	}
	
	@Override
	@Transactional(readOnly = true)
	public CompetenceData1 getCompetenceActivitiesWithSpecifiedActivityInFocus(long credId, long compId, long activityId)
					throws DbConnectionException, ResourceNotFoundException {
		CompetenceData1 compData;
		try {
			ActivityData activityWithDetails = getActivityData(credId, compId, activityId,
					true, true);
			compData = new CompetenceData1(false);
			compData.setActivityToShowWithDetails(activityWithDetails);
			
			List<ActivityData> activities = getCompetenceActivitiesData(compId);
			compData.setActivities(activities);
			return compData;
		} catch (ResourceNotFoundException|DbConnectionException ex) {
			throw ex;
		} catch (Exception e) {
			logger.error(e);
			e.printStackTrace();
			throw new DbConnectionException("Error while loading competence data");
		}
	}

	@Override
	@Transactional(readOnly = false)
	public void saveResponse(long targetActId, String path, Date postDate,
			ActivityResultType resType, UserContextData context) throws DbConnectionException {
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

			TargetActivity1 tAct = new TargetActivity1();
			tAct.setId(targetActId);

			EventType evType = resType == ActivityResultType.FILE_UPLOAD
					? EventType.AssignmentUploaded : EventType.Typed_Response_Posted;
			eventFactory.generateEvent(evType, context, tAct, null, null, null);
		} catch (Exception e) {
			logger.error(e);
			e.printStackTrace();
			throw new DbConnectionException("Error while saving assignment");
		}
	}

	@Override
	@Transactional
	public void updateTextResponse(long targetActId, String path, UserContextData context)
			throws DbConnectionException {
		try {
			String query = "UPDATE TargetActivity1 act SET " +
						   "act.result = :path " +
						   "WHERE act.id = :actId";
			
			persistence.currentManager()
				.createQuery(query)
				.setLong("actId", targetActId)
				.setString("path", path)
				.executeUpdate();

			TargetActivity1 tAct = new TargetActivity1();
			tAct.setId(targetActId);

			eventFactory.generateEvent(EventType.Typed_Response_Edit, context, tAct, null, null, null);
		} catch (Exception e) {
			logger.error(e);
			e.printStackTrace();
			throw new DbConnectionException("Error while editing response");
		}
	}

	@Override
	public void completeActivity(long targetActId, long targetCompId, UserContextData context)
			throws DbConnectionException {
		Result<Void> res = self.completeActivityAndGetEvents(targetActId, targetCompId, context);
		eventFactory.generateEvents(res.getEventQueue());
	}
	
	@Override
	@Transactional
	public Result<Void> completeActivityAndGetEvents(long targetActId, long targetCompId, UserContextData context)
			throws DbConnectionException {
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

			Result<Void> res = new Result<>();
			res.appendEvents(compManager.updateCompetenceProgress(targetCompId, context));
			
			TargetActivity1 tAct = new TargetActivity1();
			tAct.setId(targetActId);

			res.appendEvent(eventFactory.generateEventData(EventType.Completion, context, tAct, null, null, null));
			return res;
		} catch (DbConnectionException dce) {
			throw dce;
		} catch (Exception e) {
			logger.error(e);
			e.printStackTrace();
			throw new DbConnectionException("Error while updating activity progress");
		}
	}
	
	@Override
	@Transactional(readOnly = true)
	public CompetenceData1 getFullTargetActivityOrActivityData(long credId, long compId,
			long actId, long userId, boolean isManager) 
					throws DbConnectionException, ResourceNotFoundException {
		CompetenceData1 compData;
		try {
			compData = getTargetCompetenceActivitiesWithSpecifiedActivityInFocus(credId, 
					compId, actId, userId, isManager);
			if (compData == null) {
				return getCompetenceActivitiesWithSpecifiedActivityInFocus(credId, compId, actId);
			}

			return compData;
		} catch(ResourceNotFoundException|DbConnectionException ex) {
			throw ex;
		} catch (Exception e) {
			logger.error(e);
			e.printStackTrace();
			throw new DbConnectionException("Error while loading activity data");
		}
	}
	
	/**
	 * 
	 * @param credId
	 * @param compId
	 * @param actId
	 * @param userId
	 * @param isManager did request come from manage section
	 * @return
	 * @throws DbConnectionException
	 */
	private CompetenceData1 getTargetCompetenceActivitiesWithSpecifiedActivityInFocus(
			long credId, long compId, long actId, long userId, boolean isManager) 
					throws DbConnectionException {
		CompetenceData1 compData;
		try {			
			ActivityData activityWithDetails = getTargetActivityData(credId, compId, actId, userId, 
					true, true, isManager);

			if (activityWithDetails != null) {
				compData = new CompetenceData1(false);
				compData.setActivityToShowWithDetails(activityWithDetails);
				//compId is id of a competence and activityWithDetails.getCompetenceId() is id of a target competence
				List<ActivityData> activities = getTargetActivitiesData(activityWithDetails.getCompetenceId());
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
	
	/**
	 * Returns full target activity data when id of a target activity is not known.
	 * @param credId
	 * @param compId
	 * @param actId
	 * @param userId
	 * @param loadResourceLinks
	 * @param isManager did request come from manage section
	 * @return
	 * @throws DbConnectionException
	 */
	private ActivityData getTargetActivityData(long credId, long compId, long actId, long userId,
			boolean loadResourceLinks, boolean loadTags, boolean isManager)
			throws DbConnectionException, ResourceNotFoundException {
		try {	
			/*
			 * check if competence is part of a credential
			 */
			compManager.checkIfCompetenceIsPartOfACredential(credId, compId);
			
			StringBuilder query = new StringBuilder("SELECT targetAct " +
					   "FROM TargetActivity1 targetAct " +
					   "INNER JOIN fetch targetAct.activity act " +
					   "INNER JOIN targetAct.targetCompetence targetComp " +
					   		"WITH targetComp.competence.id = :compId " +
					   		"AND targetComp.user.id = :userId ");
			if (loadResourceLinks) {
				query.append("LEFT JOIN fetch act.links link " + 
							 "LEFT JOIN fetch act.files files ");
			}
			if (loadTags) {
				query.append("LEFT JOIN fetch act.tags tag ");
			}
			query.append("WHERE act.id = :actId");

			TargetActivity1 res = (TargetActivity1) persistence.currentManager()
					.createQuery(query.toString())
					.setLong("userId", userId)
					.setLong("compId", compId)
					.setLong("actId", actId)
					.uniqueResult();

			if (res != null) {
				Set<ResourceLink> links = loadResourceLinks ? res.getActivity().getLinks() : null;
				Set<ResourceLink> files = loadResourceLinks ? res.getActivity().getFiles() : null;
				Set<Tag> tags = loadTags ? res.getActivity().getTags() : null;
				ActivityData activity = activityFactory.getActivityData(res, links,
						files, tags, true, 0, isManager);
				
				return activity;
			}
			return null;
		} catch (ResourceNotFoundException rnfe) {
			throw rnfe;
		} catch (Exception e) {
			logger.error(e);
			e.printStackTrace();
			throw new DbConnectionException("Error while loading activity data");
		}
	}

	@Override
	@Transactional
	public void deleteAssignment(long targetActivityId, UserContextData context)
			throws DbConnectionException {
		try {
			String query = "UPDATE TargetActivity1 act SET " +
						   "act.result = NULL, " +
						   "act.resultPostDate = NULL " +
						   "WHERE act.id = :id";
			persistence.currentManager()
				.createQuery(query)
				.setLong("id", targetActivityId)
				.executeUpdate();

			TargetActivity1 tAct = new TargetActivity1();
			tAct.setId(targetActivityId);
			eventFactory.generateEvent(EventType.AssignmentRemoved, context, tAct, null, null, null);
		} catch (Exception e) {
			logger.error(e);
			e.printStackTrace();
			throw new DbConnectionException("Error while removing assignment");
		}
	}

	private List<ResourceLink> getActivityLinks(long actId)
			throws DbConnectionException {
		try {		
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

	private List<ResourceLink> getActivityFiles(long actId)
			throws DbConnectionException {
		try {			
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

	@Override
	@Transactional(readOnly = true)
	public Long getCompetenceIdForActivity(long actId) throws DbConnectionException {
		try {	
			String query = "SELECT comp.id " +
						   "FROM CompetenceActivity1 compActivity " +
						   "INNER JOIN compActivity.competence comp " +
				       	   "WHERE compActivity.activity.id = :actId";					    

			Long id = (Long) persistence.currentManager()
				.createQuery(query)
				.setLong("actId", actId)
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
	public CompetenceData1 getTargetCompetenceActivitiesWithResultsForSpecifiedActivity(
			long credId, long compId, long actId, long userId, boolean isManager) 
					throws DbConnectionException, ResourceNotFoundException, AccessDeniedException {
		CompetenceData1 compData = null;
		try {			
			ActivityData activityWithDetails = getTargetActivityData(credId, compId, actId, 
					userId, false, false, isManager);
			
			if (activityWithDetails != null) {
				//if it is not allowed for students to see other students responses throw AccessDeniedException
				if(!activityWithDetails.isStudentCanSeeOtherResponses()) {
					throw new AccessDeniedException();
				}
				/*
				 * if user hasn't submitted result, null should be returned
				 */
				String result = activityWithDetails.getResultData().getResult();
				if(result == null || result.isEmpty()) {
					return null;
				}
				//pass 0 for credentialId because we don't need to check again if competence belongs to credential
				activityWithDetails.setStudentResults(getStudentsResults(compId, actId, userId,false, 0, 0));
				
				compData = new CompetenceData1(false);
				compData.setActivityToShowWithDetails(activityWithDetails);
				//compId is id of a competence and activityWithDetails.getCompetenceId() is id of a target competence
				List<ActivityData> activities = getTargetActivitiesData(activityWithDetails.getCompetenceId());
				compData.setActivities(activities);
				return compData;
				//}
			}
			return null;
		} catch (AccessDeniedException ade) {
			throw ade;
		} catch (ResourceNotFoundException rnfe) {
			throw rnfe;
		} catch (Exception e) {
			logger.error(e);
			e.printStackTrace();
			throw new DbConnectionException("Error while loading activity results");
		}
	}

	@Override
	@Transactional(readOnly = true)
	public List<ActivityResultData> getStudentsResults(long compId, long actId, long userToExclude,
													   boolean paginate, int page, int limit) throws DbConnectionException, ResourceNotFoundException {
		try {
			//TODO hack - when retrieving comments for students, retrieve only comments from users learning same credentials
			//userToExclude is user for which we are returning results - another hack
			List<Long> deliveries = credManager.getIdsOfDeliveriesUserIsLearningContainingCompetence(userToExclude, compId);
			if (deliveries.isEmpty()) {
				return new ArrayList<>();
			}

			//TODO change when we upgrade to Hibernate 5.1 - it supports ad hoc joins for unmapped tables
			StringBuilder query = new StringBuilder(
		   		"SELECT targetAct.id as tActId, act.result_type, targetAct.result, targetAct.result_post_date, " +
			   "u.id as uId, u.name, u.lastname, u.avatar_url, " +
		   	   "COUNT(distinct com.id) " +
			   "FROM target_activity1 targetAct " +
					 "INNER JOIN target_competence1 targetComp " +
						"ON (targetAct.target_competence = targetComp.id " +
						"AND targetComp.competence = :compId ");
			
			if (userToExclude > 0) {
				query.append("AND targetComp.user != :userId) ");
			} else {
				query.append(") ");
			}
			
			query.append("INNER JOIN activity1 act " +
				   		 "ON (targetAct.activity = act.id " +
						 "AND act.id = :actId) " +
						 "INNER JOIN user u " +
				   			"ON (targetComp.user = u.id) " +
				   		 "LEFT JOIN comment1 com " +
				   			"ON (targetAct.id = com.commented_resource_id " +
				   			"AND com.resource_type = :resType " +
				   			"AND com.parent_comment is NULL) " +
						 "WHERE targetAct.result is not NULL " +
						 "AND EXISTS " +
							"(SELECT cred.id from target_credential1 cred WHERE cred.user = targetComp.user AND cred.credential IN (:credentials)) " +
						 "GROUP BY targetAct.id, act.result_type, targetAct.result, targetAct.result_post_date, " +
						   "u.id, u.name, u.lastname, u.avatar_url " +
						   "ORDER BY targetAct.result_post_date ");
			
			if (paginate) {
				query.append("LIMIT " + limit + " ");
				query.append("OFFSET " + page * limit);
			}
				
			Query q = persistence.currentManager()
						.createSQLQuery(query.toString())
						.setLong("compId", compId)
						.setLong("actId", actId)
						.setString("resType", CommentedResourceType.ActivityResult.name())
						.setParameterList("credentials", deliveries);
			
			if (userToExclude > 0) {
				q.setLong("userId", userToExclude);
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
							date, user, commentsNo, false, false);

					results.add(ard);
				}
			}
			return results;
		} catch (ResourceNotFoundException rnfe) {
			throw rnfe;
		} catch (Exception e) {
			logger.error(e);
			e.printStackTrace();
			throw new DbConnectionException("Error while retrieving student responses");
		}
	}

	@Override
	@Transactional(readOnly = true)
	public List<ActivityResultData> getStudentsActivityAssessmentsData(long credId, long actId,
																	   long targetActivityId, boolean isInstructor, boolean loadDataOnlyForStudentsWhereGivenUserIsInstructor, long userId,
																	   boolean paginate, int page, int limit) throws DbConnectionException, ResourceNotFoundException {
		try {
			//TODO change when we upgrade to Hibernate 5.1 - it supports ad hoc joins for unmapped tables
			StringBuilder query = new StringBuilder(
					"SELECT targetAct.id as tActId, act.result_type, targetAct.result, targetAct.result_post_date, " +
						"u.id as uId, u.name, u.lastname, u.avatar_url, " +
						"COUNT(distinct com.id), ad.id as adId, COUNT(distinct msg.id), p.is_read, act.max_points, targetComp.id, act.grading_mode, act.rubric, act.accept_grades, ad.points, targetComp.competence, targetAct.completed, rubric.rubric_type " +
						"FROM target_activity1 targetAct " +
						"INNER JOIN target_competence1 targetComp " +
						"ON (targetAct.target_competence = targetComp.id) " +
						"INNER JOIN target_credential1 cred " +
						"ON cred.user = targetComp.user AND cred.credential = :credId ");

			if (loadDataOnlyForStudentsWhereGivenUserIsInstructor) {
				query.append("INNER JOIN credential_instructor ci " +
							 "ON cred.instructor = ci.id AND ci.user = :instructorUserId ");
			}

			query.append(
						"INNER JOIN activity1 act " +
						"ON (targetAct.activity = act.id " +
						"AND act.id = :actId) " +
						"LEFT JOIN rubric " +
							"ON (act.rubric = rubric.id) " +
						"LEFT JOIN (activity_assessment ad " +
						"INNER JOIN competence_assessment compAssessment " +
						"ON compAssessment.id = ad.competence_assessment " +
						"INNER JOIN credential_competence_assessment cca " +
						"ON cca.competence_assessment = compAssessment.id " +
						"INNER JOIN credential_assessment credAssessment " +
						"ON credAssessment.id = cca.credential_assessment " +
						"INNER JOIN target_credential1 tCred " +
						"ON tCred.id = credAssessment.target_credential " +
						"AND tCred.credential = :credId) " +
						"ON act.id = ad.activity " +
						// following condition ensures that assessment for the right student is joined
						"AND compAssessment.student = targetComp.user " +
						"AND ad.type = :instructorAssessment " +
						"LEFT JOIN activity_discussion_participant p " +
						"ON ad.id = p.activity_discussion AND p.participant = targetComp.user " +
						"LEFT JOIN activity_discussion_message msg " +
						"ON ad.id = msg.discussion " +
						"INNER JOIN user u " +
						"ON (targetComp.user = u.id) " +
						"LEFT JOIN comment1 com " +
						"ON (targetAct.id = com.commented_resource_id " +
						"AND com.resource_type = :resType " +
						"AND com.parent_comment is NULL) ");


			if (targetActivityId > 0) {
				query.append("WHERE targetAct.id = :tActId " +
							 "AND targetAct.completed IS TRUE ");
			}

			query.append("GROUP BY targetAct.id, act.result_type, targetAct.result, targetAct.result_post_date, " +
					"u.id, u.name, u.lastname, u.avatar_url, " +
					"ad.id, p.is_read, act.max_points, targetComp.id, ad.points, " +
					"act.grading_mode, act.rubric, act.accept_grades, targetComp.competence " +
					"ORDER BY targetAct.result_post_date ");

			if (paginate) {
				query.append("LIMIT " + limit + " ");
				query.append("OFFSET " + page * limit);
			}

			Query q = persistence.currentManager()
					.createSQLQuery(query.toString())
					.setLong("actId", actId)
					.setLong("credId", credId)
					.setString("resType", CommentedResourceType.ActivityResult.name())
					.setString("instructorAssessment", AssessmentType.INSTRUCTOR_ASSESSMENT.name());

			if (targetActivityId > 0) {
				q.setLong("tActId", targetActivityId);
			}

			if (loadDataOnlyForStudentsWhereGivenUserIsInstructor) {
				q.setLong("instructorUserId", userId);
			}

			@SuppressWarnings("unchecked")
			List<Object[]> res = q.list();

			List<ActivityResultData> results = new ArrayList<>();
			if (res != null) {
				long compId = 0;
				for (Object[] row : res) {
					if (compId == 0) {
						compId = ((BigInteger) row[18]).longValue();
					}
					long tActId = ((BigInteger) row[0]).longValue();
					org.prosolo.common.domainmodel.credential.ActivityResultType type =
							org.prosolo.common.domainmodel.credential.ActivityResultType.valueOf((String) row[1]);
					String result = (String) row[2];
					Date date = (Date) row[3];
					long studentId = ((BigInteger) row[4]).longValue();
					String firstName = (String) row[5];
					String lastName = (String) row[6];
					String avatar = (String) row[7];
					User user = new User();
					user.setId(studentId);
					user.setName(firstName);
					user.setLastname(lastName);
					user.setAvatarUrl(avatar);
					int commentsNo = ((BigInteger) row[8]).intValue();

					ActivityResultData ard = activityFactory.getActivityResultData(tActId, type, result,
							date, user, commentsNo, isInstructor, true);

					if (type != org.prosolo.common.domainmodel.credential.ActivityResultType.NONE) {
						ard.setOtherResultsComments(getCommentsOnOtherResults(studentId, tActId, actId));
					}

					results.add(ard);

					BigInteger assessmentId = (BigInteger) row[9];

					ActivityAssessmentData ad = ard.getAssessment();
					ad.setTargetActivityId(tActId);
					ad.setCompleted((Boolean) row[19]);
					ad.setUserId(studentId);
					ad.setActivityId(actId);
					ad.setCompetenceId(compId);
					ad.setCredentialId(credId);
					ad.setType(AssessmentType.INSTRUCTOR_ASSESSMENT);

					ad.setActivityAssessmentId(assessmentId.longValue());
					ad.setEncodedActivityAssessmentId(idEncoder.encodeId(assessmentId.longValue()));
					ad.setNumberOfMessages(((BigInteger) row[10]).intValue());
					ad.setAllRead(((Character) row[11]).charValue() == 'T');

					//set grade data
					BigInteger rubricIdBI = (BigInteger) row[15];
					long rubricId = rubricIdBI != null ? rubricIdBI.longValue() : 0;
					RubricType rubricType = rubricId > 0 ? RubricType.valueOf((String) row[20]) : null;
					//TODO stef star
					Map<Long, RubricAssessmentGradeSummary> rubricGradeSummary = assessmentManager.getActivityAssessmentsRubricGradeSummary(Arrays.asList(ad.getActivityAssessmentId()));
					ad.setGrade(GradeDataFactory.getGradeDataForActivity(
							GradingMode.valueOf((String) row[14]),
							(int) row[12],
							(int) row[17],
							rubricId,
							rubricType,
							null,
							((Character) row[16]).charValue() == 'T',
							rubricGradeSummary.get(ad.getActivityAssessmentId())));

					//load additional assessment data
					AssessmentBasicData abd = assessmentManager.getInstructorAssessmentBasicData(credId,
							compId, 0, ard.getUser().getId());
					if (abd != null) {
						ad.setCompAssessmentId(abd.getCompetenceAssessmentId());
						ad.setCredAssessmentId(abd.getCredentialAssessmentId());
						ad.setAssessorId(abd.getAssessorId());
					}
				}
			}
			return results;
		} catch (ResourceNotFoundException rnfe) {
			throw rnfe;
		} catch (Exception e) {
			logger.error("Error", e);
			throw new DbConnectionException("Error while retrieving students activity assessments");
		}
	}
	
	private List<ResultCommentInfo> getCommentsOnOtherResults(long userId, long targetActivityId, long actId) {
		String query = 
				"SELECT targetAct.id, user.name, user.lastname, COUNT(DISTINCT com.id) AS noOfComments, MIN(com.post_date) AS firstCommentDate " +
				"FROM target_activity1 targetAct " +
				"INNER JOIN activity1 act ON (targetAct.activity = act.id AND act.id = :actId) " +
				"INNER JOIN target_competence1 targetComp ON (targetAct.target_competence = targetComp.id) " +
				"INNER JOIN user user ON (targetComp.user = user.id) " +
				"INNER JOIN comment1 com ON (targetAct.id = com.commented_resource_id AND com.resource_type = 'ActivityResult' " +
											"AND com.user = :userId) " +
				"WHERE targetAct.id != :targetActivityId " +
					"AND targetAct.result IS NOT NULL " +
					"AND user.id != :userId " +
				"GROUP BY targetAct.id, user.name, user.lastname ";
		
		@SuppressWarnings("unchecked")
		List<Object[]> rows = persistence.currentManager().createSQLQuery(query)
				.setLong("targetActivityId", targetActivityId)
				.setLong("userId", userId)
				.setLong("actId", actId)
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
	public ActivityAssessmentsSummaryData getActivityAssessmentDataForDefaultCredentialAssessment(long credId, long actId, long targetActivityId, boolean isInstructor, boolean loadDataOnlyForStudentsWhereGivenUserIsInstructor, long userId)
			throws DbConnectionException, ResourceNotFoundException {
		try {
			//check if activity is part of a credential
			checkIfActivityIsPartOfACredential(credId, actId);

			Activity1 activity = (Activity1) persistence.currentManager().get(Activity1.class, actId);

			ActivityAssessmentsSummaryData summary = assessmentDataFactory.getActivityAssessmentsSummaryData(activity, 0L, 0L);

			summary.getStudentResults().addAll(getStudentsActivityAssessmentsData(credId, actId, targetActivityId, isInstructor, loadDataOnlyForStudentsWhereGivenUserIsInstructor, userId,
					false, 0, 0));

			return summary;
		} catch (ResourceNotFoundException e) {
			throw e;
		} catch (Exception e) {
			logger.error("Error", e);
			throw new DbConnectionException("Error loading activity assessment");
		}
	}

	@Override
	@Transactional(readOnly = true)
	public ActivityAssessmentsSummaryData getActivityAssessmentsDataForInstructorCredentialAssessment(long credId, long actId, ResourceAccessData accessData,
																									  long userId, boolean paginate, int page, int limit)
					throws DbConnectionException, ResourceNotFoundException {
		try {
			//check if activity is part of a credential
			checkIfActivityIsPartOfACredential(credId, actId);

			Activity1 activity = (Activity1) persistence.currentManager().get(Activity1.class, actId);

			//load only data for instructors students if user does not have Edit privilege
			boolean loadDataOnlyForStudentsWhereGivenUserIsInstructor = !accessData.isCanEdit();

			long numberOfStudentsCompletedActivity = countStudentsLearningCredentialThatCompletedActivity(credId, actId, loadDataOnlyForStudentsWhereGivenUserIsInstructor, userId);
			long numberOfStudentsAssessed = 0;
			List<ActivityResultData> results = new ArrayList<>();
			if (numberOfStudentsCompletedActivity > 0) {
				results.addAll(getStudentsActivityAssessmentsData(credId, actId, 0, accessData.isCanInstruct(),
						loadDataOnlyForStudentsWhereGivenUserIsInstructor, userId, paginate, page, limit));
				numberOfStudentsAssessed = assessmentManager.getNumberOfAssessedStudentsForActivity(credId, actId, loadDataOnlyForStudentsWhereGivenUserIsInstructor, userId);
			}
			ActivityAssessmentsSummaryData summary = assessmentDataFactory.getActivityAssessmentsSummaryData(activity, numberOfStudentsCompletedActivity, numberOfStudentsAssessed);
			summary.setStudentResults(results);

			return summary;
		} catch (ResourceNotFoundException e) {
			throw e;
		} catch (Exception e) {
			logger.error("Error", e);
			throw new DbConnectionException("Error loading activity assessments");
		}
	}

	@Override
	@Transactional(readOnly = true)
	public Long countStudentsLearningCredentialThatCompletedActivity(long credId, long actId, boolean loadDataOnlyForStudentsWhereGivenUserIsInstructor, long userId) throws DbConnectionException {
		String usersCompletedActivityQ =
				"SELECT COUNT(ta.id) " +
				"FROM target_activity1 ta " +
				"INNER JOIN target_competence1 targetComp " +
				"ON ta.target_competence = targetComp.id " +
				"INNER JOIN target_credential1 cred " +
				"ON cred.user = targetComp.user AND cred.credential = :credId ";
		if (loadDataOnlyForStudentsWhereGivenUserIsInstructor) {
			usersCompletedActivityQ +=
				"INNER JOIN credential_instructor ci " +
				"ON cred.instructor = ci.id AND ci.user = :instructorUserId ";
		}

		usersCompletedActivityQ +=
				"WHERE ta.activity = :actId AND ta.completed IS TRUE";

		Query q = persistence.currentManager()
				.createSQLQuery(usersCompletedActivityQ)
				.setLong("credId", credId)
				.setLong("actId", actId);
		if (loadDataOnlyForStudentsWhereGivenUserIsInstructor) {
			q.setLong("instructorUserId", userId);
		}

		return ((BigInteger) q.uniqueResult()).longValue();
	}

	@Override
	@Transactional (readOnly = true)
	public ActivityResultData getActivityResultData(long targetActivityId, boolean loadComments, 
			boolean instructor, boolean isManager, long loggedUserId) {
		String query = 
			"SELECT targetAct, targetComp.user, act.resultType " +
			"FROM TargetActivity1 targetAct " +
			"INNER JOIN targetAct.activity act " +
			"INNER JOIN targetAct.targetCompetence targetComp " + 
			"WHERE targetAct.id = :targetActivityId";
		
		Object[] result = (Object[]) persistence.currentManager()
			.createQuery(query.toString())
			.setLong("targetActivityId", targetActivityId)
			.uniqueResult();
		
		if (result != null) {
			TargetActivity1 targetActivity = (TargetActivity1) result[0];
			User user = (User) result[1];
			org.prosolo.common.domainmodel.credential.ActivityResultType resType = 
					(org.prosolo.common.domainmodel.credential.ActivityResultType) result[2];
			
			ActivityResultData activityResult = activityFactory.getActivityResultData(
					targetActivity.getId(), resType, targetActivity.getResult(), 
					targetActivity.getResultPostDate(), user, 0, instructor, isManager);
			
			if (loadComments) {
				CommentsData commentsData = activityResult.getResultComments();

				List<CommentData> comments = commentManager.getComments(
						commentsData.getResourceType(), 
						commentsData.getResourceId(), false, 0, 
						commentDataFactory.getCommentSortData(commentsData), 
						CommentReplyFetchMode.FetchReplies, 
						loggedUserId, false);
				
				Collections.reverse(comments);
				
				commentsData.setComments(comments);
			}
			
			return activityResult;
		} 
		return null;
	}
	
	@Override
	@Transactional (readOnly = true)
	public ActivityData getActivityResponseForUserToView(long targetActId, long userToViewId)
			throws DbConnectionException {
		try {
			String query =
					"SELECT targetAct, targetComp.user " +
							"FROM TargetActivity1 targetAct " +
							"INNER JOIN FETCH targetAct.activity act " +
							"INNER JOIN targetAct.targetCompetence targetComp " +
							"WHERE targetAct.id = :targetActivityId";

			Object[] result = (Object[]) persistence.currentManager()
					.createQuery(query.toString())
					.setLong("targetActivityId", targetActId)
					.uniqueResult();

			if (result != null) {
				TargetActivity1 targetActivity = (TargetActivity1) result[0];
				User user = (User) result[1];

				/*
				 * check if user can view activity response (he posted a response for this activity or he is assessor
				 * for target activity
				 */
				boolean hasUserPostedResponse = hasUserPostedResponseForActivity(
						targetActivity.getActivity().getId(), userToViewId);
				boolean isUserAssessor = assessmentManager.isUserAssessorOfUserActivity(userToViewId, user.getId(),
						targetActivity.getActivity().getId(), false);
				if ((hasUserPostedResponse && targetActivity.getActivity().isStudentCanSeeOtherResponses())
						|| isUserAssessor) {
					ActivityData activityData = activityFactory.getActivityData(targetActivity, null,
							null, null, false, 0, false);

					UserData ud = new UserData(user.getId(), user.getFullName(),
							AvatarUtils.getAvatarUrlInFormat(user.getAvatarUrl(), ImageFormat.size120x120), user.getPosition(), user.getEmail(), true);

					activityData.getResultData().setUser(ud);
					return activityData;
				}
			}
			return null;
		} catch (Exception e) {
			logger.error(e);
			e.printStackTrace();
			throw new DbConnectionException("Error while retrieving activity response");
		}
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
//
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
//	public TargetActivity1 replaceTargetActivityOutcome(long targetActivityId, Outcome outcome, Session session){
//		TargetActivity1 targetActivity = (TargetActivity1) session.load(TargetActivity1.class, targetActivityId);
//		System.out.println("REPLACE OUTCOME SHOULD BE PROCESSED HERE...");
//		/*List<Outcome> oldOutcomes = targetActivity.getOutcomes();
//		List<Outcome> newOutcomes = new ArrayList<Outcome>();
//		newOutcomes.add(outcome);
//		targetActivity.setOutcomes(newOutcomes);
//		targetActivity.setCompleted(true);
//		targetActivity.setDateCompleted(new Date());
//		session.save(targetActivity);
//		for (Outcome oldOutcome : oldOutcomes) {
//			try {
//				this.deleteById(SimpleOutcome.class, oldOutcome.getId(), session);
//			} catch (ResourceCouldNotBeLoadedException e) {
//				e.printStackTrace();
//			}
//		}*/
//		return targetActivity;
//	}

	/**
	 * Creates a new {@link CompetenceActivity1} instance that is a bcc of the given original.
	 * 
	 * @param original
	 * @return newly created {@link CompetenceActivity1} instance
	 */
	@Transactional (readOnly = false)
	@Override
	public Result<CompetenceActivity1> cloneActivity(CompetenceActivity1 original, long compId,
			UserContextData context) throws DbConnectionException {
		try {
			Result<Activity1> res = clone(original.getActivity(), context);
			
			CompetenceActivity1 competenceActivity = new CompetenceActivity1();
			competenceActivity.setActivity(res.getResult());
			Competence1 comp = (Competence1) persistence.currentManager().load(Competence1.class, compId);
			competenceActivity.setCompetence(comp);
			competenceActivity.setOrder(original.getOrder());
			saveEntity(competenceActivity);
			Result<CompetenceActivity1> r = new Result<>();
			r.setResult(competenceActivity);
			r.appendEvents(res.getEventQueue());
			return r;
		} catch(DbConnectionException dce) {
			throw dce;
		} catch (Exception e) {
			logger.error(e);
			e.printStackTrace();
			throw new DbConnectionException("Error while cloning competence activity");
		}
	}

	/**
	 * Creates a new {@link Activity1} instance that is a clone of the given instance.
	 * 
	 * @param original the original activity to be cloned
	 * @param context
	 * @return newly created activity
	 */
	private Result<Activity1> clone(Activity1 original, UserContextData context) {
		try {
			Activity1 activity = cloneActivityBasedOnType(original);
			
			activity.setDateCreated(new Date());
			activity.setTitle(original.getTitle());
			activity.setDescription(original.getDescription());
			activity.setDuration(original.getDuration());
			activity.setLinks(cloneLinks(original.getLinks()));
			activity.setFiles(cloneLinks(original.getFiles()));
			activity.setResultType(original.getResultType());
			activity.setType(original.getType());
			activity.setGradingMode(original.getGradingMode());
			activity.setRubric(original.getRubric());
			activity.setRubricVisibility(original.getRubricVisibility());
			activity.setMaxPoints(original.getMaxPoints());
			activity.setStudentCanSeeOtherResponses(original.isStudentCanSeeOtherResponses());
			activity.setStudentCanEditResponse(original.isStudentCanEditResponse());
			User user = (User) persistence.currentManager().load(User.class, context.getActorId());
			activity.setCreatedBy(user);
			activity.setVisibleForUnenrolledStudents(original.isVisibleForUnenrolledStudents());
			activity.setDifficulty(original.getDifficulty());
			saveEntity(activity);
			Result<Activity1> res = new Result<>();
			res.setResult(activity);
			EventData ev = new EventData();
			ev.setEventType(EventType.Create);
			ev.setContext(context);
			Activity1 act = new Activity1();
			act.setId(activity.getId());
			ev.setObject(act);
			
			res.appendEvent(ev);
			return res;
		} catch (Exception e) {
			logger.error(e);
			e.printStackTrace();
			throw new DbConnectionException("Error while cloning activity");
		}
	}

	private Set<ResourceLink> cloneLinks(Set<ResourceLink> links) {
		Set<ResourceLink> cloneLinks = new HashSet<ResourceLink>();
		
		for (ResourceLink link : links) {
			ResourceLink linkClone = new ResourceLink();
			linkClone.setLinkName(link.getLinkName());
			linkClone.setUrl(link.getUrl());
			saveEntity(linkClone);
			
			cloneLinks.add(linkClone);
		}
		
		return cloneLinks;
	}

	private Activity1 cloneActivityBasedOnType(Activity1 original) {
		class ActivityCloneVisitor implements ActivityVisitor {
			private Activity1 act;
			
			@Override
			public void visit(ExternalToolActivity1 activity) {
				ExternalToolActivity1 extAct = new ExternalToolActivity1();
				extAct.setLaunchUrl(activity.getLaunchUrl());
				extAct.setSharedSecret(activity.getSharedSecret());
				extAct.setConsumerKey(activity.getConsumerKey());
				extAct.setAcceptGrades(activity.isAcceptGrades());
				extAct.setOpenInNewWindow(activity.isOpenInNewWindow());
				extAct.setScoreCalculation(activity.getScoreCalculation());
				act = extAct;
			}
			
			@Override
			public void visit(UrlActivity1 activity) {
				UrlActivity1 urlAct = new UrlActivity1();
				urlAct.setUrlType(activity.getUrlType());
				urlAct.setUrl(activity.getUrl());
				urlAct.setLinkName(activity.getLinkName());
				urlAct.setCaptions(cloneLinks(activity.getCaptions()));
				act = urlAct;
			}
			
			@Override
			public void visit(TextActivity1 activity) {
				TextActivity1 ta = new TextActivity1();
				ta.setText(activity.getText());
				act = ta;
			}
		}
		
		ActivityCloneVisitor visitor = new ActivityCloneVisitor();
		original.accept(visitor);
		return visitor.act;
	}

	@Override
	@Transactional(readOnly = false)
	public void updateActivityCreator(long newCreatorId, long oldCreatorId) 
			throws DbConnectionException {
		try {	
				String query = "UPDATE Activity1 act " +
								"SET act.createdBy.id = :newCreatorId " +
								"WHERE act.createdBy.id = :oldCreatorId";				    
	
				persistence.currentManager()
					.createQuery(query)
					.setLong("newCreatorId", newCreatorId)
					.setLong("oldCreatorId", oldCreatorId)
					.executeUpdate();
		}catch(Exception e){
			logger.error(e);
			e.printStackTrace();
			throw new DbConnectionException("Error while updating creator of activities");
		}
	}

	private boolean hasUserPostedResponseForActivity(long actId, long userId) {
		String query =
				"SELECT tAct.result " +
						"FROM TargetActivity1 tAct " +
						"INNER JOIN tAct.targetCompetence targetComp " +
						"WHERE tAct.activity.id = :actId " +
						"AND targetComp.user.id = :userId";

		String result = (String) persistence.currentManager()
				.createQuery(query)
				.setLong("actId", actId)
				.setLong("userId", userId)
				.uniqueResult();

		return result != null;
	}

	@Override
	@Transactional (readOnly = true)
	public List<Long> getIdsOfCredentialsWithActivity(long actId, CredentialType type) throws DbConnectionException {
		try {
			String query =
					"SELECT cred.id " +
					"FROM Activity1 act " +
					"INNER JOIN act.competenceActivities cAct " +
					"INNER JOIN cAct.competence comp " +
					"INNER JOIN comp.credentialCompetences cComp " +
					"INNER JOIN cComp.credential cred " +
					"WHERE act.id = :actId ";

			if (type != null) {
				query += "AND cred.type = :type";
			}

			Query q = persistence.currentManager()
					.createQuery(query)
					.setLong("actId", actId);

			if (type != null) {
				q.setString("type", type.name());
			}

			@SuppressWarnings("unchecked")
			List<Long> result = q.list();

			return result;
		} catch (Exception e) {
			e.printStackTrace();
			logger.error(e);
			throw new DbConnectionException("Error while retrieving credential ids");
		}
	}

}
