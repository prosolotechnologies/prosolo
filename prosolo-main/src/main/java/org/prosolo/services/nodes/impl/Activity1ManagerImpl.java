package org.prosolo.services.nodes.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiConsumer;

import javax.inject.Inject;

import org.apache.log4j.Logger;
import org.hibernate.Query;
import org.prosolo.common.domainmodel.activities.events.EventType;
import org.prosolo.common.domainmodel.credential.Activity1;
import org.prosolo.common.domainmodel.credential.Competence1;
import org.prosolo.common.domainmodel.credential.CompetenceActivity1;
import org.prosolo.common.domainmodel.credential.ExternalToolActivity1;
import org.prosolo.common.domainmodel.credential.ResourceLink;
import org.prosolo.common.domainmodel.credential.TargetActivity1;
import org.prosolo.common.domainmodel.credential.TargetCompetence1;
import org.prosolo.common.domainmodel.credential.TargetCredential1;
import org.prosolo.common.domainmodel.credential.TextActivity1;
import org.prosolo.common.domainmodel.credential.UrlActivity1;
import org.prosolo.common.domainmodel.credential.UrlActivityType;
import org.prosolo.common.domainmodel.user.User;
import org.prosolo.services.event.EventException;
import org.prosolo.services.event.EventFactory;
import org.prosolo.services.general.impl.AbstractManagerImpl;
import org.prosolo.services.lti.exceptions.DbConnectionException;
import org.prosolo.services.nodes.Activity1Manager;
import org.prosolo.services.nodes.Competence1Manager;
import org.prosolo.services.nodes.ResourceFactory;
import org.prosolo.services.nodes.data.ActivityData;
import org.prosolo.services.nodes.data.ActivityType;
import org.prosolo.services.nodes.data.ObjectStatus;
import org.prosolo.services.nodes.data.Operation;
import org.prosolo.services.nodes.data.ResourceLinkData;
import org.prosolo.services.nodes.factory.ActivityDataFactory;
import org.prosolo.services.nodes.impl.util.EntityPublishTransition;
import org.prosolo.services.nodes.observers.learningResources.ActivityChangeTracker;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

@Service("org.prosolo.services.nodes.Activity1Manager")
public class Activity1ManagerImpl extends AbstractManagerImpl implements Activity1Manager {

	private static final long serialVersionUID = -2783669846949034832L;

	private static Logger logger = Logger.getLogger(Activity1ManagerImpl.class);
	
	@Inject private ActivityDataFactory activityFactory;
	@Inject private Competence1Manager compManager;
	@Inject private EventFactory eventFactory;
	@Inject private ResourceFactory resourceFactory;
	
	@Override
	@Transactional(readOnly = false)
	public Activity1 saveNewActivity(ActivityData data, long userId) 
			throws DbConnectionException {
		try {
			Activity1 act = resourceFactory.createActivity(data, userId);

			User user = new User();
			user.setId(userId);
			
			if(act.isPublished()) {
				eventFactory.generateEvent(EventType.Create, user, act);
			} else {
				eventFactory.generateEvent(EventType.Create_Draft, user, act);
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
					eventFactory.generateEvent(EventType.Delete, user, act);
				} else {
					eventFactory.generateEvent(EventType.Delete_Draft, user, act);
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

		CompetenceActivity1 compAct = (CompetenceActivity1) persistence.currentManager()
			.createQuery(query)
			.setEntity("act", act)
			.uniqueResult();
		
		long competenceId = compAct.getCompetence().getId();
		if(compAct != null) {
			shiftOrderOfActivitiesUp(competenceId, compAct.getOrder());
			delete(compAct);
		}
		
		if(duration != 0) {
			compManager.updateDuration(competenceId, duration, Operation.Subtract);
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
			List<CompetenceActivity1> res = getCompetenceActivities(competenceId);

			if (res != null) {
				for (CompetenceActivity1 act : res) {
					ActivityData bad = activityFactory.getActivityData(act, null, null, true);
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
	public List<CompetenceActivity1> getCompetenceActivities(long competenceId) 
			throws DbConnectionException {
		try {
			Competence1 comp = (Competence1) persistence.currentManager().load(Competence1.class, competenceId);
			String query = "SELECT distinct compAct " +
					       "FROM CompetenceActivity1 compAct " + 
					       "INNER JOIN fetch compAct.activity act " +
					       "LEFT JOIN fetch act.links " +
					       "LEFT JOIN fetch act.files " +
					       "WHERE compAct.competence = :comp " +					       
					       "ORDER BY compAct.order";

			@SuppressWarnings("unchecked")
			List<CompetenceActivity1> res = persistence.currentManager()
				.createQuery(query)
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
			List<CompetenceActivity1> compActivities = getCompetenceActivities(compId);
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
			TargetActivity1 targetAct = new TargetActivity1();
			targetAct.setTargetCompetence(targetComp);
			Activity1 act = compActivity.getActivity();
			targetAct.setTitle(act.getTitle());
			targetAct.setDescription(act.getDescription());
			targetAct.setActivity(act);
			targetAct.setOrder(compActivity.getOrder());
			targetAct.setDuration(act.getDuration());
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
					ActivityData actData = activityFactory.getActivityData(targetAct, true);
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
	
	@Transactional(readOnly = true)
	private List<TargetActivity1> getTargetActivities(long targetCompId) throws DbConnectionException {
		try {
			TargetCompetence1 targetComp = (TargetCompetence1) persistence.currentManager().load(
					TargetCredential1.class, targetCompId);
			
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
	
	public void publishAllCompetenceActivitiesWithoutDraftVersion(Long compId) 
			throws DbConnectionException {
		try {
			List<Long> actIds = getAllCompetenceActivitiesIds(compId);
			publishDraftActivitiesWithoutDraftVersion(actIds);
		} catch(Exception e) {
			logger.error(e);
			e.printStackTrace();
			throw new DbConnectionException("Error while updating activities");
		}
	}

	@Override
	@Transactional(readOnly = false)
	public void publishDraftActivitiesWithoutDraftVersion(List<Long> actIds) 
			throws DbConnectionException {
		try {
			if(actIds == null || actIds.isEmpty()) {
				return;
			}
			
			String query = "UPDATE Activity1 act " +
						   "SET act.published = :published " + 
						   "WHERE act.hasDraft = :hasDraft " +
						   "AND act.id IN :actIds";
			persistence.currentManager()
				.createQuery(query)
				.setBoolean("published", true)
				.setBoolean("hasDraft", false)
				.setParameterList("actIds", actIds)
				.executeUpdate();
		} catch(Exception e) {
			logger.error(e);
			e.printStackTrace();
			throw new DbConnectionException("Error while updating activities");
		}
	}

	private List<Long> getAllCompetenceActivitiesIds(Long compId) {
		try {
			String query = "Select act.id " +
						   "FROM CompetenceActivity1 compAct " + 
						   "INNER JOIN compAct.competence comp " +
						   "INNER JOIN compAct.activity act " +
						   "WHERE act.deleted = :deleted " +
						   "AND comp.id = :compId";
			
			@SuppressWarnings("unchecked")
			List<Long> actIds = persistence.currentManager()
				.createQuery(query)
				.setBoolean("deleted", false)
				.setLong("compId", compId)
				.list();
			
			if(actIds == null) {
				return new ArrayList<>();
			}
			return actIds;
		} catch(Exception e) {
			logger.error(e);
			e.printStackTrace();
			throw new DbConnectionException("Error while updating activities");
		}
	}
	
	//COPYING FROM CREDENTIAL
	
	@Override
	@Transactional(readOnly = true)
	public ActivityData getActivityDataForEdit(long activityId, long creatorId) 
			throws DbConnectionException {
		try {			   
			CompetenceActivity1 res = getCompetenceActivityForCreator(activityId, creatorId, true);
			
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

	@Transactional(readOnly = true)
	private CompetenceActivity1 getCompetenceActivityForCreator(long activityId, long userId, 
			boolean loadLinks) throws DbConnectionException {
		try {
			User user = (User) persistence.currentManager().load(User.class, userId);
				
			StringBuilder builder = new StringBuilder();
			builder.append("SELECT compAct " +
					   	   "FROM CompetenceActivity1 compAct " + 
					       "INNER JOIN fetch compAct.activity act ");
			
			if(loadLinks) {
				builder.append("LEFT JOIN fetch act.links link " +
						       "LEFT JOIN fetch act.files file ");
			}
			builder.append("WHERE act.id = :actId " +
					       "AND act.deleted = :deleted ");
			
			if(userId > 0) {
				builder.append("AND act.createdBy = :user");
			}
						
			Query q = persistence.currentManager()
					.createQuery(builder.toString())
					.setLong("actId", activityId)
					.setBoolean("deleted", false);
			if(userId > 0) {
				q.setEntity("user", user);
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
	public Activity1 updateActivity(ActivityData data, long userId) throws DbConnectionException {
		try {
			Activity1 act = resourceFactory.updateActivity(data);

			User user = new User();
			user.setId(userId);
			if(data.isPublished()) {
				//activity remains published
				if(!data.isPublishedChanged()) {
					Map<String, String> params = new HashMap<>();
				    ActivityChangeTracker changeTracker = new ActivityChangeTracker(data.isPublished(),
				    		false, data.isTitleChanged(), data.isDescriptionChanged());
				    Gson gson = new GsonBuilder().create();
				    String jsonChangeTracker = gson.toJson(changeTracker);
				    params.put("changes", jsonChangeTracker);
				    eventFactory.generateEvent(EventType.Edit, user, act, params);
				} 
				/*
				 * this means that activity is published for the first time
				 */
				else if(!data.isDraft()) {
					eventFactory.generateEvent(EventType.Create, user, act);
				}
				/*
				 * Activity becomes published again. Because data can show what has changed
				 * based on draft version, we can't use that. We need to know what has changed based on
				 * original activity, so all fields are treated as changed.
				 */
				else {
					Map<String, String> params = new HashMap<>();
				    ActivityChangeTracker changeTracker = new ActivityChangeTracker(data.isPublished(),
				    		true, true, true);
				    Gson gson = new GsonBuilder().create();
				    String jsonChangeTracker = gson.toJson(changeTracker);
				    params.put("changes", jsonChangeTracker);
				    params.put("draftVersionId", data.getActivityId() + "");
				    eventFactory.generateEvent(EventType.Edit, user, act, params);
				}
			} else {
				/*
				 * if activity remains draft
				 */
				if(!data.isPublishedChanged()) {
					Map<String, String> params = new HashMap<>();
				    ActivityChangeTracker changeTracker = new ActivityChangeTracker(data.isPublished(),
				    		false, data.isTitleChanged(), data.isDescriptionChanged());
				    Gson gson = new GsonBuilder().create();
				    String jsonChangeTracker = gson.toJson(changeTracker);
				    params.put("changes", jsonChangeTracker);
					eventFactory.generateEvent(EventType.Edit_Draft, user, act, params);
				} 
				/*
				 * This means that activity was published before so draft version is created.
				 */
				else {
					Map<String, String> params = new HashMap<>();
					params.put("originalVersionId", data.getActivityId() + "");
					eventFactory.generateEvent(EventType.Create_Draft, user, act, params);
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
	
	@Override
	@Transactional(readOnly = false)
	public Activity1 updateActivity(ActivityData data) {
		Activity1 act = null;
		Class<? extends Activity1> activityClass = null;
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
		
		return updateActivityData(act, activityClass, publishTransition, data);
	}
	
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
			    	updateCompDuration(data.getCompetenceId(), newDuration, actToUpdate.getDuration());
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
						updateCompDuration(data.getCompetenceId(), newDuration1, oldDuration);
					}
					break;
			}
			
			actToUpdate.setTitle(data.getTitle());
			actToUpdate.setDescription(data.getDescription());
			actToUpdate.setDuration(data.getDurationHours() * 60 + data.getDurationMinutes());
			actToUpdate.setPublished(data.isPublished());
			actToUpdate.setUploadAssignment(data.isUploadAssignment());

			updateResourceLinks(data.getLinks(), actToUpdate.getLinks(), actToUpdate, 
					(activityForUpdate, links) -> activityForUpdate.setLinks(links) , publishTransition);
			
			updateResourceLinks(data.getFiles(), actToUpdate.getFiles(), actToUpdate, 
					(activityForUpdate, links) -> activityForUpdate.setFiles(links), publishTransition);
			
			if(actToUpdate instanceof TextActivity1) {
				TextActivity1 ta = (TextActivity1) actToUpdate;
				ta.setText(data.getText());
			} else if(actToUpdate instanceof UrlActivity1) {
				UrlActivity1 urlAct = (UrlActivity1) actToUpdate;
				if(data.getActivityType() == ActivityType.VIDEO) {
					urlAct.setType(UrlActivityType.Video);
				} else {
					urlAct.setType(UrlActivityType.Slides);
				}
				urlAct.setUrl(data.getLink());
				urlAct.setLinkName(data.getLinkName());
			} else if(actToUpdate instanceof ExternalToolActivity1) {
				ExternalToolActivity1 extAct = (ExternalToolActivity1) actToUpdate;
				extAct.setLaunchUrl(data.getLaunchUrl());
				extAct.setSharedSecret(data.getSharedSecret());
				extAct.setConsumerKey(data.getConsumerKey());
				extAct.setAcceptGrades(data.isAcceptGrades());
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
	
	private void updateCompDuration(long competenceId, long newDuration, long oldDuration) {
		long durationChange = newDuration - oldDuration;
    	Operation op = null;
    	if(durationChange > 0) {
    		op = Operation.Add;
    	} else {
    		durationChange = -durationChange;
    		op = Operation.Subtract;
    	}
    	compManager.updateDuration(competenceId, durationChange, op);
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
			Activity1 actToUpdate, BiConsumer<Activity1, Set<ResourceLink>> linkSetter, 
			EntityPublishTransition publishTransition) {
		if(resLinksData != null) {
			if(publishTransition == EntityPublishTransition.NO_TRANSITION) {
				for(ResourceLinkData rl : resLinksData) {
					switch(rl.getStatus()) {
						case CREATED:
							ResourceLink link = new ResourceLink();
							link.setLinkName(rl.getLinkName());
							link.setUrl(rl.getUrl());
							saveEntity(link);
							resLinks.add(link);
							break;
						case CHANGED:
							ResourceLink updatedLink = (ResourceLink) persistence.currentManager()
								.load(ResourceLink.class, rl.getId());
							updatedLink.setUrl(rl.getUrl());
							updatedLink.setLinkName(rl.getLinkName());
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
				for(ResourceLinkData rl : resLinksData) {
					if(rl.getStatus() != ObjectStatus.REMOVED) {
						ResourceLink link = new ResourceLink();
						link.setLinkName(rl.getLinkName());
						link.setUrl(rl.getUrl());
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
	
}
