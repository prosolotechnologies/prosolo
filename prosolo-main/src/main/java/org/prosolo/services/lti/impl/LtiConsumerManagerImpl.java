package org.prosolo.services.lti.impl;

import javax.inject.Inject;

import org.hibernate.Query;
import org.prosolo.common.domainmodel.lti.LtiConsumer;
import org.prosolo.common.domainmodel.lti.LtiTool;
import org.prosolo.common.domainmodel.lti.LtiToolSet;
import org.prosolo.services.general.impl.AbstractManagerImpl;
import org.prosolo.services.lti.LtiConsumerManager;
import org.prosolo.services.oauth.OauthService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service("org.prosolo.services.lti.LtiConsumerManager")
public class LtiConsumerManagerImpl extends AbstractManagerImpl implements LtiConsumerManager{

	private static final long serialVersionUID = -1653332490780874404L;

	@Inject OauthService oauthService;
	
	
	/*private List<LtiConsumer> getConsumersForValidation(String key, long toolSetId, LtiVersion ltiVersion){
		String queryString = 
				"SELECT new LtiConsumer (c.id, c.key, c.secret) " +
				"FROM LtiConsumer c " +
				"LEFT JOIN c.toolSet ts " +
				"WHERE ts.id = :toolSetId AND c.key = :key " +
				"AND c.ltiVersion = :ltiVersion";

		Query query = persistence.currentManager().createQuery(queryString);
		query.setLong("toolSetId", toolSetId);
		query.setString("key", key);
		query.setParameter("ltiVersion", ltiVersion);
		
		return query.list();
	}*/
	
	/*@Override
	public LtiConsumer findValidConsumer(String key, long toolSetId, LtiVersion ltiVersion, HttpServletRequest request, String url) throws OauthException {
		OauthException ex = null;
		List<LtiConsumer> consumers = getConsumersForValidation(key, toolSetId, ltiVersion);
		if(consumers == null || consumers.isEmpty()) {
			return null;
		}
		for(LtiConsumer consumer:consumers){
			try{
				oauthService.validatePostRequest(request, url, consumer.getKey(), consumer.getSecret());
				return consumer;
			}catch(OauthException e){
				ex = e;
			}
		}
		throw ex;
	}*/
	@Override
	@Transactional
	public LtiConsumer registerLTIConsumer(LtiConsumer consumer, long toolSetId) {
		//LtiToolSet toolSet = (LtiToolSet) persistence.currentManager().load(LtiToolSet.class, toolSetId);
		//LtiConsumer cons = toolSet.getConsumer();
		LtiConsumer cons = getConsumerForToolSet(toolSetId);
		if(cons.getKeyLtiTwo() != null){
			throw new RuntimeException("Consumer already registered through this link");
		}
		cons.setKeyLtiTwo(consumer.getKeyLtiTwo());
		cons.setSecretLtiTwo(consumer.getSecretLtiTwo());
		cons.setCapabilitieList(consumer.getCapabilitieList());
		cons.setServices(consumer.getServices());
		/*LtiToolSet toolSet = new LtiToolSet();
		toolSet.setId(toolSetId);
		//da li moze ovako ili mora da se radi load iz baze Tool Seta
		consumer.setToolSet(toolSet);*/
		//da li je consumer perzistentan objekat (posto nije dovucen direktno nego preko ToolSeta_
		
		return saveEntity(cons);
		
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
