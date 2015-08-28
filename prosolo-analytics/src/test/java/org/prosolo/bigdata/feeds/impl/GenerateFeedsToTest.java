package org.prosolo.bigdata.feeds.impl;

import static org.junit.Assert.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.junit.Test;
import org.prosolo.bigdata.dal.persistence.DiggestGeneratorDAO;
import org.prosolo.bigdata.dal.persistence.HibernateUtil;
import org.prosolo.bigdata.dal.persistence.impl.DiggestGeneratorDAOImpl;
import org.prosolo.common.domainmodel.feeds.FeedSource;
import org.prosolo.common.domainmodel.user.preferences.FeedsPreferences;
 

public class GenerateFeedsToTest {
	private static Logger logger = Logger.getLogger(GenerateFeedsToTest.class);
	@Test
	public void testData() {
		URL badwordsPath = Thread.currentThread().getContextClassLoader()
				.getResource("files/feeds.csv");
		List<FeedSource> sources=new ArrayList<FeedSource>();
		File badwordsFile = new File(badwordsPath.getPath());
		try {
			BufferedReader br = new BufferedReader(new FileReader(badwordsFile));
			String line;
			try {
				while ((line = br.readLine()) != null) {
					System.out.println("LINE:"+line);
					String[] parts=line.split("\\s*,\\s*");
					System.out.println("PART1:"+parts[0]);
					System.out.println("PART2:"+parts[1]);
					FeedSource fs=new FeedSource(parts[0],parts[1]);
					sources.add(fs);
				}
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}

		} catch (FileNotFoundException e2) {
			logger.error(e2);
		}
		DiggestGeneratorDAO diggestGeneratorDAO=new DiggestGeneratorDAOImpl();
		diggestGeneratorDAO.setSession(HibernateUtil.getSessionFactory().openSession());
		//Session session=diggestGeneratorDAO.openSession();
		List<Long> usersIds=diggestGeneratorDAO.getAllUsersIds();
		int sourcecounter=0;
		for(Long userid:usersIds){
			System.out.println("USER:"+userid);
			FeedsPreferences fp=diggestGeneratorDAO.getFeedsPreferences(userid);
			FeedSource pbfs=sources.get(sourcecounter);
			sourcecounter++;
			if(sourcecounter>sources.size()){
				sourcecounter=0;
			}
			diggestGeneratorDAO.save(pbfs);
			fp.setPersonalBlogSource(pbfs);
			for(int i=0;i<5;i++){
				FeedSource fs=sources.get(sourcecounter);
				sourcecounter++;
				if(sourcecounter>sources.size()){
					sourcecounter=0;
				}
				try{
					diggestGeneratorDAO.save(fs);
				}catch(Exception ex){
					ex.printStackTrace();
				}
				fp.addSubscribedRssSource(fs);
			}
			diggestGeneratorDAO.save(fp);
		}
		//session.close();
		System.out.println("GENERATE FEEDS FINISHED");
		
	}

}
