package org.prosolo.services.importing;

import java.lang.reflect.Type;

import org.prosolo.common.domainmodel.credential.Activity1;
import org.prosolo.common.domainmodel.credential.CompetenceActivity1;

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

public class CompetenceActivityDeserializer  implements JsonDeserializer<CompetenceActivity1> {

	@Override
	public CompetenceActivity1 deserialize(JsonElement json, Type typeOfT,
			JsonDeserializationContext context) throws JsonParseException {
		final JsonObject jsonObject = json.getAsJsonObject();
		int activityPosition=jsonObject.get("activityPosition").getAsInt();
		JsonObject dTypeObject=jsonObject.get("activity").getAsJsonObject();
		CompetenceActivity1 compActivity =null;
		if(dTypeObject!=null){
			Activity1 activity=null;
			String dType=dTypeObject.get("dType").getAsString();
//			if(dType.equals("UploadAssignmentActivity")){
//				 activity=context.deserialize(jsonObject.get("activity"), UploadAssignmentActivity.class);
//			}else if(dType.equals("ResourceActivity")){
//				 activity=context.deserialize(jsonObject.get("activity"), ResourceActivity.class);
//			}
			//commented out because of change of relationship between Competence and CompetenceActivity
			//compActivity = new CompetenceActivity(activityPosition, activity);
			//compActivity = ServiceLocator.getInstance().getService(DefaultManager.class).saveEntity(compActivity);
		}
		return compActivity;
	}

}
