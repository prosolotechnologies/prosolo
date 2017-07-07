package org.prosolo.web.courses.credential;

import org.prosolo.services.nodes.data.CredentialData;

import java.util.List;

/**
 * @author Stefan Vuckovic
 * @date 2017-06-30
 * @since 0.7
 */
public class CredentialDeliveryUtil {

    public static void populateCollectionsBasedOnDeliveryStartAndEnd(List<CredentialData> deliveries,
                                                                     List<CredentialData> activeDeliveries,
                                                                     List<CredentialData> pendingDeliveries,
                                                                     List<CredentialData> finishedDeliveries) {
        //put each delivery in right collection
        for (CredentialData d : deliveries) {
            switch (d.getDeliveryStatus()) {
                case ACTIVE:
                    activeDeliveries.add(d);
                    break;
                case PENDING:
                    pendingDeliveries.add(d);
                    break;
                case ENDED:
                    finishedDeliveries.add(d);
                    break;
            }
        }
    }
}
