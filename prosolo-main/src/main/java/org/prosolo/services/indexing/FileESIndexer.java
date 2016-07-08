package org.prosolo.services.indexing;

import java.io.InputStream;

import org.prosolo.common.domainmodel.activities.TargetActivity;
import org.prosolo.common.domainmodel.content.RichContent;
import org.prosolo.common.domainmodel.user.User;

/**
 * @author Zoran Jeremic 2013-06-29
 *
 */
public interface FileESIndexer {

	void indexFileForRichContent(InputStream input, RichContent richContent,
			long userId);

	void indexFileForTargetActivity(InputStream input,
			TargetActivity targetActivity, long userId);

	void indexHTMLPage(InputStream input, RichContent richContent, long userId);

}
