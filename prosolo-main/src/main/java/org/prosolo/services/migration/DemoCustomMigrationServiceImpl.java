package org.prosolo.services.migration;

import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.prosolo.common.domainmodel.credential.Competence1;
import org.prosolo.common.domainmodel.credential.Credential1;
import org.prosolo.common.domainmodel.credential.CredentialType;
import org.prosolo.common.domainmodel.organization.Organization;
import org.prosolo.common.domainmodel.organization.Role;
import org.prosolo.common.domainmodel.organization.Unit;
import org.prosolo.common.domainmodel.user.User;
import org.prosolo.common.domainmodel.user.UserGroup;
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
import org.prosolo.services.nodes.data.resourceAccess.AccessMode;
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
 * @since 1.0.0
 */
@Service ("org.prosolo.services.migration.DemoCustomMigrationService")
public class DemoCustomMigrationServiceImpl extends AbstractManagerImpl implements DemoCustomMigrationService {

    protected static Logger logger = Logger.getLogger(DemoCustomMigrationServiceImpl.class);

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
    public void migrateDataFrom06To11() {
        logger.info("MIGRATION STARTED");
        List<EventData> events = ServiceLocator.getInstance().getService(DemoCustomMigrationService.class)
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
            List<EventData> events = new LinkedList<>();
            migrateUsers();

            Result<Void> dataResult = migrateData();
            events.addAll(dataResult.getEvents());

            Result<Void> groupsResult = migrateUserGroups();
            events.addAll(groupsResult.getEvents());

            return events;
        } catch (Exception e) {
            logger.error("Error", e);
        }
        return new ArrayList<>();
    }

    private void migrateUsers() {
        try {
            logger.info("Migrate users start");
            // converting users prosolo.admin@gmail.com to Super Admin
            User userAdminAdmin = userManager.getUser("prosolo.admin@gmail.com");
            Role roleSuperAdmin = roleManager.getRoleByName(RoleNames.SUPER_ADMIN);
            roleManager.assignRoleToUser(roleSuperAdmin, userAdminAdmin.getId());


            User userGrahamHardy = userManager.getUser("graham.hardy@unisa.edu.au");

            // Create UniSA organization
            Organization orgUniSa = ServiceLocator.getInstance().getService(OrganizationManager.class)
                    .createNewOrganizationAndGetEvents("UniSA", Arrays.asList(new UserData(userGrahamHardy)), UserContextData.empty()).getResult();

            // Giving Graham Hardy an admin role in the UniSA
            Role roleAdmin = roleManager.getRoleByName(RoleNames.ADMIN);
            userGrahamHardy = roleManager.assignRoleToUser(roleAdmin, userGrahamHardy.getId());


            // Create Teaching Innovation Unit unit
            Unit unitTeachingInnovation = unitManager.createNewUnitAndGetEvents("Teaching Innovation Unit ", orgUniSa.getId(),
                    0, UserContextData.of(userGrahamHardy.getId(), orgUniSa.getId(), null, null)).getResult();


            // loading all users from the db
            Collection<User> allUsers = userManager.getAllUsers(0);
            Role roleUser = roleManager.getRoleByName(RoleNames.USER);

            for (User user : allUsers) {
                if (user.getId() != userAdminAdmin.getId()) {
                    // adding user to the UniSA organization
                    userManager.setUserOrganization(user.getId(), orgUniSa.getId());

                    // giving user a 'User' role
                    roleManager.assignRoleToUser(roleUser, user.getId());

                    // adding user to the History Department unit as a student
                    unitManager.addUserToUnitWithRoleAndGetEvents(user.getId(), unitTeachingInnovation.getId(), roleUser.getId(), UserContextData.empty());
                }
            }


            // Giving instructor and manager roles in UniSA and Teaching Innovation Unit to all registered UniSA users
            String[] unisaEmails = new String[]{
                    "cassandra.colvin@unisa.edu.au",
                    "carmel.taddeo@unisa.edu.au",
                    "sven.trenholm@unisa.edu.au",
                    "bruce.white@unisa.edu.au",
                    "jane.smith@unisa.edu.au",
                    "john.smit@unisa.edu.auw1"
            };
            Role roleManager = this.roleManager.getRoleByName(RoleNames.MANAGER);
            Role roleInstructor = this.roleManager.getRoleByName(RoleNames.INSTRUCTOR);

            for (String email : unisaEmails) {
                User user = userManager.getUser(email);
                user = this.roleManager.assignRoleToUser(roleManager, user.getId());
                user = this.roleManager.assignRoleToUser(roleInstructor, user.getId());

                unitManager.addUserToUnitWithRoleAndGetEvents(user.getId(), unitTeachingInnovation.getId(), roleManager.getId(), UserContextData.empty());
                unitManager.addUserToUnitWithRoleAndGetEvents(user.getId(), unitTeachingInnovation.getId(), roleInstructor.getId(), UserContextData.empty());
            }

            logger.info("Migrate users finished");
        } catch (Exception e) {
            e.printStackTrace();
            logger.error(e);
            throw new RuntimeException();
        }
    }

    private Result<Void> migrateData() throws Exception {
        logger.info("Migrate data start");

        Result<Void> result = new Result<>();

        /*
        collect events related to adding privileges to users to be able to generate those events
        which would fire observers to propagate privileges to deliveries and competences
         */
        List<EventData> events = new ArrayList<>();

        //connect credentials to organization and unit
        OrganizationData orgUniSa = orgManager.getAllOrganizations(0, 1, false).getFoundNodes().get(0);
        Unit unit = getOrgUnit(orgUniSa.getId());

        List<CredentialMapping> credMappings = getCredentialMappings();
        //collect ids of all deliveries to be able to remove all edit privileges from them
        List<Long> deliveryIds = new ArrayList<>();

        for (CredentialMapping mapping : credMappings) {
            Credential1 lastDelivery = (Credential1) persistence.currentManager()
                    .load(Credential1.class, mapping.lastDelivery.id);

            //create original credential from last delivery
            Result<Credential1> originalCredRes = createOriginalCredentialFromDelivery(lastDelivery.getId(), orgUniSa.getId());
            events.addAll(originalCredRes.getEvents());

            Credential1 originalCred = originalCredRes.getResult();
            //connect credential to unit

            unitManager.addCredentialToUnitAndGetEvents(originalCred.getId(), unit.getId(), UserContextData.ofOrganization(orgUniSa.getId()));

            Date start = Date.from(mapping.lastDelivery.start.atZone(ZoneId.systemDefault()).toInstant());
            Date end = mapping.lastDelivery.end != null ? Date.from(mapping.lastDelivery.end.atZone(ZoneId.systemDefault()).toInstant()) : null;

            convertOriginalCredToDelivery(orgUniSa.getId(), lastDelivery, originalCred, start, end);

            deliveryIds.add(mapping.lastDelivery.id);

            //add edit privilege to credential owner as this should be explicit in new app version
            addEditPrivilegeToCredentialOwner(lastDelivery, orgUniSa.getId());

            for (DeliveryData dd : mapping.restDeliveries) {
                Credential1 del = (Credential1) persistence.currentManager().load(Credential1.class, dd.id);
                convertOriginalCredToDelivery(orgUniSa.getId(), del, originalCred, Date.from(dd.start.atZone(ZoneId.systemDefault()).toInstant()),
                        Date.from(dd.end.atZone(ZoneId.systemDefault()).toInstant()));
                deliveryIds.add(dd.id);
            }
        }

        //delete all edit privileges from all deliveries as this will be propagated from original credential through event observers
        removeEditPrivilegesFromCredentials(deliveryIds);

        updateAllCompetences(orgUniSa.getId(), unit.getId());

        logger.info("Migrate data finished");

        result.addEvents(events);

        return result;
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
        CredentialData lastDeliveryData = credManager.getCredentialData(deliveryId, true, true, 0, AccessMode.MANAGER);
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
            c.setOrganization((Organization) persistence.currentManager().load(Organization.class, orgId));
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
                // Basics of Social Network Analysis
                new CredentialMapping(
                        new DeliveryData(1,
                                LocalDateTime.of(2017, Month.APRIL, 20, 17, 10),
                                LocalDateTime.of(2017, Month.MAY, 20, 17, 10)),
                        new ArrayList<>()),
                // Sensemaking of Social Network Analysis for Learning
                new CredentialMapping(
                        new DeliveryData(2,
                                LocalDateTime.of(2017, Month.APRIL, 20, 17, 10),
                                LocalDateTime.of(2017, Month.MAY, 20, 17, 10)),
                        new ArrayList<>()),
                // Introduction to Learning Analytics
                new CredentialMapping(
                        new DeliveryData(3,
                                LocalDateTime.of(2017, Month.APRIL, 20, 17, 10),
                                LocalDateTime.of(2017, Month.MAY, 20, 17, 10)),
                        new ArrayList<>()),
                // Text mining nuts and bolts
                new CredentialMapping(
                        new DeliveryData(4,
                                LocalDateTime.of(2017, Month.APRIL, 20, 17, 10),
                                LocalDateTime.of(2017, Month.MAY, 20, 17, 10)),
                        new ArrayList<>()),
                // Prediction modeling
                new CredentialMapping(
                        new DeliveryData(5,
                                LocalDateTime.of(2017, Month.APRIL, 20, 17, 10),
                                LocalDateTime.of(2017, Month.MAY, 20, 17, 10)),
                        new ArrayList<>()),
                // AITSL Pre-service Teacher Standards
                new CredentialMapping(
                        new DeliveryData(6,
                                LocalDateTime.of(2017, Month.APRIL, 20, 17, 10),
                                null),
                        new ArrayList<>()),
                // Standard 1: Know students and how they learn
                new CredentialMapping(
                        new DeliveryData(7,
                                LocalDateTime.of(2017, Month.APRIL, 20, 17, 10),
                                null),
                        new ArrayList<>()),
                // Standard 2: Know the content and how to teach it
                new CredentialMapping(
                        new DeliveryData(8,
                                LocalDateTime.of(2017, Month.APRIL, 20, 17, 10),
                                null),
                        new ArrayList<>()),
                // Standard 3: Plan for and implement effective teaching and learning
                new CredentialMapping(
                        new DeliveryData(9,
                                LocalDateTime.of(2017, Month.APRIL, 20, 17, 10),
                                null),
                        new ArrayList<>()),
                // Standard 4: Create and maintain supportive and safe learning environments
                new CredentialMapping(
                        new DeliveryData(10,
                                LocalDateTime.of(2017, Month.APRIL, 20, 17, 10),
                                null),
                        new ArrayList<>()),
                // Standard 5: Assess, provide feedback and report on student learning
                new CredentialMapping(
                        new DeliveryData(11,
                                LocalDateTime.of(2017, Month.APRIL, 20, 17, 10),
                                null),
                        new ArrayList<>()),
                // Standard 6 - Engage in professional learning
                new CredentialMapping(
                        new DeliveryData(12,
                                LocalDateTime.of(2017, Month.APRIL, 20, 17, 10),
                                null),
                        new ArrayList<>()),
                // Standard 7: Engage professionally with colleagues, parents/carers and the community
                new CredentialMapping(
                        new DeliveryData(13,
                                LocalDateTime.of(2017, Month.APRIL, 20, 17, 10),
                            null),
                        new ArrayList<>()),
                // Know students and how they learn
                new CredentialMapping(
                        new DeliveryData(14,
                                LocalDateTime.of(2017, Month.APRIL, 20, 17, 10),
                                null),
                        new ArrayList<>()),
                // Ruth Geer
                new CredentialMapping(
                        new DeliveryData(32768,
                                LocalDateTime.of(2017, Month.APRIL, 20, 17, 10),
                                null),
                        new ArrayList<>()),
                // Professional Knowledge
                new CredentialMapping(
                        new DeliveryData(32769,
                                LocalDateTime.of(2017, Month.APRIL, 20, 17, 10),
                                null),
                        new ArrayList<>())
        );
    }

    private Result<Void> migrateUserGroups() {
        OrganizationData orgUniSa = orgManager.getAllOrganizations(0, 1, false).getFoundNodes().get(0);
        Unit unit = getOrgUnit(orgUniSa.getId());

        List<UserGroup> userGroups = userGroupManager.getAllGroups(0,false, (Session) userGroupManager.getPersistence().currentManager());

        for (UserGroup userGroup : userGroups) {
            userGroup.setUnit(unit);
            userGroupManager.saveEntity(userGroup);
        }

        return new Result<>();
    }

    private class CredentialMapping {
        private DeliveryData lastDelivery;
        private List<DeliveryData> restDeliveries;

        private CredentialMapping(DeliveryData lastDelivery, List<DeliveryData> restDeliveries) {
            this.lastDelivery = lastDelivery;
            this.restDeliveries = restDeliveries;
        }
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
