package org.prosolo.bigdata.streaming.spark;

import java.io.IOException;
import java.util.Arrays;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.function.FlatMapFunction;
import org.apache.spark.api.java.function.Function2;
import org.apache.spark.api.java.function.PairFunction;
import org.apache.spark.streaming.Duration;
import org.apache.spark.streaming.api.java.JavaDStream;
import org.apache.spark.streaming.api.java.JavaPairDStream;
import org.apache.spark.streaming.api.java.JavaStreamingContext;
import org.prosolo.bigdata.rabbitmq.ReliableProducer;
import org.prosolo.bigdata.streaming.Topic;

import scala.Tuple2;

/**
 * @author Zoran Jeremic Apr 3, 2015
 *
 */

public class SparkStreaming {
	public static JavaStreamingContext ssc;

	public static void main(String[] args) throws IOException {
		Duration batchInterval = new Duration(2000);
		//ssc = new JavaStreamingContext("local", "Rabbit", batchInterval,
		//		System.getenv("SPARK_HOME"),
		//		JavaStreamingContext.jarOfClass(SparkStreaming.class));
		SparkConf sparkConf = new SparkConf()
		.setAppName("rabbit_prosolo")
		.setMaster("local[1]");
		sparkConf.set("spark.serializer",
				"org.apache.spark.serializer.KryoSerializer");
		 ssc = new JavaStreamingContext(sparkConf,batchInterval);
 
		System.out.println("CREATED CONTEXT");
		 JavaDStream<String> openStream = ssc.receiverStream(new RMQQueueReceiver());
		//JavaDStream<String> CSR = ssc.receiverStream(new ReliableConsumer());
		System.out.println("RECEIVER STREAM");
		// here is rabbitmq stream as spark streaming
		openStream.print();
		System.out.println("PRINT");
		
		ssc.start();
		
		System.out.println("START");
		SparkStreaming ss=new SparkStreaming();
		ss.produce();
		System.out.println("COUNT:"+openStream.count());
		
		JavaDStream<String> words = openStream.flatMap(new FlatMapFunction<String, String>() {
			@Override
			public Iterable<String> call(String line) throws Exception {
				System.out.println("words called");
			return Arrays.asList(line.split(" "));
			}
			});
		System.out.println("words count:"+words.count());
		/*
		JavaPairDStream<String, Integer> wordMap = words.mapToPair(new PairFunction<String, String, Integer>() {
			@Override
			public Tuple2<String, Integer> call(String word) throws Exception {
			return new Tuple2<String, Integer>(word,1);
			}
			});
		JavaPairDStream<String, Integer> wordCountMap = wordMap.reduceByKey(
				new Function2<Integer, Integer, Integer>() {
				@Override
				public Integer call(Integer first, Integer second) throws Exception {
				return first + second;
				}
				}
				);
				wordCountMap.print();
				*/
		ssc.awaitTermination();
		
	}
	public void produce(){
		System.out.println("SENDING MESSAGES:");
		ReliableProducer rabbitMQProducer = new ReliableProducer();
		 rabbitMQProducer.setQueue(Topic.LOGS.name().toLowerCase());
		 rabbitMQProducer.startAsynchronousPublisher();
		 for( int i=0;i<10;i++){
			 String data="SOME MESSAGE "+i;
			 rabbitMQProducer.send(data);
			 try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		 }
		 
	}
}
