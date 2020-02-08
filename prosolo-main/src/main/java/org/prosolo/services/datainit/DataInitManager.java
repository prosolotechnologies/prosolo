package org.prosolo.services.datainit;

import org.prosolo.app.bc.InitData;
import org.prosolo.services.general.AbstractManager;
import org.prosolo.bigdata.common.exceptions.DbConnectionException;

/**
 * @author stefanvuckovic
 * @date 2019-05-20
 * @since 1.3.2
 */
public interface DataInitManager extends AbstractManager {

    /**
     * Truncates all the tables from the database and initializes the data using
     * the business case given with the {@code initData}
     *
     * @param initData
     * @throws DbConnectionException
     */
    void reinitializeDBData(InitData initData);

    void truncateTables();
}
