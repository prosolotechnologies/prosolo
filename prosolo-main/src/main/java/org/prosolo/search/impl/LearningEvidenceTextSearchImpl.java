package org.prosolo.search.impl;

import org.apache.log4j.Logger;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.Operator;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.QueryStringQueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.prosolo.bigdata.common.enums.ESIndexTypes;
import org.prosolo.bigdata.common.exceptions.DbConnectionException;
import org.prosolo.common.ESIndexNames;
import org.prosolo.common.domainmodel.credential.LearningEvidenceType;
import org.prosolo.common.elasticsearch.ElasticSearchConnector;
import org.prosolo.common.util.ElasticsearchUtil;
import org.prosolo.common.util.date.DateUtil;
import org.prosolo.search.util.learningevidence.LearningEvidenceSearchConfig;
import org.prosolo.search.util.learningevidence.LearningEvidenceSearchFilter;
import org.prosolo.search.util.learningevidence.LearningEvidenceSortOption;
import org.prosolo.services.general.impl.AbstractManagerImpl;
import org.prosolo.services.indexing.ESIndexer;
import org.prosolo.services.nodes.LearningEvidenceManager;
import org.prosolo.services.nodes.data.evidence.LearningEvidenceData;
import org.prosolo.util.nodes.AnnotationUtil;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.elasticsearch.index.query.QueryBuilders.termQuery;

/**
 * @author stefanvuckovic
 * @date 2017-12-07
 * @since 1.2.0
 */
@Service("org.prosolo.search.LearningEvidenceTextSearch")
public class LearningEvidenceTextSearchImpl extends AbstractManagerImpl implements org.prosolo.search.LearningEvidenceTextSearch {

    private static final long serialVersionUID = 1088454252639176410L;

    private static Logger logger = Logger.getLogger(LearningEvidenceTextSearchImpl.class);

    //@Inject private ESIndexer esIndexer;
    @Inject private LearningEvidenceManager learningEvidenceManager;

    private int setStart(int page, int limit){
        int start = 0;
        if (page >= 0 && limit > 0) {
            start = page * limit;
        }
        return start;
    }

    @Override
    public PaginatedResult<LearningEvidenceData> searchLearningEvidences(long orgId, long userId, List<Long> evidencesToExclude,
                                                      String searchTerm, int page, int limit) {

        PaginatedResult<LearningEvidenceData> response = new PaginatedResult<>();

        try {
            SearchResponse sResponse = getSearchResponse(
                    orgId,
                    userId,
                    searchTerm,
                    LearningEvidenceSearchConfig.configure(null, LearningEvidenceSortOption.ALPHABETICALLY, false, evidencesToExclude, new String[] {"id"}),
                    page,
                    limit);

            if (sResponse != null) {
                response.setHitsNumber(sResponse.getHits().getTotalHits());

                for (SearchHit hit : sResponse.getHits()) {
                    Long id = ((Integer) hit.getSourceAsMap().get("id")).longValue();

                    try {
                        response.addFoundNode(learningEvidenceManager.getLearningEvidence(id));
                    } catch (DbConnectionException e) {
                        logger.error(e);
                    }
                }
            }
        } catch (Exception e) {
            logger.error("Error", e);
        }
        return response;
    }

    @Override
    public PaginatedResult<LearningEvidenceData> searchUserLearningEvidences(long orgId, long userId, String searchTerm,
                                                                             int page, int limit, LearningEvidenceSearchFilter filter,
                                                                             LearningEvidenceSortOption sortOption) {

        PaginatedResult<LearningEvidenceData> response = new PaginatedResult<>();

        try {
            SearchResponse sResponse = getSearchResponse(
                    orgId,
                    userId,
                    searchTerm,
                    LearningEvidenceSearchConfig.configure(filter, sortOption,true, null, null),
                    page,
                    limit);

            if (sResponse != null) {
                response.setHitsNumber(sResponse.getHits().getTotalHits());

                for (SearchHit hit : sResponse.getHits()) {
                    Map<String, Object> fields = hit.getSourceAsMap();
                    Long id = ((Integer) fields.get("id")).longValue();
                    String name = fields.get("name").toString();
                    LearningEvidenceType type = LearningEvidenceType.valueOf(fields.get("type").toString());
                    Date dateCreated = ElasticsearchUtil.parseDate((String) fields.get("dateCreated"));

                    LearningEvidenceData ev = new LearningEvidenceData();
                    ev.setId(id);
                    ev.setTitle(name);
                    ev.setType(type);
                    ev.setDateCreated(DateUtil.getMillisFromDate(dateCreated));
                    List<Map<String, Object>> evidenceTags = (List<Map<String, Object>>) fields.get("tags");
                    if (evidenceTags != null) {
                        ev.setTags(evidenceTags.stream().map(m -> m.get("title").toString()).collect(Collectors.toSet()));
                        ev.setTagsString(AnnotationUtil.getAnnotationsAsSortedCSVForTagTitles(ev.getTags()));
                    }
                    try {
                        ev.addCompetences(learningEvidenceManager.getCompetencesWithAddedEvidence(ev.getId()));
                    } catch (DbConnectionException e) {
                        logger.error(e);
                    }
                    response.addFoundNode(ev);
                }
            }
        } catch (Exception e) {
            logger.error("Error", e);
        }
        return response;
    }

    private SearchResponse getSearchResponse(long orgId, long userId, String searchTerm,
                                            LearningEvidenceSearchConfig searchConfig, int page, int limit) throws IOException {
        int start = setStart(page, limit);

        BoolQueryBuilder bQueryBuilder = QueryBuilders.boolQuery();
        if (searchTerm != null && !searchTerm.isEmpty()) {
            QueryStringQueryBuilder qb = QueryBuilders
                    .queryStringQuery(ElasticsearchUtil.escapeSpecialChars(searchTerm.toLowerCase()) + "*")
                    .defaultOperator(Operator.AND)
                    .field("name");
            if (searchConfig.isSearchKeywords()) {
                qb.field("tags.title");
            }
            bQueryBuilder.filter(qb);
        }

        bQueryBuilder.filter(termQuery("userId", userId));

        if (searchConfig.getEvidencesToExclude() != null) {
            for (Long evidenceId : searchConfig.getEvidencesToExclude()) {
                bQueryBuilder.mustNot(termQuery("id", evidenceId));
            }
        }

        if (searchConfig.getFilter() != null) {
            LearningEvidenceSearchFilter filter = searchConfig.getFilter();
            if (filter != LearningEvidenceSearchFilter.ALL) {
                bQueryBuilder.filter(termQuery("type", filter.getEvidenceType().name()));
            }
        }

        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder
                .query(bQueryBuilder)
                .from(start)
                .size(limit);

        if (searchConfig.getIncludeFields() != null) {
            searchSourceBuilder.fetchSource(searchConfig.getIncludeFields(), null);
        }
        if (searchConfig.getSortOption() != null) {
            switch (searchConfig.getSortOption()) {
                case NEWEST_FIRST:
                    searchSourceBuilder.sort("dateCreated", SortOrder.DESC);
                    break;
                case ALPHABETICALLY:
                    searchSourceBuilder.sort("name.sort", SortOrder.ASC);
                    break;
            }
        }

        String indexName = ElasticsearchUtil.getOrganizationIndexName(ESIndexNames.INDEX_EVIDENCE, orgId);
        return ElasticSearchConnector.getClient().search(searchSourceBuilder, indexName, ESIndexTypes.EVIDENCE);
    }
}
