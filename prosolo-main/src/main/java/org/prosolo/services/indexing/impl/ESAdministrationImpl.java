package org.prosolo.services.indexing.impl;

import org.apache.log4j.Logger;
import org.prosolo.common.ESIndexNames;
import org.prosolo.common.config.CommonSettings;
import org.prosolo.common.elasticsearch.ElasticSearchConnector;
import org.prosolo.common.util.ElasticsearchUtil;
import org.prosolo.services.indexing.ESAdministration;
import org.prosolo.services.nodes.OrganizationManager;
import org.prosolo.services.nodes.data.organization.OrganizationData;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

//import static org.elasticsearch.common.io.Streams.copyToStringFromClasspath;

/**
 * @author Zoran Jeremic 2013-06-28
 * 
 */
@Service("org.prosolo.services.indexing.ESAdministration")
public class ESAdministrationImpl implements ESAdministration {

	@Inject private OrganizationManager orgManager;
	private static final long serialVersionUID = 830150223713546004L;
	private static Logger logger = Logger.getLogger(ESAdministrationImpl.class);

	@Override
	public boolean createAllIndexes() {
		try {
			return createIndexes(ESIndexNames.getSystemIndexes(), ESIndexNames.getOrganizationIndexes());
		} catch (Exception e) {
			logger.error("Error", e);
			return false;
		}
	}

	@Override
	public boolean createDBIndexes() {
		try {
			return createIndexes(ESIndexNames.getRecreatableSystemIndexes(), ESIndexNames.getRecreatableOrganizationIndexes());
		} catch (Exception e) {
			logger.error("Error", e);
			return false;
		}
	}

	private boolean createIndexes(List<String> systemIndexes, List<String> organizationIndexes) throws IOException {
		for (String index : systemIndexes) {
			ElasticSearchConnector.getClient().createIndex(index);
		}

		if (!organizationIndexes.isEmpty()) {
            List<OrganizationData> organizations = orgManager.getAllOrganizations(-1, 0, false)
                    .getFoundNodes();
            for (String ind : organizationIndexes) {
                for (OrganizationData o : organizations) {
                    ElasticSearchConnector.getClient().createIndex(ElasticsearchUtil.getOrganizationIndexName(ind, o.getId()));
                }
            }
        }
		return true;
	}

	@Override
	public void createNonrecreatableSystemIndexesIfNotExist() {
		try {
			createIndexes(ESIndexNames.getNonrecreatableSystemIndexes(), Collections.emptyList());
		} catch (Exception e) {
			logger.error("Error", e);
		}
	}

	@Override
	public boolean deleteAllIndexes() {
		try {
			return ElasticSearchConnector.getClient().deleteIndex("*" + CommonSettings.getInstance().config.getNamespaceSufix() + "*");
			//return deleteIndexByName("*" + CommonSettings.getInstance().config.getNamespaceSufix() + "*");
		} catch (Exception e) {
			logger.error("Error", e);
			return false;
		}
	}

	@Override
	public boolean deleteDBIndexes() {
		try {
			//delete only indexes that can be recreated from db
			return ElasticSearchConnector.getClient().deleteIndex(ESIndexNames.getRecreatableIndexes().stream().map(ind -> ind + "*").toArray(String[]::new));
		} catch (Exception e) {
			logger.error("Error", e);
			return false;
		}
	}

	@Override
	public boolean deleteIndex(String... name) {
		try {
			return ElasticSearchConnector.getClient().deleteIndex(name);
		} catch (Exception e) {
			logger.error("Error", e);
			return false;
		}
	}

	@Override
	public boolean createIndex(String name) {
		try {
			return ElasticSearchConnector.getClient().createIndex(name);
		} catch (Exception e) {
			logger.error("Error", e);
			return false;
		}
	}



	@Override
	public boolean createOrganizationIndexes(long organizationId) {
		List<String> indexes = ESIndexNames.getOrganizationIndexes();
		try {
			for (String index : indexes) {
				ElasticSearchConnector.getClient().createIndex(ElasticsearchUtil.getOrganizationIndexName(index, organizationId));
			}
			return true;
		} catch (Exception e) {
			logger.error("Error", e);
			return false;
		}
	}

	@Override
	public boolean deleteOrganizationIndexes(long organizationId) {
		List<String> indexes = ESIndexNames.getOrganizationIndexes();
		try {
			for (String index : indexes) {
				ElasticSearchConnector.getClient().deleteIndex(ElasticsearchUtil.getOrganizationIndexName(index, organizationId));
			}
			return true;
		} catch (Exception e) {
			logger.error("Error", e);
			return false;
		}
	}


}
