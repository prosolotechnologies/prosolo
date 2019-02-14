package org.prosolo.services.nodes;

import org.hibernate.Session;
import org.hibernate.exception.ConstraintViolationException;
import org.prosolo.bigdata.common.exceptions.DbConnectionException;
import org.prosolo.common.domainmodel.learningStage.LearningStage;
import org.prosolo.common.domainmodel.organization.Organization;
import org.prosolo.common.domainmodel.organization.Role;
import org.prosolo.common.domainmodel.user.User;
import org.prosolo.common.event.context.data.UserContextData;
import org.prosolo.common.exceptions.ResourceCouldNotBeLoadedException;
import org.prosolo.search.impl.PaginatedResult;
import org.prosolo.services.data.Result;
import org.prosolo.services.event.EventQueue;
import org.prosolo.services.general.AbstractManager;
import org.prosolo.services.nodes.data.LearningResourceLearningStage;
import org.prosolo.services.nodes.data.organization.CredentialCategoryData;
import org.prosolo.services.nodes.data.organization.LearningStageData;
import org.prosolo.services.nodes.data.organization.OrganizationData;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.List;

/**
 * Created by Bojan on 6/6/2017.
 */
public interface OrganizationManager extends AbstractManager {

    PaginatedResult<OrganizationData> getAllOrganizations(int page, int limit, boolean loadAdmins)
            throws DbConnectionException;

    void deleteOrganization(long organizationId) throws DbConnectionException;

    List<User> getOrganizationUsers(long organizationId, boolean returnDeleted, Session session, List<Role> roles)
            throws DbConnectionException;

    Organization createNewOrganization(OrganizationData org, UserContextData context)
            throws DbConnectionException;

    Result<Organization> createNewOrganizationAndGetEvents(OrganizationData org, UserContextData context)
            throws DbConnectionException;

    OrganizationData getOrganizationForEdit(long organizationId, List<Role> userRoles) throws DbConnectionException;

    Organization updateOrganization(OrganizationData organization, UserContextData context)
            throws DbConnectionException, ConstraintViolationException, DataIntegrityViolationException;

    Result<Organization> updateOrganizationAndGetEvents(OrganizationData organization, UserContextData context)
            throws DbConnectionException, ConstraintViolationException, DataIntegrityViolationException;

    EventQueue updateOrganizationLearningStages(long orgId, OrganizationData organization, UserContextData context);

    void updateOrganizationCredentialCategories(long orgId, OrganizationData organization);

    OrganizationData getOrganizationDataWithoutAdmins(long organizationId);

    String getOrganizationTitle(long organizationId) throws DbConnectionException;

    List<LearningResourceLearningStage> getOrganizationLearningStagesForLearningResource(long orgId) throws DbConnectionException;

    LearningStageData getLearningStageData(long learningStageId) throws DbConnectionException;

    /**
     *
     * @param organizationId
     * @return
     * @throws DbConnectionException
     */
    List<CredentialCategoryData> getOrganizationCredentialCategoriesData(long organizationId);

    /**
     * Returns all organization credential categories that are being used in at least one credential
     *
     * @param organizationId
     * @return
     * @throws DbConnectionException
     */
    List<CredentialCategoryData> getUsedOrganizationCredentialCategoriesData(long organizationId);
}

