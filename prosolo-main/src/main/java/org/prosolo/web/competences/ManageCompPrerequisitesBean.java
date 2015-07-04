package org.prosolo.web.competences;

import java.io.Serializable;

import javax.faces.bean.ManagedBean;

import org.prosolo.domainmodel.competences.Competence;
import org.prosolo.web.util.PageUtil;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * @author "Zoran Jeremic"
 * 
 */

@ManagedBean(name = "manageCompetencePrereqBean")
@Component("manageCompetencePrereqBean")
@Scope("view")
public class ManageCompPrerequisitesBean implements Serializable {

	private static final long serialVersionUID = -804947618284183240L;

	public void connectCompetence(Competence comp) {
		ManageCompetenceBean manageCompBean = PageUtil.getViewScopedBean("manageCompetenceBean", ManageCompetenceBean.class);
		
		if (manageCompBean != null && comp != null) {
			manageCompBean.getCompData().addPrerequisite(comp);
			manageCompBean.autosave();
		}
	}

}
