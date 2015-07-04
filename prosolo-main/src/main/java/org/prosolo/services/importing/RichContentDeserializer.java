package org.prosolo.services.importing;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.Date;

import org.apache.commons.io.FileUtils;
import org.prosolo.app.Settings;
import org.prosolo.domainmodel.content.ContentType;
import org.prosolo.domainmodel.content.RichContent;
import org.prosolo.core.spring.ServiceLocator;
import org.prosolo.services.nodes.DefaultManager;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

/**
 *
 * @author Zoran Jeremic Apr 21, 2014
 *
 */

public class RichContentDeserializer  implements JsonDeserializer<RichContent> {

	String tempFilesPath;
	public RichContentDeserializer(String path){
		this.tempFilesPath=path;
	}
	@Override
	public RichContent deserialize(JsonElement json, Type typeOfT,
			JsonDeserializationContext context) throws JsonParseException {
		
		final JsonObject jsonObject = json.getAsJsonObject();
		String description=null;
		if(jsonObject.has("description")){
			description=jsonObject.get("description").getAsString();
		}
		String title=jsonObject.get("title").getAsString();
		String cType=jsonObject.get("contentType").getAsString();
		ContentType contentType=ContentType.valueOf(cType);
		RichContent richContent = new RichContent();
		richContent.setDescription(description);
		richContent.setTitle(title);
		richContent.setContentType(contentType);
		
		if(contentType.equals(ContentType.LINK)){
			String link=jsonObject.get("link").getAsString();
			richContent.setLink(link);
			if(jsonObject.has("imageUrl")){
			String imageUrl=jsonObject.get("imageUrl").getAsString();
			
			richContent.setImageUrl(imageUrl);
			}
		}else if(contentType.equals(ContentType.UPLOAD)){
			String link=jsonObject.get("link").getAsString();
			String filename=jsonObject.get("filename").getAsString();
			File inputFile=new File(this.tempFilesPath+"/"+filename);
			link=updateLinkValue(link);
			//String storelink = link.replaceAll("\\+"," ");
			String storelink = link.replaceFirst(Settings.getInstance().config.fileManagement.urlPrefixFolder, "");
			if (storelink.contains("+")) {
				storelink = storelink.replaceAll("\\+", " ");
			}
			storelink = Settings.getInstance().config.fileManagement.uploadPath + storelink;
			File destFile = new File(storelink);
			 try {
				FileUtils.copyFile(inputFile, destFile);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			//String clink = link.replaceAll(" ","\\+");
			richContent.setLink(link);
		}
		richContent=ServiceLocator.getInstance().getService(DefaultManager.class).saveEntity(richContent);
		return richContent;
	}
	private String updateLinkValue(String oldLink){
		String urlprefix=Settings.getInstance().config.fileManagement.urlPrefixFolder;
		if(oldLink.startsWith(urlprefix)){
			oldLink=oldLink.replace(urlprefix, "");
		}
		int firstind=oldLink.indexOf("/")+1;
		int lastind=oldLink.lastIndexOf("/");
		String oldtimestamp=oldLink.substring(firstind, lastind);
		Date date=new Date();
		String newLink=oldLink.replaceFirst(oldtimestamp, String.valueOf(date.getTime()));
		newLink=urlprefix+newLink;
		return newLink;
	}

}
