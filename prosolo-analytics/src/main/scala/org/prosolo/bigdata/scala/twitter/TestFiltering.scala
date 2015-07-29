package org.prosolo.bigdata.scala.twitter

 
/**
 * @author zoran Jul 28, 2015
 */
object TestFiltering extends App{
  println("test filtering started")
  val badWordsCensor:BadWordsCensor=new BadWordsCensor
  badWordsCensor.isPolite("Some sample ass text to check")
}