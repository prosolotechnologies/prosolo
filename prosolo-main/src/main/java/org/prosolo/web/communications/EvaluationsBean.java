/**
 * 
 */
package org.prosolo.web.communications;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.faces.bean.ManagedBean;

import org.apache.log4j.Logger;
import org.prosolo.services.nodes.EvaluationManager;
import org.prosolo.web.LoggedUserBean;
import org.prosolo.web.communications.evaluation.data.EvaluationFilter;
import org.prosolo.web.communications.evaluation.data.EvaluationItemData;
import org.prosolo.web.communications.util.EvaluationItemDataConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * @author "Nikola Milikic"
 * 
 */
@ManagedBean(name = "evaluations")
@Component("evaluations")
@Scope("view")
public class EvaluationsBean implements Serializable {

	private static final long serialVersionUID = -2290457206367834377L;

	@SuppressWarnings("unused")
	private static Logger logger = Logger.getLogger(EvaluationsBean.class);

	@Autowired private LoggedUserBean loggedUser;
	@Autowired private EvaluationManager evaluationManager;
	
	private List<EvaluationItemData> evaluations = new ArrayList<EvaluationItemData>();
	private boolean sortDesc = true;
	private EvaluationFilter filter = EvaluationFilter.ALL;
	
	public void init(){
		fetchEvaluations();
	}

	public void fetchEvaluations() {
		evaluations = EvaluationItemDataConverter.convertEvaluationSubmissions(
				evaluationManager.getEvaluationsByUser(loggedUser.getUserId(), sortDesc, filter), 
				loggedUser.getLocale());
	}
	
	/*
	 * ACTIONS
	 */
	
	public void changeSort(boolean desc) {
		this.sortDesc = desc;
		fetchEvaluations();
	}
	
	/*
	 * PAGE PARAMETERS
	 */
	private boolean evaluationAdded;
	
	public boolean isEvaluationAdded() {
		return evaluationAdded;
	}

	public void setEvaluationAdded(boolean evaluationAdded) {
		this.evaluationAdded = evaluationAdded;
	}

	/*
	 * GETTERS / SETTERS
	 */
	public List<EvaluationItemData> getEvaluations() {
		return evaluations;
	}

	public boolean isSortDesc() {
		return sortDesc;
	}

	public EvaluationFilter getFilter() {
		return filter;
	}

	public void setFilter(EvaluationFilter filter) {
		this.filter = filter;
	}

}
