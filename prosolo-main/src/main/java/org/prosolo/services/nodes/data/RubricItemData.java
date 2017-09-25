package org.prosolo.services.nodes.data;

import org.prosolo.services.common.observable.StandardObservable;

import java.io.Serializable;
import java.util.List;

public class RubricItemData extends StandardObservable implements Serializable {

	private static final long serialVersionUID = 1685589109425362221L;

	private long id;
	private String name;
	private double points;
	private int order;

	/*
	true if this item is synced with other items - for level: rubric level is added to the collection of levels in
	each category; for category: levels are added to collection of levels for given category.
	 */
	private boolean itemSynced;

	private ObjectStatus status = ObjectStatus.UP_TO_DATE;

	public RubricItemData() {}

	public RubricItemData(ObjectStatus status) {
		this.status = status;
	}

	public RubricItemData(long id, String name, double points, int order) {
		this.id = id;
		this.name = name;
		this.points = points;
		this.order = order;
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		observeAttributeChange("name", this.name, name);
		this.name = name;
		if (isNameChanged()) {
			setStatus(ObjectStatusTransitions.changeTransition(getStatus()));
		} else if (!hasObjectChanged()) {
			setStatus(ObjectStatusTransitions.upToDateTransition(getStatus()));
		}
	}

	public double getPoints() {
		return points;
	}

	public void setPoints(double points) {
		observeAttributeChange("points", this.points, points);
		this.points = points;
		if (arePointsChanged()) {
			setStatus(ObjectStatusTransitions.changeTransition(getStatus()));
		} else if (!hasObjectChanged()) {
			setStatus(ObjectStatusTransitions.upToDateTransition(getStatus()));
		}
	}

	public int getOrder() {
		return order;
	}

	public void setOrder(int order) {
		observeAttributeChange("order", this.order, order);
		this.order = order;
		if (isOrderChanged()) {
			setStatus(ObjectStatusTransitions.changeTransition(getStatus()));
		} else if (!hasObjectChanged()) {
			setStatus(ObjectStatusTransitions.upToDateTransition(getStatus()));
		}
	}

	public ObjectStatus getStatus() {
		return status;
	}

	public void setStatus(ObjectStatus status) {
		this.status = status;
	}

	public boolean isNameChanged() {
		return changedAttributes.containsKey("name");
	}

	public boolean arePointsChanged() {
		return changedAttributes.containsKey("points");
	}

	public boolean isOrderChanged() {
		return changedAttributes.containsKey("order");
	}

	public boolean isItemSynced() {
		return itemSynced;
	}

	public void setItemSynced(boolean itemSynced) {
		this.itemSynced = itemSynced;
	}
}
