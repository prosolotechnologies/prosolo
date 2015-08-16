package org.prosolo.bigdata.jobs;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.Trigger;

/**
 * @author Zoran Jeremic May 18, 2015
 *
 */

public class JobWrapper implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 8776194470854150898L;

	private Set<String> dependencies;
	public String jobId;
	public JobDetail jobDetail;

	public Trigger trigger;

	public String getJobId() {
		return jobId;
	}

	public void setJobId(String jobId) {
		this.jobId = jobId;
	}

	public JobDetail getJobDetail() {
		return jobDetail;
	}

	public void setJobDetail(JobDetail jobDetail) {
		this.jobDetail = jobDetail;
	}

	public Trigger getTrigger() {
		return trigger;
	}

	public void setTrigger(Trigger trigger) {
		this.trigger = trigger;
	}

	public void setDependencies(Set<String> dependencies) {
		this.dependencies = dependencies;
	}

	public String waitInterval;

	public static JobWrapper createJob(JobDetail jobDetail, String id) {
		return new JobWrapper(jobDetail, null, id);
	}

	public static JobWrapper createJob(JobDetail jobDetail, Trigger trigger,
			String id) {
		return new JobWrapper(jobDetail, trigger, id);
	}

	private JobWrapper(JobDetail jobDetail, Trigger trigger, String id) {
		super();
		this.dependencies = getDependenciesFromDataMap(jobDetail);
		this.jobDetail = jobDetail;
		this.trigger = trigger;
		this.jobId = id;

	}

	public void addToQueue(String job) {

		jobDetail.getJobDataMap().put("jobqueue_" + job, job);
	}

	public void clearQueue() {
		JobDataMap jdm = jobDetail.getJobDataMap();

		for (Iterator<Entry<String, Object>> it = jdm.entrySet().iterator(); it
				.hasNext();) {
			Entry<String, Object> cEntry = it.next();
			if (cEntry.getKey().startsWith("jobqueue_")) {
				it.remove();
			}
		}

	}

	public Set<String> getDependencies() {
		return dependencies;
	}

	private Set<String> getDependenciesFromDataMap(JobDetail jd) {
		JobDataMap jdm = jd.getJobDataMap();
		Set<String> dependencies = new HashSet<String>();

		for (Map.Entry<String, Object> e : jdm.entrySet()) {
			if (e.getKey().startsWith("jobdep")) {
				dependencies.add((String) e.getValue());
			}

		}

		return dependencies;
	}

	public Set<String> getDepQueue() {
		JobDataMap jdm = jobDetail.getJobDataMap();
		Set<String> depQueue = new HashSet<String>();

		for (Map.Entry<String, Object> e : jdm.entrySet()) {
			if (e.getKey().startsWith("jobqueue_")) {
				depQueue.add((String) e.getValue());
			}

		}

		return depQueue;
	}

	public String getWaitInterval() {
		return waitInterval;
	}

	public void setWaitInterval(String waitInterval) {
		this.waitInterval = waitInterval;
	}
}
