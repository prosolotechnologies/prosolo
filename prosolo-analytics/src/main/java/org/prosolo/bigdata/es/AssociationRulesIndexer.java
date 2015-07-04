package org.prosolo.bigdata.es;

import java.util.List;

import org.prosolo.bigdata.algorithms.fpgrowth.association_rules.AssocRule;

/**
@author Zoran Jeremic May 9, 2015
 *
 */

public interface AssociationRulesIndexer {

	void saveAssociationRulesForCompetence(long competenceid,
			AssocRule assocRule);

	void deleteAssociationRulesForCompetence(long competenceid);

	void saveFrequentCompetenceActivities(long competenceid,
			List<Long> activities);

}

