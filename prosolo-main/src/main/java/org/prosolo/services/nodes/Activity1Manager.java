package org.prosolo.services.nodes;

import java.util.List;

import org.prosolo.common.domainmodel.credential.Activity1;
import org.prosolo.common.domainmodel.credential.CompetenceActivity1;
import org.prosolo.common.domainmodel.credential.TargetActivity1;
import org.prosolo.common.domainmodel.credential.TargetCompetence1;
import org.prosolo.services.lti.exceptions.DbConnectionException;
import org.prosolo.services.nodes.data.BasicActivityData;

public interface Activity1Manager {

	List<BasicActivityData> getCompetenceActivities(long competenceId)
			throws DbConnectionException;
	
	/**
	 * 
	 * @param targetComp should be persistent object
	 * @param compActivity should be persistent object, fore best performance, 
	 * it is better if {@link Activity1} object referenced from {@code compActivity} 
	 * is loaded
	 * @return
	 * @throws DbConnectionException
	 */
	TargetActivity1 createTargetActivity(TargetCompetence1 targetComp, 
			CompetenceActivity1 compActivity) throws DbConnectionException;

}