package org.prosolo.services.importing;

import java.lang.reflect.Type;
import java.util.Date;

import org.prosolo.common.domainmodel.activities.CompetenceActivity;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

/**
 *
 * @author Zoran Jeremic Apr 18, 2014
 *
 */

public class CompetenceActivitySerializer  implements JsonSerializer<CompetenceActivity> {

	@Override
	public JsonElement serialize(CompetenceActivity competenceActivity, Type typeOfSrc,
			JsonSerializationContext context) {
		final JsonObject jsonObject = new JsonObject();
		//final JsonElement dateCreated=context.serialize(competenceActivity.getDateCreated());
		 
		jsonObject.addProperty("id", competenceActivity.getId());
		 Date dateCreated=competenceActivity.getDateCreated();
		 if(dateCreated!=null){
		jsonObject.addProperty("dateCreated", dateCreated.getTime());
		 }
	    jsonObject.addProperty("activityPosition", competenceActivity.getActivityPosition());
	    final JsonElement activity=context.serialize(competenceActivity.getActivity());
		jsonObject.add("activity", activity);
		    

	    return jsonObject;
	}
}
