package org.prosolo.services.nodes;

import org.hibernate.Session;
import org.hibernate.exception.ConstraintViolationException;
import org.prosolo.bigdata.common.exceptions.DbConnectionException;
import org.prosolo.common.domainmodel.organization.Organization;
import org.prosolo.common.domainmodel.organization.Role;
import org.prosolo.common.domainmodel.organization.settings.OrganizationPlugin;
import org.prosolo.common.domainmodel.user.User;
import org.prosolo.common.event.context.data.UserContextData;
import org.prosolo.search.impl.PaginatedResult;
import org.prosolo.services.data.Result;
import org.prosolo.services.general.AbstractManager;
import org.prosolo.services.nodes.data.LearningResourceLearningStage;
import org.prosolo.services.nodes.data.organization.*;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.List;
import java.util.Set;

/**
 * Created by Bojan on 6/6/2017.
 */
public interface OrganizationManager extends AbstractManager {

    PaginatedResult<OrganizationData> getAllOrganizations(int page, int limit, boolean loadAdmins)
            throws DbConnectionException;

    void deleteOrganization(long organizationId) throws DbConnectionException;

    List<User> getOrganizationUsers(long organizationId, boolean returnDeleted, Session session, List<Role> roles)
            throws DbConnectionException;

    Organization createNewOrganization(OrganizationBasicData organizationBasicData, UserContextData context)
            throws DbConnectionException;

    Result<Organization> createNewOrganizationAndGetEvents(OrganizationBasicData organizationBasicData, UserContextData context)
            throws DbConnectionException;

    /**
     *
     * @param orgId
     * @param pluginData
     * @param context
     *
     * @throws ConstraintViolationException
     * @throws DataIntegrityViolationException
     * @throws DbConnectionException
     */
    void updateLearningStagesPlugin(long orgId, LearningStagesPluginData pluginData, UserContextData context);

    /**
     *
     * @param orgId
     * @param pluginData
     * @param context
     * @return
     *
     * @throws ConstraintViolationException
     * @throws DataIntegrityViolationException
     * @throws DbConnectionException
     */
    Result<Void> updateLearningStagesPluginAndGetEvents(long orgId, LearningStagesPluginData pluginData, UserContextData context);

    /**
     *
     * @param orgId
     * @param pluginData
     *
     * @throws ConstraintViolationException
     * @throws DataIntegrityViolationException
     * @throws DbConnectionException
     */
    void updateEvidenceRepositoryPlugin(long orgId, EvidenceRepositoryPluginData pluginData);

    /**
     *
     * @param orgId
     * @param credentialCategoriesPluginData
     *
     * @throws ConstraintViolationException
     * @throws DataIntegrityViolationException
     * @throws DbConnectionException
     */
    void updateCredentialCategoriesPlugin(long orgId, CredentialCategoriesPluginData credentialCategoriesPluginData);

    OrganizationData getOrganizationForEdit(long organizationId, List<Role> userRoles) throws DbConnectionException;

    Organization updateOrganizationBasicInfo(long organizationid, OrganizationBasicData org, UserContextData context)
            throws DbConnectionException, ConstraintViolationException, DataIntegrityViolationException;

    Result<Organization> updateOrganizationBasicInfoAndGetEvents(long organizationid, OrganizationBasicData org, UserContextData context)
            throws DbConnectionException, ConstraintViolationException, DataIntegrityViolationException;

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

    /**
     *
     * @param orgId
     * @return
     *
     * @throws DbConnectionException
     */
    List<LearningStageData> getOrganizationLearningStagesData(long orgId);

    /**
     *
     * @param orgId
     * @param loadCategoryUsageInfo
     * @param listenChanges
     * @return
     *
     * @throws DbConnectionException
     */
    List<CredentialCategoryData> getOrganizationCredentialCategoriesData(long orgId, boolean loadCategoryUsageInfo, boolean listenChanges);

    /**
     *
     * @param pluginData
     *
     * @throws DbConnectionException
     */
    void updateAssessmentTokensPlugin(AssessmentTokensPluginData pluginData);

    /**
     *
     * @param organizationId
     * @param numberOfTokens
     *
     * @throws DbConnectionException
     */
    void resetTokensForAllOrganizationUsers(long organizationId, int numberOfTokens);

    /**
     *
     * @param organizationId
     * @param numberOfTokens
     *
     * @throws DbConnectionException
     */
    void addTokensToAllOrganizationUsers(long organizationId, int numberOfTokens);

    /**
     * Loads plugin information of a given type.
     *
     * @param pluginClass class of a plugin
     * @param organizationId organization id
     * @return
     */
    <T extends OrganizationPlugin> T getOrganizationPlugin(Class<T> pluginClass, long organizationId);

    /**
     * Loads all organization plugins.
     *
     * @param organizationId organization id
     * @return
     */
    List<OrganizationPlugin> getAllOrganizationPlugins(long organizationId);
}

