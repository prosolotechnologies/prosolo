package org.prosolo.bigdata.twitter;

 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.UnsupportedEncodingException;
 import java.net.URL;
 import java.net.URLEncoder;
 import java.security.SignatureException;
 import java.util.Iterator;
 import java.util.SortedMap;
 import java.util.TreeMap;
 import javax.crypto.Mac;
 import javax.crypto.spec.SecretKeySpec;

 import org.apache.commons.codec.binary.Base64;
 import org.apache.http.Header;
 import org.apache.http.HeaderElement;
 import org.apache.http.HttpEntity;
 import org.apache.http.HttpHost;
 import org.apache.http.HttpResponse;
 import org.apache.http.client.methods.HttpPost;
 import org.apache.http.impl.client.DefaultHttpClient;
 import twitter4j.FilterQuery;
 import twitter4j.TwitterStream;
 import twitter4j.TwitterStreamFactory;
 import twitter4j.conf.ConfigurationBuilder;
 import twitter4j.FilterQuery;
 import twitter4j.StallWarning;
 import twitter4j.Status;
 import twitter4j.StatusDeletionNotice;
 import twitter4j.StatusListener;
 import twitter4j.TwitterStream;
/** Created by zoran on 18/12/16.
 */

/**
 * zoran 18/12/16
 * */
public class TwitterTestLauncher {
    public static void main(String[] args) {

            ConfigurationBuilder cb = new ConfigurationBuilder();
            cb.setDebugEnabled(true);
            cb.setOAuthConsumerKey("YG2TL5Ih9BKS6NMpo0cEmCZUu");
            cb.setOAuthConsumerSecret("3hxRHUieDDjbB5cDkSGBAj9Wh46ZtMtKp17pF5Qhgvw5M5H0Q1");
            cb.setOAuthAccessToken("295349086-1si3jZGyT78fTh6A5ekWURFJMqjMLCJow6TCjbaD");
            cb.setOAuthAccessTokenSecret("APyIs269NuhtjkYvtAx9p3UdhWuSL356MAjLgHLwPwDb2");

            TwitterStream stream= new TwitterStreamFactory(cb.build()).getInstance();
        StatusListener listener = new StatusListener() {

            @Override
            public void onException(Exception e) {
                System.out.println("Exception occured:" + e.getMessage());
                e.printStackTrace();
            }

            @Override
            public void onTrackLimitationNotice(int n) {
                System.out.println("Track limitation notice for " + n);
            }

            @Override
            public void onStatus(Status status) {
                System.out.println("Got twit:" + status.getText());
              //  TwitterStreamBean bean = new TwitterStreamBean();
                String username = status.getUser().getScreenName();
              //  bean.setUsername(username);
                long tweetId = status.getId();
               // bean.setId(tweetId);
               // bean.setInReplyUserName(status.getInReplyToScreenName());
               // if (status != null && status.getRetweetedStatus() != null
                //        && status.getRetweetedStatus().getUser() != null) {
                //    bean.setRetwitUserName(status.getRetweetedStatus()
               //             .getUser().getScreenName());
              //  }
               // String content = status.getText();
              //  bean.setContent(content);
            }

            @Override
            public void onStallWarning(StallWarning arg0) {
                System.out.println("Stall warning");
            }

            @Override
            public void onScrubGeo(long arg0, long arg1) {
                System.out.println("Scrub geo with:" + arg0 + ":" + arg1);
            }

            @Override
            public void onDeletionNotice(StatusDeletionNotice arg0) {
                System.out.println("Status deletion notice");
            }
        };

        FilterQuery qry = new FilterQuery();
        String[] keywords = { "google","movie","music" };

        qry.track(keywords);

        stream.addListener(listener);
        stream.filter(qry);
        try {
            Thread.sleep(100000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }


    }


