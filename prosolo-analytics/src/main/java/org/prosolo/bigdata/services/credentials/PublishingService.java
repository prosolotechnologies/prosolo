package org.prosolo.bigdata.services.credentials;/**
 * Created by zoran on 27/08/16.
 */

import java.util.Date;

/**
 * zoran 27/08/16
 */
public interface PublishingService {
    void publishCredentialAtSpecificTime(long credentialId, Date startDate);

    void updatePublishingCredentialAtSpecificTime(long credentialId, Date startDate);

    void deletePublishingCredential(long credentialId);
}
