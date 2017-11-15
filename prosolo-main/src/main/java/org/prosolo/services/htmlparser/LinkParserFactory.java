package org.prosolo.services.htmlparser;

import org.apache.log4j.Logger;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.prosolo.services.htmlparser.impl.SlideShareParser;
import org.prosolo.services.htmlparser.impl.WebPageParser;
import org.prosolo.services.htmlparser.impl.YouTubeParser;
import org.prosolo.services.media.util.LinkParserException;
import org.prosolo.services.util.url.URLUtil;

import java.io.IOException;

/**
 * @author Bojan Trifkovic
 * @date 2017-09-26
 * @since 1.0.0
 */
public class LinkParserFactory {

    private static Logger logger = Logger.getLogger(LinkParserFactory.class);

    public LinkParserFactory(){}

    private String followRedirects(String url) throws LinkParserException {
        Connection parsedUrl = Jsoup.connect(url).followRedirects(true);
        String followUrl = null;

        try {
            Document document = parsedUrl.get();
            followUrl = document.baseUri();
        } catch (Exception e) {
            throw new LinkParserException("URL not valid.");
        }
        return followUrl;
    }

        public static LinkParser buildParser(String url) throws LinkParserException {
        if (!url.matches("^(https?|ftp)://.*$")) {
            url = "http://" + url;
        }
        
        LinkParserFactory linkParserFactory = new LinkParserFactory();
        String pageUrl = linkParserFactory.followRedirects(url);
        Document document;

        try {
            document = Jsoup.connect(pageUrl)
                    .userAgent("Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/535.2 (KHTML, like Gecko) Chrome/15.0.874.120 Safari/535.2")
                    .execute()
                    .parse();
        } catch (IOException e) {
            throw new LinkParserException("URL not valid.");
        }

        if (URLUtil.checkIfYoutubeLink(pageUrl)) {
            return new YouTubeParser(document);
        } else if (URLUtil.checkIfSlidesharePresentationLink(pageUrl)) {
            return new SlideShareParser(document);
        } else {
            return new WebPageParser(document);
        }
    }
}
