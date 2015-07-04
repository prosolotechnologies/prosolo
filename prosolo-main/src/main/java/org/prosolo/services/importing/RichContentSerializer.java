package org.prosolo.services.importing;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Type;

import org.apache.commons.io.FileUtils;
import org.prosolo.app.Settings;
import org.prosolo.domainmodel.content.ContentType;
import org.prosolo.domainmodel.content.RichContent;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

/**
 *
 * @author Zoran Jeremic Apr 20, 2014
 *
 */

public class RichContentSerializer   implements JsonSerializer<RichContent> {
	String filePath;
	public RichContentSerializer(String path){
		this.filePath=path;
	}
	@Override
	public JsonElement serialize(RichContent richContent, Type arg1,
			JsonSerializationContext context) {
		
		
		final JsonObject jsonObject = new JsonObject();
		jsonObject.addProperty("id", richContent.getId());
		 jsonObject.addProperty("description", richContent.getDescription());
		 jsonObject.addProperty("title", richContent.getTitle());
		 jsonObject.addProperty("contentType", richContent.getContentType().name());
		 String link=richContent.getLink();
		 if(richContent.getContentType().equals(ContentType.UPLOAD)){
			 
				link = link.replaceFirst(Settings.getInstance().config.fileManagement.urlPrefixFolder, "");
				if (link.contains("+")) {
					link = link.replaceAll("\\+", " ");
				}
				link = Settings.getInstance().config.fileManagement.uploadPath + "/"+link;
				
				File file = new File(link);
				
				String filename=file.getName();
				jsonObject.addProperty("filename", filename);
				//jsonObject.addProperty("link", filename);
				File backupFile=new File(filePath+filename);
				try {
					FileUtils.copyFile(file, backupFile);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		 } 
			 jsonObject.addProperty("link", link);
		  
		 
		 jsonObject.addProperty("imageUrl", richContent.getImageUrl());
		 if(richContent.getVisibility()!=null){
			 jsonObject.addProperty("visibility", richContent.getVisibility().name());
		 }
		 return jsonObject;
	}

}
