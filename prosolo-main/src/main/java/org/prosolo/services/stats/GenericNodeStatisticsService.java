/**
 * 
 */
package org.prosolo.services.stats;

import java.util.Date;
import java.util.List;
import java.util.Map;

import org.prosolo.domainmodel.activities.events.EventType;
import org.prosolo.domainmodel.general.Node;
import org.prosolo.util.date.TimeFrame;

/**
 * @author Nikola Milikic
 *
 */
public interface GenericNodeStatisticsService {

	Map<Date, Integer> getDateDistributionOfEventOccurence(
			String userUri, EventType eventType, List<Node> resources,
			TimeFrame timeFrame);
	
}
