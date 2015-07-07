package org.prosolo.services.importing;

import java.lang.reflect.Type;

import org.prosolo.common.domainmodel.annotation.Tag;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

/**
 *
 * @author Zoran Jeremic Apr 18, 2014
 *
 */

public class TagSerializer implements JsonSerializer<Tag> {
	
	@Override
	public JsonElement serialize(Tag tag, Type typeOfSrc, JsonSerializationContext context) {
		final JsonObject jsonObject = new JsonObject();
		jsonObject.addProperty("id", tag.getId());
		jsonObject.addProperty("title", tag.getTitle());
		return jsonObject;
	}
	
}
