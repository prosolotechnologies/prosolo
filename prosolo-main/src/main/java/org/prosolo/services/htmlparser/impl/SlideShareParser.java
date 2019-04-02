package org.prosolo.services.htmlparser.impl;

import org.apache.log4j.Logger;
import org.jsoup.nodes.Document;
import org.primefaces.json.JSONException;
import org.primefaces.json.JSONObject;
import org.prosolo.common.domainmodel.content.ContentType1;
import org.prosolo.services.htmlparser.LinkParser;
import org.prosolo.services.media.util.LinkParserException;
import org.prosolo.services.nodes.data.statusWall.AttachmentPreview;
import org.prosolo.services.nodes.data.statusWall.MediaType1;
import org.prosolo.services.util.GetRequestClient;
import org.prosolo.services.util.impl.GetRequestClientImpl;
import org.prosolo.services.util.url.URLUtil;

import java.text.MessageFormat;

/**
 * @author Bojan Trifkovic
 * @date 2017-09-26
 * @since 1.0.0
 */

public class SlideShareParser extends LinkParser {

    private static Logger logger = Logger.getLogger(SlideShareParser.class);

    private static final String slideShareApiUrlPattern = "https://www.slideshare.net/api/oembed/2?format=json&url={0}";
    private static final String slideShareEmbedUrlPattern = "http://www.slideshare.net/slideshow/embed_code/{0}";

    public SlideShareParser(Document document) {
        super(document);
    }

    @Override
    public AttachmentPreview parse() throws LinkParserException {
        if (document.baseUri() == null) {
            return null;
        }
        AttachmentPreview attachementPreview = new AttachmentPreview();
        attachementPreview.setInitialized(true);

        String fullUrl = document.baseUri().trim();

        attachementPreview.setLink(fullUrl);
        attachementPreview.setDomain(URLUtil.getDomainFromUrl(fullUrl));
        attachementPreview.setTitle(document.title());
        attachementPreview.setDescription(getMetaTag(document, "description"));
        attachementPreview.setContentType(ContentType1.LINK);

        String presentationId = getPresentationId(fullUrl);

        if (presentationId == null) {
            throw new LinkParserException("Could not parse SlideShare URL " + fullUrl);
        }

        String slideUrl = MessageFormat.format(slideShareEmbedUrlPattern, presentationId);

        attachementPreview.setMediaType(MediaType1.Slideshare);
        attachementPreview.setEmbedingLink(slideUrl);

        return attachementPreview;
    }

    private String getPresentationId(String url){
        GetRequestClient client = new GetRequestClientImpl();
        String slideShareApiUrl = MessageFormat.format(slideShareApiUrlPattern, url);
        String jsonResponse = client.sendRestGetRequest(slideShareApiUrl);

        if (jsonResponse != null && !jsonResponse.isEmpty()) {
            try {
                JSONObject jsonObject = new JSONObject(jsonResponse);

                if (jsonObject.has("slideshow_id")) {
                    Object id = jsonObject.get("slideshow_id");
                    return String.valueOf(id);
                }
            } catch (JSONException e) {
                logger.error("Could not parse SlideShare API response " + e);
            }
        }
        return null;
    }
}
