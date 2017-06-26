package org.prosolo.services.indexing;

import org.prosolo.bigdata.common.exceptions.IndexingServiceNotAvailable;

import java.io.Serializable;

/**
 * @author Zoran Jeremic 2013-06-28
 *
 */
public interface ESAdministration  extends Serializable{

	boolean deleteIndexes() throws IndexingServiceNotAvailable;

	boolean createIndexes() throws IndexingServiceNotAvailable;

	void indexTrainingSet();

	void deleteIndex(String indexName) throws IndexingServiceNotAvailable;

	void createIndex(String indexName) throws IndexingServiceNotAvailable;

	boolean createOrganizationIndexes(long organizationId) throws IndexingServiceNotAvailable;

	boolean deleteOrganizationIndexes(long organizationId) throws IndexingServiceNotAvailable;

}
