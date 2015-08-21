package org.prosolo.bigdata.htmlparsers;

import java.io.IOException;

import org.prosolo.bigdata.htmlparsers.data.AttachmentPreview;

//import org.prosolo.web.activitywall.data.AttachmentPreview;


/**
 * @author "Nikola Milikic"
 *
 */
public interface HTMLParser {

	AttachmentPreview parseUrl(String pageUrl);

	AttachmentPreview parseUrl(String pageUrl, boolean withImages);

	String getFirstImage(String pageUrl) throws IOException;

}