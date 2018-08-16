package org.prosolo.services.nodes.data.credential;

import java.util.ArrayList;
import java.util.List;

/**
 * @author stefanvuckovic
 * @date 2018-08-15
 * @since 1.2.0
 */
public class CredentialInStagesDeliveriesSummaryData extends CredentialDeliveriesSummaryData {

    private List<CredentialStageDeliveryNumberData> deliveriesNumberPerStage;

    public CredentialInStagesDeliveriesSummaryData() {}
    public CredentialInStagesDeliveriesSummaryData(long deliveriesNumber) {
        super(deliveriesNumber);
    }

    public List<CredentialStageDeliveryNumberData> getDeliveriesNumberPerStage() {
        return deliveriesNumberPerStage;
    }

    public void addDeliveryNumberForStage(long originalCredentialId, String stageName, long deliveriesNumber) {
        if (deliveriesNumberPerStage == null) {
            deliveriesNumberPerStage = new ArrayList<>();
        }
        deliveriesNumberPerStage.add(new CredentialStageDeliveryNumberData(originalCredentialId, stageName, deliveriesNumber));
    }
}
