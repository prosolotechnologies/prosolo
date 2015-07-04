package org.prosolo.services.indexing;

import java.io.Serializable;

import org.elasticsearch.client.Client;
import org.prosolo.domainmodel.activities.TargetActivity;
import org.prosolo.services.event.Event;
import org.prosolo.bigdata.common.exceptions.IndexingServiceNotAvailable;


/**
 * @author Zoran Jeremic 2013-06-10
 */
public interface ESIndexer extends Serializable{

	void indexPost(Event event);

	void addMapping(Client client, String indexName,String indexType) throws IndexingServiceNotAvailable;


	void indexFileUploadedByTargetActivity(TargetActivity targetActivity,
			long userId);

	void removeFileUploadedByTargetActivity(TargetActivity object, long id);

}
