package org.prosolo.bigdata.dal.persistence.impl;/**
 * Created by zoran on 09/01/16.
 */

import org.apache.log4j.Logger;
import org.prosolo.bigdata.dal.persistence.ClusteringDAO;
import org.prosolo.bigdata.dal.persistence.HibernateUtil;

import java.util.ArrayList;
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
    @Override
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
    }
}
