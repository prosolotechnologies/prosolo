package org.prosolo.services.nodes.data.credential;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a basic, summary data for credential deliveries from all stages in total, as well as
 * summary for each learning stage.
 *
 * @author stefanvuckovic
 * @date 2018-08-15
 * @since 1.2.0
 */
public class CredentialInStagesDeliveriesSummaryData extends CredentialDeliveriesSummaryData {

    private List<CredentialStageDeliveryNumberData> deliveriesCountPerStage;

    public CredentialInStagesDeliveriesSummaryData() {}
    public CredentialInStagesDeliveriesSummaryData(long deliveriesCount) {
        super(deliveriesCount);
    }

    public List<CredentialStageDeliveryNumberData> getDeliveriesCountPerStage() {
        return deliveriesCountPerStage;
    }

    public void addDeliveriesCountForStage(long originalCredentialId, String stageName, long deliveriesCount) {
        if (deliveriesCountPerStage == null) {
            deliveriesCountPerStage = new ArrayList<>();
        }
        deliveriesCountPerStage.add(new CredentialStageDeliveryNumberData(originalCredentialId, stageName, deliveriesCount));
    }
}
