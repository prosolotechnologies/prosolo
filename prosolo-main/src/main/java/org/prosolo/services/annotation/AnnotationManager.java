package org.prosolo.services.annotation;

import java.util.Collection;
import java.util.List;

import org.hibernate.Session;
import org.prosolo.domainmodel.annotation.Annotation;
import org.prosolo.domainmodel.annotation.AnnotationType;
import org.prosolo.domainmodel.general.BaseEntity;
import org.prosolo.domainmodel.user.User;

public interface AnnotationManager {
	
	Annotation getOrCreateAnnotation (String title, AnnotationType type);

	List<Annotation> getOrCreateAnnotations(Collection<String> titles, AnnotationType type);
	
	Annotation createAnnotation(String title, AnnotationType type);

	List<Annotation> parseCSVAnnotationsAndSave(String csvString, Collection<Annotation> existingAnns, AnnotationType type);
	
	<T extends Annotation> List<T> getAnnotationsForNode(BaseEntity resource, AnnotationType annType);
	
	int annotationCount(BaseEntity resource, AnnotationType annType);
	
	int annotationCount(BaseEntity resource, User user, AnnotationType annType);
	
	boolean isAnnotatedByUser(BaseEntity resource, User user, AnnotationType annType);
	
	boolean removeAnnotation(long resourceId, Class<? extends BaseEntity> clazz, User user, AnnotationType annType, boolean deleteAll, Session session);

	boolean removeAnnotation(BaseEntity resource, User maker, AnnotationType annType, boolean deleteAll);

	boolean removeAnnotation(BaseEntity resource, User maker, AnnotationType annType, boolean deleteAll, Session session);

	int annotationCount(long resourceId, Class<? extends BaseEntity> clazz, User maker, AnnotationType annType);
	
	int annotationCount(long resourceId, Class<? extends BaseEntity> clazz, User maker, AnnotationType annType, Session session);

	List<User> getPeopleWhoAnnotatedResource(BaseEntity resource,	AnnotationType annType);

	List<User> getPeopleWhoAnnotatedResource(long resourceId, Class<? extends BaseEntity> clazz, AnnotationType annType);

	Annotation getAnnotation(String title, AnnotationType type);


}
