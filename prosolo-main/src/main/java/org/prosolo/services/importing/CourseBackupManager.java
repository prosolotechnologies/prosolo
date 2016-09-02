package org.prosolo.services.importing;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.primefaces.model.UploadedFile;
import org.prosolo.app.Settings;
import org.prosolo.common.config.CommonSettings;
import org.prosolo.common.domainmodel.activities.CompetenceActivity;
import org.prosolo.common.domainmodel.activities.ResourceActivity;
import org.prosolo.common.domainmodel.activities.UploadAssignmentActivity;
import org.prosolo.common.domainmodel.annotation.Tag;
import org.prosolo.common.domainmodel.competences.Competence;
import org.prosolo.common.domainmodel.content.RichContent;
import org.prosolo.common.domainmodel.course.Course;
import org.prosolo.common.domainmodel.course.CourseCompetence;
import org.prosolo.common.domainmodel.user.User;
import org.prosolo.common.exceptions.ResourceCouldNotBeLoadedException;
import org.prosolo.core.hibernate.HibernateUtil;
import org.prosolo.services.nodes.CourseManager;
import org.prosolo.util.CompressUtility;
import org.prosolo.web.courses.data.CourseBackupData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * 
 * @author Zoran Jeremic Apr 18, 2014
 * 
 */
@Service("org.prosolo.services.importing.CourseBackupManager")
public class CourseBackupManager implements Serializable {
	@Autowired
	private CourseManager courseManager;
	/**
	 * 
	 */
	private static final long serialVersionUID = 5670398708965275103L;
	private String coursesPath;

	public CourseBackupManager() {
		coursesPath = Settings.getInstance().config.fileManagement.uploadPath
				+ "backups/course/";
	}

	public void createCourseBackup(long courseId, long userId,
			boolean includeCompetences, boolean includeActivities,
			boolean includeKeywords, boolean includeFiles) {
		Course course = null;
		try {
			course = courseManager.loadResource(Course.class, courseId);
			course = HibernateUtil.initializeAndUnproxy(course);
		} catch (ResourceCouldNotBeLoadedException e) {
			e.printStackTrace();
		}
		String courseDirTempPath = coursesPath + course.getId();
		final GsonBuilder gsonBuilder = new GsonBuilder()
				.setDateFormat("MMM dd, yyyy HH:mm:ss a");
		gsonBuilder.registerTypeAdapter(Course.class, new CourseSerializer(
				includeCompetences, includeKeywords));
		gsonBuilder.registerTypeAdapter(User.class, new UserSerializer(
				courseDirTempPath + "/"));
		gsonBuilder.registerTypeAdapter(CourseCompetence.class,
				new CourseCompetenceSerializer());
		gsonBuilder.registerTypeAdapter(Competence.class,
				new CompetenceSerializer(includeActivities, includeKeywords));
		gsonBuilder.registerTypeAdapter(CompetenceActivity.class,
				new CompetenceActivitySerializer());
		gsonBuilder.registerTypeAdapter(Tag.class,
				new TagSerializer());
		gsonBuilder.registerTypeAdapter(ResourceActivity.class,
				new ResourceActivitySerializer(includeFiles));
		gsonBuilder.registerTypeAdapter(UploadAssignmentActivity.class,
				new UploadAssignmentActivitySerializer());
		gsonBuilder.registerTypeAdapter(RichContent.class,
				new RichContentSerializer(courseDirTempPath + "/"));
		gsonBuilder.setPrettyPrinting();
		Writer jsonWriter = null;
		File coursebackupdir = null;
		String archivedFileLocation = coursesPath + userId + "/";
		try {
			coursebackupdir = new File(courseDirTempPath);
			if (!coursebackupdir.exists()) {
				coursebackupdir.mkdirs();
			}
			File archivedFile = new File(archivedFileLocation);
			if (!archivedFile.exists()) {
				archivedFile.mkdirs();
			}
			jsonWriter = new OutputStreamWriter(new FileOutputStream(
					courseDirTempPath + "/data.json"), "UTF-8");
		} catch (IOException e) {
			e.printStackTrace();
		}
		final Gson gson = gsonBuilder.create();
		gson.toJson(course, jsonWriter);
		try {
			jsonWriter.close();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		String archivedFilePath = archivedFileLocation + "backup_"
				+ course.getId() + "_" + System.currentTimeMillis() + ".zip";
		try {
			CompressUtility.zipDirectory(coursebackupdir.getPath(),
					archivedFilePath);
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		deleteDirectoryContent(coursebackupdir);
	}

	private void deleteDirectoryContent(File directory) {
		File[] entries = directory.listFiles();
		for (File currentFile : entries) {
			if (currentFile.isDirectory()) {
				deleteDirectoryContent(currentFile);
			}
			currentFile.delete();
		}
		directory.delete();

	}

	public Collection<CourseBackupData> readAllCourseBackupsForUser(long userId) {
		List<CourseBackupData> backups = new ArrayList<CourseBackupData>();
		String backupsLocation = this.coursesPath + userId;
		File coursebackupdir = null;
		coursebackupdir = new File(backupsLocation);
		if (!coursebackupdir.exists()) {
			coursebackupdir.mkdirs();
		}
		File[] entries = coursebackupdir.listFiles();
		for (File currentFile : entries) {
			if (currentFile.getName().contains(".zip")) {
				CourseBackupData backupData = new CourseBackupData();
				backupData.setFilename(currentFile.getName());
				backupData.setPath(currentFile.getPath());
				String link = currentFile
						.getAbsolutePath()
						.replaceFirst(
								Settings.getInstance().config.fileManagement.uploadPath,
								"");
				link = Settings.getInstance().config.fileManagement.urlPrefixFolder
						+ link;
				link = CommonSettings.getInstance().config.appConfig.domain + link;
				backupData.setLink(link);
				backups.add(backupData);
			}
		}
		return backups;
	}

	public boolean deleteCourseBackup(long userId, CourseBackupData backupData) {
		File fileToDelete = new File(backupData.getPath());
		if (fileToDelete.exists()) {
			fileToDelete.delete();

			return true;

		} else {

			return false;
		}
	}
	public boolean restoreCourseBackup(long id, CourseBackupData backupData) {
		File fileToRestore = new File(backupData.getPath());
		if (!fileToRestore.exists()) {
			return false;
		}
		Date now=new Date();
		String tempFile = coursesPath + id + "/temp"+now.getTime();
		File tempDir = new File(tempFile);
		if (!tempDir.exists()) {
			tempDir.mkdirs();
		}
		CompressUtility.unzipFile(backupData.getPath(), tempFile);
		final GsonBuilder gsonBuilder = new GsonBuilder();
		gsonBuilder.registerTypeAdapter(Course.class, new CourseDeserializer());
		gsonBuilder.registerTypeAdapter(Tag.class,
				new TagDeserializer());
		gsonBuilder.registerTypeAdapter(CompetenceActivity.class,
				new CompetenceActivityDeserializer());
		gsonBuilder.registerTypeAdapter(Competence.class,
				new CompetenceDeserializer());
		gsonBuilder.registerTypeAdapter(CourseCompetence.class,
				new CourseCompetenceDeserializer());
		gsonBuilder.registerTypeAdapter(ResourceActivity.class,
				new ResourceActivityDeserializer());
		gsonBuilder.registerTypeAdapter(RichContent.class,
				new RichContentDeserializer(tempFile));
		gsonBuilder.registerTypeAdapter(UploadAssignmentActivity.class,
				new UploadAssignmentActivityDeserializer());
		gsonBuilder.registerTypeAdapter(User.class, new UserDeserializer());
		Gson gson = gsonBuilder.create();
		Reader reader = null;
		try {
			reader = new InputStreamReader(new FileInputStream(tempFile
					+ "/data.json"), "UTF-8");
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return false;
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			return false;
		}
		@SuppressWarnings("unused")
		Course course = gson.fromJson(reader, Course.class);
		deleteDirectoryContent(tempDir);
		return true;
	}

	public void storeUploadedBackup(UploadedFile uploadedFile, long userId) {
		String archivedFileLocation = coursesPath + userId + "/";
		String prefix = FilenameUtils.getBaseName(uploadedFile.getFileName()); 
		String suffix = FilenameUtils.getExtension(uploadedFile.getFileName());
		File file=null;
		InputStream input=null;
		OutputStream output=null;
		try {
			file = File.createTempFile(prefix+"_","." + suffix,new File(archivedFileLocation) );
			input = uploadedFile.getInputstream();
			output = new FileOutputStream(file);
		} catch (FileNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		try {
		    IOUtils.copy(input, output);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
		    IOUtils.closeQuietly(output);
		    IOUtils.closeQuietly(input);
		}
		
	}

}
