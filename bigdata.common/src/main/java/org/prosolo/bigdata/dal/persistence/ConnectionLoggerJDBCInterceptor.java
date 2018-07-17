package org.prosolo.bigdata.dal.persistence;

import org.apache.log4j.Logger;
import org.apache.tomcat.jdbc.pool.ConnectionPool;
import org.apache.tomcat.jdbc.pool.JdbcInterceptor;
import org.apache.tomcat.jdbc.pool.PooledConnection;

import java.lang.reflect.Method;

/**
 * This class intercepts calls to the {@link java.sql.Connection} class: when an operation on Connection is invoked,
 * when a connection is taken out (“borrowed”) from the pool, and when the close() is called on the underlying
 * connection; these calls are logged in the separate log file logs/prosoloTransactions.log.
 *
 * @author stefanvuckovic
 * @date 2018-06-28
 * @since 1.2.0
 */
public class ConnectionLoggerJDBCInterceptor extends JdbcInterceptor {

    private static Logger logger = Logger.getLogger(ConnectionLoggerJDBCInterceptor.class);

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        if (logger.isDebugEnabled()) {
            String name = method.getName();
            if (CLOSE_VAL.equals(name)) {
                logger.debug(String.format("Closing connection [%s]", proxy));
            }
        }
        return super.invoke(proxy, method, args);
    }

    @Override
    public void reset(ConnectionPool connPool, PooledConnection conn) {
        if (connPool != null && conn != null) {
            if (logger.isDebugEnabled()) {
                logger.debug(String.format("Obtaining Connection [%s], active=[%s], idle=[%s]", conn.toString(),
                        connPool.getActive(), connPool.getIdle()));
            }
        }
    }

    @Override
    public void disconnected(ConnectionPool connPool, PooledConnection conn, boolean finalizing) {
        if (connPool != null && conn != null) {
            if (logger.isDebugEnabled()) {
                logger.debug(String.format("Closing Connection [%s], active=[%s], idle=[%s]", conn.toString(),
                        connPool.getActive(), connPool.getIdle()));
            }
        }
    }
}
