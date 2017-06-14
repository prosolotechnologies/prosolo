package org.prosolo.services.productionFixes;

import org.prosolo.bigdata.common.exceptions.DbConnectionException;
import org.prosolo.common.domainmodel.user.User;
import org.prosolo.common.exceptions.ResourceCouldNotBeLoadedException;
import org.prosolo.services.general.AbstractManager;

import java.util.List;

/**
 * Created by stefanvuckovic on 6/14/17.
 */

public interface ProductionFixesService extends AbstractManager {

    void deleteUsersCredentials(String credId, List<String> userIds) throws DbConnectionException;

    void fixUtaStudentCourses() throws Exception;

	/**
	 * @param userId
	 * @return
	 * @throws ResourceCouldNotBeLoadedException
	 */
	User loadUser(long userId) throws ResourceCouldNotBeLoadedException;
}
