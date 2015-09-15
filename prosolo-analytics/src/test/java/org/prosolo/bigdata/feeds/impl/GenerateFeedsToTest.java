package org.prosolo.bigdata.feeds.impl;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import org.apache.log4j.Logger;
import org.junit.Test;
import org.prosolo.bigdata.dal.persistence.DiggestGeneratorDAO;
import org.prosolo.bigdata.dal.persistence.impl.DiggestGeneratorDAOImpl;
import org.prosolo.common.domainmodel.feeds.FeedSource;
import org.prosolo.common.domainmodel.user.User;
import org.prosolo.common.domainmodel.user.preferences.FeedsPreferences;
import org.prosolo.common.exceptions.ResourceCouldNotBeLoadedException;
 

public class GenerateFeedsToTest {
	private static Logger logger = Logger.getLogger(GenerateFeedsToTest.class);
	@Test
	public void testData() {
		URL badwordsPath = Thread.currentThread().getContextClassLoader()
				.getResource("files/feeds.csv");
		
		List<String> sources=new ArrayList<String>();
		File badwordsFile = new File(badwordsPath.getPath());
		try {
			BufferedReader br = new BufferedReader(new FileReader(badwordsFile));
			String line;
			try {
				while ((line = br.readLine()) != null && line.length()>10) {
					System.out.println("LINE:"+line);
					String[] parts=line.split("\\s*,\\s*");
					System.out.println("PART1:"+parts[0]);
					System.out.println("PART2:"+parts[1]);
					//FeedSource fs=new FeedSource(parts[0],parts[1]);
					sources.add( parts[1]);
				}
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}

		} catch (FileNotFoundException e2) {
			logger.error(e2);
		}
		DiggestGeneratorDAO diggestGeneratorDAO=new DiggestGeneratorDAOImpl();
		//diggestGeneratorDAO.setSession(HibernateUtil.getSessionFactory().openSession());
		//Session session=diggestGeneratorDAO.openSession();
		
		//List<Long> usersIds=diggestGeneratorDAO.getAllUsersIds();
		List<Long> usersIds=new ArrayList<Long>();
		usersIds.add((long) 2);
		usersIds.add((long) 8);
		usersIds.add((long) 15);
		usersIds.add((long) 14);
		usersIds.add((long) 6);
		int sourcecounter=0;
		System.out.println("ALL users :"+usersIds.size());
		int usercounter=0;
		for(Long userid:usersIds){
			if(usercounter>2){
				break;
			}
			usercounter++;
			System.out.println("USER:"+userid);
			User user=null;
			try {
				user=diggestGeneratorDAO.load(User.class,userid);
			} catch (ResourceCouldNotBeLoadedException e) {
				e.printStackTrace();
			}
			user.setLastname(user.getLastname()+"2");
			
			FeedsPreferences fp=diggestGeneratorDAO.getFeedsPreferences(userid);
			System.out.println("SOURCES:"+sources.size());
			String url= sources.get(sourcecounter);
			System.out.println("url:"+url);
			sourcecounter++;
			if(sourcecounter>sources.size()){
				sourcecounter=0;
			}
			FeedSource pbfs=new FeedSource();
			pbfs.setLink(url);
			pbfs.setTitle(url);
			 
			diggestGeneratorDAO.save(pbfs);
			//diggestGeneratorDAO.getSession().flush();
			fp.setPersonalBlogSource(pbfs);
			fp.setSubscribedRssSources(new ArrayList<FeedSource>());
			for(int i=0;i<5;i++){
				String url2= sources.get(sourcecounter);
				FeedSource fs=new FeedSource();
				fs.setLink(url2);
				fs.setTitle(url2);
				 
				diggestGeneratorDAO.save(fs);
				sourcecounter++;
				System.out.println(" COUNTER:"+sourcecounter+" sources:"+sources.size());
				if(sourcecounter==sources.size()){
					sourcecounter=0;
					
					break;
				}
				try{
					System.out.println("ADDING FEED:"+fs.getLink()+" counter:"+sourcecounter+" size:"+sources.size());
					diggestGeneratorDAO.save(fs);
					//diggestGeneratorDAO.getSession().flush();
					fp.addSubscribedRssSource(fs);
				}catch(Exception ex){
					ex.printStackTrace();
				}
				
				
			}
			System.out.println("FEED PREFERENCE HAS FEEDS:"+fp.getSubscribedRssSources().size()+" for user:"+fp.getUser().getId());
			diggestGeneratorDAO.save(fp);
			diggestGeneratorDAO.save(user);
		}
		try {
			Thread.sleep(10000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	
		//diggestGeneratorDAO.getSession().flush();
		//session.close();
		System.out.println("GENERATE FEEDS FINISHED");
		
	}

}
