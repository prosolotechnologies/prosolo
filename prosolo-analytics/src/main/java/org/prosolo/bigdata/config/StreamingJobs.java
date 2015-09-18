package org.prosolo.bigdata.config;

import org.simpleframework.xml.Element;

/**
 * @author Zoran Jeremic, Sep 16, 2015
 *
 */
public class StreamingJobs {
	@Element(name = "twitterStreaming", required = false)
	public boolean twitterStreaming = true;

	@Element(name = "rabbitMQStreaming", required = false)
	public boolean rabbitMQStreaming = true;
}
