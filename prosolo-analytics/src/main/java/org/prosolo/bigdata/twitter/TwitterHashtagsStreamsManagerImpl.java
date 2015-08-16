package org.prosolo.bigdata.twitter;

import java.util.List;
import java.util.Queue;

import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.function.Function2;
import org.apache.spark.storage.StorageLevel;
import org.apache.spark.streaming.Time;
import org.apache.spark.streaming.api.java.JavaDStream;
import org.apache.spark.streaming.api.java.JavaStreamingContext;
//import org.apache.spark.streaming.twitter.TwitterReceiver;
//import org.apache.spark.streaming.twitter.TwitterUtils;
import org.prosolo.bigdata.dal.persistence.impl.TwitterStreamingDAOImpl;
import org.prosolo.bigdata.spark.SparkLauncher;
import org.prosolo.common.twitter.PropertiesFacade;
import org.prosolo.common.twitter.TwitterSiteProperties;

import com.twitter.chill.Base64.InputStream;

import twitter4j.FilterQuery;
import twitter4j.HashtagEntity;
import twitter4j.Status;
import twitter4j.auth.OAuthAuthorization;
import twitter4j.conf.ConfigurationBuilder;

/**
 * @author Zoran Jeremic Jun 20, 2015
 *
 */

public class TwitterHashtagsStreamsManagerImpl {

	private Queue<TwitterSiteProperties> twitterProperties;

	public void initialize() {
		PropertiesFacade propFacade = new PropertiesFacade();
		twitterProperties = propFacade.getAllTwitterSiteProperties();
		String[] filters = { "#prosolotest1" };
		TwitterStreamingDAOImpl twitterDAO = new TwitterStreamingDAOImpl();
		twitterDAO.readAllHashtagsAndLearningGoalsIds();
		initializeNewStream(filters);
	}

	private ConfigurationBuilder getTwitterConfigurationBuilder() {
		ConfigurationBuilder builder = new ConfigurationBuilder();
		TwitterSiteProperties siteProperties = twitterProperties.poll();
		builder.setOAuthAccessToken(siteProperties.getAccessToken());
		builder.setOAuthAccessTokenSecret(siteProperties.getAccessTokenSecret());
		builder.setOAuthConsumerKey(siteProperties.getConsumerKey());
		builder.setOAuthConsumerSecret(siteProperties.getConsumerSecret());
		return builder;
	}

	private void initializeNewStream(String[] filters) {
		JavaStreamingContext ctx = SparkLauncher.getSparkStreamingContext();
		ConfigurationBuilder builder = this.getTwitterConfigurationBuilder();

		JavaDStream<Status> inputDStream = null;// TwitterUtils.createStream(ctx,
												// new
												// OAuthAuthorization(builder.build()),
												// filters);

		inputDStream.foreach(new Function2<JavaRDD<Status>, Time, Void>() {
			@Override
			public Void call(JavaRDD<Status> status, Time time)
					throws Exception {
				// System.out.println("Status count:"+status.count()+" time:"+time.toString());

				List<Status> statuses = status.collect();
				for (Status st : statuses) {
					System.out.println("STATUS:" + st.getText() + " user:"
							+ st.getUser().getId());
					System.out.println("filters[i]=" + st.getUser().getId()
							+ ";");
					HashtagEntity[] hte = st.getHashtagEntities();
					for (int i = 0; i < hte.length; i++) {
						HashtagEntity ht = hte[i];
						// System.out.println("HASHTAGS:"+ht.getText());
					}

				}
				return null;
			}
		});
		ctx.start();
		ctx.awaitTermination();

	}
}
