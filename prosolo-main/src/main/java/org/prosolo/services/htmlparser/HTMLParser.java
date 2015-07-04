package org.prosolo.services.htmlparser;

import java.io.IOException;

import org.prosolo.web.activitywall.data.AttachmentPreview;


/**
 * @author "Nikola Milikic"
 *
 */
public interface HTMLParser {

	AttachmentPreview parseUrl(String pageUrl);

	AttachmentPreview parseUrl(String pageUrl, boolean withImages);

	String getFirstImage(String pageUrl) throws IOException;

}