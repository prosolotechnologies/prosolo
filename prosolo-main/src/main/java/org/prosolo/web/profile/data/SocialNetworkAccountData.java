package org.prosolo.web.profile.data;

import java.io.Serializable;

import org.prosolo.common.domainmodel.user.socialNetworks.SocialNetworkAccount;
import org.prosolo.common.domainmodel.user.socialNetworks.SocialNetworkName;
import org.prosolo.web.data.IData;

public class SocialNetworkAccountData implements Serializable, IData {

	private static final long serialVersionUID = 2744838596870425767L;

	private long id;
	private String linkEdit = "";
	private String link = "";
	private boolean changed;
	private SocialNetworkName socialNetworkName;

	public SocialNetworkAccountData() {
		// TODO Auto-generated constructor stub
	}

	public SocialNetworkAccountData(SocialNetworkAccount socialNetworkAccount){
		this.id = socialNetworkAccount.getId();
		this.link = socialNetworkAccount.getLink();
		this.socialNetworkName = socialNetworkAccount.getSocialNetwork();
	}

	public SocialNetworkAccountData(SocialNetworkName socialNetworkName) {
		this.socialNetworkName = socialNetworkName;
	}

	public long getId() {
		return id;
	}

	public void setId(long l) {
		this.id = l;
	}

	public String getLinkEdit() {
		return linkEdit;
	}

	public void setLinkEdit(String linkEdit) {
		if (!linkEdit.equals(this.link)) {
			this.changed = true;
		}
		this.linkEdit = linkEdit;
	}

	public String getLink() {
		return link;
	}

	public void setLink(String link) {
		this.link = link;
	}

	public boolean isChanged() {
		return changed;
	}

	public void setChanged(boolean changed) {
		this.changed = changed;
	}

	public SocialNetworkName getSocialNetworkName() {
		return socialNetworkName;
	}

	public void setSocialNetworkName(SocialNetworkName socialNetworkName) {
		this.socialNetworkName = socialNetworkName;
	}

}
