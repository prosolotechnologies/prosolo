package org.prosolo.bigdata.dal.persistence.impl;/**
 * Created by zoran on 09/01/16.
 */

import org.apache.log4j.Logger;
import org.hibernate.Query;
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
public class ClusteringDAOImpl extends GenericDAOImpl implements
        ClusteringDAO {
    private static Logger logger = Logger
            .getLogger(ClusteringDAO.class);

    public ClusteringDAOImpl(){
        setSession(HibernateUtil.getSessionFactory().openSession());
    }

    @SuppressWarnings({ "unchecked" })
   /* @Override
    public List<Long> getAllCoursesIds() {
        //Session session=openSession();
        String query =
                "SELECT course.id " +
                        "FROM Course course " +
                        "WHERE course.deleted = :deleted ";
        List<Long> result =null;
        try{
            result = session.createQuery(query)
                    .setParameter("deleted", false).list();
        }catch(Exception ex){
            ex.printStackTrace();
        }
        if (result != null) {
            return result;
        }
        return new ArrayList<Long>();
    }*/

   // @SuppressWarnings({ "unchecked" })
 //   @Override
  /*  public List<Long> getAllCredentialsIds() {
        //Session session=openSession();
        String query =
                "SELECT credential.id " +
                        "FROM Credential1 credential " +
                        "WHERE credential.deleted = :deleted AND credential.published = :published";
        List<Long> result =null;
        try{
            result = session.createQuery(query)
                    .setParameter("deleted", false)
                    .setParameter("published",true)
                    .list();
        }catch(Exception ex){
            ex.printStackTrace();
        }
        if (result != null) {
            return result;
        }
        return new ArrayList<Long>();
    }*/

    @Override
    public List<Long> getAllActiveDeliveriesIds(){
        try {
            StringBuilder query = new StringBuilder(
                    "SELECT del.id " +
                            "FROM Credential1 del " +
                            "WHERE del.type = :type ");// +
                            //"AND del.deliveryOf.id = :credId ");

          //  if (onlyActive) {
                query.append("AND (del.deliveryStart IS NOT NULL AND del.deliveryStart <= :now " +
                        "AND (del.deliveryEnd IS NULL OR del.deliveryEnd > :now))");
            //}

            Query q = session
                    .createQuery(query.toString())
                   // .setLong("credId", credId)
                    .setParameter("type", CredentialType.Delivery);

        //    if (onlyActive) {
                q.setTimestamp("now", new Date());
          //  }

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
        }
    }

   /* public void updateUserCourseProfile(Long courseId, Long userId, String currentCluster , String clusterName ){
        System.out.println("UPDATE USER COURSE PROFILE:"+courseId+" userId:"+userId+" cluster:"+currentCluster+" clusterFullName:"+clusterName);
        String query =
                "UPDATE " +
                        "CourseEnrollment enrollment " +
                        "set enrollment.cluster = :currentCluster, " +
                        "enrollment.clusterName = :clusterName " +
                        "WHERE enrollment.course IN " +
                        "(" +
                        "SELECT course " +
                        " FROM Course course "+
                        " WHERE course.id=:courseId"+
                        ") AND " +
                        "enrollment.user IN "+
                        "("+
                        "SELECT user "+
                        " FROM User user "+
                        " WHERE user.id=:userId"+
                        ")";
        System.out.println("QUERY:"+query);
        int result=session.createQuery(query)
                .setParameter("currentCluster",currentCluster)
                .setParameter("clusterName",clusterName)
                .setParameter("courseId",courseId)
                .setParameter("userId",userId).executeUpdate();
        System.out.println("ROWS AFFECTED:"+result);
    }*/
}
