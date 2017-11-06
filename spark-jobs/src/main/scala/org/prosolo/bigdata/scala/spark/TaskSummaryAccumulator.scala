package org.prosolo.bigdata.scala.spark

import org.apache.spark.util.AccumulatorV2

/**
  *
  * @author Zoran Jeremic
  * @date 2017-09-06
  * @since 1.0.0
  */
class TaskSummaryAccumulator() extends AccumulatorV2[TaskSummary,TaskSummary]{
  private var _record=new TaskSummary("","",0,0)

  override def isZero: Boolean = true

  override def copy(): AccumulatorV2[TaskSummary, TaskSummary] = {
    val taskAccCopy=new TaskSummaryAccumulator
    taskAccCopy._record=this._record
    taskAccCopy
  }

  override def reset(): Unit = _record=new TaskSummary("","",0,0)

  override def add(otherTask: TaskSummary): Unit = {
    //val otherTask:TaskSumm=v
    if(otherTask.jobId.length>0){
      _record.jobId=otherTask.jobId
    }
    if(otherTask.jobName.length>0){
      _record.jobName=otherTask.jobName
    }
    if(otherTask.jobStarted>0){
      _record.jobStarted=otherTask.jobStarted
    }
    if(otherTask.jobFinished>0){
      _record.jobFinished=otherTask.jobFinished
    }

  }

  override def merge(other: AccumulatorV2[TaskSummary, TaskSummary]): Unit = {
   val otherTask=other.value
    if(otherTask.jobId.length>0){
      _record.jobId=otherTask.jobId
    }
    if(otherTask.jobName.length>0){
      _record.jobName=otherTask.jobName
    }
    if(otherTask.jobStarted>0){
      _record.jobStarted=otherTask.jobStarted
    }
    if(otherTask.jobFinished>0){
      _record.jobFinished=otherTask.jobFinished
    }

  }

  override def value: TaskSummary = _record
}
