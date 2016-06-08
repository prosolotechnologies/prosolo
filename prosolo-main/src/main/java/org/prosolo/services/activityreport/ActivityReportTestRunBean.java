package org.prosolo.services.activityreport;

import javax.faces.bean.ManagedBean;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@ManagedBean(name = "activityReportTestBean")
@Component("activityReportTestBean")
@Scope("view")
public class ActivityReportTestRunBean {
	@Autowired
	private ActivityExportManager exportManager;
	private static boolean started=false;
	
	public void testBatchGenerateActivityReports(){
		System.out.println("Run batch generate activity report");
		if(started) {
			System.out.println("Process already started");
			return;
		}
		new Thread(new Runnable() {
			@Override
			public void run() {
				started=true;
			//	exportManager.runActivityExport();
				started=false;
			}}).start();
	}
}
