/**
 * 
 */
package org.prosolo.services.externalIntegration.impl;

import org.apache.log4j.Logger;
import org.prosolo.services.externalIntegration.MicroblogManager;
import org.prosolo.services.general.impl.AbstractManagerImpl;
import org.springframework.stereotype.Service;

/**
 * @author Nikola Milikic
 *
 */
@Service("org.prosolo.services.externalIntegration.MicroblogManager")
public class MicroblogManagerImpl extends AbstractManagerImpl implements MicroblogManager {

	private static final long serialVersionUID = -6271483573527748460L;

	@SuppressWarnings("unused")
	private static Logger logger = Logger.getLogger(MicroblogManagerImpl.class);

//	@Override
//	public Microblog getMicroblogService(String serviceUri) throws Exception {
//		try {
//			if (serviceUri != null) {
//				Microblog microblog = loadResourceByUri(Microblog.class, serviceUri);
//				return microblog;
//			}
//		} catch (Exception e) { }
//		return null;
//	}

//	@Override
//	// TODO
//	public UserAccount getOrCreateUserAccount(String serviceHandle, String serviceHomepage) throws Exception {
//		String queryString = 
//				"PREFIX rdf: <" + Constants.RDF_NS + ">\n" + 
//				"PREFIX sioc: <" + Constants.SIOC_NS + ">\n" + 
//				"PREFIX foaf: <" + Constants.FOAF_NS + ">\n" + 
//				"SELECT ?userAccount \n" + 
//				"WHERE {\n" + 
//					"?userAccount rdf:type sioc:UserAccount ;\n" +
//						"foaf:accountName ?username ;\n" +
//						"foaf:accountServiceHomepage <"+serviceHomepage+"> .\n" +
//					"FILTER (?username = \""+serviceHandle+"\") \n" +
//				 "}";
//		
//		UserAccount userAccount = null;
//		if (userAccounts != null && !userAccounts.isEmpty()){
//			userAccount =  loadResourceByUri(SiocUserAccount.class, userAccounts.iterator().next());
//		} else {
//			Resource twitterServiceHomepage = new Resource(serviceHomepage);
//			twitterServiceHomepage = saveEntity(twitterServiceHomepage);
//
//			userAccount = new SiocUserAccount();
//			userAccount.setUsername(serviceHandle);
//			userAccount.setAccountServiceHomepage(twitterServiceHomepage);
//			userAccount = saveEntity(userAccount);
//		}
//		
//		return userAccount;
//		return null;
//	}
	
}
