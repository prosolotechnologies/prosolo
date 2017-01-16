package org.prosolo.bigdata.jobs;/**
 * Created by zoran on 27/08/16.
 */

import org.apache.log4j.Logger;
import org.prosolo.bigdata.dal.persistence.impl.CompetenceDAOImpl;
import org.prosolo.bigdata.dal.persistence.impl.CourseDAOImpl;
import org.prosolo.bigdata.jobs.data.Resource;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

/**
 * zoran 27/08/16
 */
public class ResourceVisibilityUpdateJob  implements Job {
    private static Logger logger = Logger
            .getLogger(ResourceVisibilityUpdateJob.class.getName());
    
    private CourseDAOImpl courseDao = new CourseDAOImpl();
    private CompetenceDAOImpl compDao = new CompetenceDAOImpl();
    
    
    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
    	try {
	        JobDataMap dataMap = context.getJobDetail().getJobDataMap();
	        Resource resource = Resource.valueOf(dataMap.getString("resource"));
	        Long resourceId=dataMap.getLong("resourceId");
	        Long userId = dataMap.getLong("userId");
	        if(resource == Resource.CREDENTIAL) {
	        	courseDao.changeVisibilityForCredential(resourceId, userId);
	        } else if(resource == Resource.COMPETENCE) {
	        	compDao.changeVisibilityForCompetence(resourceId);
	        }
	        
	        logger.info("PUBLISHING " + resource.name() + " ID:" + resourceId);
    	} catch(Exception e) {
    		e.printStackTrace();
    		logger.error(e);
    	}
    }
	
}
