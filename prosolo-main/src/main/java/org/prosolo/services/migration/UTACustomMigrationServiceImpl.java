package org.prosolo.services.migration;

import org.apache.log4j.Logger;
import org.prosolo.common.domainmodel.credential.Competence1;
import org.prosolo.common.domainmodel.credential.Credential1;
import org.prosolo.common.domainmodel.credential.CredentialType;
import org.prosolo.common.domainmodel.organization.Organization;
import org.prosolo.common.domainmodel.organization.Role;
import org.prosolo.common.domainmodel.organization.Unit;
import org.prosolo.common.domainmodel.user.User;
import org.prosolo.common.event.context.data.UserContextData;
import org.prosolo.core.spring.ServiceLocator;
import org.prosolo.services.event.EventException;
import org.prosolo.services.general.impl.AbstractManagerImpl;
import org.prosolo.services.nodes.*;
import org.prosolo.services.nodes.data.CredentialData;
import org.prosolo.services.nodes.data.OrganizationData;
import org.prosolo.services.nodes.data.UnitData;
import org.prosolo.services.nodes.data.UserData;
import org.prosolo.services.util.roles.RoleNames;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.ZoneId;
import java.util.*;

/**
 * @author nikolamilikic
 * @date 2017-08-18
 * @since 0.7
 */
@Service ("org.prosolo.services.migration.UTACustomMigrationService")
public class UTACustomMigrationServiceImpl extends AbstractManagerImpl implements UTACustomMigrationService {

    protected static Logger logger = Logger.getLogger(UTACustomMigrationServiceImpl.class);

    @Inject
    private UserManager userManager;
    @Inject
    private UnitManager unitManager;
    @Inject
    private RoleManager roleManager;
    @Inject
    private OrganizationManager orgManager;
    @Inject
    private CredentialManager credManager;
    @Inject
    private UserGroupManager userGroupManager;

    @Transactional
    public void migrateCredentialsFrom06To07 () {
        try {
            logger.info("MIGRATION STARTED");
            migrateUsers();
            migrateData();
            logger.info("MIGRATION FINISHED");
        } catch (Exception e) {
            logger.error("Error", e);
        }
    }

    private void migrateUsers() {
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
                if (user.getId() != userAdminAdmin.getId() &&
                        user.getId() != userNikolaMilikic.getId()) {
                    // adding user to the UTA organization
                    userManager.setUserOrganization(user.getId(), orgUta.getId());

                    // giving user a 'User' role
                    roleManager.assignRoleToUser(roleUser, user);

                    // adding user to the History Department unit as a student
                    unitManager.addUserToUnitWithRole(user.getId(), unitHistoryDepartment.getId(), roleUser.getId(), UserContextData.empty());
                }
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
            throw new RuntimeException();
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

    private void migrateData() throws Exception {
        //connect credentials to organization and unit
        OrganizationData org = orgManager.getAllOrganizations(0, 1, false).getFoundNodes().get(0);
        Unit unit = getOrgUnit(org.getId());

        List<CredentialMapping> credMappings = getCredentialMappings();
        for (CredentialMapping mapping : credMappings) {
            Credential1 lastDelivery = (Credential1) persistence.currentManager()
                    .load(Credential1.class, mapping.lastDelivery.id);
            //create original credential from last delivery
            Credential1 originalCred = createOriginalCredentialFromDelivery(lastDelivery.getId(), org.getId());
            //connect credential to unit
            unitManager.addCredentialToUnit(originalCred.getId(), unit.getId(), UserContextData.ofOrganization(org.getId()));
            convertOriginalCredToDelivery(org.getId(), lastDelivery, originalCred, Date.from(mapping.lastDelivery.start.atZone(ZoneId.systemDefault()).toInstant()),
                    Date.from(mapping.lastDelivery.end.atZone(ZoneId.systemDefault()).toInstant()));
                for (DeliveryData dd : mapping.restDeliveries) {
                Credential1 del = (Credential1) persistence.currentManager()
                        .load(Credential1.class, dd.id);
                convertOriginalCredToDelivery(org.getId(), del, originalCred, Date.from(dd.start.atZone(ZoneId.systemDefault()).toInstant()),
                        Date.from(dd.end.atZone(ZoneId.systemDefault()).toInstant()));


            }
        }

        connectAllCompetencesToOrgAndUnit(org.getId(), unit.getId());

    }

    private Unit getOrgUnit(long orgId) {
        String q = "SELECT u FROM Unit u WHERE u.organization.id = :orgId";

        return (Unit) persistence.currentManager()
                .createQuery(q)
                .setLong("orgId", orgId)
                .uniqueResult();
    }

    private Credential1 createOriginalCredentialFromDelivery(long deliveryId, long orgId) throws Exception {
        CredentialData lastDeliveryData = credManager.getCredentialForEdit(deliveryId, 0).getResource();
        //save original credential based on the last delivery
        Credential1 originalCred = credManager.saveNewCredential(lastDeliveryData, UserContextData.of(lastDeliveryData.getCreator().getId(), orgId, null, null));
        //propagate edit privileges from last delivery to original credential
        //method name suggests that propagation is done from original cred to delivery but it can be other way too
        userGroupManager.propagateUserGroupEditPrivilegesFromCredentialToDeliveryAndGetEvents(
                deliveryId, originalCred.getId(), UserContextData.ofOrganization(orgId), persistence.currentManager());
        return originalCred;
    }

    private void convertOriginalCredToDelivery(long orgId, Credential1 credToConvert, Credential1 original, Date startDate, Date endDate) {
        credToConvert.setOrganization((Organization) persistence.currentManager().load(
                Organization.class, orgId));
        credToConvert.setType(CredentialType.Delivery);
        credToConvert.setDeliveryOf(original);
        credToConvert.setDeliveryStart(startDate);
        credToConvert.setDeliveryEnd(endDate);
    }

    private void connectAllCompetencesToOrgAndUnit(long orgId, long unitId) throws Exception {
        List<Competence1> comps = getAllCompetences();
        for (Competence1 c : comps) {
            c.setOrganization((Organization) persistence.currentManager().load(
                    Organization.class, orgId));
            unitManager.addCompetenceToUnit(c.getId(), unitId, UserContextData.ofOrganization(orgId));
        }
    }

    private List<Competence1> getAllCompetences() {
        String q = "SELECT c FROM Competence1 c";
        return persistence.currentManager().createQuery(q).list();
    }

    private List<CredentialMapping> getCredentialMappings() {
        return Arrays.asList(
                // Sensemaking of Social Network Analysis for Learning
//                new CredentialMapping(
//                        new DeliveryData(1,
//                                LocalDateTime.of(2017, Month.APRIL, 20, 17, 10),
//                                LocalDateTime.of(2017, Month.MAY, 20, 17, 10)),
//                        new ArrayList<>()),

                // Huetagogical Metaphysical Historically Accurate Enlightenment
                new CredentialMapping(
                        new DeliveryData(32768,
                                LocalDateTime.of(2017, Month.APRIL, 20, 17, 10),
                                LocalDateTime.of(2017, Month.MAY, 20, 17, 10)),
                        new ArrayList<>()),
//                new CredentialMapping(
//                        new DeliveryData(32769,
//                                LocalDateTime.of(2017, Month.APRIL, 20, 17, 10),
//                                LocalDateTime.of(2017, Month.MAY, 20, 17, 10)),
//                        new ArrayList<>()),
                // Unit 1 Historical Engagement
                new CredentialMapping(
                        new DeliveryData(131072,
                                LocalDateTime.of(2017, Month.MAY, 20, 17, 10),
                                LocalDateTime.of(2017, Month.JUNE, 20, 17, 10)),
                        Arrays.asList()),
                // Unit 2 Historical Engagement
                new CredentialMapping(
                        new DeliveryData(262144,
                                LocalDateTime.of(2017, Month.MAY, 20, 17, 10),
                                LocalDateTime.of(2017, Month.JUNE, 20, 17, 10)),
                        Arrays.asList()),
                // Unit 3 Historical Engagement
                new CredentialMapping(
                        new DeliveryData(294912,
                                LocalDateTime.of(2017, Month.MAY, 20, 17, 10),
                                LocalDateTime.of(2017, Month.JUNE, 20, 17, 10)),
                        Arrays.asList()),

                // Credential 0: Bootcamp
                new CredentialMapping(
                        new DeliveryData(491520,
                                LocalDateTime.of(2017, Month.APRIL, 20, 17, 10),
                                LocalDateTime.of(2017, Month.MAY, 20, 17, 10)),
                        Arrays.asList(
                                new DeliveryData(327684,
                                        LocalDateTime.of(2017, Month.APRIL, 20, 17, 10),
                                        LocalDateTime.of(2017, Month.MAY, 20, 17, 10)))),

                // Credential 1: The Rise of the First Civilizations
                new CredentialMapping(
                        new DeliveryData(491521,
                                LocalDateTime.of(2017, Month.APRIL, 20, 17, 10),
                                LocalDateTime.of(2017, Month.MAY, 20, 17, 10)),
                        Arrays.asList(
                                new DeliveryData(393216,
                                        LocalDateTime.of(2017, Month.APRIL, 20, 17, 10),
                                        LocalDateTime.of(2017, Month.MAY, 20, 17, 10)))),

                // Credential 2: The Classic Era in World History
                new CredentialMapping(
                        new DeliveryData(524288,
                                LocalDateTime.of(2017, Month.APRIL, 20, 17, 10),
                                LocalDateTime.of(2017, Month.MAY, 20, 17, 10)),
                        Arrays.asList(
                                new DeliveryData(425984L,
                                        LocalDateTime.of(2017, Month.APRIL, 20, 17, 10),
                                        LocalDateTime.of(2017, Month.MAY, 20, 17, 10)))),

                // Credential 3: The Postclassic Era in World History
                new CredentialMapping(
                        new DeliveryData(524289,
                                LocalDateTime.of(2017, Month.APRIL, 20, 17, 10),
                                LocalDateTime.of(2017, Month.MAY, 20, 17, 10)),
                        Arrays.asList(
                                new DeliveryData(458752,
                                        LocalDateTime.of(2017, Month.APRIL, 20, 17, 10),
                                        LocalDateTime.of(2017, Month.MAY, 20, 17, 10))))
        );
    }

    private class CredentialMapping {
        private DeliveryData lastDelivery;
        private List<DeliveryData> restDeliveries;

        private CredentialMapping(DeliveryData lastDelivery, List<DeliveryData> restDeliveries) {
            this.lastDelivery = lastDelivery;
            this.restDeliveries = restDeliveries;
        }

//        private DeliveryData newDelivery(long id, LocalDateTime start, LocalDateTime end) {
//            return new DeliveryData(id, start, end);
//        }

    }

    private class DeliveryData {
        private long id;
        private LocalDateTime start;
        private LocalDateTime end;

        private DeliveryData(long id, LocalDateTime start, LocalDateTime end) {
            this.id = id;
            this.start = start;
            this.end = end;
        }
    }
}
