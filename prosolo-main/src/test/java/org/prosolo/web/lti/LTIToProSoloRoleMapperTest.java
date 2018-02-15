package org.prosolo.web.lti;

import org.junit.Test;
import org.prosolo.services.util.roles.SystemRoleNames;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 * @author Nikola Milikic
 * @date 2018-01-29
 * @since 1.2
 */
public class LTIToProSoloRoleMapperTest {

    @Test
    public void basicLTIRoleMappings() {
        assertEquals(SystemRoleNames.USER, LTIToProSoloRoleMapper.getRole("User"));
        assertEquals(SystemRoleNames.USER, LTIToProSoloRoleMapper.getRole("Student"));
        assertEquals(SystemRoleNames.USER, LTIToProSoloRoleMapper.getRole("Learner"));

        assertEquals(SystemRoleNames.MANAGER, LTIToProSoloRoleMapper.getRole("Creator"));
        assertEquals(SystemRoleNames.MANAGER, LTIToProSoloRoleMapper.getRole("ContentDeveloper"));
        assertEquals(SystemRoleNames.MANAGER, LTIToProSoloRoleMapper.getRole("Manager"));

        assertEquals(SystemRoleNames.INSTRUCTOR, LTIToProSoloRoleMapper.getRole("Instructor"));
        assertEquals(SystemRoleNames.INSTRUCTOR, LTIToProSoloRoleMapper.getRole("Mentor"));
        assertEquals(SystemRoleNames.INSTRUCTOR, LTIToProSoloRoleMapper.getRole("TeachingAssistant"));

        assertNull(LTIToProSoloRoleMapper.getRole("Administrator"));
        assertNull(LTIToProSoloRoleMapper.getRole("Administrator/Administrator"));
        assertNull(LTIToProSoloRoleMapper.getRole("Other"));
        assertNull(LTIToProSoloRoleMapper.getRole("None"));
    }
}
