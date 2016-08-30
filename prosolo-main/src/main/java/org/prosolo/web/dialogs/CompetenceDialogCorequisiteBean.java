/**
 * 
 */
package org.prosolo.web.dialogs;

import java.io.Serializable;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;

import org.apache.log4j.Logger;
import org.prosolo.common.domainmodel.competences.Competence;
import org.prosolo.web.util.page.PageUtil;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * @author "Nikola Milikic"
 *
 */
@ManagedBean(name="competenceDialogCorequisite")
@Component("competenceDialogCorequisite")
@Scope("view")
public class CompetenceDialogCorequisiteBean implements Serializable {
	
	private static final long serialVersionUID = 4945244051513450491L;
	
	protected static Logger logger = Logger.getLogger(CompetenceDialogCorequisiteBean.class);
	
	@PostConstruct
	public void init() {
		logger.debug("Initializing managed bean " + this.getClass().getSimpleName());
	}

	public void connectCompetence(Competence comp) {
		CompetenceDialogBean competenceDialogBean = PageUtil.getViewScopedBean("competenceDialog", CompetenceDialogBean.class);
		
		if (competenceDialogBean != null) {
			competenceDialogBean.addCorequisite(comp);
		}
	}
}
