package org.prosolo.services.importing;

import java.lang.reflect.Type;
import java.util.Date;

import org.prosolo.domainmodel.activities.UploadAssignmentActivity;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

/**
 *
 * @author Zoran Jeremic Apr 20, 2014
 *
 */

public class UploadAssignmentActivitySerializer implements JsonSerializer<UploadAssignmentActivity> {
	
	@Override
	public JsonElement serialize(UploadAssignmentActivity activity, Type arg1, JsonSerializationContext context) {
		final JsonObject jsonObject = new JsonObject();
		jsonObject.addProperty("id", activity.getId());
		jsonObject.addProperty("dType", activity.getClass().getSimpleName());
		
		Date dateCreated = activity.getDateCreated();
		
		if (dateCreated != null) {
			jsonObject.addProperty("dateCreated", activity.getDateCreated().getTime());
		}
		
		jsonObject.addProperty("description", activity.getDescription());
		jsonObject.addProperty("title", activity.getTitle());
		jsonObject.addProperty("visibility", activity.getVisibility().name());
		jsonObject.addProperty("mandatory", activity.isMandatory());
		jsonObject.addProperty("duration", activity.getDuration());
		jsonObject.addProperty("maxFilesNumber", activity.getMaxFilesNumber());
		jsonObject.addProperty("visibleToEveryone", activity.isVisibleToEveryone());
		
		return jsonObject;
	}
	
}
