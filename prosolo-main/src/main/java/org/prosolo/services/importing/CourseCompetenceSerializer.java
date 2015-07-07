package org.prosolo.services.importing;

import java.lang.reflect.Type;
import java.util.Date;

import org.prosolo.common.domainmodel.course.CourseCompetence;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

/**
 *
 * @author Zoran Jeremic Apr 18, 2014
 *
 */
 
public class CourseCompetenceSerializer implements JsonSerializer<CourseCompetence> {

	@Override
	public JsonElement serialize(CourseCompetence courseCompetence, Type typeOfSrc,
			JsonSerializationContext context) {
		final JsonObject jsonObject = new JsonObject();
		//final JsonElement dateCreated=context.serialize(courseCompetence.getDateCreated());
		 jsonObject.addProperty("id", courseCompetence.getId());
		 Date dateCreated=courseCompetence.getDateCreated();
		 if(dateCreated!=null){
			 jsonObject.addProperty("dateCreated", dateCreated.getTime());
		 }
	     jsonObject.addProperty("order", courseCompetence.getOrder());
	     jsonObject.addProperty("daysOffset", courseCompetence.getDaysOffset());
	     jsonObject.addProperty("duration", courseCompetence.getDuration());
	    final JsonElement competence=context.serialize(courseCompetence.getCompetence());
	    jsonObject.add("competence", competence);
	   // jsonObject.addProperty("title", course.getTitle());
	    

	    return jsonObject;
	}

}

