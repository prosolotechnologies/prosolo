/**
 * 
 */
package org.prosolo.util;

import java.io.File;

import org.springframework.mail.javamail.ConfigurableMimeFileTypeMap;

/**
 * @author "Nikola Milikic"
 *
 */
public class FileUtil {

	public static void deleteFolderContents(File folder) {
		File[] files = folder.listFiles();
		    
		if (files != null) {
			for (File f : files) {
				if (f.isDirectory()) {
					deleteFolder(f);
				} else {
					f.delete();
				}
			}
		}
	}
	
	public static void deleteFolder(File folder) {
	    File[] files = folder.listFiles();
	    
		if (files != null) { // some JVMs return null for empty dirs
	        for (File f: files) {
	            if (f.isDirectory()) {
	                deleteFolder(f);
	            } else {
	                f.delete();
	            }
	        }
	    }
	    folder.delete();
	}
	public static String getFileType(String fileName){
		ConfigurableMimeFileTypeMap mimeMap = new ConfigurableMimeFileTypeMap();
		String type = mimeMap.getContentType(fileName);
		return type;
	}
}
