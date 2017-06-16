package org.prosolo.services.importing;

/**
 * 
 * @author Zoran Jeremic
 * @deprecated since 0.7
 */
@Deprecated
public class ResourceActivitySerializer 
//implements JsonSerializer<ResourceActivity> 
{
//	private boolean includeFiles;
//	
//	public ResourceActivitySerializer(boolean includeFiles) {
//		this.includeFiles = includeFiles;
//	}
//	
//	@Override
//	public JsonElement serialize(ResourceActivity activity, Type arg1, JsonSerializationContext context) {
//		final JsonObject jsonObject = new JsonObject();
//		// final JsonElement
//		// dateCreated=context.serialize(activity.getDateCreated());
//		jsonObject.addProperty("id", activity.getId());
//		jsonObject.addProperty("dType", activity.getClass().getSimpleName());
//		Date dateCreated = activity.getDateCreated();
//		
//		if (dateCreated != null) {
//			jsonObject.addProperty("dateCreated", dateCreated.getTime());
//		}
//		jsonObject.addProperty("description", activity.getDescription());
//		jsonObject.addProperty("title", activity.getTitle());
//		jsonObject.addProperty("visibility", activity.getVisibility().name());
//		jsonObject.addProperty("mandatory", activity.isMandatory());
//		
//		if (includeFiles) {
//			final JsonElement richContent = context.serialize(activity.getRichContent());
//			jsonObject.add("richContent", richContent);
//		}
//		
//		return jsonObject;
//	}
//	
}
