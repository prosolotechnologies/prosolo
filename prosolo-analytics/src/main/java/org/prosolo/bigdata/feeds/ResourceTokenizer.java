package org.prosolo.bigdata.feeds;

import org.prosolo.common.domainmodel.user.User;

public interface ResourceTokenizer {

	String getTokenizedStringForUser(User user);

}
