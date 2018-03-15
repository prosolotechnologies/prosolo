package org.prosolo.web.administration;

import org.apache.log4j.Logger;
import org.prosolo.app.AfterContextLoader;
import org.prosolo.bigdata.common.exceptions.IndexingServiceNotAvailable;
import org.prosolo.services.admin.BulkDataAdministrationService;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.faces.bean.ManagedBean;
import javax.inject.Inject;
import java.io.Serializable;

/**
 * @author Zoran Jeremic Feb 12, 2014
 */
@ManagedBean(name = "bulkDataAdministration")
@Component("bulkDataAdministration")
@Scope("view")
public class BulkDataAdministrationBean implements Serializable {

	private static final long serialVersionUID = -5786790275116348611L;

	@Inject
	private BulkDataAdministrationService bulkDataAdministrationService;

	private static Logger logger = Logger.getLogger(AfterContextLoader.class.getName());

	public void deleteAndReindexElasticSearch() {
		new Thread(() -> {
            try {
                logger.info("Delete and reindex elasticsearch started");
                bulkDataAdministrationService.deleteAndInitElasticSearchIndexes();
                bulkDataAdministrationService.indexDBData();
                logger.info("Delete and reindex elasticsearch finished");
            } catch (IndexingServiceNotAvailable e) {
                logger.error(e);
            }
        }).start();
	}

	public void deleteAndReindexUsers(long orgId) {
		new Thread(() -> {
            try {
                logger.info("Delete and reindex users started");
                bulkDataAdministrationService.deleteAndReindexUsersAndGroups(orgId);
                logger.info("Delete and reindex users finished");
            } catch (IndexingServiceNotAvailable e) {
                logger.error(e);
            }
        }).start();
	}

    public void deleteAndReindexLearningContent(long orgId) {
        new Thread(() -> {
            try {
                logger.info("Delete and reindex learning content (credentials, competencies and learning evidence) started");
                bulkDataAdministrationService.deleteAndReindexLearningContent(orgId);
                logger.info("Delete and reindex learning content finished");
            } catch (IndexingServiceNotAvailable e) {
                logger.error("Error", e);
            }
        }).start();
    }

    public void deleteAndReindexRubrics(long orgId) {
        new Thread(() -> {
            try {
                logger.info("Delete and reindex rubrics started");
                bulkDataAdministrationService.deleteAndReindexRubrics(orgId);
                logger.info("Delete and reindex rubrics finished");
            } catch (IndexingServiceNotAvailable e) {
                logger.error(e);
            }
        }).start();
    }

}
