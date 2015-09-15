package org.prosolo.services.es.impl;

import static org.elasticsearch.index.query.QueryBuilders.termQuery;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.log4j.Logger;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.NoNodeAvailableException;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.FilterBuilders;
import org.elasticsearch.index.query.FilteredQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.TermFilterBuilder;
import org.elasticsearch.search.SearchHit;
import org.hibernate.NonUniqueResultException;
import org.prosolo.bigdata.common.enums.ESIndexTypes;
import org.prosolo.common.domainmodel.activities.Activity;
import org.prosolo.common.domainmodel.competences.Competence;
import org.prosolo.common.domainmodel.general.Node;
import org.prosolo.common.exceptions.ResourceCouldNotBeLoadedException;
import org.prosolo.services.es.MoreNodesLikeThis;
import org.prosolo.services.general.impl.AbstractManagerImpl;
import org.prosolo.services.indexing.ESIndexNames;
import org.prosolo.services.indexing.ElasticSearchFactory;
import org.prosolo.services.nodes.CompetenceManager;
import org.prosolo.services.nodes.UserManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service("org.prosolo.services.es.MoreNodesLikeThis")
@Transactional
public class MoreNodesLikeThisImpl extends AbstractManagerImpl implements MoreNodesLikeThis{
	
	private static final long serialVersionUID = 6412102520647273287L;
	@Autowired CompetenceManager competenceManager;
	@Autowired UserManager userManager;
	private static Logger logger = Logger
			.getLogger(MoreNodesLikeThisImpl.class.getName());
	
	String[] moreNodesLikeThisFields = new String[]{"description","title"};
	String[] moreUsersLikeThisFields = new String[]{
			"learninggoals.title",
			"learninggoals.description"};
	
	@Override
	public List<Competence> getCompetencesForUserAndLearningGoal(
			String likeText, Collection<Competence> ignoredCompetences,
			int limit) {
		
		List<Competence> foundNodes = new ArrayList<Competence>();
		
		try {
			Client client = ElasticSearchFactory.getClient();
			QueryBuilder qb = null;
			
			qb = QueryBuilders.moreLikeThisQuery(moreNodesLikeThisFields)
					// moreLikeThisFieldQuery(moreNodesLikeThisFields)
					.likeText(likeText)
					.minTermFreq(1)
					.minDocFreq(1)
					.maxQueryTerms(1);
			
			BoolQueryBuilder bQueryBuilder = QueryBuilders.boolQuery();
			bQueryBuilder.should(qb);
	      
			for (Competence ignCompetence : ignoredCompetences) {
				if (ignCompetence != null) {
					bQueryBuilder.mustNot(termQuery("id", ignCompetence.getId()));
				}
			}
			
			SearchResponse sResponse = client
					.prepareSearch(ESIndexNames.INDEX_NODES)
					.setTypes(ESIndexTypes.COMPETENCE)
					.setQuery(bQueryBuilder)
					.setFrom(0)
					.setSize(limit)
					.execute()
					.actionGet();
			
			if (sResponse != null) {
				for (SearchHit hit : sResponse.getHits()) {
					// String url = (String) hit.getSource().get("url");
					int id = (int) hit.getSource().get("id");
					try {
						Competence competence = competenceManager.loadResource(Competence.class, id);
						foundNodes.add(competence);
					} catch (ResourceCouldNotBeLoadedException e) {
						logger.error("Competence was not found: " + id);
					}
				}
			}
		} catch (NoNodeAvailableException e1) {
			logger.warn(e1);
		}
		// client.close();
		return foundNodes;
	}
	@Override
	public List<Activity> getSuggestedActivitiesForCompetence(String likeText, Collection<Long> ignoredActivities, long competenceId, int limit){
		List<Activity> foundNodes = new ArrayList<Activity>();
		
		try {
			Client client = ElasticSearchFactory.getClient();
			QueryBuilder qb = null;
			
			qb = QueryBuilders.moreLikeThisQuery(moreNodesLikeThisFields).likeText(likeText).minTermFreq(1).minDocFreq(1).maxQueryTerms(1);
			
			TermFilterBuilder competenceIdFilterTerm = FilterBuilders.termFilter("competences.id", competenceId);
			
			BoolQueryBuilder bQueryBuilder = QueryBuilders.boolQuery();
			bQueryBuilder.should(qb);
			if (ignoredActivities != null) {
				for (Long ignActivityId : ignoredActivities) {
					if (ignActivityId != null) {
						bQueryBuilder.mustNot(termQuery("id", ignActivityId));
					}
				}
			}
			FilteredQueryBuilder filteredQueryBuilder = QueryBuilders.filteredQuery(bQueryBuilder, competenceIdFilterTerm);
			
			SearchResponse sResponse = client.prepareSearch(ESIndexNames.INDEX_NODES).setTypes(ESIndexTypes.ACTIVITY).setQuery(filteredQueryBuilder)
					.setFrom(0).setSize(limit).execute().actionGet();
			
			if (sResponse != null) {
				for (SearchHit hit : sResponse.getHits()) {
					// String url = (String) hit.getSource().get("url");
					int id = (int) hit.getSource().get("id");
					try {
						Activity activity = competenceManager.loadResource(Activity.class, id);
						foundNodes.add(activity);
					} catch (ResourceCouldNotBeLoadedException e) {
						logger.error("Competence was not found: " + id);
					}
				}
			}
		} catch (NoNodeAvailableException e1) {
			logger.error(e1);
		}
		return foundNodes;
	}
	
	@Override
	public List<Node> getSuggestedResourcesForUser(String likeText,	Collection<Node> ignoredResources, int limit) {
		List<Node> foundNodes = new ArrayList<Node>();
		
		try {
			Client client = ElasticSearchFactory.getClient();
			QueryBuilder qb = null;
			
			qb = QueryBuilders.moreLikeThisQuery(moreNodesLikeThisFields)// moreLikeThisFieldQuery(moreNodesLikeThisFields)
					.likeText(likeText).minTermFreq(1).minDocFreq(1).maxQueryTerms(1);
			
			BoolQueryBuilder bQueryBuilder = QueryBuilders.boolQuery();
			bQueryBuilder.should(qb);
			
			for (Node ignResource : ignoredResources) {
				if (ignResource != null) {
					bQueryBuilder.mustNot(termQuery("id", ignResource.getId()));
				}
			}
			SearchResponse sResponse = client.prepareSearch(ESIndexNames.INDEX_NODES)
					.setTypes(ESIndexTypes.COMPETENCE, ESIndexTypes.LEARNINGGOAL)
					.setQuery(bQueryBuilder)
					.setFrom(0)
					.setSize(limit)
					.execute()
					.actionGet();
			
			if (sResponse != null) {
				for (SearchHit hit : sResponse.getHits()) {
					// String url = (String) hit.getSource().get("url");
					int id = (int) hit.getSource().get("id");
					try {
						Node node = competenceManager.loadResource(Node.class, id);
						if (node != null) {
							foundNodes.add(node);
						}
					} catch (ResourceCouldNotBeLoadedException e) {
						logger.error("Resource was not found: " + id);
					} catch (NonUniqueResultException e) {
						logger.error("NonUnique resource: " + id);
						logger.fatal(e);
					}
				}
			}
		} catch (NoNodeAvailableException e1) {
			logger.error(e1);
		}
		return foundNodes;
	}
	 
}
