package org.prosolo.bigdata.services.credentials;/**
 * Created by zoran on 27/08/16.
 */

import org.junit.Test;
import org.prosolo.bigdata.jobs.data.Resource;
import org.prosolo.bigdata.services.credentials.impl.ResourceVisibilityServiceImpl;

import java.util.Calendar;
import java.util.Date;

/**
 * zoran 27/08/16
 */
public class PublishingServiceTest {
    int credentialId=119;
    @Test
    public void testCredentialPublishing(){

        VisibilityService service=new ResourceVisibilityServiceImpl();
       long ONE_MINUTE_IN_MILLIS=60000;//millisecs

        Calendar date = Calendar.getInstance();
        long t= date.getTimeInMillis();

        //publish time in a few minutes
        Date publishingDate=new Date(t + (1 * ONE_MINUTE_IN_MILLIS));
        service.updateVisibilityAtSpecificTime(credentialId, Resource.CREDENTIAL, publishingDate);

    }
    @Test
    public void testCredentialPublishingUpdateOnly(){

        VisibilityService service=new ResourceVisibilityServiceImpl();
        long ONE_MINUTE_IN_MILLIS=60000;//millisecs

        Calendar date = Calendar.getInstance();
        long t= date.getTimeInMillis();


        Date newPublishingDate=new Date(t + (2 * ONE_MINUTE_IN_MILLIS));
        service.changeVisibilityUpdateTime(credentialId, Resource.CREDENTIAL, newPublishingDate);
    }
    @Test
    public void testCredentialPublishingUpdate(){

        VisibilityService service=new ResourceVisibilityServiceImpl();
        long ONE_MINUTE_IN_MILLIS=60000;//millisecs

        Calendar date = Calendar.getInstance();
        long t= date.getTimeInMillis();

        //publish time in a few minutes
        Date publishingDate=new Date(t + (1 * ONE_MINUTE_IN_MILLIS));
        System.out.println("PUBLISH DATE:"+publishingDate.toString()+" ms:"+publishingDate.getTime());
        service.updateVisibilityAtSpecificTime(credentialId, Resource.CREDENTIAL, publishingDate);

        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        Date newPublishingDate=new Date(t + (2 * ONE_MINUTE_IN_MILLIS));
        System.out.println("UPDATE DATE:"+newPublishingDate.toString()+" ms:"+newPublishingDate.getTime());
        service.changeVisibilityUpdateTime(credentialId, Resource.CREDENTIAL, newPublishingDate);
    }
    @Test
    public void testCredentialPublishingDelete(){

        VisibilityService service=new ResourceVisibilityServiceImpl();
        long ONE_MINUTE_IN_MILLIS=60000;//millisecs

        Calendar date = Calendar.getInstance();
        long t= date.getTimeInMillis();
        //publish time in a few minutes
         service.cancelVisibilityUpdate(credentialId, Resource.CREDENTIAL);





    }
}
