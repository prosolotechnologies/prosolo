package org.prosolo.services.htmlparser;

import java.io.IOException;

import org.jsoup.nodes.Document;
import org.prosolo.services.nodes.data.activity.attachmentPreview.AttachmentPreview1;

/**
 * @author "Nikola Milikic"
 *
 */
public interface HTMLParser {

	boolean checkIfValidUrl(String url);

	Document parseUrl(String url);

	String getFirstImage(String url) throws IOException;

	String getPageTitle(String url);
	
	AttachmentPreview1 extractAttachmentPreview1(String url);

}