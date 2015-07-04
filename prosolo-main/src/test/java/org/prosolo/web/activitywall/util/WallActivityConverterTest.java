package org.prosolo.web.activitywall.util;

import org.junit.Test;

 

public class WallActivityConverterTest {

  @Test
  public void convertEmbedingLinkForYouTubeVideos() {
	  String youtubeLink="http://www.youtube.com/watch?v=DBNYwxDZ_pA";
	  String googleYoutubeApiLink="https://youtube.googleapis.com/v/";
		int index=youtubeLink.lastIndexOf("v=");
		String videoID=youtubeLink.substring(index+2);
		System.out.println("OUTPUT:"+(googleYoutubeApiLink+videoID));
  }
}
