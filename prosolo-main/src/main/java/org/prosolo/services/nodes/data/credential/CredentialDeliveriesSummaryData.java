package org.prosolo.services.nodes.data.credential;

import java.io.Serializable;

/**
 * Represents the basic, summary data for credential deliveries.
 * For instance, number of deliveries for a credential.
 *
 * @author stefanvuckovic
 * @date 2018-08-15
 * @since 1.2.0
 */
public class CredentialDeliveriesSummaryData implements Serializable {

    private static final long serialVersionUID = 8723217424105475204L;

    private long deliveriesCount;

    public long getDeliveriesCount() {
        return deliveriesCount;
    }

    public CredentialDeliveriesSummaryData() {}
    public CredentialDeliveriesSummaryData(long deliveriesCount) {
        this.deliveriesCount = deliveriesCount;
    }

    public void setDeliveriesCount(long deliveriesCount) {
        this.deliveriesCount = deliveriesCount;
    }
}
