package org.prosolo.core.stress;

import java.io.BufferedReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Collection;
import java.util.UUID;

import org.hibernate.Session;
import org.junit.Test;
import org.prosolo.common.domainmodel.user.User;
import org.prosolo.common.domainmodel.user.UserType;
import org.prosolo.services.indexing.UserEntityESService;
import org.prosolo.services.nodes.ResourceFactory;
import org.prosolo.services.nodes.RoleManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author Zoran Jeremic 2013-07-30
 *
 */
 

public class TestDataGenerator extends TestContext{
	//@Autowired private UserManager userManager;
	@Autowired private ResourceFactory resourceFactory;
	@Autowired private UserEntityESService userEntityESService;
	@Autowired private RoleManager roleManager;
 
	@Test
	public void createTestUsersTest(){
		int choice=0;
		System.out.println("Do you want to export users to CSV file (Y/N):");
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		FileWriter writer=null;
		try {
			String csvchoice=br.readLine();
			if(csvchoice.toLowerCase().equals("y")){
				System.out.println("Enter path for the file:");
				String path=br.readLine();
				writer=new FileWriter(path);
			} 
		} catch (IOException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}
		while(choice!=2){
		System.out.println("Choose your option to continue:");
		System.out.println("(1) Create new test users");
		System.out.println("(2) Exit");
		
		
		try {
			choice=Integer.parseInt(br.readLine());
			if(choice==1){
				populateDBwithUsersTest(writer);
			}
		} catch (NumberFormatException e1) {
			// TODO Auto-generated catch block
			System.out.println("Not appropriate format. Should be number.");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}  
		}
		try {
			if(writer!=null){
				writer.close();
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	@Transactional(readOnly=false, propagation=Propagation.REQUIRED)
	private void populateDBwithUsersTest(FileWriter writer){
		PasswordEncoder passwordEncrypter = new BCryptPasswordEncoder();
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		Session session=(Session) userManager.getPersistence().openSession();
		int numberOfUsers=0;
		System.out.println("Enter number of users:");
		try {
			numberOfUsers=Integer.parseInt(br.readLine());
		} catch (NumberFormatException e1) {
			System.out.println("Not appropriate format. Should be number.");
		} catch (IOException e) {
			e.printStackTrace();
		}  
		System.out.println("Number of users to generate:"+numberOfUsers);
		System.out.println("Enter name pattern:");
		String namePattern=null;
		try {
			 namePattern=br.readLine();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		System.out.println("Name pattern:"+namePattern);
	 for(int i=1;i<numberOfUsers;i++){
		 try{
		String name=namePattern+i;
		String lastname=name;
		String emailAddress=name+"@gmail.com";
		String password="pass";
		String position=namePattern;
		 
	 		User user = new User();
			user.setName(name);
			user.setLastname(lastname);
			user.setEmail(emailAddress);
			user.setVerified(true);
			user.setVerificationKey(UUID.randomUUID().toString().replace("-", ""));
 			user.setPassword(passwordEncrypter.encode(password));
			user.setPasswordLength(password.length());
			user.setSystem(false);
			user.setPosition(position);
				
			user.setUserType(UserType.REGULAR_USER);
			user.addRole(roleManager.getRoleByName("User"));
			user = userManager.saveEntity(user);
//			RecommendationPreferences recPref = new RecommendationPreferences();
//			recPref.addUserPriority(createUserDefinedPriority(UserPriorityType.TOPIC_PRIORITY));
//			recPref.addUserPriority(createUserDefinedPriority(UserPriorityType.LEARNING_GOAL_PRIORITY));
//			recPref.addUserPriority(createUserDefinedPriority(UserPriorityType.LEARNING_HISTORY_PRIORITY));
//			recPref.setUser(user);
//			recPref = userManager.saveEntity(recPref);
//			FeedsPreferences feedsPreferences = new FeedsPreferences();
//			feedsPreferences.setUser(user);
//			feedsPreferences = userManager.saveEntity(feedsPreferences);
//			TopicPreference tPref = new TopicPreference();
//			tPref.setUser(user);
//			tPref = userManager.saveEntity(tPref);
			userManager.flush();
			userEntityESService.saveUserNode(user,session);
			if(writer!=null){
				writer.append(emailAddress);
				writer.append(",");
				writer.append(password);
				writer.append("\n");
			}
			}catch(Exception e){
				e.getStackTrace();
			}
	 		}
			try {
				if(writer!=null){
					writer.flush();
				}
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
 
	}
//	private UserDefinedPriority createUserDefinedPriority(UserPriorityType topicPriority) {
//		UserDefinedPriority priority = new UserDefinedPriority();
//		priority.setPriorityType(topicPriority);
//		priority.setPriorityScale(scaleManager.getScale0To5());
//		priority.setPriorityLevel(1);
//		return userManager.saveEntity(priority);
//	}
	@Test
	public  void listAllUsers(){
		Collection<User> users=userManager.getAllUsers(0);
		for(User user:users){
			System.out.println("USER:"+user.getId()+" "+user.getName()+" "+user.getLastname()+" "+user.getPassword());
		}
	}
 
	
}
