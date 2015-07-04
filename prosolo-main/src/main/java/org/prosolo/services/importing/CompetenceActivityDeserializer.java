package org.prosolo.services.importing;

import java.lang.reflect.Type;

import org.prosolo.domainmodel.activities.Activity;
import org.prosolo.domainmodel.activities.CompetenceActivity;
import org.prosolo.domainmodel.activities.ResourceActivity;
import org.prosolo.domainmodel.activities.UploadAssignmentActivity;
import org.prosolo.core.spring.ServiceLocator;
import org.prosolo.services.nodes.DefaultManager;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

/**
 *
 * @author Zoran Jeremic Apr 21, 2014
 *
 */

public class CompetenceActivityDeserializer  implements JsonDeserializer<CompetenceActivity> {

	@Override
	public CompetenceActivity deserialize(JsonElement json, Type typeOfT,
			JsonDeserializationContext context) throws JsonParseException {
		final JsonObject jsonObject = json.getAsJsonObject();
		int activityPosition=jsonObject.get("activityPosition").getAsInt();
		JsonObject dTypeObject=jsonObject.get("activity").getAsJsonObject();
		CompetenceActivity compActivity =null;
		if(dTypeObject!=null){
			Activity activity=null;
			String dType=dTypeObject.get("dType").getAsString();
			if(dType.equals("UploadAssignmentActivity")){
				 activity=context.deserialize(jsonObject.get("activity"), UploadAssignmentActivity.class);
			}else if(dType.equals("ResourceActivity")){
				 activity=context.deserialize(jsonObject.get("activity"), ResourceActivity.class);
			}
			 compActivity = new CompetenceActivity(activityPosition, activity);
			compActivity = ServiceLocator.getInstance().getService(DefaultManager.class).saveEntity(compActivity);
		}
		return compActivity;
	}

}
