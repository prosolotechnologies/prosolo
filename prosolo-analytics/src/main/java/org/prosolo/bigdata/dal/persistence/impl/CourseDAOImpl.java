package org.prosolo.bigdata.dal.persistence.impl;

import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.prosolo.bigdata.common.exceptions.DbConnectionException;
import org.prosolo.bigdata.dal.persistence.CourseDAO;
import org.prosolo.bigdata.dal.persistence.HibernateUtil;
import org.prosolo.common.domainmodel.credential.CredentialType;
import org.prosolo.common.domainmodel.user.UserGroupPrivilege;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class CourseDAOImpl extends GenericDAOImpl implements CourseDAO {

    private static Logger logger = Logger
            .getLogger(CourseDAOImpl.class);

    public CourseDAOImpl(boolean openLongSession) {
        logger.info("CourseDAOImpl constructor init");
        if (openLongSession) {
            setSession(HibernateUtil.getSessionFactory().openSession());
        }
        logger.info("CourseDAOImpl constructor init finished");
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<Long> getAllCredentialIds() {
        Session session = HibernateUtil.getSessionFactory().openSession();
        Transaction t = null;
        String query =
                "SELECT cred.id " +
                        "FROM Credential1 cred " +
                        "WHERE cred.deleted = :deleted " +
                        "AND cred.type = :type";
        List<Long> result = null;
        try {
            t = session.beginTransaction();
            result = session.createQuery(query)
                    .setBoolean("deleted", false)
                    .setString("type", CredentialType.Delivery.name())//.setBoolean("published", true)
                    .list();
            t.commit();
        } catch (Exception ex) {
            logger.error(ex);
            ex.printStackTrace();
            if (t != null) {
                t.rollback();
            }
        } finally {
            session.close();
        }
        if (result != null) {
            return result;
        }
        return new ArrayList<Long>();
    }

    @Override
    public String getCredentialTitle(long credId) {
        String title = null;
        String query =
                "SELECT cred.title " +
                        "FROM Credential1 cred " +
                        "WHERE cred.id = :credId";
        try {
            title = (String) session.createQuery(query)
                    .setParameter("credId", credId)
                    .uniqueResult();
        } catch (Exception ex) {
            logger.error(ex);
            ex.printStackTrace();
        }
        return title;
    }

    @Override
    public void publishCredential(long credentialId) {
        String query =
                "UPDATE Credential1 " +
                        "SET published = 1, " +
                        "scheduledPublishDate = null"
                        + " WHERE id = :credId";
        session.createQuery(query)
                .setParameter("credId", credentialId)
                .executeUpdate();
    }

    @Override
    public void changeVisibilityForCredential(long credentialId, long userId) throws DbConnectionException {
        try {
            //TODO cred-redesign-07
//			Credential1 cred = (Credential1) session.load(Credential1.class, credentialId);
//			if(cred.isPublished()) {
//				cred.setPublished(false);
//			} else {
//				cred.setPublished(true);
//				new CompetenceDAOImpl().publishCompetences(credentialId, userId);
//				session.flush();
//				cred.setDuration(getRecalculatedDuration(credentialId));
//			}
//			cred.setScheduledPublishDate(null);
//			
//			CredentialIndexerImpl.getInstance().updateVisibility(credentialId, cred.isPublished());
        } catch (Exception e) {
            logger.error(e);
            e.printStackTrace();
            throw new DbConnectionException("Error while updating credential visibility");
        }
    }

    @Override
    public Date getScheduledVisibilityUpdateDate(long credId) {
        String query =
                "SELECT cred.scheduledPublicDate " +
                        "FROM Credential1 cred " +
                        "WHERE cred.id = :credId";
        try {
            return (Date) session.createQuery(query)
                    .setParameter("credId", credId)
                    .uniqueResult();
        } catch (Exception ex) {
            logger.error(ex);
            ex.printStackTrace();
        }
        return null;
    }

    @Override
    public UserGroupPrivilege getUserPrivilegeForCredential(long credId, long userId)
            throws DbConnectionException {
        try {
            String query = "SELECT credUserGroup.privilege, cred.createdBy.id, cred.visibleToAll " +
                    "FROM CredentialUserGroup credUserGroup " +
                    "INNER JOIN credUserGroup.userGroup userGroup " +
                    "RIGHT JOIN credUserGroup.credential cred " +
                    "INNER JOIN userGroup.users user " +
                    "WITH user.user.id = :userId " +
                    "WHERE cred.id = :credId " +
                    "ORDER BY CASE WHEN credUserGroup.privilege = :editPriv THEN 1 WHEN credUserGroup.privilege = :viewPriv THEN 2 ELSE 3 END";

            Object[] res = (Object[]) session
                    .createQuery(query)
                    .setLong("userId", userId)
                    .setLong("credId", credId)
                    .setParameter("editPriv", UserGroupPrivilege.Edit)
                    .setParameter("viewPriv", UserGroupPrivilege.Learn)
                    .setMaxResults(1)
                    .uniqueResult();

            if (res == null) {
                return UserGroupPrivilege.None;
            }
            UserGroupPrivilege priv = (UserGroupPrivilege) res[0];
            if (priv == null) {
                priv = UserGroupPrivilege.None;
            }
            long owner = (long) res[1];
            boolean visibleToAll = (boolean) res[2];
            return owner == userId
                    ? UserGroupPrivilege.Edit
                    : priv == UserGroupPrivilege.None && visibleToAll ? UserGroupPrivilege.Learn : priv;
        } catch (Exception e) {
            e.printStackTrace();
            logger.error(e);
            throw new DbConnectionException("Error while trying to retrieve user privilege for credential");
        }
    }

    private long getRecalculatedDuration(long credId) {
        String query = "SELECT sum(c.duration) FROM CredentialCompetence1 cc " +
                "INNER JOIN cc.competence c " +
                "WHERE cc.credential.id = :credId " +
                "AND c.published = :published";
        Long res = (Long) session
                .createQuery(query)
                .setLong("credId", credId)
                .setBoolean("published", true)
                .uniqueResult();

        return res != null ? res : 0;
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<String> getCredentialHashtags(long id) throws DbConnectionException {
        Session session = HibernateUtil.getSessionFactory().openSession();
        Transaction t = null;
        List<String> result = null;
        try {
            t = session.beginTransaction();
            result = getCredentialHashtags(id, session);
            t.commit();
        } catch (Exception ex) {
            logger.error(ex);
            ex.printStackTrace();
            if (t != null) {
                t.rollback();
            }
            throw new DbConnectionException("Error while retrieving credential hashtags");
        } finally {
            session.close();
        }
        return result;
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<String> getCredentialHashtags(long id, Session session) {
        String query =
                "SELECT ht.title " +
                        "FROM Credential1 cred " +
                        "INNER JOIN cred.hashtags ht " +
                        "WHERE cred.id = :id";

        List<String> result = session.createQuery(query)
                .setLong("id", id)
                .list();
        if (result != null) {
            return result;
        }
        return new ArrayList<String>();
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<Long> getIdsOfCredentialsUserIsLearning(long userId, Session session)
            throws DbConnectionException {
        String query =
                "SELECT tCred.credential.id " +
                        "FROM TargetCredential1 tCred " +
                        "WHERE tCred.user.id = :userId";
        List<Long> result = null;
        try {
            result = session.createQuery(query)
                    .setLong("userId", userId)
                    .list();
            return result;
        } catch (Exception ex) {
            logger.error(ex);
            ex.printStackTrace();
            throw new DbConnectionException("Error while retrieving user credentials ids");
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public long getOrganizationIdForCredential(long credentialId) {
        Session session = HibernateUtil.getSessionFactory().openSession();
        Transaction t = null;
        String query =
                "SELECT cred.organization.id " +
                        "FROM Credential1 cred " +
                        "WHERE cred.id = :credentialId";
        try {
            t = session.beginTransaction();
            Long id = (Long) session.createQuery(query)
                    .setLong("credentialId", credentialId)
                    .uniqueResult();
            t.commit();
            return id != null ? id.longValue() : 0;
        } catch (Exception ex) {
            logger.error("Error", ex);
            if (t != null) {
                t.rollback();
            }
        } finally {
            session.close();
        }
        return 0;
    }
}
