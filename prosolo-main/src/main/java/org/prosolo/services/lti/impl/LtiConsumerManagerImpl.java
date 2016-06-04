package org.prosolo.services.lti.impl;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.hibernate.Query;
import org.prosolo.common.domainmodel.lti.LtiConsumer;
import org.prosolo.common.domainmodel.lti.LtiService;
import org.prosolo.services.common.exception.DbConnectionException;
import org.prosolo.services.general.impl.AbstractManagerImpl;
import org.prosolo.services.lti.LtiConsumerManager;
import org.prosolo.services.lti.exceptions.ConsumerAlreadyRegisteredException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service("org.prosolo.services.lti.LtiConsumerManager")
public class LtiConsumerManagerImpl extends AbstractManagerImpl implements LtiConsumerManager{

	private static final long serialVersionUID = -1653332490780874404L;
	
	@Override
	@Transactional
	public LtiConsumer registerLTIConsumer(long toolSetId, String key, String secret, List<String> capabilities, List<org.prosolo.web.lti.json.data.Service> services) throws RuntimeException{
		//LtiToolSet toolSet = (LtiToolSet) persistence.currentManager().load(LtiToolSet.class, toolSetId);
		//LtiConsumer cons = toolSet.getConsumer();
		try{
			LtiConsumer cons = getConsumerForToolSet(toolSetId);
			if(cons.getKeyLtiTwo() != null){
				throw new ConsumerAlreadyRegisteredException("Consumer already registered through this link");
			}
			cons.setKeyLtiTwo(key);
			cons.setSecretLtiTwo(secret);
			cons.setCapabilitieList(capabilities);
			
			Set<LtiService> serviceSet = new HashSet<LtiService>();
			for(org.prosolo.web.lti.json.data.Service s:services){
				LtiService ls = new LtiService();
				ls.setConsumer(cons);
				ls.setActionList(s.getActions());
				ls.setFormatList(s.getFormats());
				ls.setEndpoint(s.getEndpoint());
				serviceSet.add(ls);
			}
			cons.setServices(serviceSet);
			
			return saveEntity(cons);
		}catch(ConsumerAlreadyRegisteredException care){
			throw care;
		}catch(Exception e){
			throw new DbConnectionException("Error while registering consumer");
		}
		
	}
	
	private LtiConsumer getConsumerForToolSet(long toolSetId){
		String queryString = 
				"SELECT c " +
				"FROM LtiConsumer c " +
				"INNER JOIN fetch c.toolSet ts " +
				"WHERE ts.id = :id ";

		Query query = persistence.currentManager().createQuery(queryString);
		query.setLong("id", toolSetId);
		
		return (LtiConsumer) query.uniqueResult();	
	}
	
}
