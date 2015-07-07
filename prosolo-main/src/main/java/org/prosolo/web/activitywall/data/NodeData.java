/**
 * 
 */
package org.prosolo.web.activitywall.data;

import java.io.Serializable;

import org.prosolo.core.hibernate.HibernateUtil;
import org.prosolo.common.domainmodel.activities.ExternalToolActivity;
import org.prosolo.common.domainmodel.general.BaseEntity;

/**
 * @author "Nikola Milikic"
 * 
 */
public class NodeData implements Serializable {

	private static final long serialVersionUID = 4375176115398598090L;

	private long id;
	private String shortType;
	private Class<? extends BaseEntity> clazz;
	private String title;
	private String description;
	private String uri;
	private String launchUrl;
	private String sharedSecret;
	private String consumerKey;
	
	public NodeData() { }
	
	public NodeData(BaseEntity resource) {
	 
	 	resource=HibernateUtil.initializeAndUnproxy(resource);
		this.id = resource.getId();
		this.clazz = resource.getClass();
		this.title = resource.getTitle();
		this.description = resource.getDescription();
			
		if (resource instanceof ExternalToolActivity) {
			this.launchUrl = ((ExternalToolActivity) resource).getLaunchUrl();
			this.sharedSecret = ((ExternalToolActivity) resource).getSharedSecret();
			this.setConsumerKey(((ExternalToolActivity) resource).getConsumerKey());
		}
	}
	
	public NodeData(long id, String shortType, Class<? extends BaseEntity> clazz, String title) {
		super();
		this.id = id;
		this.shortType = shortType;
		this.clazz = clazz;
		this.title = title;
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}
	
	public String getShortType() {
		return shortType;
	}

	public void setShortType(String shortType) {
		this.shortType = shortType;
	}

	public Class<? extends BaseEntity> getClazz() {
		return clazz;
	}

	public void setClazz(Class<? extends BaseEntity> clazz) {
		this.clazz = clazz;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}
	
	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getUri() {
		return uri;
	}

	public void setUri(String uri) {
		this.uri = uri;
	}

	@Override
	public String toString() {
		return "NodeData [id=" + id + ", shortType=" + shortType + ", clazz="
				+ clazz + ", title=" + title + ", uri=" + uri + "]";
	}

	public String getLaunchUrl() {
		return launchUrl;
	}

	public void setLaunchUrl(String launchUrl) {
		this.launchUrl = launchUrl;
	}

	public String getSharedSecret() {
		return sharedSecret;
	}

	public void setSharedSecret(String sharedSecret) {
		this.sharedSecret = sharedSecret;
	}

	public String getConsumerKey() {
		return consumerKey;
	}

	public void setConsumerKey(String consumerKey) {
		this.consumerKey = consumerKey;
	}
	
}
