/**
 * 
 */
package org.prosolo.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author "Nikola Milikic"
 * 
 */
public class CollectionsUtil {

	public static <T, S> List<Map.Entry<T, S>> mapToList(Map<T, S> map) {
		if (map == null) {
			return null;
		}

		List<Map.Entry<T, S>> list = new ArrayList<Map.Entry<T, S>>();
		list.addAll(map.entrySet());

		return list;
	}
}
