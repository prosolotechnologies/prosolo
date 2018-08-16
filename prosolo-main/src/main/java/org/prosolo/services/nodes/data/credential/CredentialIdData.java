package org.prosolo.services.nodes.data.credential;

import org.prosolo.services.common.observable.StandardObservable;

import java.io.Serializable;

/**
 * @author stefanvuckovic
 * @date 2018-08-16
 * @since 1.2.0
 */
public class CredentialIdData extends StandardObservable implements Serializable {

    private long id;
    private String title;
    private int order;

    public CredentialIdData(boolean listenChanges) {
        this.listenChanges = listenChanges;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        observeAttributeChange("title", this.title, title);
        this.title = title;
    }

    public int getOrder() {
        return order;
    }

    public void setOrder(int order) {
        this.order = order;
    }

    public String getFormattedOrder() {
        return String.format("%02d", order);
    }
}
