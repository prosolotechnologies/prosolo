package org.prosolo.services.interaction.impl;

import static org.junit.Assert.*;

import java.util.List;

import org.junit.Test;
import org.prosolo.common.domainmodel.activities.Activity;
import org.prosolo.common.domainmodel.activities.TargetActivity;
import org.prosolo.common.domainmodel.competences.Competence;
import org.prosolo.common.domainmodel.competences.TargetCompetence;
import org.prosolo.core.stress.TestContext;
import org.prosolo.services.interaction.AnalyticalServiceCollector;
import org.prosolo.services.nodes.ActivityManager;
import org.springframework.beans.factory.annotation.Autowired;

/**
@author Zoran Jeremic Apr 19, 2015
 *
 */

public class AnalyticalServiceCollectorImplTest  extends TestContext {

	@Autowired private AnalyticalServiceCollector analyticalServiceCollector;
	@Autowired private ActivityManager activityManager;
	@Test
	public void testIncreaseUserActivityLog() {
		fail("Not yet implemented");
	}

	@Test
	public void testCreateActivityInteractionData() {
		
		List<TargetActivity> tActivities=activityManager.getAllTargetActivities();
		for(TargetActivity targetActivity:tActivities){
			Activity activity=targetActivity.getActivity();
			 TargetCompetence targetCompetence=targetActivity.getParentCompetence();
			 Competence competence=targetCompetence.getCompetence();
			analyticalServiceCollector.createActivityInteractionData(competence.getId(),activity.getId());
		}
		
	}

}

