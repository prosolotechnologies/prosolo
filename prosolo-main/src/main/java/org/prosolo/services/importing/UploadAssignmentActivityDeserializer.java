package org.prosolo.services.importing;

/**
 * 
 * @author Zoran Jeremic
 * @deprecated since 0.7
 */
@Deprecated
public class UploadAssignmentActivityDeserializer 
//implements JsonDeserializer<UploadAssignmentActivity> 
{
	
//	@Override
//	public UploadAssignmentActivity deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
//		final JsonObject jsonObject = json.getAsJsonObject();
//		long date = jsonObject.get("dateCreated").getAsLong();
//		Date dateCreated = null;
//		
//		if (date > 0) {
//			dateCreated = new Date(date);
//		}
//		
//		String description = jsonObject.get("description").getAsString();
//		String title = jsonObject.get("title").getAsString();
//		String visibility = jsonObject.get("visibility").getAsString();
//		VisibilityType visType = VisibilityType.valueOf(visibility);
//		boolean mandatory = jsonObject.get("mandatory").getAsBoolean();
//		int maxFilesNumber = jsonObject.get("maxFilesNumber").getAsInt();
//		boolean visibleToEveryone = jsonObject.get("visibleToEveryone").getAsBoolean();
//		User maker = null;
//		
//		Tag[] annotations = context.deserialize(jsonObject.get("tags"), Tag[].class);
//		Set<Tag> tags = new HashSet<Tag>();
//		
//		if (annotations != null && annotations.length > 0) {
//			for (int i = 0; i < annotations.length; i++) {
//				tags.add(annotations[i]);
//			}
//		}
//		UploadAssignmentActivity activity = new UploadAssignmentActivity();
//		activity.setTitle(title);
//		activity.setDescription(description);
//		activity.setMandatory(mandatory);
//		activity.setMaker(maker);
//		activity.setDateCreated(dateCreated);
//		activity.setMaxFilesNumber(maxFilesNumber);
//		activity.setVisibleToEveryone(visibleToEveryone);
//		
//		if (tags != null && !tags.isEmpty())
//			activity.setTags(tags);
//		
//		if (visType != null) {
//			activity.setVisibility(visType);
//		} else {
//			activity.setVisibility(VisibilityType.PRIVATE);
//		}
//		
//		ServiceLocator.getInstance().getService(DefaultManager.class).saveEntity(activity);
//		return activity;
//	}
	
}
