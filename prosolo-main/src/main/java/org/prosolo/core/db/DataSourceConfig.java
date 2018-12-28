package org.prosolo.core.db;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

/**
 * @author stefanvuckovic
 * @date 2018-12-20
 * @since 1.2.0
 */
@Configuration
public class DataSourceConfig {

    @Bean(destroyMethod = "close")
    public DataSource dataSource() {
        return org.prosolo.bigdata.dal.persistence.HibernateUtil.dataSource();
    }

}
