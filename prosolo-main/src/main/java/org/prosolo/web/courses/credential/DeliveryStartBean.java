package org.prosolo.web.courses.credential;

import java.io.IOException;
import java.io.Serializable;
import java.util.Date;

import javax.faces.bean.ManagedBean;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.inject.Inject;

import org.apache.log4j.Logger;
import org.prosolo.bigdata.common.exceptions.CompetenceEmptyException;
import org.prosolo.bigdata.common.exceptions.DbConnectionException;
import org.prosolo.bigdata.common.exceptions.IllegalDataStateException;
import org.prosolo.common.event.context.data.LearningContextData;
import org.prosolo.services.event.EventException;
import org.prosolo.services.nodes.CredentialManager;
import org.prosolo.services.urlencoding.UrlIdEncoder;
import org.prosolo.web.LoggedUserBean;
import org.prosolo.web.util.page.PageUtil;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@ManagedBean(name = "deliveryStartBean")
@Component("deliveryStartBean")
@Scope("request")
public class DeliveryStartBean implements Serializable {

	private static final long serialVersionUID = 8787756984308412061L;

	private static Logger logger = Logger.getLogger(DeliveryStartBean.class);
	
	@Inject private LoggedUserBean loggedUser;
	@Inject private CredentialManager credentialManager;
	@Inject private UrlIdEncoder idEncoder;
	
	private Date start;
	private Date end;
	
	/*
	 * ACTIONS
	 */
	
	public void createDelivery(long credId) {
		LearningContextData context = PageUtil.extractLearningContextData();
		try {
			long deliveryId = credentialManager.createCredentialDelivery(credId, start, end, 
					loggedUser.getUserId(), context).getId();

			ExternalContext extContext = FacesContext.getCurrentInstance().getExternalContext();
			try {
				extContext.redirect(extContext.getRequestContextPath() + "/manage/credentials/" 
						+ idEncoder.encodeId(deliveryId) + "/edit");
			} catch (IOException e) {
				logger.error(e);
			}
		} catch (EventException ee) {
			logger.error(ee);
		} catch (DbConnectionException dce) {
			logger.error(dce);
			PageUtil.fireErrorMessage("Error while creating new credential delivery. Please try again.");
		} catch (CompetenceEmptyException cee) {
			logger.error(cee);
			PageUtil.fireErrorMessage("At least one of the credential competencies is empty so it can not be published.");
		} catch (IllegalDataStateException idse) {
			logger.error(idse);
			PageUtil.fireErrorMessage(idse.getMessage());
		}
	}


	/*
	 * GETTERS/SETTERS
	 */
	
	public Date getStart() {
		return start;
	}


	public void setStart(Date start) {
		this.start = start;
	}


	public Date getEnd() {
		return end;
	}


	public void setEnd(Date end) {
		this.end = end;
	}
}
