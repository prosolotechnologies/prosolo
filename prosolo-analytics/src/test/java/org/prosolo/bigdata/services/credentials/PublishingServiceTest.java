package org.prosolo.bigdata.services.credentials;/**
 * Created by zoran on 27/08/16.
 */

import org.junit.Test;
import org.prosolo.bigdata.services.credentials.impl.PublishingServiceImpl;

import java.util.Calendar;
import java.util.Date;

/**
 * zoran 27/08/16
 */
public class PublishingServiceTest {
    int credentialId=119;
    @Test
    public void testCredentialPublishing(){

        PublishingService service=new PublishingServiceImpl();
       long ONE_MINUTE_IN_MILLIS=60000;//millisecs

        Calendar date = Calendar.getInstance();
        long t= date.getTimeInMillis();

        //publish time in a few minutes
        Date publishingDate=new Date(t + (1 * ONE_MINUTE_IN_MILLIS));
        service.publishCredentialAtSpecificTime(credentialId,publishingDate);

    }
    @Test
    public void testCredentialPublishingUpdateOnly(){

        PublishingService service=new PublishingServiceImpl();
        long ONE_MINUTE_IN_MILLIS=60000;//millisecs

        Calendar date = Calendar.getInstance();
        long t= date.getTimeInMillis();


        Date newPublishingDate=new Date(t + (2 * ONE_MINUTE_IN_MILLIS));
        service.updatePublishingCredentialAtSpecificTime(credentialId,newPublishingDate);
    }
    @Test
    public void testCredentialPublishingUpdate(){

        PublishingService service=new PublishingServiceImpl();
        long ONE_MINUTE_IN_MILLIS=60000;//millisecs

        Calendar date = Calendar.getInstance();
        long t= date.getTimeInMillis();

        //publish time in a few minutes
        Date publishingDate=new Date(t + (1 * ONE_MINUTE_IN_MILLIS));
        System.out.println("PUBLISH DATE:"+publishingDate.toString()+" ms:"+publishingDate.getTime());
        service.publishCredentialAtSpecificTime(credentialId,publishingDate);

        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        Date newPublishingDate=new Date(t + (2 * ONE_MINUTE_IN_MILLIS));
        System.out.println("UPDATE DATE:"+newPublishingDate.toString()+" ms:"+newPublishingDate.getTime());
        service.updatePublishingCredentialAtSpecificTime(credentialId,newPublishingDate);
    }
    @Test
    public void testCredentialPublishingDelete(){

        PublishingService service=new PublishingServiceImpl();
        long ONE_MINUTE_IN_MILLIS=60000;//millisecs

        Calendar date = Calendar.getInstance();
        long t= date.getTimeInMillis();
        //publish time in a few minutes
         service.deletePublishingCredential(credentialId);





    }
}
