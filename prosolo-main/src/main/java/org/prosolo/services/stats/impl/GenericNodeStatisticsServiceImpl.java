package org.prosolo.services.stats.impl;


import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.hibernate.Query;
import org.hibernate.Session;
import org.prosolo.domainmodel.activities.events.EventType;
import org.prosolo.domainmodel.general.Node;
import org.prosolo.services.event.Event;
import org.prosolo.services.general.impl.AbstractManagerImpl;
import org.prosolo.services.stats.GenericNodeStatisticsService;
import org.prosolo.util.date.DateUtil;
import org.prosolo.util.date.TimeFrame;
import org.springframework.stereotype.Service;

/**
 * @author Nikola Milikic
 *
 */
@Service("org.prosolo.services.stats.GenericNodeStatisticsService")
class GenericNodeStatisticsServiceImpl extends AbstractManagerImpl implements GenericNodeStatisticsService {
	
	private static final long serialVersionUID = 5052742187086634025L;
	
	@SuppressWarnings("unused")
	private static Logger logger = Logger.getLogger(GenericNodeStatisticsService.class);
	
	@Override
	public Map<Date, Integer> getDateDistributionOfEventOccurence(
			String userUri, EventType eventType, List<Node> resources,
			TimeFrame timeFrame) {
		
		String query = 
				"SELECT DISTINCT e " +
				"FROM Event e " +
				"WHERE e.action = :action " +
					"AND ( e.dateCreated > :dateCreated ) " +
					"AND e.object IN ( ";
		
		for (int i = 0; i < resources.size(); i++) {
			query += ":res"+i;
			if (i != resources.size()-1)
				query += ", ";
		}
		query += ") ";
		
		if (userUri != null) {
			query += "AND e.actor.uri = :userUri ";
		}
		
		Session session = persistence.currentManager();

		Date timeFrameMargin = DateUtil.getTimeFrameMarginDate(timeFrame);
		
		Query q = session.createQuery(query).
				setString("action", eventType.name()).
				setDate("timestamp", timeFrameMargin);
		
		for (int i = 0; i < resources.size(); i++) {
			q.setEntity("res"+i, resources.get(i));
		}
		
		if (userUri != null) {
			q.setString("userUri", userUri);
		}
		
		@SuppressWarnings("unchecked")
		List<Event> events = q.list();
		
		Map<Date, Integer> eventDateDistribution = new HashMap<Date, Integer>();
		
		if (events != null && !events.isEmpty()) {
			for (Event event : events) {
				Date date = event.getDateCreated();
				Integer count = eventDateDistribution.get(date);
				
				if (count == null)
					count = 0;
				
				eventDateDistribution.put(date, ++count);
			}
		}
		
		return eventDateDistribution;
	}
	
}
