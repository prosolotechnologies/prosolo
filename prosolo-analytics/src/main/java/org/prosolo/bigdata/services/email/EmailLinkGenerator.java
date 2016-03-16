package org.prosolo.bigdata.services.email;

import org.prosolo.common.domainmodel.general.BaseEntity;

public interface EmailLinkGenerator {

	<T extends BaseEntity> String getLink(T entity, long userId, String context);

}