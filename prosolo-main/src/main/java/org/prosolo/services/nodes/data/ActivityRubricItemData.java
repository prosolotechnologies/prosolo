package org.prosolo.services.nodes.data;

import java.io.Serializable;

/**
 * @author stefanvuckovic
 * @date 2017-10-16
 * @since 1.0.0
 */
public class ActivityRubricItemData implements Serializable {

    private static final long serialVersionUID = 3807175760781397473L;

    private long id;
    private String name;
    private double weight;
    private int points;
    private int order;

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
        this.name = name;
    }

    public double getWeight() {
        return weight;
    }

    public void setWeight(double weight) {
        this.weight = weight;
    }

    public int getPoints() {
        return points;
    }

    public void setPoints(int points) {
        this.points = points;
    }

    public int getOrder() {
        return order;
    }

    public void setOrder(int order) {
        this.order = order;
    }
}
