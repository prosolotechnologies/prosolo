package org.prosolo.bigdata.es.impl;

import java.io.Serializable;
//import java.util.Set;

import org.apache.log4j.Logger;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.prosolo.bigdata.common.enums.ESIndexTypes;
//import org.prosolo.bigdata.config.Settings;
//import org.prosolo.services.indexing.ESIndexNames;
//import org.prosolo.services.indexing.ElasticSearchFactory;
import org.prosolo.bigdata.es.AbstractESIndexer;
import org.prosolo.bigdata.es.CredentialIndexer;
import org.prosolo.common.ESIndexNames;

public class CredentialIndexerImpl extends AbstractESIndexer implements
		CredentialIndexer, Serializable {
	
	private static final long serialVersionUID = -1740927229182115398L;
	
	private static Logger logger = Logger
			.getLogger(CredentialIndexerImpl.class.getName());
	
	private CredentialIndexerImpl() {
		
	}
	
	private static class SingletonHolder {
        private static final CredentialIndexerImpl INSTANCE = new CredentialIndexerImpl();
    }
    
    public static CredentialIndexerImpl getInstance(){
        return SingletonHolder.INSTANCE;
    }
	
	@Override
	public void updateVisibilityToPublic(long credentialId) {
		try {
			XContentBuilder doc = XContentFactory.jsonBuilder()
			    .startObject()
		        .field("visible", true)
		        .endObject();
			partialUpdate(ESIndexNames.INDEX_NODES, ESIndexTypes.CREDENTIAL, credentialId + "", doc);
		} catch(Exception e) {
			logger.error(e);
			e.printStackTrace();
		}
	}

}
