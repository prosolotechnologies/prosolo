package org.prosolo.core.spring;

import org.hibernate.FlushMode;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.dao.DataAccessResourceFailureException;

public class OpenSessionInViewFilter extends
		org.springframework.orm.hibernate4.support.OpenSessionInViewFilter {

	@Override
	protected Session openSession(SessionFactory sessionFactory)
			throws DataAccessResourceFailureException {
		Session session = super.openSession(sessionFactory);
		session.setFlushMode(FlushMode.COMMIT);
		return session;
	}

}
