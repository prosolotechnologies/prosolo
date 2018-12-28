package org.prosolo.web.assessments;

import org.apache.log4j.Logger;
import org.prosolo.bigdata.common.exceptions.DbConnectionException;
import org.prosolo.bigdata.common.exceptions.IllegalDataStateException;
import org.prosolo.common.domainmodel.assessment.AssessmentType;
import org.prosolo.services.assessment.data.AssessmentDiscussionMessageData;
import org.prosolo.services.assessment.data.grading.*;
import org.prosolo.services.urlencoding.UrlIdEncoder;
import org.prosolo.web.LoggedUserBean;
import org.prosolo.web.util.page.PageUtil;

import javax.inject.Inject;
import java.io.Serializable;

/**
 * @author stefanvuckovic
 *
 */
public abstract class LearningResourceAssessmentBean implements Serializable {

	private static final long serialVersionUID = -1159085003702303825L;

	private static Logger logger = Logger.getLogger(LearningResourceAssessmentBean.class);

	@Inject
	protected UrlIdEncoder idEncoder;
	@Inject
	protected LoggedUserBean loggedUserBean;

	// adding new comment
	private String newCommentValue;

	public abstract GradeData getGradeData();
	public abstract RubricCriteriaGradeData getRubricForLearningResource();
	public abstract void editComment(String newContent, String activityMessageEncodedId);
	protected abstract void addComment();
	public abstract void updateGrade() throws DbConnectionException, IllegalDataStateException;
	public abstract AssessmentType getType();

	protected void initializeGradeData() {
		try {
			GradeData gradeData = getGradeData();
			gradeData.accept(new GradeDataVisitor<Void>() {

				@Override
				public Void visit(ManualSimpleGradeData gradeData) {
					return null;
				}

				@Override
				public Void visit(AutomaticGradeData gradeData) {
					return null;
				}

				@Override
				public Void visit(ExternalToolAutoGradeData gradeData) {
					return null;
				}

				@Override
				public Void visit(CompletionAutoGradeData gradeData) {
					return null;
				}

				@Override
				public Void visit(NongradedGradeData gradeData) {
					return null;
				}

				@Override
				public Void visit(RubricGradeData gradeData) {
					if (!gradeData.isInitialized()) {
						gradeData.setRubricCriteria(getRubricForLearningResource());
					}
					return null;
				}

				@Override
				public Void visit(DescriptiveRubricGradeData gradeData) {
					return null;
				}

				@Override
				public Void visit(PointRubricGradeData gradeData) {
					return null;
				}
			});
		} catch (DbConnectionException e) {
			logger.error("Error", e);
			PageUtil.fireErrorMessage("Error loading the data. Please refresh the page and try again.");
		}
	}

	public boolean isCurrentUserMessageSender(AssessmentDiscussionMessageData messageData) {
		return idEncoder.encodeId(loggedUserBean.getUserId()).equals(messageData.getEncodedSenderId());
	}

	/*
	ACTIONS
	 */

	public void addCommentToAssessmentDiscussion() {
		try {
			addComment();
			cleanupCommentData();
		} catch (DbConnectionException e) {
			logger.error(e);
			PageUtil.fireErrorMessage("Error while saving a comment. Please try again.");
		}
	}

	private void cleanupCommentData() {
		newCommentValue = "";
	}

	/*
	 * GETTERS / SETTERS
	 */

	public String getNewCommentValue() {
		return newCommentValue;
	}

	public void setNewCommentValue(String newCommentValue) {
		this.newCommentValue = newCommentValue;
	}

}
