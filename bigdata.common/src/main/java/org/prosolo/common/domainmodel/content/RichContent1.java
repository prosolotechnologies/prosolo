/**
 * 
 */
package org.prosolo.common.domainmodel.content;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;

@Embeddable
public class RichContent1 implements Serializable {

	private static final long serialVersionUID = 8739363976698447988L;
	
	private String title;
	private String description;
	private String link;
	private String imageUrl;
	private ImageSize imageSize;
	private Date lastIndexingDate;
	
	private ContentType1 contentType;
	
	public RichContent1() { }
	
	public RichContent1(RichContent1 richContent) {
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
	public ContentType1 getContentType() {
		return contentType;
	}

	public void setContentType(ContentType1 contentType) {
		this.contentType = contentType;
	}

	public Date getLastIndexingDate() {
		return lastIndexingDate;
	}

	public void setLastIndexingDate(Date lastIndexingDate) {
		this.lastIndexingDate = lastIndexingDate;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	@Column(length = 90000)
	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	@Enumerated(EnumType.STRING)
	public ImageSize getImageSize() {
		return imageSize;
	}

	public void setImageSize(ImageSize imageSize) {
		this.imageSize = imageSize;
	}

}
