package org.prosolo.services.lti.impl;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.hibernate.Query;
import org.prosolo.bigdata.common.exceptions.DbConnectionException;
import org.prosolo.common.config.CommonSettings;
import org.prosolo.common.domainmodel.lti.LtiConsumer;
import org.prosolo.common.domainmodel.lti.LtiTool;
import org.prosolo.common.domainmodel.lti.LtiToolSet;
import org.prosolo.common.domainmodel.organization.Organization;
import org.prosolo.common.domainmodel.organization.Unit;
import org.prosolo.common.domainmodel.user.User;
import org.prosolo.common.domainmodel.user.UserGroup;
import org.prosolo.services.general.impl.AbstractManagerImpl;
import org.prosolo.services.lti.ToolSetManager;
import org.prosolo.services.lti.data.ExternalToolFormData;
import org.prosolo.services.lti.exceptions.ConsumerAlreadyRegisteredException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service("org.prosolo.services.lti.TooSetManager")
public class ToolSetManagerImpl extends AbstractManagerImpl implements ToolSetManager {

	private static final long serialVersionUID = 3890580875937743709L;

	@Override
	@Transactional
	public ExternalToolFormData saveToolSet(ExternalToolFormData tool, long actorId) throws DbConnectionException{
		return saveToolSet(tool, UUID.randomUUID().toString(), UUID.randomUUID().toString(), actorId);
	}

	@Override
	@Transactional
	public ExternalToolFormData saveToolSet(ExternalToolFormData tool, String keyLtiOne, String secretLtiOne, long actorId) throws DbConnectionException {
		try {
			LtiConsumer consumer = new LtiConsumer();
			consumer.setKeyLtiOne(keyLtiOne);
			consumer.setSecretLtiOne(secretLtiOne);
			consumer = saveEntity(consumer);

			LtiToolSet ts = new LtiToolSet();
			ts.setConsumer(consumer);
			ts.setRegistrationUrl(CommonSettings.getInstance().config.appConfig.domain + "ltitoolproxyregistration.xhtml");
			ts = saveEntity(ts);

			LtiTool ltiTool = new LtiTool();
			ltiTool.setToolType(tool.getToolType());
			ltiTool.setName(tool.getTitle());
			ltiTool.setDescription(tool.getDescription());
			ltiTool.setCreatedBy(loadResource(User.class, actorId));
			ltiTool.setOrganization(loadResource(Organization.class, tool.getOrganizationId()));
			if (tool.getUnitId() > 0) {
				ltiTool.setUnit(loadResource(Unit.class, tool.getUnitId()));
			}
			if (tool.getUserGroupData() != null) {
				ltiTool.setUserGroup(loadResource(UserGroup.class, tool.getUserGroupData().getId()));
			}

			ltiTool.setToolSet(ts);
			ltiTool.setLaunchUrl(CommonSettings.getInstance().config.appConfig.domain + "ltiproviderlaunch.xhtml");
//			ltiTool = saveEntity(ltiTool);

			Set<LtiTool> tools = new HashSet<>();
			tools.add(ltiTool);
			ts.setTools(tools);

			saveEntity(ltiTool);

			return new ExternalToolFormData(ltiTool);
		} catch(Exception e) {
			logger.error("error", e);
			throw new DbConnectionException("Error saving the tool");
		}
	}

	@Override
	@Transactional(readOnly = true)
	public boolean checkIfToolSetExists(long toolSetId) throws RuntimeException {
		try{
			String queryString = "SELECT ts.id, c.keyLtiTwo " + "FROM LtiToolSet ts " + "INNER JOIN ts.consumer c "
				+ "WHERE ts.id = :id";
			Query query = persistence.currentManager().createQuery(queryString);
			query.setLong("id", toolSetId);
	
			Object[] res = (Object[]) query.uniqueResult();
			if (res == null) {
				return false;
			}
			String key = (String) res[1];
	
			if (key != null) {
				throw new ConsumerAlreadyRegisteredException();
			}
			return true;
		}catch(ConsumerAlreadyRegisteredException care){
			throw care;
		}catch(Exception e){
			throw new DbConnectionException();
		}

	}
}
