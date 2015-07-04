/**
 * 
 */
package org.prosolo.services.rest.courses.data;

import java.util.ArrayList;
import java.util.List;

/**
 * @author "Nikola Milikic"
 * 
 */
public class CompetenceJsonData {

	private long id;
	private String name;
	private List<SerieJsonData> series;
	
	public CompetenceJsonData() {
		series = new ArrayList<SerieJsonData>();
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
		this.name = name;
	}

	public List<SerieJsonData> getSeries() {
		return series;
	}

	public void setSeries(List<SerieJsonData> series) {
		this.series = series;
	}
	
	public void addSerie(SerieJsonData serie) {
		if (serie != null && !series.contains(serie)) {
			series.add(serie);
		}
	}

}
