package org.prosolo.services.nodes.data;

import org.prosolo.common.domainmodel.annotation.Tag;
import org.prosolo.common.domainmodel.credential.CredentialType;
import org.prosolo.services.common.observable.StandardObservable;
import org.prosolo.services.nodes.util.TimeUtil;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

public class TitleData implements Serializable {

	private static final long serialVersionUID = -6846469896617514649L;

	private String organizationTitle;
	private String unitTitle;
	private String userGroupTitle;

	private TitleData() { }

	public static TitleData ofOrganizationUnitAndUserGroupTitle(
			String organizationTitle, String unitTitle, String userGroupTitle) {
		TitleData td = new TitleData();
		td.organizationTitle = organizationTitle;
		td.unitTitle = unitTitle;
		td.userGroupTitle = userGroupTitle;
		return td;
	}

	public static TitleData ofOrganizationAndUnitTitle(
			String organizationTitle, String unitTitle) {
		TitleData td = new TitleData();
		td.organizationTitle = organizationTitle;
		td.unitTitle = unitTitle;
		return td;
	}

	public String getOrganizationTitle() {
		return organizationTitle;
	}

	public String getUnitTitle() {
		return unitTitle;
	}

	public String getUserGroupTitle() {
		return userGroupTitle;
	}

}
