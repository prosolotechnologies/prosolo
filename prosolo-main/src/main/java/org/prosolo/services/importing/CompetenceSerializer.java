package org.prosolo.services.importing;

/**
 * 
 * @author Zoran Jeremic
 * @deprecated since 0.7
 */
@Deprecated
public class CompetenceSerializer 
	//implements JsonSerializer<Competence1> 
{
//	private boolean includeActivities = true;
//	private boolean includePrerequisites = true;
//	private boolean includeCorequisites = true;
//	private boolean includeAnnotations = true;
//	
//	public CompetenceSerializer() {
//		
//	}
//	
//	public CompetenceSerializer(boolean includeActivities, boolean includeAnnotations) {
//		this.includeActivities = includeActivities;
//		this.includeAnnotations = includeAnnotations;
//	}
//
//	@Override
//	public JsonElement serialize(Competence1 competence, Type typeOfSrc, JsonSerializationContext context) {
//		final JsonObject jsonObject = new JsonObject();
//		jsonObject.addProperty("id", competence.getId());
//		Date dateCreated = competence.getDateCreated();
//		if (dateCreated != null) {
//			jsonObject.addProperty("dateCreated", dateCreated.getTime());
//		}
//		jsonObject.addProperty("description", competence.getDescription());
//		jsonObject.addProperty("title", competence.getTitle());
//		jsonObject.addProperty("duration", competence.getDuration());
//		jsonObject.addProperty("validityPeriod", competence.getValidityPeriod());
//		if (includeActivities) {
//			final JsonElement activities = context.serialize(competence.getActivities());
//			jsonObject.add("activities", activities);
//		}
//		if (includePrerequisites) {
//			final JsonElement prerequisites = context.serialize(competence.getPrerequisites());
//			jsonObject.add("prerequisites", prerequisites);
//		}
//		if (includeCorequisites) {
//			final JsonElement corequisites = context.serialize(competence.getCorequisites());
//			jsonObject.add("corequisites", corequisites);
//		}
//		if (this.includeAnnotations) {
//			final JsonElement tags = context.serialize(competence.getTags());
//			jsonObject.add("tags", tags);
//			final JsonElement hashtags = context.serialize(competence.getHashtags());
//			jsonObject.add("hashtags", hashtags);
//		}
//		// jsonObject.addProperty("title", course.getTitle());
//		
//		return jsonObject;
//	}

}