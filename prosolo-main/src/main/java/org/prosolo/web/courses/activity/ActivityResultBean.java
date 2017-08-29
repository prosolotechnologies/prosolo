package org.prosolo.web.courses.activity;

import org.apache.log4j.Logger;
import org.primefaces.event.FileUploadEvent;
import org.primefaces.model.UploadedFile;
import org.prosolo.common.event.context.data.LearningContextData;
import org.prosolo.services.interaction.data.CommentsData;
import org.prosolo.services.nodes.Activity1Manager;
import org.prosolo.services.nodes.data.ActivityResultData;
import org.prosolo.services.nodes.data.ActivityResultType;
import org.prosolo.services.upload.UploadManager;
import org.prosolo.web.LoggedUserBean;
import org.prosolo.web.useractions.CommentBean;
import org.prosolo.web.util.page.PageUtil;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.faces.bean.ManagedBean;
import javax.inject.Inject;
import java.io.Serializable;
import java.util.Date;

@ManagedBean(name = "activityResultBean")
@Component("activityResultBean")
@Scope("request")
public class ActivityResultBean implements Serializable {

	private static final long serialVersionUID = -7294009175388443872L;

	private static Logger logger = Logger.getLogger(ActivityResultBean.class);
	
	@Inject private LoggedUserBean loggedUser;
	@Inject private Activity1Manager activityManager;
	@Inject private CommentBean commentBean;
	@Inject private UploadManager uploadManager;

	public void initializeResultCommentsIfNotInitialized(ActivityResultData result) {
		try {
			CommentsData cd = result.getResultComments();
			if(!cd.isInitialized()) {
				//cd.setInstructor(false);
				commentBean.loadComments(cd);
			}
		} catch(Exception e) {
			logger.error(e);
		}
	}
	
	public void updateTextResponse(ActivityResultData result) {
		try {
			// strip all tags except <br>
			//result.setResult(PostUtil.cleanHTMLTagsExceptBrA(result.getResult()));
			activityManager.updateTextResponse(result.getTargetActivityId(), result.getResult(),
					loggedUser.getUserContext());
			
			PageUtil.fireSuccessfulInfoMessage("The response have been updated");
		} catch(Exception e) {
			logger.error(e);
			PageUtil.fireErrorMessage("Error updating the response");
		}
	}
	
	public void uploadAssignment(FileUploadEvent event, ActivityResultData result) {
		UploadedFile uploadedFile = event.getFile();
		String page = (String) event.getComponent().getAttributes().get("page");
		String lContext = (String) event.getComponent().getAttributes().get("learningContext");
		String service = (String) event.getComponent().getAttributes().get("service");
		try {
			String fileName = uploadedFile.getFileName();
			String fullPath = uploadManager.storeFile(uploadedFile, fileName);
			Date postDate = new Date();
			activityManager.saveResponse(result.getTargetActivityId(), fullPath, postDate,
					ActivityResultType.FILE_UPLOAD, loggedUser.getUserContext(new LearningContextData(page, lContext, service)));
			result.setAssignmentTitle(fileName);
			result.setResult(fullPath);
			result.setResultPostDate(postDate);
		} catch (Exception e) {
			logger.error(e);
			PageUtil.fireErrorMessage("Error while uploading assignment");
		}
	}
	
}
