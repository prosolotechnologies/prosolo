package org.prosolo.bigdata.dal.persistence.impl;/**
 * Created by zoran on 09/01/16.
 */

import org.apache.log4j.Logger;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.prosolo.bigdata.common.exceptions.DbConnectionException;
import org.prosolo.bigdata.dal.persistence.ClusteringDAO;
import org.prosolo.bigdata.dal.persistence.HibernateUtil;
import org.prosolo.bigdata.scala.clustering.userprofiling.ClusterName;
import org.prosolo.bigdata.scala.clustering.userprofiling.ClusterName.*;
import org.prosolo.common.domainmodel.credential.Credential1;
import org.prosolo.common.domainmodel.credential.CredentialType;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;


/**
 * zoran 09/01/16
 */
public class ClusteringDAOImpl implements
        ClusteringDAO {
    private static Logger logger = Logger
            .getLogger(ClusteringDAO.class);

    public ClusteringDAOImpl(){


    }


    @Override
    public List<Long> getAllActiveDeliveriesIds(){
        Session session = HibernateUtil.getSessionFactory().openSession();
        try {
            StringBuilder query = new StringBuilder(
                    "SELECT del.id " +
                            "FROM Credential1 del " +
                            "WHERE del.type = :type ");// +

                query.append("AND (del.deliveryStart IS NOT NULL AND del.deliveryStart <= :now " +
                        "AND (del.deliveryEnd IS NULL OR del.deliveryEnd > :now))");
             Query q = session
                    .createQuery(query.toString())
                    .setParameter("type", CredentialType.Delivery);
        q.setTimestamp("now", new Date());

            @SuppressWarnings("unchecked")
            List<Long> result = q.list();

            if (result != null) {
                return result;
            }
            return new ArrayList<Long>();
        } catch (Exception e) {
            logger.error(e);
            e.printStackTrace();
            throw new DbConnectionException("Error while retrieving credential deliveries");
        }finally{
            session.close();
        }
    }


}
