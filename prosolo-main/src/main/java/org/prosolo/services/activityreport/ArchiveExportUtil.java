package org.prosolo.services.activityreport;

import static org.apache.commons.lang.StringUtils.isAllUpperCase;

import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.text.WordUtils;
import org.apache.log4j.Logger;
import org.prosolo.app.Settings;
import org.prosolo.util.urigenerator.MD5HashUtility;

/**
 * Random utility methods used for activity report generation.
 * 
 * @author vita
 */
public class ArchiveExportUtil {

	protected static Logger logger = Logger.getLogger(ArchiveExportUtil.class.getName());
	
	private static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy_MM_dd"); 
	
	private static final SimpleDateFormat dateFormatForm = new SimpleDateFormat("E MMM dd, yyyy"); 

	private static final SimpleDateFormat dateFormatDetailed = new SimpleDateFormat("E MMM dd, yyyy HH:mm:ss:SSS"); 

	static {
		dateFormatForm.setTimeZone(TimeZone.getTimeZone("UTC"));
		dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
		dateFormatDetailed.setTimeZone(TimeZone.getTimeZone("UTC"));
	}
	
	public static String formatDate(Date d) {
		return dateFormat.format(d);
	}
	
	public static String formatDateDetailed(Date d) {
		return dateFormatDetailed.format(d);
	}
	
	public static String formatDateForm(Date d) {
		return dateFormatForm.format(d);
	}
	
 
	public static String createArchivePath(Long userId, String fromDateString, String toDateString) {
		return Settings.getInstance().config.services.activityReportService.reportDirectory + toDateString + "/" + hashActorId(userId) + "_from_" + fromDateString + "_to_" + toDateString + ".csv";
	}
	
	public static String createArchivePath(Long userId, Date fromDate, Date toDate) {
		return createArchivePath(userId, formatDate(fromDate), formatDate(toDate));
	}
	
	private static String hashActorId(Long id) {
		String idHash;
		try {
			idHash = MD5HashUtility.getMD5Hex(id.toString());
		} catch (NoSuchAlgorithmException e) {
			logger.warn("MD5 Not available");
			idHash = Long.toString(id.toString().hashCode());
		}
		return idHash;
	}
	
	public static String capitalizeName(String text) {
	    if (text.contains("_"))
	    	return StringUtils.remove(WordUtils.capitalizeFully(text, '_'), "_");
	    
	    if (isAllUpperCase(text))
	    	return text.substring(0, 1).toUpperCase() + text.substring(1).toLowerCase();

	    return text;
	}
	
	public static String prepareForCSV(String s) {
		if(s == null)
			return "";
		s = s.replaceAll("\r\n", "\\\\n");
	    s = s.replaceAll("\n", "\\\\n");
	    //s = s.replaceAll("\"", "\\\\\"");
	    return StringEscapeUtils.escapeCsv(s);
	}
	
	public static boolean isNumeric(String str)	{
	  return str.matches("-?\\d+(\\.\\d+)?");  //match a number with optional '-' and decimal.
	}
	
	public static void main(String[] args) {
		System.out.println(capitalizeName("ABAC"));
	}
}
