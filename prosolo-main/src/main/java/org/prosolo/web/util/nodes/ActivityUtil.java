/**
 * 
 */
package org.prosolo.web.util.nodes;

import java.util.Arrays;
import java.util.List;

import org.prosolo.domainmodel.activities.Activity;
import org.prosolo.domainmodel.activities.ResourceActivity;
import org.prosolo.domainmodel.content.ContentType;
import org.prosolo.domainmodel.content.RichContent;
import org.prosolo.web.activitywall.data.ActivityWallData;

/**
 * @author "Nikola Milikic"
 *
 */
public class ActivityUtil {

	public static String getActivityTitle(Activity activity) {
		String title = activity.getTitle();
		
		if (title == null || title.equals("")) {
			if (activity instanceof ResourceActivity) {
				RichContent rc = ((ResourceActivity) activity).getRichContent();
				
				if (rc != null) {
					if (rc.getTitle() != null && !rc.getTitle().equals("")) {
						title = rc.getTitle();
					} else if (rc.getContentType().equals(ContentType.LINK)) {
						title = rc.getLink();
					}
				}
			}
		}
		return title;
	}
	
	public static String extractActivityIds(List<ActivityWallData> activities) {
		if (activities.size() > 0) {
			long[] ids = new long[activities.size()];
			
			for (int i = 0; i < ids.length; i++) {
				ids[i] = activities.get(i).getActivity().getId();
			}
			return Arrays.toString(ids);
		}
		return null;
	}
	
}
