/**
 * 
 */
package org.prosolo.services.rest.courses;

import java.util.List;

import org.prosolo.web.courses.data.CourseCompetenceData;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

/**
 * @author "Nikola Milikic"
 *
 */
public class CourseParser {
	
	public static String printToJson(Object obj) {
		final GsonBuilder builder = new GsonBuilder();
	    final Gson gson = builder.create();
	    
	    return gson.toJson(obj);
	}

	public static JsonArray parseMockCompetences() {
		JsonArray compsArray = new JsonArray();
		
		JsonObject compObj = new JsonObject();
		compObj.addProperty("id", 1);
		compObj.addProperty("name", "Data Analysis");
		
		JsonArray series = new JsonArray();
		
		JsonObject s1 = new JsonObject();
		s1.addProperty("name", "Current");
		s1.addProperty("start", 1);
		s1.addProperty("duration", 5);
		series.add(s1);
		
		JsonObject s2 = new JsonObject();
		s2.addProperty("name", "Average");
		s2.addProperty("start", 1);
		s2.addProperty("duration", 3);
		s2.addProperty("color", "#f0f0f0");
		series.add(s2);
		
		compObj.add("series", series);
		
		compsArray.add(compObj);
		
		return compsArray;
	}
	
	public static JsonArray parseCourseCompetence(List<CourseCompetenceData> competences, List<CourseCompetenceData> originalCompetences, boolean enrolled) {
		JsonArray compsArray = new JsonArray();
		
		int index = 0;
			
		if (originalCompetences != null && !originalCompetences.isEmpty()) {
			for (CourseCompetenceData courseCompetence : originalCompetences) {
				JsonObject compCourseJson = new JsonObject();
				compCourseJson.addProperty("id", index+1);
				compCourseJson.addProperty("compid", courseCompetence.getCompetenceId());
				compCourseJson.addProperty("name", courseCompetence.getTitle());
				// because it is predefined
				compCourseJson.addProperty("predefined", true);
				
				JsonArray series = new JsonArray();
				
				JsonObject s1 = new JsonObject();
				s1.addProperty("name", "Recommended");
				s1.addProperty("start", courseCompetence.getOriginalDaysOffset()+1);
				s1.addProperty("duration", courseCompetence.getOriginalDuration());
				s1.addProperty("color", "#f0f0f0");
				s1.addProperty("fixed", true);
				series.add(s1);
				
				if (enrolled) {
					JsonObject s2 = new JsonObject();
					s2.addProperty("name", "Current");
					s2.addProperty("start", courseCompetence.getModifiedDaysOffset()+1);
					s2.addProperty("duration", courseCompetence.getModifiedDuration());
					s2.addProperty("color", "#E5ECF9");
					series.add(s2);
				}
				
				compCourseJson.add("series", series);
				
				compsArray.add(compCourseJson);
				
				index++;
			}
		}
		
		if (competences != null && !competences.isEmpty()) {
			for (CourseCompetenceData courseCompetence : competences) {
				JsonObject compCourse = new JsonObject();
				compCourse.addProperty("id", index+1);
				compCourse.addProperty("compid", courseCompetence.getCompetenceId());
				compCourse.addProperty("name", courseCompetence.getTitle());
				
				JsonArray series = new JsonArray();
				
				JsonObject s1 = new JsonObject();
				s1.addProperty("name", "Current");
				s1.addProperty("start", courseCompetence.getModifiedDaysOffset()+1);
				s1.addProperty("duration", courseCompetence.getModifiedDuration());
				s1.addProperty("color", "#E5ECF9");
				series.add(s1);
				
				CourseCompetenceData originalCourseCompetence = findCourseCompetence(originalCompetences, courseCompetence.getCompetenceId());
				
				if (originalCourseCompetence != null) {
					JsonObject s2 = new JsonObject();
					s2.addProperty("name", "Recommended");
					s2.addProperty("fixed", true);
					s2.addProperty("start", originalCourseCompetence.getOriginalDaysOffset()+1);
					s2.addProperty("duration", originalCourseCompetence.getOriginalDuration());
					s2.addProperty("color", "#f0f0f0");
					series.add(s2);
				}
				
				compCourse.add("series", series);
				
				compsArray.add(compCourse);
				
				index++;
			}
		}
		return compsArray;
	}
	
	private static CourseCompetenceData findCourseCompetence(List<CourseCompetenceData> courseCompsData, long competenceId) {
		if (courseCompsData != null && !courseCompsData.isEmpty()) {
			for (CourseCompetenceData courseCompetence : courseCompsData) {
				if (courseCompetence.getCompetenceId() == competenceId) {
					return courseCompetence;
				}
			}
		}
		return null;
	}
	
}
