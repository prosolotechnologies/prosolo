package org.prosolo.web.reports;

import java.io.Serializable;

import javax.faces.bean.ManagedBean;

import org.apache.log4j.Logger;
import org.primefaces.event.SelectEvent;
import org.primefaces.model.LazyDataModel;
import org.prosolo.services.activityreport.ActivityExportManager;
import org.prosolo.services.logging.LoggingDBManager;
import org.prosolo.services.upload.AmazonS3UploadManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.mongodb.DBObject;

/**
@author Zoran Jeremic Jan 27, 2014
 */
@ManagedBean(name = "logsBean")
@Component("logsBean")
@Scope("view")
public class LogsBean   implements Serializable {

	private static final long serialVersionUID = -2855916520900836884L;
	private static Logger logger = Logger.getLogger(LogsBean.class.getName());
	
	@Autowired private LazyLogsDataModelImpl<LogRow> lazyModel; 
	@Autowired private LogsFilterBean logsFilterBean;
	@Autowired private LoggingDBManager loggingDBManager;
	@Autowired private AmazonS3UploadManager s3Manager;
	@Autowired private ActivityExportManager activityExport;
	
	private LogRow selectedLog;
	private String exportDataLink = "";
	public String getExportDataLink() {
		return exportDataLink;
	}
	public void setExportDataLink(String exportDataLink) {
		this.exportDataLink = exportDataLink;
	}
	public String getExportDataLabel() {
		return exportDataLabel;
	}
	public void setExportDataLabel(String exportDataLabel) {
		this.exportDataLabel = exportDataLabel;
	}

	private String exportDataLabel = "";
	
//	private List<String> HEADER = new ArrayList<String>();		
//	private static Map<String, String> CSV_MAP = new HashMap<String, String>();
//	private static int CSV_INCREMENT_SIZE = 10000;
//	private static int PART_INCREMENT = 1;
	
//	private ByteArrayOutputStream uploadStream;
//	private ZipOutputStream archiveStream; 
	public void onRowSelect(SelectEvent event){
 
	}
	public void applyFilters(){
		//lazyModel.loadFilteredData(logsFilterBean.getUsersList(), logsFilterBean.isTwitterPosts());
		DBObject filterQuery = loggingDBManager.createFilterQuery(logsFilterBean.getUsersList(), 
		logsFilterBean.isTwitterPosts(),
		logsFilterBean.getSelectedEventTypes(),
		logsFilterBean.getSelectedObjectTypes(),
		logsFilterBean.getStartDate(),
		logsFilterBean.getEndDate());
		lazyModel.setFilterQuery(filterQuery);
	}

	public void resetFilters() {
		System.out.println("Reset filters");
		lazyModel.setFilterQuery(null);
		logsFilterBean.init();
	}

	public void exportData() {
//		HEADER.addAll(Arrays.asList("ActionID", "Time", "User", "Action", "Object", "ObjectType", "Target", "TargetType", "Parameters"));		
//		PART_INCREMENT = 1;
		//logsFilterBean.init();

		exportDataLabel = "Content loading...";
		logger.info("Page sizing " + lazyModel.getRowCount());						
						
		prepareData(lazyModel.getRowCount());
	}

	
	private void prepareData(int totalRowCount){		
		DBObject filterQuery = loggingDBManager.createFilterQuery(logsFilterBean.getUsersList(), 
				logsFilterBean.isTwitterPosts(),
				logsFilterBean.getSelectedEventTypes(),
				logsFilterBean.getSelectedObjectTypes(),
				logsFilterBean.getStartDate(),
				logsFilterBean.getEndDate());
				//lazyModel.setFilterQuery(filterQuery);							
					
		System.out.println("Filter query:"+filterQuery.toString());
	
		exportDataLink = activityExport.exportCompleteLog(filterQuery);
		System.out.println("export data link...");
		exportDataLabel = "Click to download";						
	}
	/*
	 * GETTERS / SETTERS
	 */
	public LazyDataModel<LogRow> getLazyModel() {
		return lazyModel;
	}

	public LogRow getSelectedLog() {
		return selectedLog;
	}

	public void setSelectedLog(LogRow selectedLog) {
		this.selectedLog = selectedLog;
	}
}
