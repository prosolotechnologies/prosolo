package org.prosolo.core.hibernate.userTypes;

import java.io.Serializable;
import java.net.URI;
import java.net.URISyntaxException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

import org.apache.log4j.Logger;
import org.hibernate.HibernateException;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.type.StandardBasicTypes;
import org.hibernate.usertype.UserType;

public class URIUserType implements UserType {

	private static Logger logger = Logger.getLogger(URIUserType.class);

	private static final int[] SQL_TYPES = { Types.VARCHAR };

	public URIUserType() {
	}

	@Override
	public int[] sqlTypes() {
		return SQL_TYPES;
	}

	@Override
	public Class<URI> returnedClass() {
		return URI.class;
	}

	@Override
	public boolean equals(Object x, Object y) {
		return (x == y) || (x != null && y != null && (x.equals(y)));
	}

	@Override
	public Object deepCopy(Object o) {
		if (o == null) {
			return null;
		}

		URI deepCopy = null;
		try {
			deepCopy = new URI(o.toString());
		} catch (URISyntaxException e) {
			logger.error("Problem creating deepcopy of URI " + o.toString());
		}
		return deepCopy;
	}

	@Override
	public boolean isMutable() {
		return true;
	}

	@Override
	public Object assemble(Serializable cached, Object owner) {
		return deepCopy(cached);
	}

	@Override
	public Serializable disassemble(Object value) {
		return (Serializable) deepCopy(value);
	}

	@Override
	public Object replace(Object original, Object target, Object owner) {
		return deepCopy(original);
	}

	@Override
	public int hashCode(Object x) {
		return x.hashCode();
	}

	@Override
	public Object nullSafeGet(ResultSet rs, String[] names,
			SessionImplementor session, Object owner)
			throws HibernateException, SQLException {
		 
		 String val = (String) StandardBasicTypes.STRING.nullSafeGet(rs,
			 	names[0], session, owner);
 
		URI url = null;

		try {
			url = new URI(val);
		} catch (URISyntaxException e) {
			logger.error("Problem creating URI from " + val);
		}

		return url; 
		 
	}

	@Override
	public void nullSafeSet(PreparedStatement st, Object value, int index,
			SessionImplementor session) throws HibernateException, SQLException {
		URI val = (URI) value;
		String url = "";
		if (val != null) {
			url = val.toString();
		}
		st.setString(index, url);
	}

}
