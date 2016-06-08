package org.prosolo.web.reports;

import java.util.List;
import java.util.Map;

import org.primefaces.model.LazyDataModel;
import org.primefaces.model.SortOrder;
import org.prosolo.services.logging.LoggingDBManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

//import com.mongodb.DBObject;

/**
@author Zoran Jeremic Jan 27, 2014
 */
//@Service("org.prosolo.web.reports.LazyLogsDataModel")
public class LazyLogsDataModelImpl<T> extends LazyDataModel<LogRow> {

	private static final long serialVersionUID = 3117323370947471054L;

	//@Autowired private LoggingDBManager loggingDBManager;
	
	private List<LogRow> datasource;  
//	private DBObject filterQuery;
	
	public LazyLogsDataModelImpl() { }

	@Override
	public LogRow getRowData(String logId) {
		for (LogRow log : datasource) {
			if (log.getId().equals(logId)) {
				return log;
			}
		}
		return null;
	}

	@Override
	public String getRowKey(LogRow log) {
		return log.getId();
	}
	//Primefaces 5 requires Object instead of String as a value in a map
	@Override
	public List<LogRow> load(int first, int pageSize, String sortField,
			SortOrder sortOrder, Map<String, Object> filters) {
		
	//	int logsCount = loggingDBManager.getLogsCount(filterQuery);
	//	this.setRowCount(logsCount);
//	    	if(filterQuery==null){
//	    	int logsCount=loggingDBManager.getLogsCount();
//	    	this.setRowCount(logsCount);
//
//	    	}else{
//	    		int logsCount=loggingDBManager.getLogsCountFiltered(filterQuery);
//				this.setRowCount(logsCount);
//	    	}
	//	List<LogRow> data = loggingDBManager.loadLogsForPage(first, pageSize, filterQuery);
	//	datasource = data;
		return datasource;
	}

	//public DBObject getFilterQuery() {
	//	return filterQuery;
	//}

	//public void setFilterQuery(DBObject filterQuery) {
	//	this.filterQuery = filterQuery;
	//}

}
