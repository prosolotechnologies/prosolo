package org.prosolo.services.annotation.impl;

import org.apache.log4j.Logger;
import org.prosolo.common.domainmodel.annotation.AnnotatedResource;
import org.prosolo.common.domainmodel.annotation.Annotation1;
import org.prosolo.common.domainmodel.annotation.AnnotationType;
import org.prosolo.common.domainmodel.user.User;
import org.prosolo.services.annotation.Annotation1Manager;
import org.prosolo.services.annotation.AnnotationManager;
import org.prosolo.services.general.impl.AbstractManagerImpl;
import org.prosolo.services.lti.exceptions.DbConnectionException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service("org.prosolo.services.interaction.Annotation1Manager")
public class Annotation1ManagerImpl extends AbstractManagerImpl implements Annotation1Manager {
	
	private static final long serialVersionUID = -3320496304223917775L;
	
	private static Logger logger = Logger.getLogger(AnnotationManager.class);
	
	@Override
	@Transactional(readOnly = true)
	public boolean hasUserAnnotatedResource(long userId, long resourceId, AnnotationType annotationType,
			AnnotatedResource annotatedResource) {
		User user = (User) persistence.currentManager().load(User.class, userId);
		
		String query = "SELECT COUNT(ann.id) FROM Annotation1 ann " +
					   "WHERE ann.annotatedResourceId = :resourceId " +
					   "AND ann.annotatedResource = :annotatedResource " +
					   "AND ann.annotationType = :annotationType " +
					   "AND ann.maker = :maker";
		
		long count = (long) persistence.currentManager()
				.createQuery(query)
				.setLong("resourceId", resourceId)
				.setParameter("annotatedResource", annotatedResource)
				.setParameter("annotationType", annotationType)
				.setEntity("maker", user)
				.uniqueResult();
		
		return count == 1;
	}
	
	@Override
	@Transactional(readOnly = false)
	public Annotation1 createAnnotation(long userId, long resourceId, AnnotatedResource resource, 
			AnnotationType annotationType) {
		Annotation1 annotation = new Annotation1();
		User maker = (User) persistence.currentManager().load(User.class, userId);
		annotation.setMaker(maker);
		annotation.setAnnotatedResource(resource);
		annotation.setAnnotatedResourceId(resourceId);
		annotation.setAnnotationType(annotationType);
		
		return saveEntity(annotation);
	}
	
	@Override
	@Transactional(readOnly = false)
	public void deleteAnnotation(long userId, long resourceId, AnnotatedResource resource, 
			AnnotationType annotationType) throws DbConnectionException {
		try {
			String query = "DELETE FROM Annotation1 annotation " +
						   "WHERE annotation.maker = :maker " +
						   "AND annotation.annotatedResourceId = :resourceId " +
						   "AND annotation.annotatedResource = :resource " +
						   "AND annotation.annotationType = :annotationType";
			
			persistence.currentManager()
				.createQuery(query)
				.setLong("resourceId", resourceId)
				.setParameter("resource", resource)
				.setParameter("annotationType", annotationType)
				.executeUpdate();
		} catch(Exception e) {
			logger.error(e);
			e.printStackTrace();
			throw new DbConnectionException("Error while deleting annotation");
		}
	}
	
}
