package org.prosolo.bigdata.dal.cassandra.impl;

import org.junit.Before;
import org.junit.Test;
import org.prosolo.bigdata.dal.cassandra.UserObservationsDBManager;
import org.prosolo.bigdata.dal.cassandra.UserRecommendationsDBManager;
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
        for(int i=0;i<2000;i++){
            long userid = r.nextInt(1000);
            long credential = r.nextInt(100);
            dbManager.enrollUserToCourse(userid,credential);
        }
    }
    @Test
    public void randomlyGenerateUserPreferences(){
        UserRecommendationsDBManager dbManager= UserRecommendationsDBManagerImpl.getInstance();
        Random r = new Random();
        for(int i=0;i<2000;i++){
            long userid = r.nextInt(1000);

            long resourceid1 = r.nextInt(100);
            String resourcetype1= Credential1.class.getSimpleName();
            int pr1=r.nextInt(100);
            Double preference1=(double) pr1/100;
            dbManager.insertStudentPreference(userid,resourcetype1,resourceid1,preference1, System.currentTimeMillis());
            for(int x=0;x<5;x++) {
                long resourceid2 = r.nextInt(300);
                String resourcetype2 = Competence1.class.getSimpleName();
                int pr2 = r.nextInt(100);
                Double preference2 = (double) pr2 / 100;
                dbManager.insertStudentPreference(userid, resourcetype2, resourceid2, preference2, System.currentTimeMillis());
            }

            for(int y=0;y<15;y++) {
                long resourceid3 = r.nextInt(500);
                String resourcetype3 = Competence1.class.getSimpleName();
                int pr3 = r.nextInt(100);
                Double preference3 = (double) pr3 / 100;
                dbManager.insertStudentPreference(userid, resourcetype3, resourceid3, preference3, System.currentTimeMillis());
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