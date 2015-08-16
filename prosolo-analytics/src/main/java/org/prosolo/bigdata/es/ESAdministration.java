package org.prosolo.bigdata.es;

import org.prosolo.bigdata.common.exceptions.IndexingServiceNotAvailable;

/**
 * @author Zoran Jeremic May 9, 2015
 *
 */

public interface ESAdministration {

	boolean createIndexes() throws IndexingServiceNotAvailable;

	void deleteIndex(String indexName) throws IndexingServiceNotAvailable;

	boolean deleteIndexes() throws IndexingServiceNotAvailable;

}
