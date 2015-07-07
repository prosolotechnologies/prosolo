package org.prosolo.services.indexing;


import org.prosolo.common.domainmodel.general.BaseEntity;

/**
 * @author Zoran Jeremic 2013-06-29
 *
 */
public interface NodeEntityESService extends AbstractBaseEntityESService {

	void saveNodeToES(BaseEntity resource);

}
