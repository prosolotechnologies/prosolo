package org.prosolo.services.importing;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Type;

import org.apache.commons.io.FileUtils;
import org.prosolo.app.Settings;
import org.prosolo.common.domainmodel.user.User;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

/**
 *
 * @author Zoran Jeremic Apr 20, 2014
 *
 */

public class UserSerializer implements JsonSerializer<User>  {
	String filePath;
	public UserSerializer(String path){
		this.filePath=path;
	}

	@Override
	public JsonElement serialize(User user, Type arg1,
			JsonSerializationContext context) {
		final JsonObject jsonObject = new JsonObject();
		//final JsonElement dateCreated=context.serialize(activity.getDateCreated());
		jsonObject.addProperty("id", user.getId());
		jsonObject.addProperty("email", user.getEmail().getAddress());
		jsonObject.addProperty("lastname", user.getLastname());
		jsonObject.addProperty("name",user.getName());
		jsonObject.addProperty("position", user.getPosition());
		jsonObject.addProperty("avatar_url", user.getAvatarUrl());
		 String link=user.getAvatarUrl();
			//link = link.replaceFirst(Settings.getInstance().config.fileManagement.urlPrefixFolder, "");
			//if (link.contains("+")) {
			//	link = link.replaceAll("\\+", " ");
			//}
			link =Settings.getInstance().config.fileManagement.uploadPath+ Settings.getInstance().config.services.userService.userAvatarPath + link;
			File file = new File(link);
			
			String filename=file.getName();
			jsonObject.addProperty("link", filename);
			File backupFile=new File(filePath+filename);
			try {
				FileUtils.copyDirectory(file, backupFile);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		return jsonObject;
	}

}
