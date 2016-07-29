package org.prosolo.bigdata.dal.cassandra.impl;

import org.junit.Before;
import org.junit.Test;
import org.prosolo.bigdata.dal.cassandra.UserObservationsDBManager;
import org.prosolo.bigdata.dal.cassandra.UserRecommendationsDBManager;
import org.prosolo.bigdata.utils.DateUtil;
import org.prosolo.common.domainmodel.credential.Activity1;
import org.prosolo.common.domainmodel.credential.Competence1;
import org.prosolo.common.domainmodel.credential.Credential1;

import java.io.*;
import java.net.URL;
import java.util.Random;
import java.util.Set;

import static org.junit.Assert.*;

/**
 * Created by Zoran on 13/12/15.
 */
public class UserObservationsDBManagerImplTest {

    @Before
    public void setUp() throws Exception {

    }

    @Test
    public void testFindAllUserCourses() throws Exception {
        UserObservationsDBManager dbManager=UserObservationsDBManagerImpl.getInstance();
        Set<Long> courses=dbManager.findAllUserCourses(101l);
        assertEquals("Not correct result",2, courses.size());

    }

    @Test
    public void testEnrollUserToCourse() throws Exception {
        UserObservationsDBManager dbManager= UserObservationsDBManagerImpl.getInstance();
        dbManager.enrollUserToCourse(101l,56l);
        dbManager.enrollUserToCourse(101l,57l);
    }

    @Test
    public void testWithrawUserFromCourse() throws Exception {
        UserObservationsDBManager dbManager= UserObservationsDBManagerImpl.getInstance();
        dbManager.withdrawUserFromCourse(101l,56l);
        dbManager.withdrawUserFromCourse(101l,57l);
        dbManager.withdrawUserFromCourse(101l,58l);

    }

    @Test
    public void randomlyEnrollUsersToCourses(){
        UserObservationsDBManager dbManager= UserObservationsDBManagerImpl.getInstance();
        Random r = new Random();
        for(int i=0;i<200;i++){
            long userid = r.nextInt(29);
            long credential = r.nextInt(100);
            dbManager.enrollUserToCourse(userid,credential);
        }
    }
    @Test
    public void randomlyGenerateUserPreferences(){
        UserRecommendationsDBManager dbManager= UserRecommendationsDBManagerImpl.getInstance();
        Random r = new Random();
        int userMax=26;
        int credMax=30;
        int compMax=100;
        int actMax=300;
        long date=DateUtil.getDaysSinceEpoch();
        for (int  xd=0;xd<2;xd++){
            long d=date-xd;
           System.out.println("GENERATING DATE:"+d) ;

        for(int i=0;i<200;i++){
            long userid = r.nextInt(userMax);
            int weightR=r.nextInt(100);
            Double weight=(double)weightR/100;
            long resourceid1 = r.nextInt(credMax);
            String resourcetype1= Credential1.class.getSimpleName();
            int pr1=r.nextInt(100);
            Double preference1=(double) pr1/100;
            dbManager.insertStudentPreferenceForDate(userid,resourcetype1,resourceid1,weight/4, d);

                long resourceid2 = r.nextInt(compMax);
                String resourcetype2 = Competence1.class.getSimpleName();
                int pr2 = r.nextInt(100);
                Double preference2 = (double) pr2 / 100;
                dbManager.insertStudentPreferenceForDate(userid, resourcetype2, resourceid2, weight/2,d);



                long resourceid3 = r.nextInt(actMax);
                String resourcetype3 = Activity1.class.getSimpleName();
                int pr3 = r.nextInt(100);
                Double preference3 = (double) pr3 / 100;
                dbManager.insertStudentPreferenceForDate(userid, resourcetype3, resourceid3, weight,d);
        }
        }
    }

    @Test
    public void importMoocUserCourses()throws Exception{
        URL filePath = Thread.currentThread().getContextClassLoader()
                .getResource("files/mooc_users_enrollment_data.csv");
        UserObservationsDBManager dbManager= UserObservationsDBManagerImpl.getInstance();
        File moocFile = new File(filePath.getPath());
        try{
            BufferedReader br = new BufferedReader(new FileReader(moocFile));
            String line;
            try {
                boolean first=true;
                while ((line = br.readLine()) != null) {
                    if(!first){

                        String[] parts=line.split("\\s*,\\s*");
                                            System.out.println("USER:"+parts[2]+" COURSE:"+parts[1]);
                        dbManager.enrollUserToCourse(Long.valueOf(parts[2]),Long.valueOf(parts[1]));
                    }
                    first=false;

                }

                } catch (IOException e1) {
                    // TODO Auto-generated catch block
                    e1.printStackTrace();
                }
        } catch (FileNotFoundException e2) {
           e2.printStackTrace();
        }
    }
}