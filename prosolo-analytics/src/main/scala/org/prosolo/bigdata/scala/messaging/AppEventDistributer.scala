package org.prosolo.bigdata.scala.messaging

import java.net.InetAddress

import com.google.gson.GsonBuilder
import org.prosolo.common.event.{EventData, EventQueue}
import org.prosolo.common.messaging.MessageWrapperAdapter
import org.prosolo.common.messaging.data.{AppEventMessage, BroadcastMessage, MessageWrapper, ServiceType}
import org.prosolo.common.messaging.rabbitmq.impl.ReliableProducerImpl
import org.prosolo.common.messaging.rabbitmq.{QueueNames, ReliableProducer}
import org.slf4j.LoggerFactory

/**
 * @author stefanvuckovic
 */
object AppEventDistributer {
  val producer:ReliableProducer=new ReliableProducerImpl
  val logger = LoggerFactory.getLogger(getClass)
  producer.setQueue(QueueNames.APP_EVENT.name.toLowerCase)
  producer.startAsynchronousPublisher
  val gson=new GsonBuilder
  gson.registerTypeAdapter(classOf[MessageWrapper], new MessageWrapperAdapter)
  
  def distributeMessage(events: EventQueue) {
    val message:AppEventMessage = new AppEventMessage(events)
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