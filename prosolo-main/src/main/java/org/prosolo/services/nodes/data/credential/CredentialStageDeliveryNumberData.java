package org.prosolo.services.nodes.data.credential;

/**
 * Represents a number of deliveries for specified credential and stage.
 *
 * @author stefanvuckovic
 * @date 2018-08-15
 * @since 1.2.0
 */
public class CredentialStageDeliveryNumberData {

    private final long originalCredentialId;
    private final String stageName;
    private final long deliveriesCount;

    public CredentialStageDeliveryNumberData(long originalCredentialId, String stageName, long deliveriesCount) {
        this.originalCredentialId = originalCredentialId;
        this.stageName = stageName;
        this.deliveriesCount = deliveriesCount;
    }

    public long getOriginalCredentialId() {
        return originalCredentialId;
    }

    public String getStageName() {
        return stageName;
    }

    public long getDeliveriesCount() {
        return deliveriesCount;
    }
}
