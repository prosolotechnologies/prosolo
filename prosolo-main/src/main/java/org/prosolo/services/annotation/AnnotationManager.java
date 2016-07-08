package org.prosolo.services.annotation;

import java.util.Collection;
import java.util.List;

import org.hibernate.Session;
import org.prosolo.common.domainmodel.annotation.Annotation;
import org.prosolo.common.domainmodel.annotation.AnnotationType;
import org.prosolo.common.domainmodel.general.BaseEntity;
import org.prosolo.common.domainmodel.user.User;

public interface AnnotationManager {
	
	Annotation getOrCreateAnnotation (String title, AnnotationType type);

	List<Annotation> getOrCreateAnnotations(Collection<String> titles, AnnotationType type);
	
	Annotation createAnnotation(String title, AnnotationType type);

	List<Annotation> parseCSVAnnotationsAndSave(String csvString, Collection<Annotation> existingAnns, AnnotationType type);
	
	<T extends Annotation> List<T> getAnnotationsForNode(BaseEntity resource, AnnotationType annType);
	
	int annotationCount(BaseEntity resource, AnnotationType annType);
	
	int annotationCount(BaseEntity resource, long userId, AnnotationType annType);
	
	boolean isAnnotatedByUser(BaseEntity resource, long userId, AnnotationType annType);
	
	boolean removeAnnotation(long resourceId, Class<? extends BaseEntity> clazz, long userId, AnnotationType annType, boolean deleteAll, Session session);

	boolean removeAnnotation(BaseEntity resource, long userId, AnnotationType annType, boolean deleteAll);

	boolean removeAnnotation(BaseEntity resource, long userId, AnnotationType annType, boolean deleteAll, Session session);

	int annotationCount(long resourceId, Class<? extends BaseEntity> clazz, long makerId, AnnotationType annType);
	
	int annotationCount(long resourceId, Class<? extends BaseEntity> clazz, long makerId, AnnotationType annType, Session session);

	List<User> getPeopleWhoAnnotatedResource(BaseEntity resource,	AnnotationType annType);

	List<User> getPeopleWhoAnnotatedResource(long resourceId, Class<? extends BaseEntity> clazz, AnnotationType annType);

	Annotation getAnnotation(String title, AnnotationType type);


}
