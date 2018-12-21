package org.prosolo.services.common.data;

/**
 * Represents selectable data with arbitrary object that represents data and a flag that indicates
 * whether this piece of data (object) is selected (enabled).
 *
 * @author stefanvuckovic
 * @date 2018-11-16
 * @since 1.2.0
 */
public class SelectableData<T> {

    private final T data;
    private boolean selected;

    public SelectableData(T data, boolean selected) {
        this.data = data;
        this.selected = selected;
    }

    public SelectableData(T data) {
        this(data, false);
    }

    public T getData() {
        return data;
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }
}
