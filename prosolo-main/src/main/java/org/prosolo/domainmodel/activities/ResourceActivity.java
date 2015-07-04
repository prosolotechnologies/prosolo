package org.prosolo.domainmodel.activities;

import javax.persistence.Entity;
import javax.persistence.OneToOne;

import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import org.prosolo.domainmodel.activities.Activity;
import org.prosolo.domainmodel.content.RichContent;

/**
@author Zoran Jeremic Nov 17, 2013
 */
@Entity
public class ResourceActivity extends Activity {

	private static final long serialVersionUID = -6093005926048217600L;
	
	private RichContent richContent;
	
	@OneToOne
	@Cascade(CascadeType.SAVE_UPDATE)
	public RichContent getRichContent() {
		return richContent;
	}

	public void setRichContent(RichContent richContent) {
		this.richContent = richContent;
	}
}
