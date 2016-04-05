package org.prosolo.services.nodes.impl;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;

import org.prosolo.common.domainmodel.annotation.Tag;
import org.prosolo.common.domainmodel.credential.Activity1;
import org.prosolo.common.domainmodel.credential.Competence1;
import org.prosolo.common.domainmodel.credential.CredentialCompetence1;
import org.prosolo.common.domainmodel.credential.TargetActivity1;
import org.prosolo.common.domainmodel.credential.TargetCompetence1;
import org.prosolo.common.domainmodel.credential.TargetCredential1;
import org.prosolo.common.domainmodel.user.User;
import org.prosolo.services.annotation.TagManager;
import org.prosolo.services.event.EventFactory;
import org.prosolo.services.general.impl.AbstractManagerImpl;
import org.prosolo.services.lti.exceptions.DbConnectionException;
import org.prosolo.services.nodes.Competence1Manager;
import org.prosolo.services.nodes.data.CompetenceData1;
import org.prosolo.services.nodes.factory.CompetenceDataFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service("org.prosolo.services.nodes.Competence1Manager")
public class Competence1ManagerImpl extends AbstractManagerImpl implements Competence1Manager {

	private static final long serialVersionUID = -2783669846949034832L;

	@Inject
	private EventFactory eventFactory;
	@Inject
	private TagManager tagManager;
	@Inject
	private CompetenceDataFactory competenceFactory;

	@Override
	@Transactional(readOnly = true)
	public List<CompetenceData1> getTargetCompetencesData(long targetCredentialId) throws DbConnectionException {
		List<CompetenceData1> result = new ArrayList<>();
		try {
			TargetCredential1 targetCred = (TargetCredential1) persistence.currentManager().load(
					TargetCredential1.class, targetCredentialId);
			String query = "SELECT targetComp " +
					       "FROM TargetCompetence1 targetComp " + 
					       "INNER JOIN fetch targetComp.createdBy user " +
					       "LEFT JOIN fetch comp.tags tags " +
					       "WHERE targetComp.targetCredential = :targetCred " +
					       "ORDER BY targetComp.order";

			@SuppressWarnings("unchecked")
			List<TargetCompetence1> res = persistence.currentManager()
				.createQuery(query)
				.setEntity("targetCred", targetCred)
				.list();

			if (res != null) {
				for (TargetCompetence1 targetComp : res) {
					CompetenceData1 compData = competenceFactory.getCompetenceData(
							targetComp.getCreatedBy(), targetComp, true);
					result.add(compData);
				}
			}
			return result;
		} catch (Exception e) {
			logger.error(e);
			e.printStackTrace();
			throw new DbConnectionException("Error while loading competence data");
		}
	}

	@Override
	@Transactional(readOnly = false)
	public TargetCompetence1 createTargetCompetence(TargetCredential1 targetCred, 
			CredentialCompetence1 cc) {
		TargetCompetence1 targetComp = new TargetCompetence1();
		Competence1 comp = cc.getCompetence();
		targetComp.setTargetCredential(targetCred);
		targetComp.setCompetence(comp);
		targetComp.setDuration(comp.getDuration());
		targetComp.setStudentAllowedToAddActivities(comp.isStudentAllowedToAddActivities());
		targetComp.setOrder(cc.getOrder());
		targetComp.setCreatedBy(comp.getCreatedBy());
		
		if(comp.getTags() != null) {
			Set<Tag> tags = new HashSet<>();
			for(Tag tag : comp.getTags()) {
				tags.add(tag);
			}
			targetComp.setTags(tags);
		}
		saveEntity(targetComp);
		
		List<Activity1> activities = comp.getActivities();
		if(activities != null) {
			for(Activity1 act : activities) {
				TargetActivity1 ta = createTargetActivity(targetComp, act);
				targetComp.getTargetActivities().add(ta);
			}
		}
		
		return targetComp;
	}
	
	@Transactional(readOnly = false)
	private TargetActivity1 createTargetActivity(TargetCompetence1 targetComp, Activity1 act) {
		TargetActivity1 targetAct = new TargetActivity1();
		targetAct.setTargetCompetence(targetComp);
		targetAct.setActivity(act);
		targetAct.setOrder(act.getOrderInCompetence());
		targetAct.setDuration(act.getDuration());
		return saveEntity(targetAct);
	}
	
	@Override
	@Transactional(readOnly = true)
	public CompetenceData1 getCompetenceData(long compId, boolean loadCreator, boolean loadTags, 
			boolean loadActivities, boolean shouldTrackChanges) throws DbConnectionException {
		try {
			Competence1 comp = getCompetence(compId, loadCreator, loadTags, loadActivities);
			
			User creator = loadCreator ? comp.getCreatedBy() : null;
			Set<Tag> tags = loadTags ? comp.getTags() : null;
			
			CompetenceData1 compData = competenceFactory.getCompetenceData(
					creator, comp, tags, shouldTrackChanges);
			return compData;
		} catch(Exception e) {
			logger.error(e);
			e.printStackTrace();
			throw new DbConnectionException("Error while loading competence data");
		}
	}
	
	@Transactional(readOnly = true)
	private Competence1 getCompetence(long compId, boolean loadCreator, boolean loadTags,
			boolean loadActivities) {
		StringBuilder builder = new StringBuilder();
		builder.append("SELECT comp " + 
					   "FROM Competence1 comp ");
		if(loadCreator) {
			builder.append("INNER JOIN fetch comp.createdBy user ");
		}
		if(loadTags) {
			builder.append("LEFT JOIN fetch comp.tags tags ");
		}
		if(loadActivities) {
			builder.append("LEFT JOIN fetch comp.activities act ");
		}
		builder.append("WHERE comp.id = :compId " +
				   "comp.deleted = :deleted " + 
				   "AND (comp.published = :published OR (comp.published = :notPublished " +
				   "AND comp.hasDraft = :hasDraft)) " +
				   "AND comp.draft = :draft ");
		if(loadActivities) {
			builder.append("AND (act is null or " +
				   "(act.deleted = :deleted " +
				   "AND (act.published = :published OR (act.published = :notPublished " +
				   "AND act.hasDraft = :hasDraft)) " +
				   "AND act.draft = :draft))");
		}

		Competence1 res = (Competence1) persistence.currentManager()
			.createQuery(builder.toString())
			.setEntity("compId", compId)
			.setBoolean("deleted", false)
			.setBoolean("published", true)
			.setBoolean("notPublished", false)
			.setBoolean("draft", false)
			.setBoolean("hasDraft", true)
			.uniqueResult();

		return res;
	}
	
}
