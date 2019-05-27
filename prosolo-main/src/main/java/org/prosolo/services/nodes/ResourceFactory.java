package org.prosolo.services.nodes;

import org.hibernate.Session;
import org.prosolo.bigdata.common.exceptions.DbConnectionException;
import org.prosolo.bigdata.common.exceptions.IllegalDataStateException;
import org.prosolo.bigdata.common.exceptions.StaleDataException;
import org.prosolo.common.domainmodel.activitywall.PostReshareSocialActivity;
import org.prosolo.common.domainmodel.credential.Competence1;
import org.prosolo.common.domainmodel.credential.Credential1;
import org.prosolo.common.domainmodel.organization.Role;
import org.prosolo.common.domainmodel.outcomes.SimpleOutcome;
import org.prosolo.common.event.context.data.UserContextData;
import org.prosolo.services.data.Result;
import org.prosolo.services.general.AbstractManager;
import org.prosolo.services.nodes.data.competence.CompetenceData1;
import org.prosolo.services.nodes.data.credential.CredentialData;

public interface ResourceFactory extends AbstractManager {

    Role createNewRole(String name, String description, boolean systemDefined);

    SimpleOutcome createSimpleOutcome(int resultValue, long targetActId, Session session);
    
    String getLinkForObjectType(String simpleClassName, long id, String linkField) 
			throws DbConnectionException;

	Result<Credential1> updateCredential(CredentialData data, UserContextData context) throws StaleDataException, IllegalDataStateException;

	Result<Competence1> updateCompetence(CompetenceData1 data, UserContextData context) throws StaleDataException,
			IllegalDataStateException;
	
	PostReshareSocialActivity sharePost(long userId, String text, long socialActivityId)
   			throws DbConnectionException;
	

}