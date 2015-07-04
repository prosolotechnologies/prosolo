package org.prosolo.services.logging;

import java.util.Set;

/**
 * @author Zoran Jeremic 2013-10-21
 * 
 */

public class LoggingServiceAdmin extends AbstractDB {
	
	public boolean dropAllCollections() {
		Set<String> collections = this.get_collection_names();
	
		if (collections != null) {
			for (String collection : collections) {
				if (!collection.equals("system.indexes")) {
					this.getCollection(collection).drop();
				}
			}
		}
		return true;
	}
}
