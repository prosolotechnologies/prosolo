/**
 * 
 */
package org.prosolo.services.nodes.util;

import org.prosolo.common.domainmodel.activities.TargetActivity;
import org.prosolo.common.exceptions.ResourceCouldNotBeLoadedException;
import org.prosolo.services.nodes.DefaultManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author "Nikola Milikic"
 *
 */
@Service("org.prosolo.services.nodes.util.ActivityUtil")
public class ActivityUtil {

	@Autowired private DefaultManager defaultManager;
	
	public boolean equalActities(long actId1, long actId2) throws ResourceCouldNotBeLoadedException {
		TargetActivity act1 = defaultManager.loadResource(TargetActivity.class, actId1);
		TargetActivity act2 = defaultManager.loadResource(TargetActivity.class, actId2);
		
		if (act1 != null && act2 != null) {
			if (act1.equals(act2)) {
				return true;
			}
			
			if (act1.getActivity().equals(act2.getActivity())) {
				return true;
			}
			
			if (act1.getBasedOn() != null && act1.getBasedOn().equals(act2)) {
				return true;
			}
			
			if (act2.getBasedOn() != null) {
				if (act2.getBasedOn().equals(act1)) {
					return true;
				}
				
				if (act1.getBasedOn() != null && act1.getBasedOn().equals(act2.getBasedOn())) {
					return true;
				}
			}
		}
		return false;
	}
}
