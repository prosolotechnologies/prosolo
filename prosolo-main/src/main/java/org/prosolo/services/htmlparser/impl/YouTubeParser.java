package org.prosolo.services.htmlparser.impl;

import org.jsoup.nodes.Document;
import org.prosolo.common.domainmodel.content.ContentType1;
import org.prosolo.services.htmlparser.LinkParser;
import org.prosolo.services.media.util.LinkParserException;
import org.prosolo.services.nodes.data.statusWall.AttachmentPreview;
import org.prosolo.services.nodes.data.statusWall.MediaType1;
import org.prosolo.services.util.url.URLUtil;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Bojan Trifkovic
 * @date 2017-09-26
 * @since 1.0.0
 */
public class YouTubeParser extends LinkParser {

    public YouTubeParser(Document document) {
        super(document);
    }

    @Override
    public AttachmentPreview parse() throws LinkParserException {
        if (document.baseUri() == null) {
            return null;
        }
        AttachmentPreview attachmentPreview = new AttachmentPreview();
        attachmentPreview.setInitialized(true);

        String fullUrl = document.baseUri().trim();
        attachmentPreview.setLink(fullUrl);
        attachmentPreview.setDomain(URLUtil.getDomainFromUrl(fullUrl));
        attachmentPreview.setTitle(document.title());
        attachmentPreview.setDescription(getMetaTag(document, "description"));
        attachmentPreview.setContentType(ContentType1.LINK);

        String id = null;
        Pattern pattern = Pattern.compile("(?:https?:\\/\\/)?(?:www\\.)?youtu\\.?be(?:\\.com)?\\/?.*(?:watch|embed)?(?:.*v=|v\\/|\\/)([\\w-_]+)(&.*)?",
                Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(fullUrl);
        if (matcher.matches()) {
            id = matcher.group(1);
        }

        attachmentPreview.setEmbedId(id);
        attachmentPreview.setMediaType(MediaType1.Youtube);

        return attachmentPreview;
    }

}
