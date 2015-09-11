/**
 * 
 */
package org.prosolo.web.dialogs;

import java.io.Serializable;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;

import org.apache.log4j.Logger;
import org.prosolo.common.domainmodel.competences.Competence;
import org.prosolo.web.util.PageUtil;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * @author "Nikola Milikic"
 *
 */
@ManagedBean(name="competenceDialogPrerequisite")
@Component("competenceDialogPrerequisite")
@Scope("view")
public class CompetenceDialogPrerequisiteBean implements Serializable {
	
	private static final long serialVersionUID = 7513326390494971222L;
	
	protected static Logger logger = Logger.getLogger(CompetenceDialogPrerequisiteBean.class);

	@PostConstruct
	public void init() {
		logger.debug("Initializing managed bean " + this.getClass().getSimpleName());
	}

	public void connectCompetence(Competence comp) {
		CompetenceDialogBean competenceDialogBean = PageUtil.getViewScopedBean("competenceDialog", CompetenceDialogBean.class);
		
		if (competenceDialogBean != null) {
			competenceDialogBean.addPrerequisite(comp);
		}
	}
}
