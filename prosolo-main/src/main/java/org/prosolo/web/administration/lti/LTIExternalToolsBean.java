/**
 * 
 */
package org.prosolo.web.administration.lti;

import lombok.Getter;
import lombok.Setter;
import org.apache.log4j.Logger;
import org.prosolo.bigdata.common.exceptions.DbConnectionException;
import org.prosolo.search.impl.PaginatedResult;
import org.prosolo.services.lti.LtiToolManager;
import org.prosolo.services.lti.data.ExternalToolFormData;
import org.prosolo.services.nodes.OrganizationManager;
import org.prosolo.services.nodes.UnitManager;
import org.prosolo.services.urlencoding.UrlIdEncoder;
import org.prosolo.web.util.page.PageUtil;
import org.prosolo.web.util.pagination.Paginable;
import org.prosolo.web.util.pagination.PaginationData;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.faces.bean.ManagedBean;
import javax.inject.Inject;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;


/**
 * JSF bean responsible for a page that lists all created LTI tools for given unit
 *
 * @author stefanvuckovic
 * @date 2019-07-12
 * @since 1.3.3
 * 
 */
@ManagedBean(name = "ltiExternalToolsBean")
@Component("ltiExternalToolsBean")
@Scope("view")
public class LTIExternalToolsBean implements Serializable, Paginable {

	private static Logger logger = Logger.getLogger(LTIExternalToolsBean.class);
	
	@Inject private LtiToolManager toolManager;
	@Inject private UrlIdEncoder idEncoder;
	@Inject private UnitManager unitManager;
	@Inject private OrganizationManager organizationManager;

	@Getter @Setter
	private String organizationId;
	@Getter @Setter
	private String unitId;
	@Getter @Setter
	private int page;

	@Getter
	private long decodedOrganizationId;
	@Getter
	private long decodedUnitId;

	@Getter
	private List<ExternalToolFormData> tools = new ArrayList<>();
	@Getter
	private PaginationData paginationData = new PaginationData();
	@Getter
	private String organizationTitle;
	@Getter
	private String unitTitle;

	public void init() {
		decodedOrganizationId = idEncoder.decodeId(organizationId);
		decodedUnitId = idEncoder.decodeId(unitId);
		if (decodedOrganizationId > 0 && decodedUnitId > 0) {
			if (page > 0) {
				paginationData.setPage(page);
			}
			try {
				unitTitle = unitManager.getUnitTitle(decodedOrganizationId, decodedUnitId);
				if (unitTitle != null) {
					organizationTitle = organizationManager.getOrganizationTitle(decodedOrganizationId);
					loadTools();
				} else {
					PageUtil.notFound();
				}
			} catch (DbConnectionException e) {
				logger.error("error", e);
				PageUtil.fireErrorMessage("Error loading the page");
			}
		} else {
			PageUtil.notFound();
		}
	}

	private void loadTools() {
		tools.clear();
		PaginatedResult<ExternalToolFormData> res = toolManager.getPaginatedTools(
				decodedOrganizationId, decodedUnitId, paginationData.getLimit(), (paginationData.getPage() - 1) * paginationData.getLimit());
		tools = res.getFoundNodes();
		this.paginationData.update((int) res.getHitsNumber());
	}

	@Override
	public void changePage(int page) {
		if (this.paginationData.getPage() != page) {
			try {
				this.paginationData.setPage(page);
				loadTools();
			} catch (Exception e) {
				logger.error("error", e);
				PageUtil.fireErrorMessage("Error loading the data");
			}
		}

	}
}
