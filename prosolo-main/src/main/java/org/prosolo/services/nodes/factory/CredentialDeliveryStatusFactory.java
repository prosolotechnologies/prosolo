package org.prosolo.services.nodes.factory;

import java.util.Date;

import org.prosolo.services.nodes.data.credential.CredentialDeliveryStatus;
import org.springframework.stereotype.Component;

@Component
public class CredentialDeliveryStatusFactory {

	public CredentialDeliveryStatus getDeliveryStatus(Date start, Date end) {
		Date now = new Date();
		//if delivery start is not set or is in future then delivery is pending
		if (start == null || start.after(now)) {
			return CredentialDeliveryStatus.PENDING;
		} 
		//if delivery start is in past and delivery end is not set or is in future delivery is active
		else if (end == null || end.after(now)) {
			return CredentialDeliveryStatus.ACTIVE;
		} else {
			return CredentialDeliveryStatus.ENDED;
		}
	}
}
