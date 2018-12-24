package org.prosolo.services.indexing;


import org.prosolo.common.domainmodel.general.BaseEntity;
import org.prosolo.common.elasticsearch.AbstractESIndexer;

/**
 * @author Zoran Jeremic 2013-06-29
 *
 */
public interface NodeEntityESService extends AbstractESIndexer {

	void saveNodeToES(BaseEntity resource);

}
