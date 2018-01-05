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
import org.prosolo.bigdata.es.CredentialIndexer;
import org.prosolo.common.ESIndexNames;
import org.prosolo.common.elasticsearch.impl.AbstractESIndexerImpl;

public class CredentialIndexerImplImpl extends AbstractESIndexerImpl implements
		CredentialIndexer, Serializable {
	
	private static final long serialVersionUID = -1740927229182115398L;
	
	private static Logger logger = Logger
			.getLogger(CredentialIndexerImplImpl.class.getName());
	
	private CredentialIndexerImplImpl() {
		
	}
	
	private static class SingletonHolder {
        private static final CredentialIndexerImplImpl INSTANCE = new CredentialIndexerImplImpl();
    }
    
    public static CredentialIndexerImplImpl getInstance(){
        return SingletonHolder.INSTANCE;
    }
	
	@Override
	public void updateVisibility(long credentialId, boolean published) {
		try {
			XContentBuilder doc = XContentFactory.jsonBuilder()
			    .startObject()
		        .field("published", published)
		        .endObject();
			partialUpdate(ESIndexNames.INDEX_CREDENTIALS, ESIndexTypes.CREDENTIAL, credentialId + "", doc);
		} catch(Exception e) {
			logger.error(e);
			e.printStackTrace();
		}
	}

}
