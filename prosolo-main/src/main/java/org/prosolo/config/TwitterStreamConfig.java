package org.prosolo.config;

import org.simpleframework.xml.Element;

/**
 * @author Zoran Jeremic 2013-10-14
 *
 */

public class TwitterStreamConfig {
	
	@Element(name = "queue-size")
	public int queueSize;
	
	@Element(name = "reduced-queue-size")
	public int reducedQueueSize;
	
	@Element(name = "filter-density-size")
	public int filterDensitySize;
	
	@Element(name = "fullfiltering-density-thrash-seconds")
	public int fullFilteringDensityTrashSeconds;
	
	@Element(name = "fullfiltering-number-check")
	public int fullFilteringNumberCheck;
	
	@Element(name = "blacklist-enter-criteria")
	public int blackListEnterCriteria;
	
	@Element(name = "post-consume-interval-miliseconds")
	public int postConsumeIntervalMiliseconds;
	
	
}
