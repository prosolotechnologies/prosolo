package org.prosolo.util.mongo;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Locale;

import com.google.gson.JsonObject;
//import com.mongodb.DBObject;

/**
@author Zoran Jeremic Jan 28, 2014
 */
@Deprecated
public class MongoGsonUtils {

	/*public static JsonObject convertDBObject2JsonObject(DBObject dbObject) {
		// DBObject dbObject = (DBObject) dbCursor.next();
		DateFormat readFormat = new SimpleDateFormat("yyyy-mm-dd'T'HH:mm:ssZ",
				Locale.getDefault());
		Mongo2gson mongo2gson = new Mongo2gson(readFormat);
		JsonObject jsonObject = mongo2gson.getAsJsonObject(dbObject);

		return jsonObject;
	}*/

}