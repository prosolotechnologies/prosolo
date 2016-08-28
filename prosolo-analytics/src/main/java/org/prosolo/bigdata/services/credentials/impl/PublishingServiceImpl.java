package org.prosolo.bigdata.services.credentials.impl;/**
 * Created by zoran on 27/08/16.
 */

import org.prosolo.bigdata.jobs.CronSchedulerImpl;
import org.prosolo.bigdata.jobs.JobWrapper;
import org.prosolo.bigdata.jobs.PublishCredentialJob;
import org.prosolo.bigdata.services.credentials.PublishingService;
import org.quartz.*;

import java.util.Date;

import static org.quartz.TriggerBuilder.newTrigger;

/**
 * zoran 27/08/16
 */
public class PublishingServiceImpl implements PublishingService {

    private static String PUBLISHCREDENTIALPREFIX="publishcredential_";
    private static String PUBLISHGROUP="publishing";

    @Override
    public void publishCredentialAtSpecificTime(long credentialId, Date startDate){
        String identity=PUBLISHCREDENTIALPREFIX+credentialId;
        JobBuilder jobBuilder=JobBuilder.newJob(PublishCredentialJob.class).withIdentity(identity,PUBLISHGROUP);
        jobBuilder.storeDurably(false);
        jobBuilder.usingJobData("credentialId",credentialId);

        JobDetail jobDetails=jobBuilder.build();
            SimpleTrigger trigger=(SimpleTrigger) TriggerBuilder.newTrigger()
                .withIdentity(identity,PUBLISHGROUP)
                .startAt(startDate)
                .forJob(identity,PUBLISHGROUP)
                .build();

        JobWrapper jobWrapper= JobWrapper.createJob(jobDetails,trigger,identity);
        CronSchedulerImpl.getInstance().addWrappedJob(jobWrapper);
    }

    @Override
    public void updatePublishingCredentialAtSpecificTime(long credentialId, Date startDate){
        String identity=PUBLISHCREDENTIALPREFIX+credentialId;
        CronSchedulerImpl.getInstance().rescheduleJobTrigger(identity,PUBLISHGROUP,startDate);//replaceJobTrigger(identity, PUBLISHGROUP, newTrigger);

    }
    @Override
    public void deletePublishingCredential(long credentialId){
        String identity=PUBLISHCREDENTIALPREFIX+credentialId;
        CronSchedulerImpl.getInstance().removeJobTrigger(identity,PUBLISHGROUP);//removeJobTriggerAndWrapper(identity);
    }
}
