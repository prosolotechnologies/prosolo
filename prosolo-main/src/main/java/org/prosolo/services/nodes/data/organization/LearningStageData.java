package org.prosolo.services.nodes.data.organization;

import org.prosolo.services.common.observable.StandardObservable;
import org.prosolo.services.nodes.data.ObjectStatus;
import org.prosolo.services.nodes.data.ObjectStatusTransitions;

import java.io.Serializable;

/**
 * @author stefanvuckovic
 * @date 2017-11-14
 * @since 1.2.0
 */
public class LearningStageData extends StandardObservable implements Serializable {

    private static final long serialVersionUID = 3654673062588736467L;

    private long id;
    private String title;
    private int order;
    private boolean used;
    private ObjectStatus status = ObjectStatus.UP_TO_DATE;

    public LearningStageData(boolean listenChanges) {
        if (listenChanges) {
            startObservingChanges();
        }
    }

    public LearningStageData(long id, String title, int order, boolean used, boolean listenChanges) {
        this.id = id;
        this.title = title;
        this.order = order;
        this.used = used;
        if (listenChanges) {
            startObservingChanges();
        }
    }

    public ObjectStatus getStatus() {
        return status;
    }

    public void setStatus(ObjectStatus status) {
        this.status = status;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        observeAttributeChange("title", this.title, title);
        this.title = title;
        if (listenChanges) {
            if (isTitleChanged()) {
                setStatus(ObjectStatusTransitions.changeTransition(getStatus()));
            } else if (!hasObjectChanged()) {
                setStatus(ObjectStatusTransitions.upToDateTransition(getStatus()));
            }
        }
    }

    public void setOrder(int order) {
        observeAttributeChange("order", this.order, order);
        this.order = order;
        if (listenChanges) {
            if (isOrderChanged()) {
                setStatus(ObjectStatusTransitions.changeTransition(getStatus()));
            } else if (!hasObjectChanged()) {
                setStatus(ObjectStatusTransitions.upToDateTransition(getStatus()));
            }
        }
    }

    public int getOrder() {
        return order;
    }

    public boolean isUsed() {
        return used;
    }

    public void setUsed(boolean used) {
        this.used = used;
    }

    public boolean isTitleChanged() {
        return changedAttributes.containsKey("title");
    }

    public boolean isOrderChanged() {
        return changedAttributes.containsKey("order");
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }
}
