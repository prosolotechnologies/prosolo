package org.prosolo.bigdata.jobs;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.prosolo.bigdata.config.JobsMap;
import org.prosolo.bigdata.config.QuartzJobConfig;
import org.prosolo.bigdata.config.Settings;
import org.prosolo.bigdata.scala.spark.SparkContextLoader;
import org.quartz.SchedulerException;

/**
 * @author Zoran Jeremic May 18, 2015
 *
 */

public class CronInitializer extends HttpServlet {
	private Logger logger = Logger.getLogger(this.getClass());
	/**
	 * 
	 */
	private static final long serialVersionUID = -1252377621936489130L;

	@Override
	public void init() throws ServletException {
		// @SuppressWarnings("unused")
		System.out.println("CRON INITIALIZER INIT");
		CronScheduler cronScheduler = CronSchedulerImpl.getInstance();
	 	Map<String, QuartzJobConfig> jobs=Settings.getInstance().config.schedulerConfig.jobs.jobsConfig;
		//Map<String, QuartzJobConfig> jobs=null;
		//List<QuartzJobConfig> jobsConfigs = Settings.getInstance().config.schedulerConfig.jobs.jobsConfig;
		// Set<String> jobsKeys=jobsMap.keySet();
		System.out.println("JOBS NUMBER:"+jobs.size());

		for(Map.Entry<String, QuartzJobConfig> entry: jobs.entrySet()){
			try {
				cronScheduler.checkAndActivateJob(entry.getKey(), entry.getValue());
			} catch (ClassNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (SchedulerException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		/*for (QuartzJobConfig jobConfig : jobsConfigs) {
			// QuartzJobConfig jobConfig= jobsMap.get(jobKey);
			try {
				cronScheduler.checkAndActivateJob(jobConfig);
			} catch (ClassNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (SchedulerException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}*/

	}

	@Override
	public void service(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

	}
}
