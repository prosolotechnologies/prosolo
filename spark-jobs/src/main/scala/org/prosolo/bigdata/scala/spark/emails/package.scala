package org.prosolo.bigdata.scala.spark

package emails {



  import scala.collection.immutable.HashMap

  case class Notification(date:Long,notificationType:String, id:Long, actorFullName:String, actorId:Long, email:String, link:String, objectTitle:String,
                          objectType:String, receiverFullName:String, receiverId:Long,
                          objectId: Long, targetId: Long, targetTitle:String,
                          section: String, relationToTarget: String, predicate:String) extends Serializable
  case class NotificationsSummary(receiver:Long, total:Int, notificationTypesCounts:HashMap[String,Int], notificationsByType:HashMap[String,Array[Notification]]) extends EmailSummary with Serializable
  case class Receiver(receiver:Long, fullname:String, email:String)
  case class NotificationReceiverSummary(receiver:Receiver, summary:NotificationsSummary, role: NotificationSections.NotificationSection)extends EmailSummary with Serializable
  case class EmailSuccess(email:String, template:String, subject:String, success:Boolean)
  object NotificationSections {
    sealed abstract class NotificationSection (val name: String)
    case object STUDENT extends NotificationSection ("Student")
    case object MANAGE extends NotificationSection ("Manage")
  }
}
