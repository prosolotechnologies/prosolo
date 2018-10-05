package org.prosolo.web.courses.credential;

import org.prosolo.services.nodes.data.credential.CredentialData;

import java.util.List;

/**
 * @author Stefan Vuckovic
 * @date 2017-06-30
 * @since 1.0.0
 */
public class CredentialDeliveryUtil {

    public static void populateCollectionsBasedOnDeliveryStartAndEnd(List<CredentialData> deliveries,
                                                                     List<CredentialData> ongoingDeliveries,
                                                                     List<CredentialData> pendingDeliveries,
                                                                     List<CredentialData> pastDeliveries) {
        //put each delivery in right collection
        for (CredentialData d : deliveries) {
            switch (d.getDeliveryStatus()) {
                case ACTIVE:
                    ongoingDeliveries.add(d);
                    break;
                case PENDING:
                    pendingDeliveries.add(d);
                    break;
                case ENDED:
                    pastDeliveries.add(d);
                    break;
            }
        }
    }
}
