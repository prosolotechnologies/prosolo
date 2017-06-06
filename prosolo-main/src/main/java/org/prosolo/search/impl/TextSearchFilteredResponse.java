package org.prosolo.search.impl;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Special kind of response when number of results per filter (category) should be returned where filter is
 * specified by enum (S generic type parameter). Number of results per filter can be retrieved by using method
 * {@link #getNumberOfResultsForFilter(Enum)} with specific enum filter passed as a parameter.
 * 
 * @author stefanvuckovic
 *
 * @param <T>
 * @param <S>
 */
public class TextSearchFilteredResponse<T, S extends Enum<S>> extends TextSearchResponse1<T> implements Serializable {

	private static final long serialVersionUID = -1816643120995180978L;
	
	private Map<S, Long> filters = new HashMap<>();

	public TextSearchFilteredResponse(List<T> foundNodes, long hits) {
		setFoundNodes(foundNodes);
		setHitsNumber(hits);
	}

	public TextSearchFilteredResponse(List<T> nodes) {
		setFoundNodes(nodes);
	}

	public TextSearchFilteredResponse() { }
	
	public void putFilter(S key, Long value) {
		filters.put(key, value);
	}
	
	public long getNumberOfResultsForFilter(S key) {
		Long no = filters.get(key);
		if(no == null) {
			throw new NullPointerException("Filter not found");
		}
		return no;
	}

}
