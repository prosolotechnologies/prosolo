package org.prosolo.services.htmlparser.impl;

import org.apache.log4j.Logger;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.prosolo.common.domainmodel.content.ContentType1;
import org.prosolo.common.domainmodel.content.ImageSize;
import org.prosolo.common.util.net.HTTPSConnectionValidator;
import org.prosolo.services.htmlparser.Image;
import org.prosolo.services.htmlparser.LinkParser;
import org.prosolo.services.media.util.LinkParserException;
import org.prosolo.services.nodes.data.statusWall.AttachmentPreview;
import org.prosolo.services.nodes.data.statusWall.MediaType1;
import org.prosolo.services.util.url.URLUtil;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * @author Bojan Trifkovic
 * @date 2017-09-27
 * @since 1.0.0
 */
public class WebPageParser extends LinkParser {

    private static Logger logger = Logger.getLogger(WebPageParser.class);

    public WebPageParser(Document document) {
        super(document);
    }

    @Override
    public AttachmentPreview parse() throws LinkParserException {
        if (document.baseUri() == null) {
            return null;
        }
        String fullUrl = document.baseUri().trim();

        AttachmentPreview htmlPage = new AttachmentPreview();
        htmlPage.setInitialized(true);

        htmlPage.setLink(fullUrl);
        htmlPage.setDomain(URLUtil.getDomainFromUrl(htmlPage.getLink()));
        htmlPage.setTitle(document.title());
        htmlPage.setDescription(getMetaTag(document, "description"));

        Image image = getLargestImage(document);
        if (image != null) {
            if (image.getWidth() > 350) {
                htmlPage.setImageSize(ImageSize.Large);
                htmlPage.setImageUrl(image.getUrl());
            } else {
                if (image.getWidth() > 100) {
                    htmlPage.setImageSize(ImageSize.Small);
                    htmlPage.setImageUrl(image.getUrl());
                }
            }
        }

        htmlPage.setContentType(ContentType1.LINK);
        htmlPage.setMediaType(MediaType1.Link_Other);

        return htmlPage;
    }
    
    private Image getLargestImage(Document document) {
        Elements metaOgImage = document.getElementsByTag("meta");
        int largestWidth = -1;
        Image largestImage = null;
        String ogDescription = null;
        Image ogImageDetails;
        boolean ogImageExists = false;

        for (Element og : metaOgImage) {
            if ((og.attr("property").toString()).equalsIgnoreCase("og:image")) {
                ogDescription = og.attr("content").toString();
                ogImageExists = true;
                try {
                    ogImageDetails = getImage(ogDescription);
                    if (ogImageDetails != null) {
                        int width = ogImageDetails.getWidth();
                        if (width > largestWidth) {
                            largestWidth = width;
                            largestImage = ogImageDetails;
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        if (!ogImageExists) {
            Elements images = document.select("img[src]");
            for (Element imgElement : images) {
                String imgSrc = imgElement.attr("src");

                if (!imgSrc.endsWith(".gif")) {
                    imgSrc = imgElement.absUrl("src");

                    try {
                        Image imageDetails = getImage(imgSrc);
                        if (imageDetails != null) {

                            //int height = imageDetails.getHeight();
                            int width = imageDetails.getWidth();
                            if (width > largestWidth) {
                                largestWidth = width;
                                largestImage = imageDetails;
                            }
                        }
                    } catch (Exception e) {
                        logger.info("Error fetching image form URL '" + imgSrc + "'." + e.getLocalizedMessage());
                    }
                }
            }
        }
        return largestImage;
    }

    private Image getImage(String resourceFile) throws Exception {
        HttpURLConnection connection = (HttpURLConnection) new URL(resourceFile).openConnection();
        HTTPSConnectionValidator.checkIfHttpsConnection((HttpURLConnection) connection);
        connection.setConnectTimeout(3000);
        connection.setReadTimeout(3000);
        HttpURLConnection.setFollowRedirects(true);
        connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64; rv:10.0.2) Gecko/20100101 Firefox/10.0.2");
        connection.connect();

        InputStream imageInputStream = connection.getInputStream();
        BufferedImage bimg = ImageIO.read(imageInputStream);

        if (bimg != null) {
            int width = bimg.getWidth();
            int height = bimg.getHeight();
            return new Image(resourceFile, width, height);
        } else {
            return null;
        }
    }
}
