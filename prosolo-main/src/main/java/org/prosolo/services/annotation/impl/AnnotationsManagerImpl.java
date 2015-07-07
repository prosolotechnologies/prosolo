package org.prosolo.services.annotation.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.Query;
import org.hibernate.Session;
import org.prosolo.common.domainmodel.activitywall.SocialActivity;
import org.prosolo.common.domainmodel.activitywall.comments.Comment;
import org.prosolo.common.domainmodel.annotation.Annotation;
import org.prosolo.common.domainmodel.annotation.AnnotationType;
import org.prosolo.common.domainmodel.annotation.CommentAnnotation;
import org.prosolo.common.domainmodel.annotation.NodeAnnotation;
import org.prosolo.common.domainmodel.annotation.SimpleAnnotation;
import org.prosolo.common.domainmodel.annotation.SocialActivityAnnotation;
import org.prosolo.common.domainmodel.course.Course;
import org.prosolo.common.domainmodel.general.BaseEntity;
import org.prosolo.common.domainmodel.general.Node;
import org.prosolo.common.domainmodel.user.User;
import org.prosolo.core.hibernate.HibernateUtil;
import org.prosolo.services.annotation.AnnotationManager;
import org.prosolo.services.general.impl.AbstractManagerImpl;
import org.prosolo.util.nodes.AnnotationUtil;
import org.springframework.transaction.annotation.Transactional;

public abstract class AnnotationsManagerImpl extends AbstractManagerImpl implements AnnotationManager {
	
	private static final long serialVersionUID = 2230866591157432352L;
	
	@SuppressWarnings("unused")
	private static Logger logger = Logger.getLogger(AnnotationManager.class);
	
	@Override
	@Transactional (readOnly = false)
	public Annotation getOrCreateAnnotation(String title, AnnotationType type) {
		Annotation ann = getAnnotation(title, type);

		if (ann != null) {
			return ann;
		} else {
			return createAnnotation(title, type);
		}
	}

	@Override
	@Transactional (readOnly = true)
	public Annotation getAnnotation(String title, AnnotationType type) {
		title = title.toLowerCase();
		
		String query = 
				"SELECT DISTINCT ann " +
				"FROM Annotation ann " +
				"WHERE ann.annotationType = :annType " +
					"AND ann.title = :title";
		
		@SuppressWarnings("unchecked")
		List<Annotation> result = persistence.currentManager().createQuery(query).
				setString("annType", type.name()).
				setString("title", title).
				list();
		
		if (result != null && !result.isEmpty()) {
			return result.iterator().next();
		}
		return null;
	}
	
	@Override
	@Transactional (readOnly = false)
	public List<Annotation> getOrCreateAnnotations (Collection<String> titles, AnnotationType type) {
		List<Annotation> annotations = new ArrayList<Annotation>();
		
		if (titles != null) {
			for (String t : titles) {
				annotations.add(getOrCreateAnnotation(t, type));
			}
		}
		
		return annotations;
	}
	
	@Override
	@Transactional (readOnly = false)
	public Annotation createAnnotation(String title, AnnotationType type) {
		Annotation newTag = new SimpleAnnotation(type);
		newTag.setTitle(title);
		newTag = saveEntity(newTag);
		return newTag;
	}

	@Override
	@Transactional (readOnly = false)
	public List<Annotation> parseCSVAnnotationsAndSave(String csvString, Collection<Annotation> existingAnns, 
			AnnotationType type) {
		List<Annotation> newAnnList = new ArrayList<Annotation>();

		if (csvString != null) {
			List<String> stringAnns = AnnotationUtil.getTrimmedSplitStrings(csvString);
			List<String> annsToAdd = new ArrayList<String>(stringAnns);
			
			if (existingAnns != null) {
				for (Annotation a : existingAnns) {
					if (stringAnns.contains(a.getTitle())) {
						annsToAdd.remove(a.getTitle());
						newAnnList.add(a);
						continue;
					}
				}
			}
			
			// adding newly created annotations
			if (!annsToAdd.isEmpty()) {
				for (String annToAdd : annsToAdd) {
					newAnnList.add(getOrCreateAnnotation(annToAdd, type));
				}
			}
		}
		return newAnnList;
	}
	
	@Override
	@Transactional (readOnly = true)
	public <T extends Annotation> List<T> getAnnotationsForNode(BaseEntity resource, AnnotationType annType) {
		String queryString = null;

		if (resource instanceof Node) {
			queryString = 
				"SELECT DISTINCT ann " +
				"FROM Node node " +
				"LEFT JOIN node.annotations ann " +
				"WHERE node = :resource " +
					"AND ann.annotationType = :annType ";
		} else if (resource instanceof Course) {
			queryString = 
				"SELECT DISTINCT ann " +
				"FROM Course course " +
				"LEFT JOIN course.annotations ann " +
				"WHERE course = :resource " +
					"AND ann.annotationType = :annType ";
		}
		
		Query query = persistence.currentManager().createQuery(queryString).
			setEntity("resource", resource).
			setString("annType", annType.name());
		
		@SuppressWarnings("unchecked")
		List<T> result = query.list();
		
		if (result != null && !result.isEmpty()) {
			return result;
		}
		return null;
	}
 
	@Override
	@Transactional (readOnly = true)
	public int annotationCount(BaseEntity resource, AnnotationType annType) {
		return annotationCount(resource, null, annType);
	}

	@Override
	@Transactional (readOnly = true)
	public int annotationCount(BaseEntity resource, User maker, AnnotationType annType) {
		return annotationCount(resource.getId(), resource.getClass(),  maker, annType);
	}
	
	@SuppressWarnings("unchecked")
	@Transactional (readOnly = true)
	public List<Annotation> getAnnotation(long resourceId, Class<? extends BaseEntity> clazz, User maker, AnnotationType annType) {
		String property = getAnnotationAttributeName(clazz);
		
		String queryString = 
			"SELECT ann " +
			"FROM Annotation ann " +
			"LEFT JOIN ann."+property+" res " +
			"LEFT JOIN ann.maker maker ";
		
		queryString +=
			"WHERE res.id = :resourceId " +
				"AND ann.annotationType = :annType ";
		
		if (maker != null) {
			queryString += "AND maker = :maker ";
		}
		
		Query query = persistence.currentManager().createQuery(queryString)
				.setCacheable(true)
				.setLong("resourceId", resourceId)
				.setString("annType", annType.name());
		
		if (maker != null) {
			query.setEntity("maker", maker);
		}
		return query.list();
	}
	
	@Override
	@Transactional (readOnly = true)
	public int annotationCount(long resourceId, Class<? extends BaseEntity> clazz, User maker, AnnotationType annType) {
		return annotationCount(resourceId, clazz, maker, annType, getPersistence().currentManager());
	}
	
	@Override
	@Transactional (readOnly = true)
	public int annotationCount(long resourceId, Class<? extends BaseEntity> clazz, User maker, 
			AnnotationType annType, Session session) {
		
		String property = getAnnotationAttributeName(clazz);
		
		String queryString = 
			"SELECT COUNT(DISTINCT maker) " +
			"FROM Annotation ann " +
			"LEFT JOIN ann."+property+" res " +
			"LEFT JOIN ann.maker maker ";
		
		queryString +=
			"WHERE res.id = :resourceId " +
				"AND ann.annotationType = :annType ";
		
		if (maker != null) {
			queryString += "AND maker = :maker ";
		}
		
		Query query = session.createQuery(queryString)
				.setCacheable(true)
				.setLong("resourceId", resourceId)
				.setString("annType", annType.name());
		
		if (maker != null) {
			query.setEntity("maker", maker);
		}
		Long result = (Long) query.uniqueResult();
		
		return (int) result.longValue();
	}

	@Override
	@Transactional (readOnly = true)
	public boolean isAnnotatedByUser(BaseEntity resource, User user,
			AnnotationType annType) {
		// if count method returns number equals to 0, return false. Else, return true;
		return annotationCount(resource, user, annType) == 0 ? false : true;
	}
	
	@Override
	@Transactional (readOnly = false)
	public boolean removeAnnotation(BaseEntity resource, User maker, AnnotationType annType, boolean deleteAll) {
		resource = HibernateUtil.initializeAndUnproxy(resource);
		return removeAnnotation(resource.getId(), resource.getClass(), maker, annType, deleteAll, getPersistence().currentManager());
	}

	@Override
	@Transactional (readOnly = false)
	public boolean removeAnnotation(BaseEntity resource, User maker, AnnotationType annType, boolean deleteAll, Session session) {
		resource = HibernateUtil.initializeAndUnproxy(resource);
		return removeAnnotation(resource.getId(), resource.getClass(), maker, annType, deleteAll, session);
	}
	
	@Override
	@Transactional (readOnly = false)
	public boolean removeAnnotation(long resourceId, Class<? extends BaseEntity> clazz, User maker, AnnotationType annType, boolean deleteAll, Session session) {
		String property = getAnnotationAttributeName(clazz);
		
		String query = 
			"SELECT ann " +
			"FROM Annotation ann " +
			"LEFT JOIN ann."+property+" res " +
			"LEFT JOIN ann.maker maker " +
			"WHERE res.id = :resourceId " +
				"AND ann.annotationType = :annType " +
				"AND maker = :maker ";
		
		@SuppressWarnings("unchecked")
		List<Annotation> annotations = session.createQuery(query)
			.setLong("resourceId", resourceId)
			.setString("annType", annType.name())
			.setEntity("maker", maker)
			.list();
		
		if (annotations != null && !annotations.isEmpty()) {
			for (Annotation ann : annotations) {
				session.delete(ann);
			}
			return true;
		}
		return false;
	}
	
	@Override
	@Transactional (readOnly = true)
	public List<User> getPeopleWhoAnnotatedResource(BaseEntity resource, AnnotationType annType) {
		resource = HibernateUtil.initializeAndUnproxy(resource);
		return getPeopleWhoAnnotatedResource(resource.getId(), resource.getClass(), annType);
	}
	
	@Override
	@Transactional (readOnly = true)
	public List<User> getPeopleWhoAnnotatedResource(long resourceId, Class<? extends BaseEntity> clazz, AnnotationType annType) {
		String property = getAnnotationAttributeName(clazz);
		
		String query = 
			"SELECT DISTINCT maker " +
			"FROM Annotation ann " +
			"LEFT JOIN ann."+property+" res " +
			"LEFT JOIN ann.maker maker " +
			"WHERE res.id = :resourceId " +
				"AND ann.annotationType = :annType " +
			"ORDER BY ann.dateCreated ASC";
		
		@SuppressWarnings("unchecked")
		List<User> result = persistence.currentManager().createQuery(query)
			.setLong("resourceId", resourceId)
			.setString("annType", annType.name())
			.list();
		
		if (result != null && !result.isEmpty()) {
			return result;
		}

		return null;
	}

	public Annotation createAnnotation(BaseEntity resource, AnnotationType annType) {
		if (resource instanceof Node) {
			return new NodeAnnotation(annType);
		} else if (resource instanceof SocialActivity) {
			return new SocialActivityAnnotation(annType);
		} else if (resource instanceof Comment) {
			return new CommentAnnotation(annType);
		} else {
			return new SimpleAnnotation(annType);
		}
	}
	
	private String getAnnotationAttributeName(Class<? extends BaseEntity> clazz) {
		String property = null;
		
		if (Node.class.isAssignableFrom(clazz)) {
			property = "node";
		} else if (Comment.class.isAssignableFrom(clazz)) {
			property = "comment";
		} else if (SocialActivity.class.isAssignableFrom(clazz)) {
			property = "socialActivity";
		} else {
			String className = clazz.getSimpleName();
			property = className.substring(0, 1).toLowerCase() + className.substring(1, className.length());
		}
		return property;
	}
	
}
