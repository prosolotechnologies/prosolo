package org.prosolo.common.domainmodel.user;

import org.junit.Test;
import org.prosolo.common.domainmodel.organization.Role;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * @author Nikola Milikic
 * @date 2018-03-16
 * @since 1.2
 */
public class UserTest {

    @Test
    public void hasRole() {
        Role role1 = new Role();
        role1.setId(1);

        Role role2 = new Role();
        role2.setId(2);

        User user = new User();
        user.addRole(role1);
        user.addRole(role2);

        assertTrue(user.hasRole(1));
        assertFalse(user.hasRole(3));
    }
}