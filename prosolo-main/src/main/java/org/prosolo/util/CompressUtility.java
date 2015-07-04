package org.prosolo.util;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

/**
 *
 * @author Zoran Jeremic Apr 20, 2014
 *
 */

public class CompressUtility {
	/** Zip the contents of the directory, and save it in the zipfile */
	  public static void zipDirectory(String dir, String zipfileLocation) throws IOException{
	  File directoryToZip = new File(dir);

		List<File> fileList = new ArrayList<File>();
		getAllFiles(directoryToZip, fileList);
		writeZipFile(directoryToZip, fileList, zipfileLocation);
	  }
	  public static void getAllFiles(File dir, List<File> fileList) throws IOException {
			File[] files = dir.listFiles();
			for (File file : files) {
				fileList.add(file);
				if (file.isDirectory()) {
					getAllFiles(file, fileList);
				} 
			}
		}

		public static void writeZipFile(File directoryToZip, List<File> fileList, String zipFile) {
			try {
				FileOutputStream fos = new FileOutputStream(zipFile);
				ZipOutputStream zos = new ZipOutputStream(fos);
				for (File file : fileList) {
					if (!file.isDirectory()) { // we only zip files, not directories
						addToZip(directoryToZip, file, zos);
					}
				}
				zos.close();
				fos.close();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		public static void addToZip(File directoryToZip, File file, ZipOutputStream zos) throws FileNotFoundException,
				IOException {
			FileInputStream fis = new FileInputStream(file);
			String zipFilePath = file.getCanonicalPath().substring(directoryToZip.getCanonicalPath().length() + 1,
					file.getCanonicalPath().length());
			ZipEntry zipEntry = new ZipEntry(zipFilePath);
			zos.putNextEntry(zipEntry);
			byte[] bytes = new byte[1024];
			int length;
			while ((length = fis.read(bytes)) >= 0) {
				zos.write(bytes, 0, length);
			}
			zos.closeEntry();
			fis.close();
		}
		public static void unzipFile(String filePath, String extractToPath){
	         
	        FileInputStream fis = null;
	        ZipInputStream zipIs = null;
	        ZipEntry zEntry = null;
	        try {
	            fis = new FileInputStream(filePath);
	            zipIs = new ZipInputStream(new BufferedInputStream(fis));
	            while((zEntry = zipIs.getNextEntry()) != null){
	                try{
	                    byte[] tmp = new byte[4*1024];
	                    FileOutputStream fos = null;
	                   // String opFilePath = "C:/"+zEntry.getName();
	                    fos = new FileOutputStream(extractToPath+"/"+zEntry.getName());
	                    int size = 0;
	                    while((size = zipIs.read(tmp)) != -1){
	                        fos.write(tmp, 0 , size);
	                    }
	                    fos.flush();
	                    fos.close();
	                } catch(Exception ex){
	                     
	                }
	            }
	            zipIs.close();
	        } catch (FileNotFoundException e) {
	            // TODO Auto-generated catch block
	            e.printStackTrace();
	        } catch (IOException e) {
	            // TODO Auto-generated catch block
	            e.printStackTrace();
	        }
	    }

}
