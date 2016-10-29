package org.prosolo.bigdata.dal.persistence.impl;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.prosolo.bigdata.dal.persistence.HibernateUtil;
import org.prosolo.bigdata.dal.persistence.UserDAO;

public class UserDAOImpl extends GenericDAOImpl implements UserDAO {

	private static Logger logger = Logger.getLogger(UserDAO.class);
	
	public UserDAOImpl() {
		 setSession(HibernateUtil.getSessionFactory().openSession());
		//setSession(HibernateUtil.getSessionFactory().getCurrentSession());
	}
	public static class UserDAOImplHolder {
		public static final UserDAOImpl INSTANCE = new UserDAOImpl();
	}
	public static UserDAOImpl getInstance() {
		return UserDAOImplHolder.INSTANCE;
	}

	private List<Long[]> partitions(int length, int size) {
		List<Long[]> result = new ArrayList<Long[]>();
		for (long i = 0; i < length; i = i + size) {
			if (i + size >= length) {
				result.add(new Long[] { i, (long) (length - 1) });
			} else {
				result.add(new Long[] { i, i + size - 1 });
			}
		}
		return result;
	}
	
	private String name(Object first, Object last) {
		if (first == null && last == null) return "";
		if (first != null && last != null) return first.toString() + " " + last.toString();
		return last == null ? first.toString() : last.toString(); 
	}
	
	private String avatar(Object avatar) {
		return avatar == null ? "" : avatar.toString();
	}

	@SuppressWarnings("unchecked")
	@Override
	public Map<Long, Map<String, String>> getUsersData(Long[] users) {
		Map<Long, Map<String, String>> result = new HashMap<Long, Map<String, String>>();
		for (Long[] partition : partitions(users.length, 50)) {
			String query = "SELECT id, name, lastname, avatar_url FROM user WHERE id in (:users)";
			Long[] in = Arrays.copyOfRange(users, partition[0].intValue(), partition[1].intValue() + 1);
			try {
			List<Object[]> list = session.createSQLQuery(query).setParameterList("users", Arrays.asList(in)).list();
				for(Object[] row : list) {
					Map<String, String> data = new HashMap<String, String>();
					data.put("name", name(row[1], row[2]));
					data.put("avatar", avatar(row[3]));
					result.put(((BigInteger) row[0]).longValue(), data);
				}
				return result;
			} catch (Exception e) {
				logger.error("Cannot read users data.", e);
			}
		}
		return result;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public List<String> getUserNames(List<Long> userIds) {
		List<String> result = new ArrayList<>();
		try {
			String query = "SELECT user.name, user.lastname FROM User user WHERE user.id in (:users)";
			
			List<Object[]> users = session.createQuery(query)
					 .setParameterList("users", userIds)
					 .list();
			if(users != null) {
				for(Object[] row : users) {
					result.add(name(row[0], row[1]));
				}
			}
			
		} catch (Exception e) {
			logger.error("Cannot read users data.", e);
			e.printStackTrace();
		}
		return result;
	}
	
	@Override
	public Map<String, String> getUserNameAndEmail(long userId) {
		Map<String, String> result = new HashMap<>();
		try {
			String query = "SELECT user.name, user.lastname, user.email " + 
					"FROM User user " +
					"WHERE user.id = :userId";
			
			Object[] res = (Object[]) session.createQuery(query)
					 .setLong("userId", userId)
					 .uniqueResult();
			
			String name = name(res[0], res[1]);
			String email = (String) res[2];
			
			result.put("name", name);
			result.put("email", email);
		} catch (Exception e) {
			logger.error("Cannot read user name.", e);
			e.printStackTrace();
		}
		return result;
	}

}
