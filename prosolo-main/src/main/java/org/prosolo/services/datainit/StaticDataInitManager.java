package org.prosolo.services.datainit;

/**
 * Service with purpose to initialize static data that is supposed to be initialized when database is formatted no matter what business case
 * is selected
 *
 * @author stefanvuckovic
 * @date 2019-06-12
 * @since 1.3.2
 */
public interface StaticDataInitManager {

    /**
     * Initializes static data that is supposed to be initialized when database is formatted no matter what business case
     * is selected
     */
    void initStaticData();
}
