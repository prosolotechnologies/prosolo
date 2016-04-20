package org.prosolo.services.annotation;

import org.prosolo.common.domainmodel.annotation.AnnotatedResource;
import org.prosolo.common.domainmodel.annotation.Annotation1;
import org.prosolo.common.domainmodel.annotation.AnnotationType;
import org.prosolo.services.lti.exceptions.DbConnectionException;

public interface Annotation1Manager {

	boolean hasUserAnnotatedResource(long userId, long resourceId, AnnotationType annotationType,
			AnnotatedResource annotatedResource);
	
	Annotation1 createAnnotation(long userId, long resourceId, AnnotatedResource resource, 
			AnnotationType annotationType);
	
	void deleteAnnotation(long userId, long resourceId, AnnotatedResource resource, 
			AnnotationType annotationType) throws DbConnectionException;

}
