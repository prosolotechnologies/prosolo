package org.prosolo.web.util;

import org.prosolo.services.common.data.SelectableData;

import java.util.List;

/**
 * @author stefanvuckovic
 * @date 2018-12-12
 * @since 1.2.0
 */
public class SelectableDataUtil {

    public static boolean areAllItemsSelected(List<SelectableData> items) {
        if (items == null || items.isEmpty()) {
            return false;
        }
        return items.stream().allMatch(item -> item.isSelected());
    }
}
