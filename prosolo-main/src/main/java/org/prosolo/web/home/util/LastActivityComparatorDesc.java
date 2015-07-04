/**
 * 
 */
package org.prosolo.web.home.util;

import java.util.Comparator;

import org.prosolo.web.goals.data.LastActivityAware;

/**
 * @author "Nikola Milikic"
 *
 */
public class LastActivityComparatorDesc implements Comparator<LastActivityAware> {

	@Override
	public int compare(LastActivityAware o1, LastActivityAware o2) {
		if (o1.getLastActivity() == null) {
			return -1;
		}
		if (o2.getLastActivity() == null) {
			return 1;
		}
		return o2.getLastActivity().compareTo(o1.getLastActivity());
	}

}
