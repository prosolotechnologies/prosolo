package org.prosolo.config;

import org.simpleframework.xml.Element;

/**
@author Zoran Jeremic Sep 7, 2014
 */

public class RabbitMQConfig {

	@Element(name = "distributed")
	public boolean distributed;
	
	@Element(name = "master-node")
	public boolean masterNode;
	
	@Element(name = "host")
	public String host;
	
	@Element(name = "port")
	public int port;
	
	@Element(name = "virtualHost")
	public String virtualHost;
	
	@Element(name = "username")
	public String username;
	
	
	@Element(name = "password")
	public String password;
	
	@Element(name = "exchange")
	public String exchange;
	
	@Element(name = "queue")
	public String queue;
	
	@Element(name = "durableQueue")
	public boolean durableQueue;
	
	@Element(name = "exclusiveQueue")
	public boolean exclusiveQueue;
	
	@Element(name = "autodeleteQueue")
	public boolean autodeleteQueue;
	
	@Element(name = "routingKey")
	public String routingKey;
	
	@Element(name = "messageExpiration")
	public String messageExpiration;
	
	@Element(name = "queuePrefix")
	public String queuePrefix;
	
	 
	
}
