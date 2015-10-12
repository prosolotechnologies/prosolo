package org.prosolo.services.indexing;

import java.io.Serializable;

import org.elasticsearch.client.Client;
import org.prosolo.common.domainmodel.activities.TargetActivity;
import org.prosolo.services.event.Event;


/**
 * @author Zoran Jeremic 2013-06-10
 */
public interface ESIndexer extends Serializable{

	void indexPost(Event event);

	void addMapping(Client client, String indexName,String indexType);


	void indexFileUploadedByTargetActivity(TargetActivity targetActivity,
			long userId);

	void removeFileUploadedByTargetActivity(TargetActivity object, long id);

}
