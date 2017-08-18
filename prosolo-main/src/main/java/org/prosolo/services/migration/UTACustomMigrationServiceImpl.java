package org.prosolo.services.migration;

import org.apache.log4j.Logger;
import org.prosolo.bigdata.common.exceptions.IndexingServiceNotAvailable;
import org.prosolo.common.domainmodel.organization.Organization;
import org.prosolo.common.domainmodel.organization.Role;
import org.prosolo.common.domainmodel.user.User;
import org.prosolo.common.event.context.data.UserContextData;
import org.prosolo.core.spring.ServiceLocator;
import org.prosolo.services.admin.BulkDataAdministrationService;
import org.prosolo.services.event.EventException;
import org.prosolo.services.indexing.ESAdministration;
import org.prosolo.services.nodes.*;
import org.prosolo.services.nodes.data.UnitData;
import org.prosolo.services.nodes.data.UserData;
import org.prosolo.services.util.roles.RoleNames;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import java.util.Arrays;
import java.util.Collection;

/**
 * @author nikolamilikic
 * @date 2017-08-18
 * @since 0.7
 */
@Service ("org.prosolo.services.migration.UTACustomMigrationService")
public class UTACustomMigrationServiceImpl implements UTACustomMigrationService {

    protected static Logger logger = Logger.getLogger(UTACustomMigrationServiceImpl.class);

    @Inject
    private BulkDataAdministrationService bulkDataAdministrationService;
    @Inject
    private UserManager userManager;
    @Inject
    private UnitManager unitManager;
    @Inject
    private RoleManager roleManager;

    @Transactional
    public void migrateCredentialsFrom06To07 () {
        // formatting indexes
        try {
            bulkDataAdministrationService.deleteAndInitElasticSearchIndexes();
            bulkDataAdministrationService.indexDBData();
        } catch (IndexingServiceNotAvailable e) {
            e.printStackTrace();
            logger.error(e);
        }

        try {
            // converting users Admin Admin (email=zoran.jeremic@gmail.com, id=1) and Nikola Milikic (email=zoran.jeremic@uta.edu, id=163840) to Super Admins
            User userAdminAdmin = userManager.getUser("zoran.jeremic@gmail.com");
            User userNikolaMilikic = userManager.getUser("zoran.jeremic@uta.edu");
            Role roleSuperAdmin = roleManager.getRoleByName(RoleNames.SUPER_ADMIN);
            roleManager.assignRoleToUser(roleSuperAdmin, userAdminAdmin);
            roleManager.assignRoleToUser(roleSuperAdmin, userNikolaMilikic);

            // loading user Justin Dellinger to be a creator of the organization and unit
            User userJustinDellinger = userManager.getUser("jdelling@uta.edu");

            // Create UTA organization
            Organization orgUta = ServiceLocator.getInstance().getService(OrganizationManager.class)
                    .createNewOrganization("UTA", Arrays.asList(new UserData(userJustinDellinger)), UserContextData.empty());

            // Giving Justin Dellinger an admin role in UTA
            Role roleAdmin = roleManager.getRoleByName(RoleNames.ADMIN);
            userJustinDellinger = roleManager.assignRoleToUser(roleAdmin, userJustinDellinger);


            // Create History Department unit
            UnitData unitHistoryDepartment = unitManager.createNewUnit("History Department", orgUta.getId(),
                    0, UserContextData.of(userJustinDellinger.getId(), orgUta.getId(), null, null));


            // loading all users from the db
            Collection<User> allUsers = userManager.getAllUsers();
            Role roleUser = roleManager.getRoleByName(RoleNames.USER);

            for (User user : allUsers) {
                // adding user to the UTA organization
                userManager.setUserOrganization(user.getId(), orgUta.getId());

                // giving user a 'User' role
                roleManager.assignRoleToUser(roleUser, user);

                // adding user to the History Department unit as a student
                unitManager.addUserToUnitWithRole(user.getId(), unitHistoryDepartment.getId(), roleUser.getId(), UserContextData.empty());
            }


            // Giving Kimberly Breuer and Matt Crosslin manager roles in UTA and adding them to the History Department as teachers
            User userKimberlyBreuer = userManager.getUser("breuer@uta.edu");
            User userMattCrosslin = userManager.getUser("matt@uta.edu");
            Role roleManage = roleManager.getRoleByName(RoleNames.MANAGER);
            userKimberlyBreuer = roleManager.assignRoleToUser(roleManage, userKimberlyBreuer);
            unitManager.addUserToUnitWithRole(userKimberlyBreuer.getId(), unitHistoryDepartment.getId(), roleManage.getId(), UserContextData.empty());

            userMattCrosslin = roleManager.assignRoleToUser(roleManage, userMattCrosslin);
            unitManager.addUserToUnitWithRole(userMattCrosslin.getId(), unitHistoryDepartment.getId(), roleManage.getId(), UserContextData.empty());


            // deleting unused users
            deleteUsers(userJustinDellinger.getId());



        } catch (EventException e) {
            e.printStackTrace();
            logger.error(e);
        }
    }

    @Override
    public void deleteUsers(long newCreatorId) {
        long[] usersToDelete = new long[]{2, 32768, 65536, 65537, 65538, 98304, 262144, 131072 };

        for (long id : usersToDelete) {
            try {
                userManager.deleteUser(id, newCreatorId, UserContextData.empty());
            } catch (EventException e) {
                e.printStackTrace();
            }
        }
    }
}
