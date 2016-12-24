package org.prosolo.bigdata.dal.persistence;

import java.util.Date;
import java.util.List;

import org.prosolo.bigdata.common.exceptions.DbConnectionException;

public interface CourseDAO {

	List<Long> getAllCredentialIds();
	
	String getCredentialTitle(long courseId);
	
	public void publishCredential(long credentialId);
	
	void changeVisibilityForCredential(long credentialId) throws DbConnectionException;
	
	Date getScheduledVisibilityUpdateDate(long credId);

}