package org.prosolo.services.htmlparser;

import java.io.IOException;

import org.jsoup.nodes.Document;
import org.prosolo.services.nodes.data.activity.attachmentPreview.AttachmentPreview1;

/**
 * @author "Nikola Milikic"
 *
 */
public interface HTMLParser {

	Document parseUrl(String url);

	String getPageTitle(String url);

}