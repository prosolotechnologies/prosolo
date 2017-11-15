package org.prosolo.services.htmlparser;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.prosolo.services.media.util.LinkParserException;
import org.prosolo.services.nodes.data.activity.attachmentPreview.AttachmentPreview1;

/**
 * @author Bojan Trifkovic
 * @date 2017-09-26
 * @since 1.0.0
 */
public abstract class LinkParser {

    protected Document document;

    protected LinkParser(Document document) {
        this.document = document;
    }

    public String getMetaTag(Document document, String attr) {
        Elements elements = document.select("meta[name=" + attr + "]");

        for (Element element : elements) {
            final String s = element.attr("content");
            if (s != null)
                return s;
        }

        elements = document.select("meta[property=" + attr + "]");

        for (Element element : elements) {
            final String s = element.attr("content");
            if (s != null)
                return s;
        }
        return null;
    }

    public abstract AttachmentPreview1 parse() throws LinkParserException;
}
