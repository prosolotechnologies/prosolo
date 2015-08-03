package org.prosolo.bigdata.scala.messaging

import org.prosolo.common.messaging.MessageWrapperAdapter
import org.prosolo.common.messaging.rabbitmq.{ReliableProducer,QueueNames}
import org.prosolo.common.messaging.rabbitmq.impl.ReliableProducerImpl
import org.prosolo.common.messaging.data.{ServiceType,BroadcastMessage,MessageWrapper}
import java.net.InetAddress
import com.google.gson.GsonBuilder

/**
 * @author zoran Aug 2, 2015
 */
object BroadcastDistributer {
  val producer:ReliableProducer=new ReliableProducerImpl
  producer.setQueue(QueueNames.BROADCAST.name.toLowerCase)
  producer.startAsynchronousPublisher
  val gson=new GsonBuilder
  gson.registerTypeAdapter(classOf[MessageWrapper], new MessageWrapperAdapter)
  
  def distributeMessage(serviceType:ServiceType, parameters:java.util.Map[String,String]){
    val message:BroadcastMessage=new BroadcastMessage
    message.setParameters(parameters)
    val wrapper:MessageWrapper=new MessageWrapper
    val localhost = InetAddress.getLocalHost
    wrapper.setSender(localhost.getHostAddress)
    wrapper.setMessage(message)
    wrapper.setTimecreated(System.currentTimeMillis())
    val msg=gson.create.toJson(wrapper)
    producer.send(msg)
    
  }
}