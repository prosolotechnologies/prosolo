package org.prosolo.web.useractions;

import java.io.IOException;
import java.io.Serializable;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;

import org.apache.log4j.Logger;
import org.primefaces.event.FileUploadEvent;
import org.primefaces.model.UploadedFile;
import org.prosolo.common.util.string.StringUtil;
import org.prosolo.services.activityWall.SocialActivityHandler;
import org.prosolo.services.htmlparser.HTMLParser;
import org.prosolo.services.interaction.PostManager;
import org.prosolo.services.nodes.DefaultManager;
import org.prosolo.services.nodes.data.activity.attachmentPreview.AttachmentPreview;
import org.prosolo.services.upload.UploadManager;
import org.prosolo.web.ApplicationBean;
import org.prosolo.web.LoggedUserBean;
import org.prosolo.web.useractions.data.NewPostData;
import org.prosolo.web.util.page.PageUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@ManagedBean(name = "postaction")
@Component("postaction")
@Scope("view")
public class PostActionBean implements Serializable {
	
	private static final long serialVersionUID = 1139030868333754289L;

	private static Logger logger = Logger.getLogger(PostActionBean.class);
	
	@Autowired private LoggedUserBean loggedUser;
	@Autowired private DefaultManager defaultManager;
	@Autowired private PostManager postManager;
	@Autowired private HTMLParser htmlParser;
	@Autowired private UploadManager uploadManager;

	@Autowired private ApplicationBean applicationBean;
	@Autowired private SocialActivityHandler socialActivityHandler;
	
	private NewPostData newPostData;
	
	@PostConstruct
	public void init() {
		newPostData = new NewPostData();
	}

	public void resetData(AttachmentPreview attachmentPreview) {
		attachmentPreview.reset();
	}
	
	public void handleFileUpload(FileUploadEvent event) {
    	UploadedFile uploadedFile = event.getFile();
    	
		try {
			AttachmentPreview attachmentPreview = uploadManager.uploadFile(uploadedFile, uploadedFile.getFileName());
			
			newPostData.setAttachmentPreview(attachmentPreview);
		} catch (IOException ioe) {
			logger.error(ioe.getMessage());
			
			PageUtil.fireErrorMessage("The file was not uploaded!");
		}
    }
    
    public void fetchLinkContents(NewPostData newPostData) {
		String linkString = newPostData.getLink();
		
		if (linkString != null && linkString.length() > 0) {
			logger.debug("User "+loggedUser.getUserId()+" is fetching contents of a link: "+linkString);
			
			AttachmentPreview attachmentPreview = htmlParser.extractAttachmentPreview(StringUtil.cleanHtml(linkString.trim()));
			
			if (attachmentPreview != null) {
				newPostData.setAttachmentPreview(attachmentPreview);
			} else {
				newPostData.getAttachmentPreview().setInvalidLink(true);
			}
		}
	}
	
	public void getNextImage(){
		int currentIndex = newPostData.getAttachmentPreview().getSelectedImageIndex();
		int size = newPostData.getAttachmentPreview().getImages().size();
		
		if (currentIndex == -1) {
			// do nothing
		} else if (currentIndex == size-1) {
			currentIndex = 0;
		} else {
			currentIndex++;
		}
		
		newPostData.getAttachmentPreview().setSelectedImageIndex(currentIndex);
		
		if (currentIndex >= 0)
			newPostData.getAttachmentPreview().setImage(newPostData.getAttachmentPreview().getImages().get(currentIndex));
	}
	
	public void getPrevImage(){
		int currentIndex = newPostData.getAttachmentPreview().getSelectedImageIndex();
		int size = newPostData.getAttachmentPreview().getImages().size();
		
		if (currentIndex == -1) {
			// do nothing
		} else if (currentIndex == 0) {
			currentIndex = size - 1;
		} else {
			currentIndex--;
		}
		
		newPostData.getAttachmentPreview().setSelectedImageIndex(currentIndex);
		
		if (currentIndex >= 0)
			newPostData.getAttachmentPreview().setImage(newPostData.getAttachmentPreview().getImages().get(currentIndex));
	}
    
    /*
     * GETTERS / SETTERS
     */
    public NewPostData getNewPostData() {
		return newPostData;
	}

	public void setNewPostData(NewPostData newPostData) {
		this.newPostData = newPostData;
	}

}
