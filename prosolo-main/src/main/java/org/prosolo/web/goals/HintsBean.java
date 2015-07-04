package org.prosolo.web.goals;

import java.io.Serializable;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;

import org.apache.log4j.Logger;
import org.prosolo.services.interfaceSettings.InterfaceSettingsManager;
import org.prosolo.web.LoggedUserBean;
import org.prosolo.web.util.PageUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@ManagedBean(name="goalhintsbean")
@Component("goalhintsbean")
@Scope("session")
@Deprecated
// TODO: Nikola to remove dependency to this class
public class HintsBean implements Serializable {
	
	private static final long serialVersionUID = -4476293771015890728L;
	
	private static Logger logger = Logger.getLogger(HintsBean.class);
	
	@Autowired private LoggedUserBean loggedUser;
	@Autowired private InterfaceSettingsManager interfaceSettingsManager;
	
	private boolean goalHintsOff = true;
	private Set<String> hiddenBubbles;
	
	@PostConstruct
	public void init() {
		logger.debug("Initializing managed bean " + this.getClass().getSimpleName());
//		StickyMenuSettings stickyMessages = interfaceSettingsManager.getOrCreateUserSettings(loggedUser.refreshUser()).getStickyMenuSettings();
//		init(stickyMessages);
	}

//	private void init(StickyMenuSettings stickyMessages) {
//		this.goalHintsOff = stickyMessages.isGoalHelpOff();
//		this.hiddenBubbles = new HashSet<String>(stickyMessages.getHiddenBubbles());
//	}
	
	/*
	 * ACTIONS
	 */
	
	public void toggleGoalHints() {
		goalHintsOff = !goalHintsOff;
		
		logger.debug("User "+loggedUser.getUser()+" is changing goal hints to "+goalHintsOff);
		
		boolean successful = interfaceSettingsManager.changeGoalHints(loggedUser.getInterfaceSettings(), goalHintsOff);
		
		if (successful) {
			if (!goalHintsOff) {
				interfaceSettingsManager.showAllBubbleMessages(loggedUser.getInterfaceSettings());
				hiddenBubbles.clear();
			}
			
			String newStatus = goalHintsOff ? "off" : "on";
			PageUtil.fireSuccessfulInfoMessage("Goal hints are "+newStatus);
			logger.debug("User "+loggedUser.getUser()+" has successfully changed goal hints to "+goalHintsOff);
		}
	}
	
	public void hideBubbleMessage(){
		String messageBubble = PageUtil.getPostParameter("messageBubble");
		
		logger.debug("User "+loggedUser.getUser()+" is hiding bubble named '"+messageBubble+"'");
		
		try {
			if (messageBubble != null) {
				boolean successful = interfaceSettingsManager.hideBubbleMessage(loggedUser.getInterfaceSettings(), messageBubble);
			
				if (successful) {
					hiddenBubbles.add(messageBubble);
				}
			}
		} catch (IllegalArgumentException iae) {
			
		}
	}
	
	public boolean checkIfHidden(String messageBubble) {
		if (goalHintsOff)
			return true;
		
		return hiddenBubbles.contains(messageBubble);
	}
	
	/*
	 * GETTERS/SETTERS
	 */
	public boolean isGoalHintsOff() {
		return goalHintsOff;
	}

	public void setGoalHintsOff(boolean goalHintsOff) {
		this.goalHintsOff = goalHintsOff;
	}
	
}
