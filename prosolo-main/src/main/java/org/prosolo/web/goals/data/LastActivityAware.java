package org.prosolo.web.goals.data;

import java.util.Date;

public interface LastActivityAware {

	Date getLastActivity();

	void setLastActivity(Date lastActivity);
}
