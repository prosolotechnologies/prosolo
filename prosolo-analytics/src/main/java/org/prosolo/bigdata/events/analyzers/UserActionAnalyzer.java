package org.prosolo.bigdata.events.analyzers;

import java.util.Date;

import org.prosolo.bigdata.dal.cassandra.UserObservationsDBManager;
import org.prosolo.bigdata.dal.cassandra.impl.UserObservationsDBManagerImpl;
import org.prosolo.bigdata.events.pojo.LogEvent;
import org.prosolo.bigdata.utils.DateUtil;
import org.prosolo.common.domainmodel.activities.events.EventType;

/**
 * @author Zoran Jeremic, Oct 11, 2015
 *
 */
public class UserActionAnalyzer implements EventAnalyzer {
	UserObservationsDBManager dbManager = new UserObservationsDBManagerImpl();

	@Override
	public void analyzeLogEvent(LogEvent event) {
		long date = DateUtil.getDaysSinceEpoch(event.getTimestamp());
 
		UserActionGroup userAction = null;
		EventType eventType = event.getEventType();
		if (eventType.equals(EventType.LOGIN)) {
			userAction = UserActionGroup.LOGIN;
	 
		} else if (eventType.equals(EventType.NAVIGATE)) {

			userAction = UserActionGroup.LMSUSE;
		} else if (eventType.equals(EventType.SERVICEUSE)) {

			userAction=UserActionGroup.LMSUSE;
		}else if(eventType.equals(EventType.Comment) || eventType.equals(EventType.AddNote)||eventType.equals(EventType.Follow)||eventType.equals(EventType.JOIN_GOAL_REQUEST)
				||eventType.equals(EventType.JOIN_GOAL_INVITATION)||eventType.equals(EventType.SEND_MESSAGE)){
			userAction=UserActionGroup.DISCUSSIONACTION;
		}else if(eventType.equals(EventType.SELECT_COMPETENCE)||eventType.equals(EventType.SELECT_GOAL)){
			userAction=UserActionGroup.RESOURCEACTION;
		}
		if(userAction!=null){
			increaseUserAction(event.getActorId(), date, userAction);
		}

	}

	private void increaseUserAction(long userid, long date,
			UserActionGroup userAction) {

		long login = 0, lmsuse = 0, resourceview = 0, discussionview = 0;

		switch (userAction) {
		case LOGIN:
			login = 1;
			break;
		case LMSUSE: 
			lmsuse = 1;
			break;
		case RESOURCEACTION:
			resourceview = 1;
			break;
		case DISCUSSIONACTION:
			discussionview = 1;
			break;
		default:
			break;
		}
		dbManager.updateUserObservationsCounter(date, userid, login, lmsuse, resourceview, discussionview);
	}

}
