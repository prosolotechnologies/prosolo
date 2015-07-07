/**
 * 
 */
package org.prosolo.web.communications.evaluation.data;

import org.prosolo.common.domainmodel.workflow.evaluation.Badge;
import org.prosolo.common.domainmodel.workflow.evaluation.BadgeType;

/**
 * @author "Nikola Milikic"
 * 
 */
public class SelectedBadge {

	private BadgeType type;
	private Badge badge;
	private String title;
	private boolean selected;
	
	public SelectedBadge(Badge badge) {
		this.badge = badge;
		this.type = badge.getType();
		this.title = badge.getTitle();
		this.selected = false;
	}

	/**
	 * @param badge2
	 * @param b
	 */
	public SelectedBadge(Badge badge, boolean selected) {
		this(badge);
		this.selected = selected;
	}

	/**
	 * @return the type
	 */
	public BadgeType getType() {
		return type;
	}

	/**
	 * @param type
	 *            the type to set
	 */
	public void setType(BadgeType type) {
		this.type = type;
	}
	
	/**
	 * @return the badge
	 */
	public Badge getBadge() {
		return badge;
	}

	/**
	 * @param badge the badge to set
	 */
	public void setBadge(Badge badge) {
		this.badge = badge;
	}

	/**
	 * @return the title
	 */
	public String getTitle() {
		return title;
	}

	/**
	 * @param title the title to set
	 */
	public void setTitle(String title) {
		this.title = title;
	}

	/**
	 * @return the selected
	 */
	public boolean isSelected() {
		return selected;
	}

	/**
	 * @param selected
	 *            the selected to set
	 */
	public void setSelected(boolean selected) {
		this.selected = selected;
	}
	
	public String getCapitalizedName(){
		String name = type.name();
		return name.substring(0, 1).toUpperCase() + name.substring(1).toLowerCase();
	}

}
