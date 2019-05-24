/**
 * 
 */
package org.prosolo.services.nodes.data.statusWall;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.prosolo.common.domainmodel.content.ContentType1;
import org.prosolo.common.domainmodel.content.ImageSize;

@Getter @Setter
@ToString
public class AttachmentPreview implements Serializable {

	private static final long serialVersionUID = 7923932956660196648L;

	private long id;
	private String title;
	private String description;
	private String link;
	private String domain;
	private String imageUrl;
	private ImageSize imageSize;
	private String fileName;
	private ContentType1 contentType;
	private String embedingLink;
	private String embedId;
	private MediaType1 mediaType;

	// used for new post dialog
	private boolean invalidLink;
	private List<String> images;
	private boolean initialized = false;
	
	public AttachmentPreview() {
		this.images = new ArrayList<>();
	}
	
	public void setImages(List<String> images) {
		this.images = images;
		
		if (images != null && !images.isEmpty()) {
			this.imageUrl = images.get(0);
		}
	}

}
