package org.prosolo.web.util.youtube;

import java.io.IOException;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;

/*
 * Copyright (c) 2015 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.media.MediaHttpDownloader;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.YouTube.Captions.Download;
import com.google.api.services.youtube.model.Caption;
import com.google.api.services.youtube.model.CaptionListResponse;
import com.google.common.collect.Lists;

/**
 * This sample creates and manages caption tracks by:
 *
 * 1. Uploading a caption track for a video via "captions.insert" method.
 * 2. Getting the caption tracks for a video via "captions.list" method.
 * 3. Updating an existing caption track via "captions.update" method.
 * 4. Download a caption track via "captions.download" method.
 * 5. Deleting an existing caption track via "captions.delete" method.
 *
 * @author Ibrahim Ulukaya
 */
public class Captions {

	private static Logger logger = Logger.getLogger(Captions.class);
    /**
     * Define a global instance of a YouTube object, which will be used to make
     * YouTube Data API requests.
     */
    private YouTube youtube;

    /**
     * Define a global variable that specifies the caption download format.
     */
    private static final String SRT = "srt";


    public void downloadVideoCaption(String videoId) {

        // This OAuth 2.0 access scope allows for full read/write access to the
        // authenticated user's account and requires requests to use an SSL connection.
        List<String> scopes = Lists.newArrayList("https://www.googleapis.com/auth/youtube.force-ssl");

        try {
            // Authorize the request.
            Credential credential = Auth.authorize(scopes, "captions");

            // This object is used to make YouTube Data API requests.
            youtube = new YouTube.Builder(Auth.HTTP_TRANSPORT, Auth.JSON_FACTORY, credential)
                    .setApplicationName("captions").build();
            
            if(youtube != null) {
            	//videoId = "fycEHgt3HMY";
	            List<Caption> captions = listCaptions(videoId);
	            if(!captions.isEmpty()) {
	          	  	Caption cap = captions.get(0);
	          	  	String captionId = cap.getId();
	          	  //	captionId = "TqXDnlamg84o4bX0q2oaHz4nfWZdyiZMOrcuWsSLyPc=";
	          	  	downloadCaption(captionId);
	            }
            }
        } catch (Exception e) {
        	e.printStackTrace();
            logger.error(e);
        }
    }

    /**
     * Downloads a caption track for a YouTube video. (captions.download)
     *
     * @param captionId The id parameter specifies the caption ID for the resource
     * that is being downloaded. In a caption resource, the id property specifies the
     * caption track's ID.
     * @throws IOException
     */
    private void downloadCaption(String captionId) throws IOException {
      // Create an API request to the YouTube Data API's captions.download
      // method to download an existing caption track.
      Download captionDownload = youtube.captions().download(captionId).setTfmt(SRT);

      // Set the download type and add an event listener.
      MediaHttpDownloader downloader = captionDownload.getMediaHttpDownloader();

      // Indicate whether direct media download is enabled. A value of
      // "True" indicates that direct media download is enabled and that
      // the entire media content will be downloaded in a single request.
      // A value of "False," which is the default, indicates that the
      // request will use the resumable media download protocol, which
      // supports the ability to resume a download operation after a
      // network interruption or other transmission failure, saving
      // time and bandwidth in the event of network failures.
      downloader.setDirectDownloadEnabled(false);

      String captions = IOUtils.toString(captionDownload.executeAsInputStream(), "UTF-8");
      System.out.println("captions:\n " + captions);
    }

    /**
     * Returns a list of caption tracks. (captions.listCaptions)
     *
     * @param videoId The videoId parameter instructs the API to return the
     * caption tracks for the video specified by the video id.
     * @throws IOException
     */
    private List<Caption> listCaptions(String videoId) throws IOException {
      // Call the YouTube Data API's captions.list method to
      // retrieve video caption tracks.
      CaptionListResponse captionListResponse = youtube.captions().
          list("id", videoId).execute();

      return captionListResponse.getItems();
    }
}
