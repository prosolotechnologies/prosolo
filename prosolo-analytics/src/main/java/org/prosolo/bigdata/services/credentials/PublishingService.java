package org.prosolo.bigdata.services.credentials;/**
 * Created by zoran on 27/08/16.
 */

import java.util.Date;

import org.quartz.SchedulerException;

/**
 * zoran 27/08/16
 */
public interface PublishingService {
    void publishCredentialAtSpecificTime(long credentialId, Date startDate);

    void updatePublishingCredentialAtSpecificTime(long credentialId, Date startDate);

    void deletePublishingCredential(long credentialId);
    
    boolean publishJobExists(long credentialId) throws SchedulerException;
}
