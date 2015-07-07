package org.prosolo.services.indexing;

import org.prosolo.common.domainmodel.annotation.Tag;

/**
 * @author Zoran Jeremic 2013-08-21
 *
 */
public interface TagEntityESService {

	public abstract void saveTagToES(Tag tag);

}