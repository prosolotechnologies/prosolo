package org.prosolo.services.media.util;

import org.junit.Test;

 

public class SlideShareUtilsTest {

  @Test
  public void convertSlideShareURLToEmbededUrl() {
	  String slideShareUrl="http://www.slideshare.net/jeremycod/consuming-restful-web-services-in-php";
    SlideShareUtils.convertSlideShareURLToEmbededUrl(slideShareUrl, null);
  }
}
