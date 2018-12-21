package org.prosolo.core.stress;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import org.hibernate.ObjectNotFoundException;
import org.hibernate.Session;
import org.junit.Ignore;
import org.junit.Test;
import org.prosolo.common.domainmodel.annotation.Tag;
import org.prosolo.common.domainmodel.user.User;
import org.prosolo.common.domainmodel.user.oauth.OauthAccessToken;
import org.prosolo.common.domainmodel.user.preferences.TopicPreference;
import org.prosolo.common.domainmodel.user.socialNetworks.ServiceType;
import org.prosolo.common.exceptions.ResourceCouldNotBeLoadedException;
import org.prosolo.services.annotation.TagManager;
import org.prosolo.services.user.UserManager;
import org.prosolo.util.nodes.AnnotationUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

/**
 * 
 * @author Zoran Jeremic, Jun 5, 2014
 * 
 */
public class TestHashTagsGenerator extends TestContext {
	@Autowired private TagManager tagManager;
	//@Autowired private TwitterStreamsManager twitterStreamsManager;
	@Autowired private UserManager userManager;

	@Test
	public void addRandomTwitterAccountTest(){
		Collection<User> allUsers=userManager.getAllUsers(0);
		long initial=295349086;
		Random generator = new Random();
		for(int i=0;i<100;i++){
		for(User user:allUsers){
			long tId=initial;
			if(user.getId()>3){
				int add=generator.nextInt(100000000);
				tId=initial-add;
			} 
			
				
				
				OauthAccessToken token=new OauthAccessToken();
				token.setProfileLink("https://twitter.com/unknown");
				token.setProfileName("unknown");
				token.setService(ServiceType.TWITTER);
				token.setToken("295349086-yPqvn88kiSI4hvbwoyugP01CTCi3ufuuWCUGoQcW");
				token.setTokenSecret("GuQvyg55VcSXWw3768SOPmomIOOGURSeRvZDOHn4");
				token.setUserId(tId);
				token.setUser(user);
				userManager.saveEntity(token);
			
		 
			
		}
		}
	}
	@Ignore
	@Test
	public void addRandomHashTagsForUsersTest() {
		int numbOfUsersToAdd = 0;
		
		try {
			BufferedReader br = new BufferedReader(new InputStreamReader(System.in,"UTF-8"));
		 	System.out.println("How many users you want to assign hashtags to:");
			int numberOfHashTags = 0;
			 numbOfUsersToAdd = Integer.parseInt(br.readLine());
			//numbOfUsersToAdd = Integer.parseInt(System.console().readLine());
			System.out.println("Do you want to randomly assign hashtags to user profile (Y/N):");
			String randchoice = br.readLine();
			if (randchoice.toLowerCase().equals("y")) {
				System.out
						.println("How many hashtags per user you want to assign:");
				numberOfHashTags = Integer.parseInt(br.readLine());
			}
			int numbOfUsers = this.getNumberOfUsers();
			Random generator = new Random();
			InputStream is = Thread
					.currentThread()
					.getContextClassLoader()
					.getResourceAsStream(
							"org/prosolo/test/data/keywordslist.csv");
			BufferedReader ir = new BufferedReader(new InputStreamReader(is));
			String line;
			List<String> tags = new ArrayList<String>();
			while ((line = ir.readLine()) != null) {
				tags.add(line);
			}
			ArrayList<Long> userIds=this.getAllUsersIds();
		 	for (int i = 0; i < numbOfUsersToAdd; i++) {
				int userInd = generator.nextInt(numbOfUsers - 2) + 2;
				long userId=userIds.get(userInd);
				User user = null;
				try{
				 user = userManager.loadResource(User.class, userId);
				}catch(ObjectNotFoundException ex){
					System.out.println("User not found:"+userId);
				}
				List<String> userTags = new ArrayList<String>();
				if (randchoice.toLowerCase().equals("y") && numberOfHashTags > 0) {
					for (int h = 0; h < numberOfHashTags; h++) {
						int tagInd = generator.nextInt(tags.size());
						String tag = tags.get(tagInd);
						System.out.print("..."+tag);
						if (!userTags.contains(tag)) {
							userTags.add(tag);
						}  

					}
				} else {
					System.out.println("Add hashtags as a comma separated list for user:"+user.getName()+" "+user.getLastname());
					String tagsList= br.readLine();
					userTags=AnnotationUtil.getTrimmedSplitStrings(tagsList);
					for(String t:userTags){
						System.out.println("added:"+t);
					}
				}
				Set<Tag> hashTagList = tagManager.getOrCreateTags(userTags);
				TopicPreference topicPreference = (TopicPreference) userManager.getUserPreferences(user, TopicPreference.class);
				Set<Tag> oldHashTags = new HashSet<Tag>();
				oldHashTags.addAll(topicPreference.getPreferredHashtags());
				topicPreference.setPreferredHashtags(new HashSet<Tag>(hashTagList));
				tagManager.saveEntity(topicPreference);
				System.out.println(" UPDATING USER:" + user.getEmail() + " - " + i);

			}
		} catch (NumberFormatException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (ResourceCouldNotBeLoadedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
	@Ignore
	@Test
	public void addRandomHashTagsForLearningGoalsTest() {
		int numbOfLearningGoalsToAdd = 0;
		
		try {
			BufferedReader br = new BufferedReader(new InputStreamReader(System.in,"UTF-8"));
		 	System.out.println("How many learning goals you want to assign hashtags to:");
			int numberOfHashTags = 0;
			 numbOfLearningGoalsToAdd = Integer.parseInt(br.readLine());
			//numbOfUsersToAdd = Integer.parseInt(System.console().readLine());
			System.out.println("Do you want to randomly assign hashtags to user profile (Y/N):");
			String randchoice = br.readLine();
			if (randchoice.toLowerCase().equals("y")) {
				System.out
						.println("How many hashtags per learning goal you want to assign:");
				numberOfHashTags = Integer.parseInt(br.readLine());
			}
			int numbOfLearningGoals = this.getNumberOfLearningGoals();
			Random generator = new Random();
			InputStream is = Thread
					.currentThread()
					.getContextClassLoader()
					.getResourceAsStream(
							"org/prosolo/test/data/keywordslist.csv");
			BufferedReader ir = new BufferedReader(new InputStreamReader(is));
			String line;
			List<String> tags = new ArrayList<String>();
			
			while ((line = ir.readLine()) != null) {
				tags.add(line);
			}
			
			ArrayList<Long> lGoalsIds = this.getAllLearningGoalsIds();
			
//			for (int i = 0; i < numbOfLearningGoalsToAdd; i++) {
//				int lGoalInd = generator.nextInt(numbOfLearningGoals - 2) + 2;
//				long lGoalId = lGoalsIds.get(lGoalInd);
//				LearningGoal lGoal = null;
//				try {
//					lGoal = userManager.loadResource(LearningGoal.class, lGoalId);
//				} catch (ObjectNotFoundException ex) {
//					System.out.println("Learning Goal not found:" + lGoalId);
//				}
//				List<String> lGoalTags = new ArrayList<String>();
//				if (randchoice.toLowerCase().equals("y") && numberOfHashTags > 0) {
//					for (int h = 0; h < numberOfHashTags; h++) {
//						int tagInd = generator.nextInt(tags.size());
//						String tag = tags.get(tagInd);
//						System.out.print("..." + tag);
//						if (!lGoalTags.contains(tag)) {
//							lGoalTags.add(tag);
//						}
//						
//					}
//				} else {
//					System.out.println("Add hashtags as a comma separated list for learning goal:" + lGoal.getTitle());
//					String tagsList = br.readLine();
//					lGoalTags = AnnotationUtil.getTrimmedSplitStrings(tagsList);
//					for (String t : lGoalTags) {
//						System.out.println("added:" + t);
//					}
//				}
//				Set<Tag> hashTagList = tagManager.getOrCreateTags(lGoalTags);
//				lGoal.setHashtags(hashTagList);
//				tagManager.saveEntity(lGoal);
//				
//				// twitterStreamsManager.updateHashTagsForUserAndRestartStream(oldHashTags,
//				// topicPreference.getPreferredHashTags(), user.getId());
//
//			}
		} catch (NumberFormatException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
//		} catch (ResourceCouldNotBeLoadedException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
		}

	}

	public void updateHashTagsActionForUser(User user) {
		Collection<String> hashTagsToAdd = new ArrayList<String>();
		Set<Tag> hashTagList = tagManager
				.getOrCreateTags(hashTagsToAdd);
		TopicPreference topicPreference = (TopicPreference) userManager
				.getUserPreferences(user, TopicPreference.class);
		Set<Tag> oldHashTags = new HashSet<Tag>();
		oldHashTags.addAll(topicPreference.getPreferredHashtags());
		topicPreference.setPreferredHashtags(new HashSet<Tag>(
				hashTagList));
		tagManager.saveEntity(topicPreference);
		System.out.println("UPDATING USER:" + user.getEmail());
		//twitterStreamsManager.updateHashTagsForUserAndRestartStream(
			//	oldHashTags, topicPreference.getPreferredHashtags(),
			//	user.getId());

	}

	@Transactional(readOnly = true)
	private int getNumberOfUsers() {
		Session session = (Session) userManager.getPersistence().openSession();
		String query = "SELECT DISTINCT cast(COUNT(user) as int) "
				+ "FROM User user " + "WHERE user.deleted = :deleted ";

		int number = (Integer) session.createQuery(query)
				.setBoolean("deleted", false).uniqueResult();
		System.out.println("Have users:" + number);
		return number;
	}
	@Transactional(readOnly = true)
	private int getNumberOfLearningGoals() {
		Session session = (Session) userManager.getPersistence().openSession();
		String query = "SELECT DISTINCT cast(COUNT(learningGoal) as int) "
				+ "FROM LearningGoal learningGoal ";

		int number = (Integer) session.createQuery(query)
				.uniqueResult();
		System.out.println("Have learning goals:" + number);
		return number;
	}
	@Transactional (readOnly = true)
	public ArrayList<Long> getAllLearningGoalsIds() {
		Session session = (Session) userManager.getPersistence().openSession();
		
		String query = 
			"SELECT lGoal.id " +
			"FROM LearningGoal lGoal " ;
		
		@SuppressWarnings("unchecked")
		ArrayList<Long> result = (ArrayList<Long>) session.createQuery(query).
				list();
		
		if (result != null) {
  			return result;
		}

		return new ArrayList<Long>();
	}
	@Transactional (readOnly = true)
	public ArrayList<Long> getAllUsersIds() {
		Session session = (Session) userManager.getPersistence().openSession();
		
		String query = 
			"SELECT user.id " +
			"FROM User user " +
			"WHERE user.deleted = :deleted ";
		
		@SuppressWarnings("unchecked")
		ArrayList<Long> result = (ArrayList<Long>) session.createQuery(query).
				setBoolean("deleted", false).
				list();
		
		if (result != null) {
  			return result;
		}

		return new ArrayList<Long>();
	}

}
