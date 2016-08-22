package org.prosolo.web.competences;

import java.io.Serializable;

import javax.faces.bean.ManagedBean;

import org.prosolo.common.domainmodel.competences.Competence;
import org.prosolo.web.util.page.PageUtil;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * @author "Zoran Jeremic"
 * 
 */

@ManagedBean(name = "manageCompetenceCoreqBean")
@Component("manageCompetenceCoreqBean")
@Scope("view")
public class ManageCompCorequisitesBean implements Serializable {

	private static final long serialVersionUID = -305189513775708106L;

	public void connectCompetence(Competence comp) {
		ManageCompetenceBean manageCompBean = PageUtil.getViewScopedBean("manageCompetenceBean", ManageCompetenceBean.class);
		
		if (manageCompBean != null && comp != null) {
			manageCompBean.getCompData().addCorequisite(comp);
			manageCompBean.autosave();
		}
	}

}
