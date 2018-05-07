package org.prosolo.services.nodes.data.organization;

import org.prosolo.common.domainmodel.general.BaseEntity;
import org.prosolo.services.common.observable.StandardObservable;
import org.prosolo.services.nodes.data.ObjectStatus;
import org.prosolo.services.nodes.data.ObjectStatusTransitions;

import java.io.Serializable;
import java.util.Objects;

/**
 * @author stefanvuckovic
 * @date 2018-04-11
 * @since 1.2.0
 */
public class CredentialCategoryData extends StandardObservable implements Serializable {

    private static final long serialVersionUID = -7713124910554801759L;

    private long id;
    private String title;
    private boolean used;
    private ObjectStatus status = ObjectStatus.UP_TO_DATE;

    public CredentialCategoryData(boolean listenChanges) {
        if (listenChanges) {
            startObservingChanges();
        }
    }

    public CredentialCategoryData(long id, String title, boolean listenChanges) {
        this.id = id;
        this.title = title;
        if (listenChanges) {
            startObservingChanges();
        }
    }

    public CredentialCategoryData(long id, String title, boolean used, boolean listenChanges) {
        this.id = id;
        this.title = title;
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

    public boolean isTitleChanged() {
        return changedAttributes.containsKey("title");
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public boolean isUsed() {
        return used;
    }

    public void setUsed(boolean used) {
        this.used = used;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        CredentialCategoryData that = (CredentialCategoryData) o;
        return id == that.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
