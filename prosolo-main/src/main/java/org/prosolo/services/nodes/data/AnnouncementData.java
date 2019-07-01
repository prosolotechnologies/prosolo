package org.prosolo.services.nodes.data;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.prosolo.common.domainmodel.credential.Announcement;
import org.prosolo.common.domainmodel.user.User;
import org.prosolo.common.util.ImageFormat;
import org.prosolo.common.util.date.DateUtil;
import org.prosolo.common.domainmodel.credential.AnnouncementPublishMode;
import org.prosolo.web.util.AvatarUtils;

@Getter
@Setter
@NoArgsConstructor
public class AnnouncementData {

	private long id;
	private String text;
	private String title;
	private AnnouncementPublishMode publishMode;
	private String creatorFullName;
	private String creatorAvatarUrl;
	private long creationTime;

	public AnnouncementData(Announcement announcement, User createdBy) {
		this.id = announcement.getId();
		this.title = announcement.getTitle();
		this.text = announcement.getText();
		this.creatorFullName = createdBy.getFullName();
		this.creatorAvatarUrl = AvatarUtils.getAvatarUrlInFormat(createdBy, ImageFormat.size120x120);
		this.creationTime = DateUtil.getMillisFromDate(announcement.getDateCreated());
		this.publishMode = announcement.getPublishMode();
	}

}
