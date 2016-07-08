package org.prosolo.services.nodes.impl;

import static org.junit.Assert.assertEquals;

import javax.inject.Inject;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.prosolo.core.spring.SpringConfig;
import org.prosolo.services.nodes.CourseManager;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * Created by Zoran on 13/12/15.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes={ SpringConfig.class })
public class CourseManagerImplTest {
    @Inject
    private CourseManager courseManager;
    @Test
    public void findCourseIdForTargetCompetenceTest(){
       Long courseid= courseManager.findCourseIdForTargetCompetence(65547l);
        System.out.println("FOUND:"+courseid);
        assertEquals("NOT FOUND CORRECT COURSE ID",(long)1,(long) courseid);
    }
    @Test
    public void findCourseIdForTargetActivityTest(){
        Long courseid= courseManager.findCourseIdForTargetActivity(65544l);
        System.out.println("FOUND:"+courseid);
        assertEquals("NOT FOUND CORRECT COURSE ID",(long)1,(long) courseid);
    }
    @Test
    public void findCourseIdForTargetLearningGoalTest(){
        Long courseid= courseManager.findCourseIdForTargetLearningGoal(166l);
        System.out.println("FOUND:"+courseid);
        assertEquals("NOT FOUND CORRECT COURSE ID",(long)1,(long) courseid);
    }
}