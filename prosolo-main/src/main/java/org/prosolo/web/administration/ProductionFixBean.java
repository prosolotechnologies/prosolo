package org.prosolo.web.administration;

import org.apache.log4j.Logger;
import org.prosolo.bigdata.common.exceptions.DbConnectionException;
import org.prosolo.services.productionFixes.ProductionFixesService;
import org.prosolo.web.util.page.PageUtil;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.faces.bean.ManagedBean;
import javax.inject.Inject;
import java.io.Serializable;

@ManagedBean(name = "productionFixBean")
@Component("productionFixBean")
@Scope("request")
public class ProductionFixBean implements Serializable {

	private static final long serialVersionUID = -674084842403152327L;

	protected static Logger logger = Logger.getLogger(ProductionFixBean.class);

	@Inject private ProductionFixesService productionFixesService;


	//production fixes

	public void fix() {
		try {
			productionFixesService.deleteUsersCredentials();
		} catch (DbConnectionException e) {
			logger.error(e);
			PageUtil.fireErrorMessage("Error while applying the fix");
		}
	}

}
