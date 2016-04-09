package org.prosolo.services.nodes.impl;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import org.apache.log4j.Logger;
import org.prosolo.common.domainmodel.credential.Activity1;
import org.prosolo.common.domainmodel.credential.Competence1;
import org.prosolo.common.domainmodel.credential.CompetenceActivity1;
import org.prosolo.common.domainmodel.credential.TargetActivity1;
import org.prosolo.common.domainmodel.credential.TargetCompetence1;
import org.prosolo.services.general.impl.AbstractManagerImpl;
import org.prosolo.services.lti.exceptions.DbConnectionException;
import org.prosolo.services.nodes.Activity1Manager;
import org.prosolo.services.nodes.data.BasicActivityData;
import org.prosolo.services.nodes.factory.ActivityDataFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service("org.prosolo.services.nodes.Activity1Manager")
public class Activity1ManagerImpl extends AbstractManagerImpl implements Activity1Manager {

	private static final long serialVersionUID = -2783669846949034832L;

	private static Logger logger = Logger.getLogger(Activity1ManagerImpl.class);
	
	@Inject
	private ActivityDataFactory activityFactory;
	
	@Override
	@Transactional(readOnly = true)
	public List<BasicActivityData> getCompetenceActivities(long competenceId) 
			throws DbConnectionException {
		List<BasicActivityData> result = new ArrayList<>();
		try {
			Competence1 comp = (Competence1) persistence.currentManager().load(Competence1.class, competenceId);
			String query = "SELECT compAct " +
					       "FROM CompetenceActivity1 compAct " + 
					       "INNER JOIN compAct.activity act " +
					       "WHERE compAct.competence = :comp " +
					       "ORDER BY compAct.order";

			@SuppressWarnings("unchecked")
			List<CompetenceActivity1> res = persistence.currentManager()
				.createQuery(query)
				.setEntity("comp", comp)
				.list();

			if (res != null) {
				for (CompetenceActivity1 act : res) {
					BasicActivityData bad = activityFactory.getActivityData(act, true);
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
	@Transactional(readOnly = false)
	public TargetActivity1 createTargetActivity(TargetCompetence1 targetComp, 
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
	
}
