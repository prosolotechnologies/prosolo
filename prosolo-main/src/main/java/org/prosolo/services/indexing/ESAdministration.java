package org.prosolo.services.indexing;

import org.prosolo.bigdata.common.exceptions.IndexingServiceNotAvailable;

import java.io.Serializable;

/**
 * @author Zoran Jeremic 2013-06-28
 *
 */
public interface ESAdministration  extends Serializable{

	//boolean deleteAllIndexes() throws IndexingServiceNotAvailable;

	boolean deleteDBIndexes() throws IndexingServiceNotAvailable;

	//boolean createAllIndexes() throws IndexingServiceNotAvailable;

	boolean createDBIndexes() throws IndexingServiceNotAvailable;

	void createNonrecreatableSystemIndexesIfNotExist() throws IndexingServiceNotAvailable;

	void indexTrainingSet();

	void deleteIndex(String indexName) throws IndexingServiceNotAvailable;

	void createIndex(String indexName) throws IndexingServiceNotAvailable;

	boolean createOrganizationIndexes(long organizationId) throws IndexingServiceNotAvailable;

	boolean deleteOrganizationIndexes(long organizationId) throws IndexingServiceNotAvailable;

	/**
	 * Deletes index by exact name or wildcard
	 * @param name
	 * @return
	 */
	boolean deleteIndexByName(String name);

	/**
	 * Deletes indexes from array matched by exact name or wildcard
	 *
	 * @param indexNames
	 * @return
	 */
	boolean deleteIndexesByName(String[] indexNames);

}
