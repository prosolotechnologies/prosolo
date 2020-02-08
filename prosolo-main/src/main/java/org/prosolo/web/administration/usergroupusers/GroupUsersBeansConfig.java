package org.prosolo.web.administration.usergroupusers;

import org.prosolo.search.UserTextSearch;
import org.prosolo.services.nodes.RoleManager;
import org.prosolo.services.nodes.UnitManager;
import org.prosolo.services.user.UserGroupManager;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

import javax.inject.Inject;

/**
 * Bean definitions for group users beans
 *
 * @author stefanvuckovic
 * @date 2019-08-14
 * @since 1.3.3
 */
@Configuration
public class GroupUsersBeansConfig {

    @Inject private UserTextSearch userTextSearch;
    @Inject private UserGroupManager userGroupManager;
    @Inject private RoleManager roleManager;
    @Inject private UnitManager unitManager;

    @Bean
    @Scope(BeanDefinition.SCOPE_PROTOTYPE)
    public GroupUsersBeanStrategy groupUsersBeanStrategy(UserType userType) {
        switch (userType) {
            case STUDENT:
                return new RegularGroupUsersBeanStrategy(userTextSearch, userGroupManager, roleManager);
            case INSTRUCTOR:
                return new InstructorGroupUsersBeanStrategy(userTextSearch, userGroupManager, roleManager);
        }
        return null;
    }

    @Bean
    @Scope(BeanDefinition.SCOPE_PROTOTYPE)
    public GroupUserAddBeanStrategy groupUserAddBeanStrategy(UserType userType) {
        switch (userType) {
            case STUDENT:
                return new RegularGroupUserAddBeanStrategy(userGroupManager, userTextSearch, unitManager);
            case INSTRUCTOR:
                return new InstructorGroupUserAddBeanStrategy(userGroupManager, userTextSearch, unitManager);
        }
        return null;
    }

}
