/**
 * 
 */
package org.prosolo.domainmodel.interfacesettings;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.ElementCollection;
import javax.persistence.Entity;

import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import org.hibernate.annotations.Type;
import org.prosolo.domainmodel.general.BaseEntity;

/**
 * @author "Nikola Milikic"
 *
 */
@Entity
public class StickyMenuSettings extends BaseEntity {
	
	private static final long serialVersionUID = 3305381841499077788L;
	
	private boolean goalHelpOff;
	private Set<String> hiddenBubbles;
	
	public StickyMenuSettings() {
		hiddenBubbles = new HashSet<String>();
	}
	
	@Type(type = "true_false")
	public boolean isGoalHelpOff() {
		return goalHelpOff;
	}
	
	public void setGoalHelpOff(boolean goalHelpOff) {
		this.goalHelpOff = goalHelpOff;
	}
	
	@ElementCollection(targetClass = String.class)
	@Cascade({ CascadeType.MERGE, CascadeType.SAVE_UPDATE, CascadeType.DELETE })
	public Set<String> getHiddenBubbles() {
		return hiddenBubbles;
	}
	
	public void setHiddenBubbles(Set<String> hiddenBubbles) {
		this.hiddenBubbles = hiddenBubbles;
	}
	
	public boolean addHiddenBubble(String hiddenBubble) {
		return hiddenBubbles.add(hiddenBubble);
	}
	
}
