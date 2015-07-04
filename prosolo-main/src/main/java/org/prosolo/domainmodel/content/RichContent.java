/**
 * 
 */
package org.prosolo.domainmodel.content;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.OneToOne;

import org.prosolo.domainmodel.content.ContentType;
import org.prosolo.domainmodel.content.RichContent;
import org.prosolo.domainmodel.general.BaseEntity;
import org.prosolo.domainmodel.general.Node;
import org.prosolo.domainmodel.organization.VisibilityType;

/**
 * @author "Nikola Milikic"
 * 
 */
@Entity
public class RichContent extends BaseEntity {

	private static final long serialVersionUID = -7465539459092552242L;

	private String link;
	private String imageUrl;
	private Date lastIndexingDate;
	
	// if contentType is RESOURCE
	private Node resource;
	
	private ContentType contentType;
	private VisibilityType visibility;
	
	public RichContent() { }
	
	public RichContent(RichContent richContent) {
		setTitle(richContent.getTitle());
		setDescription(richContent.getDescription());
		this.link = richContent.getLink();
		this.imageUrl = richContent.getImageUrl();
		this.lastIndexingDate = richContent.getLastIndexingDate();
		this.contentType = richContent.getContentType();
	}

	/**
	 * @return the link
	 */
	public String getLink() {
		return link;
	}

	public void setLink(String link) {
		this.link = link;
	}

	public String getImageUrl() {
		return imageUrl;
	}

	public void setImageUrl(String imageUrl) {
		this.imageUrl = imageUrl;
	}

	@Enumerated(EnumType.STRING)
	public ContentType getContentType() {
		return contentType;
	}

	public void setContentType(ContentType contentType) {
		this.contentType = contentType;
	}
	
	@Enumerated(EnumType.STRING)
	public VisibilityType getVisibility() {
		return visibility;
	}

	public void setVisibility(VisibilityType visibility) {
		this.visibility = visibility;
	}

	public Date getLastIndexingDate() {
		return lastIndexingDate;
	}

	public void setLastIndexingDate(Date lastIndexingDate) {
		this.lastIndexingDate = lastIndexingDate;
	}
	
	@OneToOne
	public Node getResource() {
		return resource;
	}

	public void setResource(Node resource) {
		this.resource = resource;
	}

}
