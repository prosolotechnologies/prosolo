package org.prosolo.recommendation.dal.impl;

import java.util.Collection;
import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.prosolo.common.domainmodel.annotation.Tag;
import org.prosolo.common.domainmodel.organization.VisibilityType;
import org.prosolo.recommendation.dal.SearchQueries;
import org.prosolo.services.general.impl.AbstractManagerImpl;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service("org.prosolo.recommendation.dal.SearchQueries")
@Transactional(propagation=Propagation.REQUIRED, readOnly=true)
public class SearchQueriesImpl extends AbstractManagerImpl implements SearchQueries{

	private static final long serialVersionUID = 193846832354212208L;
	
	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(SearchQueriesImpl.class.getName());
	
	@Override
	public Session getSession(){
		return persistence.currentManager();
	}
	 
	@Override
	public Collection<Tag> getTagsForUser(String userUri) {
		String query = 
			"SELECT DISTINCT tag " +
			"FROM Node node " +
			"LEFT JOIN node.tags as tag " +
			"LEFT JOIN node.visibility as visibility " +
			"WHERE AND visibility = :visType ";
		
		@SuppressWarnings("unchecked")
		List<Tag> tags = persistence.currentManager().createQuery(query)
				.setParameter("visType", VisibilityType.PUBLIC)
				.list();
		
		if (tags != null && !tags.isEmpty()) {
			return tags;
		}
		return null;
	}
	
	@Override
	public Tag getTagByTitle(String tagLabel){
		String query = 
				"SELECT DISTINCT tag " +
				"FROM Annotation tag "+
				" WHERE tag.content = :label";
		
		@SuppressWarnings("unchecked")
		List<Tag> tags = persistence.currentManager().createQuery(query)
				.setString("label", tagLabel)
				.list();
		
		if (tags != null && !tags.isEmpty()) {
			return tags.iterator().next();
		}
		return null;
	}
	
//	@SuppressWarnings("unchecked")
	/*
	public  Collection<Node> getRelatedResources(String userUri,
			List<SearchResultData> keys, @SuppressWarnings("rawtypes") Class[] resourcesToSearch, int PAGE_SIZE, 
			int pageNumber, PageCount pageCount) throws Exception {
		
		Session session = persistence.currentManager();

		List<String> keysList = new ArrayList<String>();

		for (SearchResultData k : keys) {
			keysList.add(k.uri.toString());
		}
		String queryResources = "SELECT DISTINCT node "	+ getRelatedResourcesQuery();
		List<Node> resources = null;
		Query qQueryResources = session.createQuery(queryResources);
		qQueryResources.setParameter("annType", AnnotationType.Tag);
		qQueryResources.setParameterList("keyUriList", keysList);
		qQueryResources.setParameter("visType", VisibilityType.PUBLIC);

		qQueryResources.setFirstResult(PAGE_SIZE * (pageNumber - 1));
		qQueryResources.setMaxResults(PAGE_SIZE);
		resources = qQueryResources.list();

		if (pageNumber == 1) {
			String queryResourcesNumber = "SELECT cast(COUNT(DISTINCT node) as int) "
					+ getRelatedResourcesQuery();
			Query qQueryResourcesNumber = session.createQuery(queryResourcesNumber);
			qQueryResourcesNumber.setParameter("annType", AnnotationType.Tag);
			qQueryResourcesNumber.setParameterList("keyUriList", keysList);
			qQueryResourcesNumber.setParameter("visType", VisibilityType.PUBLIC);
			Integer resNumber = (Integer) qQueryResourcesNumber.uniqueResult();
			pageCount.setCount(NumbUtils.getNumberOfPages(resNumber, PAGE_SIZE));
		}
		return resources;
	}
	*/

}
