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
import org.prosolo.bigdata.es.CompetenceIndexer;
import org.prosolo.common.ESIndexNames;

public class CompetenceIndexerImpl extends AbstractESIndexer implements
		CompetenceIndexer, Serializable {
	
	private static final long serialVersionUID = -1740927229182115398L;
	
	private static Logger logger = Logger
			.getLogger(CompetenceIndexerImpl.class.getName());
	
	private CompetenceIndexerImpl() {
		
	}
	
	private static class SingletonHolder {
        private static final CompetenceIndexerImpl INSTANCE = new CompetenceIndexerImpl();
    }
    
    public static CompetenceIndexerImpl getInstance(){
        return SingletonHolder.INSTANCE;
    }
	
	@Override
	public void updateVisibility(long compId, boolean published) {
		try {
			XContentBuilder doc = XContentFactory.jsonBuilder()
			    .startObject()
		        .field("published", published)
		        .endObject();
			partialUpdate(ESIndexNames.INDEX_NODES, ESIndexTypes.COMPETENCE1, compId + "", doc);
		} catch(Exception e) {
			logger.error(e);
			e.printStackTrace();
		}
	}

}
