package org.prosolo.services.importing;

import java.util.Collection;

import org.hibernate.Session;
import org.prosolo.common.domainmodel.user.Email;
import org.prosolo.common.domainmodel.user.User;
import org.prosolo.common.domainmodel.user.preferences.UserPreference;
import org.prosolo.services.indexing.UserEntityESService;
import org.prosolo.services.nodes.UserManager;
import org.prosolo.web.util.AvatarUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author Zoran Jeremic 2013-07-30
 *
 */
@Transactional
@Service("org.prosolo.services.importing.DataGenerator")
public class DataGenerator {
	@Autowired UserManager userManager;
	@Autowired private UserEntityESService userEntityESService;
	public void populateDBWithTestData(){
		populateDBwithTestUsers();
	}
	public void populateDBwithTestUsers(){
		//Collection<User> users=ServiceLocator.getInstance()
		//.getService(UserManager.class).getAllUsers();
		//UserManager userManager=new UserManagerImpl();
		Collection<User> users=userManager.getAllUsers();
		for(User user:users){
			for(int i=0;i<100;i++){
				 cloneUser(user,i);
			}
		}
		
	}
	private void cloneUser(User oldUser, int id){
		User user = new User();
		user.setName(oldUser.getName()+id);
		user.setLastname(oldUser.getLastname());
		Email email=new Email();
		email.setAddress(id+"test@email.com");
		userManager.saveEntity(email);
		user.setEmail(email);
		UserPreference uPreferences=new UserPreference();
		uPreferences.setTitle("test");
		uPreferences.setUser(user);
		userManager.saveEntity(uPreferences);
		//user.addPreference(uPreferences);
		 
		user.setPassword(oldUser.getPassword());
		user.setPasswordLength(oldUser.getPassword().length());
		//Portfolio oldPortfolio=oldUser.getPortfolio();
		
		user.setAvatarUrl(AvatarUtils.getDefaultAvatarUrl());
		user.setSystem(false);
		user.setOrganization(oldUser.getOrganization());
		userManager.saveEntity(user);
		userEntityESService.saveUserNode(user,(Session) userManager.getPersistence().currentManager());
		
	}
	
}
