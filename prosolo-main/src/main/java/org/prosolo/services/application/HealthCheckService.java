package org.prosolo.services.application;

import org.prosolo.bigdata.common.exceptions.DbConnectionException;

/**
 * @author Nikola Milikic
 * @date 2018-07-05
 * @since 1.2
 */
public interface HealthCheckService {

    void pingDatabase() throws DbConnectionException;
}
