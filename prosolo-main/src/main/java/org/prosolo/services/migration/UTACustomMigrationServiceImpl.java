package org.prosolo.services.migration;

import org.apache.log4j.Logger;
import org.prosolo.common.domainmodel.credential.Competence1;
import org.prosolo.common.domainmodel.credential.Credential1;
import org.prosolo.common.domainmodel.credential.CredentialType;
import org.prosolo.common.domainmodel.organization.Organization;
import org.prosolo.common.domainmodel.organization.Role;
import org.prosolo.common.domainmodel.organization.Unit;
import org.prosolo.common.domainmodel.user.User;
import org.prosolo.common.domainmodel.user.UserGroupPrivilege;
import org.prosolo.common.event.context.data.UserContextData;
import org.prosolo.core.spring.ServiceLocator;
import org.prosolo.services.data.Result;
import org.prosolo.services.event.EventData;
import org.prosolo.services.event.EventException;
import org.prosolo.services.event.EventFactory;
import org.prosolo.services.general.impl.AbstractManagerImpl;
import org.prosolo.services.nodes.*;
import org.prosolo.services.nodes.data.CredentialData;
import org.prosolo.services.nodes.data.OrganizationData;
import org.prosolo.services.nodes.data.ResourceVisibilityMember;
import org.prosolo.services.nodes.data.UserData;
import org.prosolo.services.util.roles.RoleNames;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
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
    @Inject private EventFactory eventFactory;

    @Override
    public void migrateCredentialsFrom06To07 () {
        logger.info("MIGRATION STARTED");
        List<EventData> events = ServiceLocator.getInstance().getService(UTACustomMigrationService.class)
               .migrateCredentials();
        for (EventData ev : events) {
           try {
               eventFactory.generateEvent(ev);
           } catch (EventException e) {
               logger.error(e);
           }
        }
        logger.info("MIGRATION FINISHED");
    }

    @Override
    @Transactional
    public List<EventData> migrateCredentials() {
        try {
            migrateUsers();
            return migrateData();
        } catch (Exception e) {
            logger.error("Error", e);
        }
        return new ArrayList<>();
    }

    private void migrateUsers() {
        try {
            logger.info("Migrate users start");
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
                    .createNewOrganizationAndGetEvents("UTA", Arrays.asList(new UserData(userJustinDellinger)), UserContextData.empty()).getResult();

            // Giving Justin Dellinger an admin role in UTA
            Role roleAdmin = roleManager.getRoleByName(RoleNames.ADMIN);
            userJustinDellinger = roleManager.assignRoleToUser(roleAdmin, userJustinDellinger);


            // Create History Department unit
            Unit unitHistoryDepartment = unitManager.createNewUnitAndGetEvents("History Department", orgUta.getId(),
                    0, UserContextData.of(userJustinDellinger.getId(), orgUta.getId(), null, null)).getResult();


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
                    unitManager.addUserToUnitWithRoleAndGetEvents(user.getId(), unitHistoryDepartment.getId(), roleUser.getId(), UserContextData.empty());
                }
            }


            // Giving Kimberly Breuer and Matt Crosslin manager roles in UTA and adding them to the History Department as teachers
            User userKimberlyBreuer = userManager.getUser("breuer@uta.edu");
            User userMattCrosslin = userManager.getUser("matt@uta.edu");
            Role roleManage = roleManager.getRoleByName(RoleNames.MANAGER);
            userKimberlyBreuer = roleManager.assignRoleToUser(roleManage, userKimberlyBreuer);
            unitManager.addUserToUnitWithRoleAndGetEvents(userKimberlyBreuer.getId(), unitHistoryDepartment.getId(), roleManage.getId(), UserContextData.empty());

            userMattCrosslin = roleManager.assignRoleToUser(roleManage, userMattCrosslin);
            unitManager.addUserToUnitWithRoleAndGetEvents(userMattCrosslin.getId(), unitHistoryDepartment.getId(), roleManage.getId(), UserContextData.empty());


            // deleting unused users
            deleteUsers(userJustinDellinger.getId());

            logger.info("Migrate users finished");
        } catch (Exception e) {
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
                userManager.deleteUserAndGetEvents(id, newCreatorId, UserContextData.empty());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private List<EventData> migrateData() throws Exception {
        logger.info("Migrate data start");
        /*
        collect events related to adding privileges to users to be able to generate those events
        which would fire observers to propagate privileges to deliveries and competences
         */
        List<EventData> events = new ArrayList<>();

        //connect credentials to organization and unit
        OrganizationData org = orgManager.getAllOrganizations(0, 1, false).getFoundNodes().get(0);
        Unit unit = getOrgUnit(org.getId());

        List<CredentialMapping> credMappings = getCredentialMappings();
        //collect ids of all deliveries to be able to remove all edit privileges from them
        List<Long> deliveryIds = new ArrayList<>();
        for (CredentialMapping mapping : credMappings) {
            Credential1 lastDelivery = (Credential1) persistence.currentManager()
                    .load(Credential1.class, mapping.lastDelivery.id);
            //create original credential from last delivery
            Result<Credential1> originalCredRes = createOriginalCredentialFromDelivery(lastDelivery.getId(), org.getId());
            events.addAll(originalCredRes.getEvents());
            Credential1 originalCred = originalCredRes.getResult();
            //connect credential to unit
            unitManager.addCredentialToUnitAndGetEvents(originalCred.getId(), unit.getId(), UserContextData.ofOrganization(org.getId()));
            convertOriginalCredToDelivery(org.getId(), lastDelivery, originalCred, Date.from(mapping.lastDelivery.start.atZone(ZoneId.systemDefault()).toInstant()),
                    Date.from(mapping.lastDelivery.end.atZone(ZoneId.systemDefault()).toInstant()));
            deliveryIds.add(mapping.lastDelivery.id);
            //add edit privilege to credential owner as this should be explicit in new app version
            //addEditPrivilegeToCredentialOwner(lastDelivery, org.getId());
            for (DeliveryData dd : mapping.restDeliveries) {
                Credential1 del = (Credential1) persistence.currentManager()
                        .load(Credential1.class, dd.id);
                convertOriginalCredToDelivery(org.getId(), del, originalCred, Date.from(dd.start.atZone(ZoneId.systemDefault()).toInstant()),
                        Date.from(dd.end.atZone(ZoneId.systemDefault()).toInstant()));
                deliveryIds.add(dd.id);
                //add edit privilege to credential owner as this should be explicit in new app version
                //addEditPrivilegeToCredentialOwner(del, org.getId());
            }
        }

        //delete all edit privileges from all deliveries as this will be propagated from original credential through event observers
        removeEditPrivilegesFromCredentials(deliveryIds);

        updateAllCompetences(org.getId(), unit.getId());

        connectCompetenceVersions();

        logger.info("Migrate data finished");

        return events;
    }

    private void removeEditPrivilegesFromCredentials(List<Long> deliveryIds) {
        String q = "DELETE FROM CredentialUserGroup cug " +
                   "WHERE cug.credential.id IN (:credIds) " +
                   "AND cug.privilege = :priv";

        int affected = persistence.currentManager().createQuery(q)
                .setParameterList("credIds", deliveryIds)
                .setString("priv", UserGroupPrivilege.Edit.name())
                .executeUpdate();
        logger.info("Number of deleted credential groups for deliveries: " + affected);
    }

    private void connectCompetenceVersions() {
        List<CompetenceVersionMapping> versionMappings = getCompetencesVersionsMappings();
        for (CompetenceVersionMapping mapping : versionMappings) {
            Competence1 comp = (Competence1) persistence.currentManager().load(Competence1.class,
                    mapping.getVersionId());
            comp.setOriginalVersion((Competence1) persistence.currentManager().load(Competence1.class,
                    mapping.getOriginalId()));
        }
        persistence.currentManager().flush();
    }

    private void addEditPrivilegeToCredentialOwner(Credential1 cred, long orgId) {
        userGroupManager.saveUserToDefaultCredentialGroupAndGetEvents(cred.getCreatedBy().getId(), cred.getId(),
                UserGroupPrivilege.Edit, UserContextData.ofOrganization(orgId));
    }

    private void addEditPrivilegeToCompetenceOwner(Competence1 comp, long orgId) {
        userGroupManager.saveUserToDefaultCompetenceGroupAndGetEvents(comp.getCreatedBy().getId(),
                comp.getId(), UserGroupPrivilege.Edit, UserContextData.ofOrganization(orgId));
    }

    private Unit getOrgUnit(long orgId) {
        String q = "SELECT u FROM Unit u WHERE u.organization.id = :orgId";

        return (Unit) persistence.currentManager()
                .createQuery(q)
                .setLong("orgId", orgId)
                .uniqueResult();
    }

    private Result<Credential1> createOriginalCredentialFromDelivery(long deliveryId, long orgId) throws Exception {
        CredentialData lastDeliveryData = credManager.getCredentialForEdit(deliveryId, 0).getResource();
        //save original credential based on the last delivery
        Result<Credential1> res = credManager.saveNewCredentialAndGetEvents(lastDeliveryData, UserContextData.of(lastDeliveryData.getCreator().getId(), orgId, null, null));
        //propagate edit privileges from last delivery to original credential
        res.addEvents(copyEditPrivilegesFromDeliveryToOriginal(orgId, deliveryId, res.getResult().getId()));
        persistence.currentManager().flush();
        return res;
    }

    private List<EventData> copyEditPrivilegesFromDeliveryToOriginal(long orgId, long deliveryId, long credId) {
       List<EventData> events = new ArrayList<>();
       List<ResourceVisibilityMember> editors = userGroupManager.getCredentialVisibilityUsers(deliveryId, UserGroupPrivilege.Edit);
       for (ResourceVisibilityMember editor :editors) {
           events.addAll(userGroupManager.saveUserToDefaultCredentialGroupAndGetEvents(editor.getUserId(), credId,
                   UserGroupPrivilege.Edit, UserContextData.ofOrganization(orgId)).getEvents());
       }
       return events;
    }

    private void convertOriginalCredToDelivery(long orgId, Credential1 credToConvert, Credential1 original, Date startDate, Date endDate) {
        credToConvert.setOrganization((Organization) persistence.currentManager().load(
                Organization.class, orgId));
        credToConvert.setType(CredentialType.Delivery);
        credToConvert.setDeliveryOf(original);
        credToConvert.setDeliveryStart(startDate);
        credToConvert.setDeliveryEnd(endDate);
        persistence.currentManager().flush();
    }

    private void updateAllCompetences(long orgId, long unitId) throws Exception {
        List<Competence1> comps = getAllCompetences();
        for (Competence1 c : comps) {
            //connect competence to organization and unit
            c.setOrganization((Organization) persistence.currentManager().load(
                    Organization.class, orgId));
            //set date published for competency so big edits can't be made
            c.setDatePublished(Date.from(LocalDateTime.of(2017, Month.APRIL, 14, 2, 13)
                    .atZone(ZoneId.systemDefault()).toInstant()));

            persistence.currentManager().flush();

            //add edit privilege to comp owner
            addEditPrivilegeToCompetenceOwner(c, orgId);

            unitManager.addCompetenceToUnitAndGetEvents(c.getId(), unitId, UserContextData.ofOrganization(orgId));
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
                                new DeliveryData(425984,
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

    private List<CompetenceVersionMapping> getCompetencesVersionsMappings() {
        return Arrays.asList(
            // Credential 0: Bootcamp competencies
            new CompetenceVersionMapping(294912, 425984),
            new CompetenceVersionMapping(294913, 425985),
            new CompetenceVersionMapping(294914, 425986),
            new CompetenceVersionMapping(294915, 425987),

            // Credential 1: The Rise of the First Civilizations competencies
            new CompetenceVersionMapping(327680, 425988),
            new CompetenceVersionMapping(327681, 425989),
            new CompetenceVersionMapping(327682, 425990),
            new CompetenceVersionMapping(327683, 425991),
            new CompetenceVersionMapping(327684, 425992),
            new CompetenceVersionMapping(327685, 425993),
            new CompetenceVersionMapping(327686, 425994),
            new CompetenceVersionMapping(327687, 425995),
            new CompetenceVersionMapping(327688, 425996),
            new CompetenceVersionMapping(327689, 425997),
            new CompetenceVersionMapping(327690, 425998),
            new CompetenceVersionMapping(327691, 425999),

            // Credential 2: The Classic Era in World History competencies
            new CompetenceVersionMapping(360448, 458752),
            new CompetenceVersionMapping(360449, 458753),
            new CompetenceVersionMapping(360450, 458754),
            new CompetenceVersionMapping(360451, 458755),
            new CompetenceVersionMapping(360452, 458756),
            new CompetenceVersionMapping(360454, 458757),
            new CompetenceVersionMapping(360455, 458758),
            new CompetenceVersionMapping(360456, 458759),
            new CompetenceVersionMapping(360457, 458760),

            // Credential 3: The Postclassic Era in World History competencies
            new CompetenceVersionMapping(393216, 458761),
            new CompetenceVersionMapping(393217, 458762),
            new CompetenceVersionMapping(393218, 458763),
            new CompetenceVersionMapping(393219, 458764),
            new CompetenceVersionMapping(393220, 458765),
            new CompetenceVersionMapping(393221, 458766),
            new CompetenceVersionMapping(393222, 458767),
            new CompetenceVersionMapping(393223, 458768),
            new CompetenceVersionMapping(393224, 458769)
        );
    }

    private class CompetenceVersionMapping {
        long originalId;
        long versionId;

        public CompetenceVersionMapping(long originalId, long versionId) {
            this.originalId = originalId;
            this.versionId = versionId;
        }

        public long getOriginalId() {
            return originalId;
        }

        public long getVersionId() {
            return versionId;
        }
    }
}
