package org.prosolo.services.importing;

/**
 * 
 * @author Zoran Jeremic
 * @deprecated since 0.7
 */
@Deprecated
public class CourseSerializer 
// implements JsonSerializer<Course> 
{
//	private boolean includeCourseCompetences = true;
//	private boolean includeAnnotations = true;
//
//	public CourseSerializer(){
//		
//	}
//	public CourseSerializer(boolean includeCourseCompetences, boolean includeAnnotations) {
//		this.includeCourseCompetences = includeCourseCompetences;
//		this.includeAnnotations=includeAnnotations;
//	}
//
//	@Override
//	public JsonElement serialize(Course course, Type typeOfSrc, JsonSerializationContext context) {
//		final JsonObject jsonObject = new JsonObject();
//		
//		jsonObject.addProperty("id", course.getId());
//		Date dateCreated = course.getDateCreated();
//		
//		if (dateCreated != null) {
//			jsonObject.addProperty("dateCreated", dateCreated.getTime());
//		}
//		jsonObject.addProperty("title", course.getTitle());
//		jsonObject.addProperty("description", course.getDescription());
//		jsonObject.addProperty("creatorType", course.getCreatorType().name());
//		jsonObject.addProperty("studentsCanAddNewCompetences", course.isStudentsCanAddNewCompetences());
//		jsonObject.addProperty("published", course.isPublished());
//		
//		if (course.getMaker() != null) {
//			User user = HibernateUtil.initializeAndUnproxy(course.getMaker());
//			final JsonElement maker = context.serialize(user);
//			jsonObject.add("maker", maker);
//		}		
//		
//		if (this.includeAnnotations) {
//			final JsonElement tags = context.serialize(course.getTags());
//			jsonObject.add("tags", tags);
//			
//			final JsonElement hashtags = context.serialize(course.getHashtags());
//			jsonObject.add("hashtags", hashtags);
//		}
//		
//		if (this.includeCourseCompetences) {
//			final JsonElement courseCompetences = context.serialize(course.getCompetences());
//			jsonObject.add("competences", courseCompetences);
//		}
//
//		return jsonObject;
//	}

}
