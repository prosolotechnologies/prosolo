package org.prosolo.web.courses.credential;

import org.apache.log4j.Logger;
import org.prosolo.bigdata.common.exceptions.DbConnectionException;
import org.prosolo.bigdata.common.exceptions.IllegalDataStateException;
import org.prosolo.services.nodes.CredentialManager;
import org.prosolo.services.urlencoding.UrlIdEncoder;
import org.prosolo.web.LoggedUserBean;
import org.prosolo.web.util.ResourceBundleUtil;
import org.prosolo.web.util.page.PageSection;
import org.prosolo.web.util.page.PageUtil;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.faces.bean.ManagedBean;
import javax.inject.Inject;
import java.io.Serializable;

@ManagedBean(name = "deliveryStartBean")
@Component("deliveryStartBean")
@Scope("request")
public class DeliveryStartBean implements Serializable {

	private static final long serialVersionUID = 8787756984308412061L;

	private static Logger logger = Logger.getLogger(DeliveryStartBean.class);
	
	@Inject private LoggedUserBean loggedUser;
	@Inject private CredentialManager credentialManager;
	@Inject private UrlIdEncoder idEncoder;

	private long startTime = -1;
	private long endTime = -1;
	
	/*
	 * ACTIONS
	 */
	
	public void createDelivery(long credId) {
		try {
			long deliveryId = credentialManager.createCredentialDelivery(credId, startTime, endTime,
					loggedUser.getUserContext()).getId();
			PageUtil.fireSuccessfulInfoMessageAcrossPages("New " + ResourceBundleUtil.getLabel("delivery").toLowerCase() + " has been created");
			//TODO it would probably be better to move redirection logic to the appropriate beans that know which section to redirect to
			//find out which section we are currently in and redirect there
			PageSection section = PageUtil.getSectionForView();
			if (section == PageSection.MANAGE) {
				PageUtil.redirect("/manage/credentials/" + idEncoder.encodeId(deliveryId) + "/edit");
			} else if (section == PageSection.ADMIN) {
				PageUtil.redirect("/admin/organizations/" + PageUtil.getGetParameter("orgId") + "/units/"
						+ PageUtil.getGetParameter("unitId") + "/credentials");
			}
		} catch (DbConnectionException dce) {
			logger.error(dce);
			String growlMessage = "Error creating new " + ResourceBundleUtil.getMessage("label.credential").toLowerCase() + " " + ResourceBundleUtil.getMessage("label.delivery").toLowerCase() + ". Please try again.";
			PageUtil.fireErrorMessage(growlMessage);
		} catch (IllegalDataStateException idse) {
			logger.error(idse);
			PageUtil.fireErrorMessage(idse.getMessage());
		}
	}


	/*
	 * GETTERS/SETTERS
	 */

	public long getStartTime() {
		return startTime;
	}

	public void setStartTime(long startTime) {
		this.startTime = startTime;
	}

	public long getEndTime() {
		return endTime;
	}

	public void setEndTime(long endTime) {
		this.endTime = endTime;
	}
}
