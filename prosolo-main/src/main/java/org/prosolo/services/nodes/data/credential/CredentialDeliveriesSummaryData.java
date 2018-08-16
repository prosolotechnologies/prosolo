package org.prosolo.services.nodes.data.credential;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * @author stefanvuckovic
 * @date 2018-08-15
 * @since 1.2.0
 */
public class CredentialDeliveriesSummaryData implements Serializable {

    private static final long serialVersionUID = 8723217424105475204L;

    private long deliveriesNumber;

    public long getDeliveriesNumber() {
        return deliveriesNumber;
    }

    public CredentialDeliveriesSummaryData() {}
    public CredentialDeliveriesSummaryData(long deliveriesNumber) {
        this.deliveriesNumber = deliveriesNumber;
    }

    public void setDeliveriesNumber(long deliveriesNumber) {
        this.deliveriesNumber = deliveriesNumber;
    }
}
