package org.prosolo.services.nodes.data.credential;

/**
 * @author stefanvuckovic
 * @date 2018-08-15
 * @since 1.2.0
 */
public class CredentialStageDeliveryNumberData {

    private final long originalCredentialId;
    private final String stageName;
    private final long deliveriesNumber;

    public CredentialStageDeliveryNumberData(long originalCredentialId, String stageName, long deliveriesNumber) {
        this.originalCredentialId = originalCredentialId;
        this.stageName = stageName;
        this.deliveriesNumber= deliveriesNumber;
    }

    public long getOriginalCredentialId() {
        return originalCredentialId;
    }

    public String getStageName() {
        return stageName;
    }

    public long getDeliveriesNumber() {
        return deliveriesNumber;
    }
}
