package org.prosolo.services.nodes.data.assessments.grading;

import java.io.Serializable;

/**
 * @author stefanvuckovic
 * @date 2017-10-16
 * @since 1.0.0
 */
public class RubricItemGradeData implements Serializable {

    private static final long serialVersionUID = 3807175760781397473L;

    private long id;
    private String name;
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

    public int getOrder() {
        return order;
    }

    public void setOrder(int order) {
        this.order = order;
    }
}
