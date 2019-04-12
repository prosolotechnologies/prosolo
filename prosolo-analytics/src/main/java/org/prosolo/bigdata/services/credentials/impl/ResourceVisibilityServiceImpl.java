package org.prosolo.bigdata.services.credentials.impl;/**
 * Created by zoran on 27/08/16.
 */

import java.util.Date;

import org.apache.log4j.Logger;
import org.prosolo.bigdata.jobs.CronSchedulerImpl;
import org.prosolo.bigdata.jobs.JobWrapper;
import org.prosolo.bigdata.jobs.ResourceVisibilityUpdateJob;
import org.prosolo.bigdata.jobs.data.Resource;
import org.prosolo.bigdata.services.credentials.VisibilityService;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.SchedulerException;
import org.quartz.SimpleTrigger;
import org.quartz.TriggerBuilder;

/**
 * zoran 27/08/16
 */
public class ResourceVisibilityServiceImpl implements VisibilityService {

    private static String CREDENTIALPREFIX = "publiccredential_";
    private static String COMPETENCEPREFIX = "publiccompetence_";
    private static String VISIBILITYGROUP = "visibility";

    private static Logger logger = Logger
            .getLogger(ResourceVisibilityServiceImpl.class);


    public ResourceVisibilityServiceImpl() {
        logger.info("ResourceVisibilityServiceImpl constructor init");
    }

    @Override
    public void updateVisibilityAtSpecificTime(long actorId, long resourceId, Resource resource,
                                               Date startDate) {
        String identity = getIdentity(resource, resourceId);
        JobBuilder jobBuilder = JobBuilder.newJob(ResourceVisibilityUpdateJob.class).withIdentity(identity, VISIBILITYGROUP);
        jobBuilder.storeDurably(false);
        jobBuilder.usingJobData("resourceId", resourceId);
        jobBuilder.usingJobData("resource", resource.name());
        jobBuilder.usingJobData("userId", actorId);

        JobDetail jobDetails = jobBuilder.build();
        SimpleTrigger trigger = (SimpleTrigger) TriggerBuilder.newTrigger()
                .withIdentity(identity, VISIBILITYGROUP)
                .startAt(startDate)
                .forJob(identity, VISIBILITYGROUP)
                .build();

        JobWrapper jobWrapper = JobWrapper.createJob(jobDetails, trigger, identity);
        CronSchedulerImpl.getInstance().addWrappedJob(jobWrapper);
    }

    @Override
    public void changeVisibilityUpdateTime(long resourceId, Resource resource, Date startDate) {
        String identity = getIdentity(resource, resourceId);
        CronSchedulerImpl.getInstance().rescheduleJobTrigger(identity, VISIBILITYGROUP, startDate);//replaceJobTrigger(identity, PUBLISHGROUP, newTrigger);
    }

    @Override
    public void cancelVisibilityUpdate(long resourceId, Resource res) {
        String identity = getIdentity(res, resourceId);
        CronSchedulerImpl.getInstance().removeJobTrigger(identity, VISIBILITYGROUP);//removeJobTriggerAndWrapper(identity);
    }

    @Override
    public boolean visibilityUpdateJobExists(long resourceId, Resource res) throws SchedulerException {
        String identity = getIdentity(res, resourceId);
        return CronSchedulerImpl.getInstance().isJobAlreadyRunning(identity, VISIBILITYGROUP);
    }

    private String getIdentity(Resource res, long resId) {
        return res == Resource.CREDENTIAL ? CREDENTIALPREFIX + resId :
                COMPETENCEPREFIX + resId;
    }
}
