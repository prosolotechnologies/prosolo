package org.prosolo.services.importing;

import java.lang.reflect.Type;
import java.util.Date;

import org.prosolo.domainmodel.competences.Competence;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

/**
 *
 * @author Zoran Jeremic Apr 18, 2014
 *
 */

public class CompetenceSerializer  implements JsonSerializer<Competence> {
	private boolean includeActivities = true;
	private boolean includePrerequisites = true;
	private boolean includeCorequisites = true;
	private boolean includeAnnotations = true;
	
	public CompetenceSerializer() {
		
	}
	
	public CompetenceSerializer(boolean includeActivities, boolean includeAnnotations) {
		this.includeActivities = includeActivities;
		this.includeAnnotations = includeAnnotations;
	}

	@Override
	public JsonElement serialize(Competence competence, Type typeOfSrc, JsonSerializationContext context) {
		final JsonObject jsonObject = new JsonObject();
		jsonObject.addProperty("id", competence.getId());
		Date dateCreated = competence.getDateCreated();
		if (dateCreated != null) {
			jsonObject.addProperty("dateCreated", dateCreated.getTime());
		}
		jsonObject.addProperty("description", competence.getDescription());
		jsonObject.addProperty("title", competence.getTitle());
		jsonObject.addProperty("duration", competence.getDuration());
		jsonObject.addProperty("validityPeriod", competence.getValidityPeriod());
		if (includeActivities) {
			final JsonElement activities = context.serialize(competence.getActivities());
			jsonObject.add("activities", activities);
		}
		if (includePrerequisites) {
			final JsonElement prerequisites = context.serialize(competence.getPrerequisites());
			jsonObject.add("prerequisites", prerequisites);
		}
		if (includeCorequisites) {
			final JsonElement corequisites = context.serialize(competence.getCorequisites());
			jsonObject.add("corequisites", corequisites);
		}
		if (this.includeAnnotations) {
			final JsonElement tags = context.serialize(competence.getTags());
			jsonObject.add("tags", tags);
			final JsonElement hashtags = context.serialize(competence.getHashtags());
			jsonObject.add("hashtags", hashtags);
		}
		// jsonObject.addProperty("title", course.getTitle());
		
		return jsonObject;
	}

}