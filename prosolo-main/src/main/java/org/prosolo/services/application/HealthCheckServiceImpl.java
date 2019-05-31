package org.prosolo.services.application;

import org.prosolo.bigdata.common.exceptions.DbConnectionException;
import org.prosolo.services.general.impl.AbstractManagerImpl;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author Nikola Milikic
 * @date 2018-07-05
 * @since 1.2
 */
@Service ("org.prosolo.services.application.HealthCheckService")
public class HealthCheckServiceImpl extends AbstractManagerImpl implements HealthCheckService {

    @Override
    @Transactional (readOnly = true)
    public void pingDatabase() throws DbConnectionException {
        try {
            String query = "SELECT 1";

            persistence.currentManager().createSQLQuery(query)
                    .uniqueResult();
        } catch (Exception e) {
            e.printStackTrace();
            throw new DbConnectionException("Error contacting the database");
        }
    }
}
