package org.prosolo.bigdata.dal.cassandra.impl;/**
 * Created by zoran on 20/07/16.
 */

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.PreparedStatement;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;
import org.prosolo.bigdata.dal.cassandra.UserRecommendationsDBManager;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static org.prosolo.bigdata.dal.cassandra.impl.UserRecommendationsDBManagerImpl.Statements.FIND_USER_PREFERENCE_FOR_DATE;
import static org.prosolo.bigdata.dal.cassandra.impl.UserRecommendationsDBManagerImpl.Statements.INSERT_USER_PREFERENCE_FOR_DATE;
//import static org.prosolo.bigdata.dal.cassandra.impl.UserRecommendationsDBManagerImpl.Statements.INSERT_USER_PREFERENCE_FOR_RESOURCE;


/**
 * zoran 20/07/16
 */
public class UserRecommendationsDBManagerImpl  extends SimpleCassandraClientImpl implements UserRecommendationsDBManager{
    private static final Map<UserRecommendationsDBManagerImpl.Statements, PreparedStatement> prepared = new ConcurrentHashMap<UserRecommendationsDBManagerImpl.Statements, PreparedStatement>();

    private static final Map<UserRecommendationsDBManagerImpl.Statements, String> statements = new HashMap<UserRecommendationsDBManagerImpl.Statements, String>();

    public enum Statements {
       // INSERT_USER_PREFERENCE_FOR_RESOURCE,
        INSERT_USER_PREFERENCE_FOR_DATE,
        FIND_USER_PREFERENCE_FOR_DATE
    }
    private UserRecommendationsDBManagerImpl(){
        super();
    }
    public static class UserRecommendationsDBManagerImplHolder {
        public static final UserRecommendationsDBManagerImpl INSTANCE = new UserRecommendationsDBManagerImpl();
    }
    public static UserRecommendationsDBManagerImpl getInstance() {
        return UserRecommendationsDBManagerImpl.UserRecommendationsDBManagerImplHolder.INSTANCE;
    }
    private PreparedStatement getStatement(Session session, UserRecommendationsDBManagerImpl.Statements statement) {
        // If two threads access prepared map concurrently, prepared can be repeated twice.
        // This should be better than synchronizing access.
        if (prepared.get(statement) == null) {
            prepared.put(statement, session.prepare(statements.get(statement)));
        }
        return prepared.get(statement);
    }

    static {
       // statements.put(INSERT_USER_PREFERENCE_FOR_RESOURCE, "INSERT INTO "+TablesNames.USERRECOM_USERRESOURCEPREFERENCES_RECORD +"(timestamp, userid, resourcetype, resourceid, preference) VALUES(?,?,?,?,?); ");
        statements.put(INSERT_USER_PREFERENCE_FOR_DATE, "INSERT INTO "+TablesNames.USERRECOM_USERRESOURCEPREFERENCES +"(userid, resourcetype, resourceid, preference, dateepoch) VALUES(?,?,?,?,?); ");
        statements.put(FIND_USER_PREFERENCE_FOR_DATE, "SELECT preference FROM "+TablesNames.USERRECOM_USERRESOURCEPREFERENCES +" WHERE userid=? AND resourcetype=? AND resourceid=? AND dateepoch=?; ");
       // String getLogEventsInPeriod = "SELECT * FROM logevents where actorid = ?  and timestamp >= ? and timestamp <= ? ALLOW FILTERING;";
          }

  /*  @Override
    public void insertStudentPreferenceRecord(Long student, String resourcetype, Long resourceid, Double preference, Long timestamp) {
        System.out.println("INSERT studentPreference... for timestamp:"+timestamp+" student:"+student+" resource type:"+resourcetype+" resourceid:"+resourceid+" preference:"+preference);
        PreparedStatement prepared = getStatement(getSession(), INSERT_USER_PREFERENCE_FOR_RESOURCE);

        BoundStatement statement = new BoundStatement(prepared);
        statement.setLong(0,timestamp);
        statement.setLong(1,student);
        statement.setString(2,resourcetype);
        statement.setLong(3,resourceid);
        statement.setDouble(4,preference);

        try {
            this.getSession().execute(statement);
        }catch(Exception ex){
            ex.printStackTrace();
        }

    }*/
    @Override
    public void insertStudentPreferenceForDate(Long student, String resourcetype, Long resourceid, Double preference, Long dateEpoch) {
        System.out.println("INSERT studentPreference... for date:"+dateEpoch+" student:"+student+" resource type:"+resourcetype+" resourceid:"+resourceid+" preference:"+preference);
        PreparedStatement prepared = getStatement(getSession(), INSERT_USER_PREFERENCE_FOR_DATE);

        BoundStatement statement = new BoundStatement(prepared);
        statement.setLong(0,student);
        statement.setString(1,resourcetype);
        statement.setLong(2,resourceid);
        statement.setDouble(3,preference);
        statement.setLong(4,dateEpoch);
        try {
            this.getSession().execute(statement);
        }catch(Exception ex){
            ex.printStackTrace();
        }

    }
    @Override
    public Double getStudentPreferenceForDate(Long student, String resourcetype, Long resourceid,Long dateEpoch) {
        Double res = 0.0;
        try {
            BoundStatement statement = new BoundStatement(getStatement(getSession(),FIND_USER_PREFERENCE_FOR_DATE));

            statement.setLong(0,student);
            statement.setString(1,resourcetype);
            statement.setLong(2,resourceid);
            statement.setLong(3,dateEpoch);
            Row row = this.getSession().execute(statement).one();
            res = (row == null) ? -1 : row.getDouble(0);
        } catch(Exception e) {
            logger.error(e);
            e.printStackTrace();
        }
        return res;
    }

}
