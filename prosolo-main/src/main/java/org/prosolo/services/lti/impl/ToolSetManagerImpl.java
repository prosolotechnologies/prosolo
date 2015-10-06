package org.prosolo.services.lti.impl;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.hibernate.Query;
import org.prosolo.app.Settings;
import org.prosolo.common.domainmodel.lti.LtiConsumer;
import org.prosolo.common.domainmodel.lti.LtiTool;
import org.prosolo.common.domainmodel.lti.LtiToolSet;
import org.prosolo.services.general.impl.AbstractManagerImpl;
import org.prosolo.services.lti.ToolSetManager;
import org.prosolo.services.lti.exceptions.ConsumerAlreadyRegisteredException;
import org.prosolo.web.lti.LTIConfigLoader;
import org.prosolo.web.lti.json.data.ToolProxy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service("org.prosolo.services.lti.TooSetManager")
public class ToolSetManagerImpl extends AbstractManagerImpl implements ToolSetManager {

	private static final long serialVersionUID = 3890580875937743709L;

	@Override
	@Transactional
	public LtiToolSet saveToolSet(LtiTool tool) {
		    String domain = Settings.getInstance().config.application.domain;
			LtiToolSet ts = new LtiToolSet();
			Set<LtiTool> tools = new HashSet<>();
			tool.setToolSet(ts);
			String launchUrl = domain+"launch";
			tool.setLaunchUrl(launchUrl);
			tools.add(tool);
			ts.setTools(tools);
			LtiConsumer consumer = new LtiConsumer();
			consumer.setKeyLtiOne(UUID.randomUUID().toString());
			consumer.setSecretLtiOne(UUID.randomUUID().toString());
			ts.setConsumer(consumer);
			String regUrl = Settings.getInstance().config.application.domain+"ltiregistration";
			ts.setRegistrationUrl(regUrl);
			return saveEntity(ts);
	}

	@Override
	@Transactional
	public boolean checkIfToolSetExists(long toolSetId) throws RuntimeException {
		String queryString = "SELECT ts.id, c.keyLtiTwo " + "FROM LtiToolSet ts " + "INNER JOIN ts.consumer c "
				+ "WHERE ts.id = :id";
		Query query = persistence.currentManager().createQuery(queryString);
		query.setLong("id", toolSetId);

		Object[] res = (Object[]) query.uniqueResult();
		if (res == null) {
			return false;
		}
		String key = (String) res[1];
		System.out.println("KEY " + key);

		if (key != null) {
			throw new ConsumerAlreadyRegisteredException();
		}
		return true;

	}
}
