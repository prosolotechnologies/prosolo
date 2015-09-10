package org.prosolo.bigdata.spark;

import org.apache.log4j.Logger;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.streaming.Durations;
import org.apache.spark.streaming.StreamingContext;
import org.apache.spark.streaming.api.java.JavaStreamingContext;

/**
 * @author Zoran Jeremic May 10, 2015
 *
 */

public class SparkLauncher {
	private final static Logger logger = Logger.getLogger(SparkLauncher.class
			.getName());
	private static JavaSparkContext javaSparkContext = null;
	private static StreamingContext scalaStreamingContext = null;

	public static synchronized JavaSparkContext getSparkContext() {
		if (javaSparkContext == null) {
			try {
				System.out.println("GET SPARK JAVA CONTEXT");
				javaSparkContext = new JavaSparkContext(getSparkConf());
				logger.debug("SPARK HOME=" + javaSparkContext.getSparkHome());
			} catch (Exception ex) {
				ex.printStackTrace();
			}
			System.out.println("CREATED SPARK JAVA CONTEXT");
		}

		return javaSparkContext;
	}

	public static JavaStreamingContext getSparkStreamingContext() {
		/*
		 * if(javaSparkStreamingContext==null){ SparkConf conf=getSparkConf();
		 * javaSparkStreamingContext=new JavaStreamingContext(conf,
		 * Durations.seconds(10)); }
		 */
		return null;
	}

	public static StreamingContext getSparkScalaStreamingContext() {
		if (scalaStreamingContext == null) {
			System.out.println("GET SPARK SCALA CONTEXT");
			SparkConf conf = getSparkConf();
			scalaStreamingContext = new StreamingContext(conf,
					Durations.seconds(1));
		}
		return scalaStreamingContext;
		// return new StreamingContext(conf, Durations.seconds(10));
	}

	private static SparkConf getSparkConf() {
		System.out.println("GET SPARK CONF");
		System.setProperty("spark.executor.memory", "5g");
		Runtime runtime = Runtime.getRuntime();
		runtime.gc();
		int numOfCores = runtime.availableProcessors();
		numOfCores = 0;
		SparkConf conf = new SparkConf();
		conf.setMaster("local[" + numOfCores + "]");
		conf.set("spark.executor.memory", "4g");
		conf.setAppName("prosolo.bigdata");
		conf.set("spark.serializer",
				"org.apache.spark.serializer.KryoSerializer");
		return conf;
	}

}
