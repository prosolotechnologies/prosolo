/**
 * 
 */
package org.prosolo.core.db.hibernate;

import org.apache.log4j.Logger;
import org.hibernate.Hibernate;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.proxy.HibernateProxy;

/**
 * @author "Nikola Milikic"
 *
 */
public class HibernateUtil {
	
	private static Logger logger = Logger .getLogger(HibernateUtil.class.getName());

	@SuppressWarnings("unchecked")
	public static <T> T initializeAndUnproxy(T entity) {
	    if (entity == null) {
	        throw new 
	           NullPointerException("Entity passed for initialization is null");
	    }

	    Hibernate.initialize(entity);
	    if (entity instanceof HibernateProxy) {
	        entity = (T) ((HibernateProxy) entity).getHibernateLazyInitializer()
	                .getImplementation();
	    }
	    return entity;
	}
	
	public static void close(Session session) {
        if (session != null) {
            try {
                session.close();
            } catch (HibernateException ignored) {
                logger.error("Couldn't close Session", ignored);
            }
        }
    }
}
