package org.prosolo.services.nodes.data.credential;

import org.prosolo.services.common.observable.StandardObservable;
import org.prosolo.web.util.ResourceBundleUtil;

import java.io.Serializable;

/**
 *
 * Represents a group of credential attributes that identifies a credential (delivery) and is often
 * used together: credential id, title and delivery order (specific only to deliveries).
 *
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

    /**
     * For delivery, returns string in the following format: 'delivery(localized) order: title'.
     * For credential template title is returned.
     * @return
     */
    public String getFullTitle() {
        return order > 0
                ? ResourceBundleUtil.getLabel("delivery") + " " + getFormattedOrder() + ": " + getTitle()
                : getTitle();
    }
}
