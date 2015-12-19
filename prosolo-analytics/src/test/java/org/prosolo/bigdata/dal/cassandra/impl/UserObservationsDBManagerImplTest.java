package org.prosolo.bigdata.dal.cassandra.impl;

import org.junit.Before;
import org.junit.Test;
import org.prosolo.bigdata.dal.cassandra.UserObservationsDBManager;

import java.io.*;
import java.net.URL;
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
        UserObservationsDBManager dbManager=new UserObservationsDBManagerImpl();
        Set<Long> courses=dbManager.findAllUserCourses(101l);
        assertEquals("Not correct result",2, courses.size());

    }

    @Test
    public void testEnrollUserToCourse() throws Exception {
        UserObservationsDBManager dbManager=new UserObservationsDBManagerImpl();
        dbManager.enrollUserToCourse(101l,56l);
        dbManager.enrollUserToCourse(101l,57l);
    }

    @Test
    public void testWithrawUserFromCourse() throws Exception {
        UserObservationsDBManager dbManager=new UserObservationsDBManagerImpl();
        dbManager.withdrawUserFromCourse(101l,56l);
        dbManager.withdrawUserFromCourse(101l,57l);
        dbManager.withdrawUserFromCourse(101l,58l);

    }

    @Test
    public void importMoocUserCourses()throws Exception{
        URL filePath = Thread.currentThread().getContextClassLoader()
                .getResource("files/mooc_users_enrollment_data.csv");
        UserObservationsDBManager dbManager=new UserObservationsDBManagerImpl();
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