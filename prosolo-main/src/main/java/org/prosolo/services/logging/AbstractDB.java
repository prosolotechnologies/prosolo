package org.prosolo.services.logging;




import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.prosolo.app.Settings;
import org.prosolo.config.MongoDBServerConfig;
import org.prosolo.config.MongoDBServersConfig;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;
import com.mongodb.ServerAddress;
import com.mongodb.WriteResult;
import com.mongodb.util.JSON;

/**
 * @author zoran
 *
 */
public abstract class AbstractDB {
	private static Logger logger = Logger.getLogger(AbstractDB.class.getName());
	protected static DB db = null;
	protected static MongoClient mongoClient;
	//private MongoManager mongoManager;
	private final String ERROR = "{'err':'true', 'ok':0}";
	private static String eventsObservedCollection="log_events_observed";
	protected static String userActivitiesCollection="log_activity_date";
	protected static String userLatestActivityTimeCollection="log_user_latest_activity_time";


	public DBCollection getEventObservedCollection() {
		return this.getCollection(eventsObservedCollection);
	}
	public DBCollection  getUserActivitiesCollection() {
		return this.getCollection(userActivitiesCollection);
	}
	public DBCollection getUserLatestActivityTimeCollection() {
		return this.getCollection(userLatestActivityTimeCollection);
	}

	public DB getDb(){
		if(db==null){
			 db=mongoClient.getDB(Settings.getInstance().config.mongoDatabase.dbName);
		}
		return  db;
	}
	
	/**
	 * Initiate the connection to MongoDB servers. An array with the servers on a replicaSet architecture is provided.
	 *@throws UnknownHostException
	 **/
	public AbstractDB() {
		try {
			if (mongoClient == null)
			{
			  MongoDBServersConfig dbServersConfig=Settings.getInstance().config.mongoDatabase.dbServersConfig;
			  List<ServerAddress> serverAddresses=new ArrayList<ServerAddress>();
			  for(MongoDBServerConfig dbsConfig:dbServersConfig.dbServerConfig){
				ServerAddress serverAddress=new ServerAddress(dbsConfig.dbHost,dbsConfig.dbPort);
				serverAddresses.add(serverAddress);
			  }
			  mongoClient=new MongoClient(serverAddresses);
			}
			if(mongoClient==null){
				logger.debug("AbstractDB mongoClient is null");

			}
		} catch (UnknownHostException e) {
			logger.error("UnknownHostException:"+e.getMessage());
		}
	}

	/**
	 * Get a set of collection names
	 * @return	colls collection names
	 */
	public Set<String> get_collection_names() {
		Set < String > colls = getDb().getCollectionNames();
		return colls;
	}

	/**
	 * insert a document to a collection
	 * @param	collection Collection name	
	 * @param	json JSON string
	 * @return	result The result is a WriteResult object that is converted to JSON text.
	 */
	public String put(String collection, String json) {
		try { 
			 
			WriteResult wr = getCollection(collection).insert(query(json));
			return (wr.toString());
		} catch (Exception e) {
			return ERROR;
		}
	}

	/**
	 * UPDATE the first document is the collection that matches the query and return.
	 * @param	collection Collection name	
	 * @param	query JSON query string
	 * @param	update JSON update string	
	 * @return	result The result is a WriteResult object that is converted to JSON text.
	 */
	public String set(String collection, String query, String update) {
		try {
			 WriteResult wr = getCollection(collection).update(query(query), query(update));
			return (wr.toString());
		} catch (Exception e) {
			return ERROR;
		}
	}

	/**
	 * UPDATE all documents that match the query. 
	 * @param	collection Collection name	
	 * @param	query JSON query string
	 * @param	update JSON update string	
	 * @param	all passed to indicate a Multi-Update Vs single-update. see set(c,q,u) for single update
	 * @return	result The result is a WriteResult object that is converted to JSON text.
	 */
	public String set(String collection, String query, String update, boolean all) {
		try {
			 
			BasicDBObject set = new BasicDBObject("$set", query(update));
			WriteResult wr = getCollection(collection).updateMulti(query(query), set);
			return (wr.toString());
		} catch (Exception e) {
			return ERROR;
		}
	}

	/**
	 * Update and return the document.
	* @param collection The collection to do the query on
    * @param query - query to match
    * @param update - update to apply
    * @param fields - fields to be returned
	* @param sort - sort to apply before picking first document
    * @param remove - if true, document found will be removed
    * @param returnNew - if true, the updated document is returned, otherwise the old document is returned (or it would be lost forever)
    * @param upsert - do upsert (insert if document not present)
	* @return	string - A DBCursor that is converted to pure String of JSON.
	*/
	public String setget(String collection, String query, String update, String fields, String sort, boolean remove, boolean returnNew, boolean upsert){
			DBObject dbo = getCollection(collection).findAndModify(query(query), query(fields), query(sort), remove, query(update), returnNew, upsert);
			try {

				return (result(dbo));
			} catch (Exception e) {
				e.printStackTrace();
				return ERROR;
			} finally {

			}
	}


	/**
	 * Remove documents that match the query. 
	 * @param	collection Collection name	
	 * @param	query JSON query string
	 * @return	result The result is a WriteResult object that is converted to JSON text.
	 */
	public String remove(String collection, String query) {
		try {
			WriteResult wr = getCollection(collection).remove(query(query));
			return (wr.toString());
		} catch (Exception e) {
			return ERROR;
		}
	}



	/**
	 * Drops a collection
	 * @param	collection Collection name
	 * @return	boolean
	 */
	public Boolean drop(String collection) {
		try {
			getCollection(collection).drop();
			return true;
		} catch (Exception e) {
			return false;
		}
	}



	/**
	 * Create an index. e.g. {"a":1} or {"a":-1} or {"a":1, "a":-1, "b":1}
	 * @param	collection Collection name
	 * @param	query JSON query. e.g: {"a":1} or {"a":-1} or {"a":1, "a":-1, "b":1}	
	 * @return	boolean
	 */
	public Boolean index(String collection, String query) {
		try {
			getCollection(collection).ensureIndex(query(query));
			return true;
		} catch (Exception e) {
			return false;
		}
	}



	/**
	 * Drop an index. e.g. {"a":1}
	 * @param	collection Collection name
	 * @param	query JSON query. e.g: {"a":1}	
	 * @return	boolean
	 */
	public Boolean dropindex(String collection, String query) {
		try {
			getCollection(collection).dropIndex(query(query));
			return true;
		} catch (Exception e) {
			return false;
		}
	}



	/**
	 * Count the number of collections that match a query
	 * @param	collection Collection name
	 * @param	query JSON
	 * @return	long
	 */
	public long count(String collection, String query) {
		long count = getCollection(collection).count(query(query));
		try {
			return (count);
		} catch (Exception e) {
			return 0;
		}
	}

//public JsonObject getOne(String collection, String query, String fields){
//	DBObject dbObject=getCollection(collection).findOne(query(query), query(fields));
//	JsonObject jsonObject =MongoGsonUtils.convertDBObject2JsonObject(dbObject);
//	return jsonObject;
//}

	/**
	 * Query the a collection
	 * @param	collection Collection name
	 * @param	query JSON query to match
	 * @return	string - A DBCursor that is converted to pure String of JSON.
	 */
	public String get(String collection, String query) {
		DBCursor cursor = getCollection(collection).find(query(query));
		try {
			return (result(cursor));
		} catch (Exception e) {
			return ERROR;
		} finally {
			cursor.close();
		}
	}
 
	/**
	 * Get all documents in a collection
	 * @param	collection Collection name
	 * @return	string - A DBCursor that is converted to pure String of JSON.
	 */
	public String get(String collection) {
		DBCursor cursor = getCollection(collection).find();
		try {
			return (result(cursor));
		} catch (Exception e) {
			return ERROR;
		} finally {
			cursor.close();
		}
	}

	/**
	 * Convert a DBCursor to json
	 * @param	query JSON query
	 * @return	json
	 */
	public String result(DBCursor result) {
		return JSON.serialize(result);
	}



	/**
	 * Convert a DBCursor to json
	 * @param	query JSON query
	 * @return	json
	 */
	public String result(DBObject result) {
		return JSON.serialize(result);
	}


	/**
	 * Convert a JSON string to DBObject
	 * @param	query JSON string
	 * @return	DBObject 
	 */
	public DBObject query(String query) {
		return (DBObject) JSON.parse(query);
	}



	/**
	 * Convert a JSON string to DBObject
	 * @param	query JSON Object
	 * @return	DBObject 
	 */
	public DBObject query(Object query) {
		return (DBObject) JSON.parse(query.toString());
	}
	/**
	 * Returns an instance of DBCollection based on its name
	 * @param c
	 * @return
	 */
	public DBCollection getCollection(String collectionName){
		return  getDb().getCollection(collectionName);
	}
	
	public DBObject getOne(String collection, String query, String fields){
		DBObject dbObject=getCollection(collection).findOne(query(query), query(fields));
		//JsonObject jsonObject =MongoGsonUtils.convertDBObject2JsonObject(dbObject);
		return dbObject;
	}


	 
 


}

