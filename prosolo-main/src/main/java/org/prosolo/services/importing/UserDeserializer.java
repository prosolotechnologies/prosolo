package org.prosolo.services.importing;

import java.lang.reflect.Type;

import org.prosolo.common.domainmodel.user.User;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

/**
 *
 * @author Zoran Jeremic Apr 21, 2014
 *
 */

public class UserDeserializer  implements JsonDeserializer<User> {

	@Override
	public User deserialize(JsonElement json, Type typeOfT,
			JsonDeserializationContext context) throws JsonParseException {
		// TODO Auto-generated method stub
		return null;
	}

}
