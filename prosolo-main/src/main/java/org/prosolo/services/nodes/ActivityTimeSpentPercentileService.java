package org.prosolo.services.nodes;

import java.util.List;

public interface ActivityTimeSpentPercentileService {

	int getPercentileGroup(List<Long> times, long timeSpentForObservedActivity);

}