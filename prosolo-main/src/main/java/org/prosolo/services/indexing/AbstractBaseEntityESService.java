package org.prosolo.services.indexing;

import java.util.Map;

import org.elasticsearch.common.xcontent.XContentBuilder;
import org.prosolo.common.domainmodel.general.BaseEntity;

/**
 * @author Zoran Jeremic 2013-06-29
 *
 */
public interface AbstractBaseEntityESService {

	void delete(String id, String indexName, String indexType);

	void indexNode(XContentBuilder builder, String indexId, String indexName,String indexType);

	String getIndexTypeForNode(BaseEntity resource);

	public void deleteNodeFromES(BaseEntity resource);

	String getIndexNameForNode(BaseEntity resource);
	
	void partialUpdateByScript(String indexName, String indexType, String docId,
			String script, Map<String, Object> scriptParams);

	 
 

}
