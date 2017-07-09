package org.prosolo.bigdata.feeds.impl;

import org.prosolo.bigdata.feeds.ResourceTokenizer;
import org.prosolo.common.domainmodel.user.User;
//import org.springframework.transaction.annotation.Transactional;

public class ResourceTokenizerImpl implements ResourceTokenizer{
	//DiggestGeneratorDAO diggestGeneratorDAO=new DiggestGeneratorDAOImpl();
	
	@Override
	//@Transactional
	public String getTokenizedStringForUser(User user) {
		//TODO reimplement or delete
		return null;
	}

	//TODO reimplement or delete
	//private void getTokenizedStringForUserLearningGoal(User user, TargetLearningGoal tGoal, StringBuffer userTokensBuffer) {

	//}
	
//	private void getTokenizedStringForTagsAndHashtags(Node node, StringBuffer userTokensBuffer) {
//		for (Tag ann : node.getTags()) {
//			getTokenizedStringForAnnotation(ann, userTokensBuffer);
//		}
//		for (Tag ann : node.getHashtags()) {
//			getTokenizedStringForAnnotation(ann, userTokensBuffer);
//		}
//	}
	
//	private void getTokenizedStringForTargetCompetence(TargetCompetence tCompetence, StringBuffer userTokensBuffer) {
//		userTokensBuffer.append(tCompetence.getTitle() + " ");
//		userTokensBuffer.append(tCompetence.getDescription() + " ");
//
//		for (TargetActivity tAct : tCompetence.getTargetActivities()) {
//			getTokenizedStringForTargetActivity(tAct, userTokensBuffer);
//		}
//		getTokenizedStringForTagsAndHashtags(tCompetence, userTokensBuffer);
//		getTokenizedStringForTagsAndHashtags(tCompetence.getCompetence(), userTokensBuffer);
//	}

//	private void getTokenizedStringForAnnotation(Tag tag, StringBuffer userTokensBuffer) {
//		userTokensBuffer.append(tag.getTitle() + " ");
//	}
	
//	private void getTokenizedStringForTargetActivity(TargetActivity tActivity, StringBuffer userTokensBuffer) {
//		userTokensBuffer.append(tActivity.getTitle() + " ");
//	}

}
