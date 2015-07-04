package org.prosolo.services.upload.impl;

 

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.security.NoSuchAlgorithmException;
import java.util.Date;

import org.apache.commons.io.FilenameUtils;
import org.junit.Test;
import org.prosolo.core.stress.TestContext;
import org.prosolo.services.upload.AmazonS3UploadManager;
import org.prosolo.util.urigenerator.MD5HashUtility;
import org.springframework.beans.factory.annotation.Autowired;

/**
 *
 * @author Zoran Jeremic, May 22, 2014
 *
 */
public class AmazonS3UploadManagerImplTest extends TestContext{
	@Autowired AmazonS3UploadManager s3Manager;
	@Test
	public void storeFileTest() {
		System.out.println("test uploading");
		FileInputStream sourceInputStream = null;
		try {
			File file=new File("/home/zoran/Desktop/trainingset/dm.pdf");
			
			String ext = FilenameUtils.getExtension(file.getName());
			System.out.println("File to store:"+file.getName()+" :"+ext);
			sourceInputStream = new FileInputStream(file);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Date now=new Date();
		String key = null;
		try {
			key = MD5HashUtility.getMD5Hex(String.valueOf(now.getTime()));
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		s3Manager.storeInputStreamByKey(sourceInputStream, key + ".pdf", "application/pdf");
		System.out.println("Finished uploading");
	}

}
