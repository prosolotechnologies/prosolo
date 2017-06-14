package org.prosolo.services.productionFixes;

import org.prosolo.bigdata.common.exceptions.DbConnectionException;
import org.prosolo.services.general.AbstractManager;

import java.util.List;

/**
 * Created by stefanvuckovic on 6/14/17.
 */

public interface ProductionFixesService extends AbstractManager {

    void deleteUsersCredentials(String credId, List<String> userIds) throws DbConnectionException;

    void deleteUsersCredentials() throws DbConnectionException;
}
