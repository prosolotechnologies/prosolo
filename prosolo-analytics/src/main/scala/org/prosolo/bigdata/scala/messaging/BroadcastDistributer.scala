package org.prosolo.bigdata.scala.messaging

import org.prosolo.common.messaging.MessageWrapperAdapter
import org.prosolo.common.messaging.rabbitmq.{QueueNames, ReliableProducer}
import org.prosolo.common.messaging.rabbitmq.impl.ReliableProducerImpl
import org.prosolo.common.messaging.data.{BroadcastMessage, MessageWrapper, ServiceType}
import java.net.InetAddress

import com.google.gson.GsonBuilder
import org.prosolo.bigdata.scala.twitter.StatusListener._
import org.slf4j.LoggerFactory

/**
 * @author zoran Aug 2, 2015
 */
object BroadcastDistributer {
  val producer:ReliableProducer=new ReliableProducerImpl
  val logger = LoggerFactory.getLogger(getClass)
  producer.setQueue(QueueNames.BROADCAST.name.toLowerCase)
  producer.startAsynchronousPublisher
  val gson=new GsonBuilder
  gson.registerTypeAdapter(classOf[MessageWrapper], new MessageWrapperAdapter)
  
  def distributeMessage(serviceType:ServiceType, parameters:java.util.Map[String,String]){
    val message:BroadcastMessage=new BroadcastMessage
    message.setServiceType(ServiceType.BROADCAST_SOCIAL_ACTIVITY)
    message.setParameters(parameters)
    val wrapper:MessageWrapper=new MessageWrapper
    val localhost = InetAddress.getLocalHost
    wrapper.setSender(localhost.getHostAddress)
    wrapper.setMessage(message)
    wrapper.setTimecreated(System.currentTimeMillis())
    val msg=gson.create.toJson(wrapper)
    logger.debug("SENDING MESSAGE:"+msg)
    producer.send(msg)
    
  }
}