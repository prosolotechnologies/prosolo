package org.prosolo.services.importing;

/**
 * 
 * @author Zoran Jeremic
 * @deprecated since 0.7
 */
@Deprecated
public class ResourceActivityDeserializer 
		//implements JsonDeserializer<ResourceActivity> 
{

//	@Override
//	public ResourceActivity deserialize(JsonElement json, Type typeOfT,
//			JsonDeserializationContext context) throws JsonParseException {
//	
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
//		RichContent richContent = context.deserialize(jsonObject.get("richContent"), RichContent.class);
//		User maker = null;
//		
//		
//		Tag[] annotations = context.deserialize(jsonObject.get("tags"), Tag[].class);
//		Set<Tag> tags = new HashSet<Tag>();
//		
//		if (annotations != null) {
//			for (int i = 0; i < annotations.length; i++) {
//				tags.add(annotations[i]);
//			}
//		}
//		
//		ResourceActivity activity = new ResourceActivity();
//		activity.setTitle(title);
//		activity.setDescription(description);
//		activity.setRichContent(richContent);
//		activity.setMaker(maker);
//		activity.setMandatory(mandatory);
//		activity.setDateCreated(dateCreated);
//
//		if (tags != null && !tags.isEmpty()) {
//			activity.setTags(tags);
//		}
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
