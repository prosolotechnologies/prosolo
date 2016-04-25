package org.prosolo.bigdata.dal.cassandra.impl;

import java.io.Serializable;
import java.util.*;

import org.prosolo.bigdata.dal.cassandra.UserObservationsDBManager;
import org.prosolo.bigdata.events.analyzers.ObservationType;

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.PreparedStatement;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;

/**
 * @author Zoran Jeremic, Oct 11, 2015
 *
 */
public class UserObservationsDBManagerImpl  extends SimpleCassandraClientImpl
implements Serializable, UserObservationsDBManager{
	static HashMap<String, PreparedStatement> preparedStatements = new HashMap<String, PreparedStatement>();
	HashMap<String, String> queries = new HashMap<String, String>();
	ObservationType[] observationTypes=ObservationType.class.getEnumConstants();

	public UserObservationsDBManagerImpl() {
		super();
		this.prepareStatements();
	}

	public static class UserObservationsDBManagerImplHolder {
		public static final UserObservationsDBManagerImpl INSTANCE = new UserObservationsDBManagerImpl();
	}
	public static UserObservationsDBManagerImpl getInstance() {
		return UserObservationsDBManagerImplHolder.INSTANCE;
	}
 
	

	public HashMap<String, PreparedStatement> getPreparedStatements() {
		return preparedStatements;
	}

	private void prepareStatements() {
	 
		String updateClusteringusersobservationsbydate = "UPDATE sna_clusteringusersobservationsbydate  SET login=login+?,lmsuse=lmsuse+?, resourceview=resourceview+?, discussionview=discussionview+? WHERE date=? AND userid=?;";
		this.queries.put("updateClusteringusersobservationsbydate", updateClusteringusersobservationsbydate);
		
		String findClusteringusersobservationsbydate = "SELECT date, userid, discussionview, lmsuse, resourceview FROM sna_clusteringusersobservationsbydate WHERE date=?;";
		this.queries.put("findClusteringusersobservationsbydate", findClusteringusersobservationsbydate);
		
		
		String updateUserprofileactionsobservationsbydate = "UPDATE profile_userprofileactionsobservationsbydate  "
				+ "SET attach=attach+?,progress=progress+?, comment=comment+?, creating=creating+?,"
				+ "evaluation=evaluation+?, join=join+?,like=like+?, login=login+?,"
				+ "posting=posting+?, content_access=content_access+?, message=message+?, search=search+? "
				+ "WHERE date=? AND course=? AND userid=?;";
		this.queries.put("updateUserprofileactionsobservationsbydate", updateUserprofileactionsobservationsbydate);
		
		String findUserprofileactionsobservationsbydate = "SELECT date,  userid, "
				+ "attach,  progress,  comment,  creating,  evaluation,join,like,"
				+ "login ,posting,content_access,message,search "
				+ "FROM profile_userprofileactionsobservationsbydate "
				+ "WHERE date=? and course=?;";
		this.queries.put("findUserprofileactionsobservationsbydate", findUserprofileactionsobservationsbydate);

		String insertUserquartilefeaturesbyprofile  = "INSERT INTO profile_userquartilefeaturesbyprofile(course,  profile,date, userid, sequence) VALUES (?, ?, ?,?,?);";
		this.queries.put("insertUserquartilefeaturesbyprofile",
				insertUserquartilefeaturesbyprofile);

		String insertUserquartilefeaturesbydate  = "INSERT INTO profile_userquartilefeaturesbydate(course,  date, userid,profile, sequence) VALUES (?, ?, ?,?,?);";
		this.queries.put("insertUserquartilefeaturesbydate",
				insertUserquartilefeaturesbydate);

		String findUserquartilefeaturesbycourse = "SELECT * FROM profile_userquartilefeaturesbyprofile WHERE course=? ALLOW FILTERING;";
		this.queries.put("findUserquartilefeaturesbycourse",
				findUserquartilefeaturesbycourse);

		String findUserquartilefeaturesbyprofileAndDate = "SELECT * FROM profile_userquartilefeaturesbyprofile WHERE course=? and profile=? and date=? ALLOW FILTERING;";
		this.queries.put("findUserquartilefeaturesbyprofileanddate",
				findUserquartilefeaturesbyprofileAndDate);

		String findUserquartilefeaturesbyDate = "SELECT * FROM profile_userquartilefeaturesbydate WHERE course=? and date=? ALLOW FILTERING;";
		this.queries.put("findUserquartilefeaturesbydate",
				findUserquartilefeaturesbyDate);


		String findUserquartilefeaturesbyprofile = "SELECT * FROM profile_userquartilefeaturesbyprofile WHERE course=? and profile=? ALLOW FILTERING;";
		this.queries.put("findUserquartilefeaturesbyprofile",
				findUserquartilefeaturesbyprofile);

		String findUserCourses="SELECT * FROM usercourses WHERE userid=? ALLOW FILTERING;";
		this.queries.put("findUserCourses",findUserCourses);

		String addCourseToUserCourses="UPDATE usercourses SET courses=courses+? WHERE userid=?;";
		this.queries.put("addCourseToUserCourses", addCourseToUserCourses);

		String deleteCourseFromUserCourses="UPDATE usercourses SET courses=courses-? WHERE userid=?;";
		this.queries.put("deleteCourseFromUserCourses", deleteCourseFromUserCourses);
		
		Set<String> stQueries = this.queries.keySet();
		for (String query : stQueries) {
			preparedStatements.put(query,
					this.getSession().prepare(queries.get(query)));
		}
	}
	@Override
	@Deprecated
	public boolean updateUserObservationsCounter(Long date, Long userid,
			long login, long lmsuse, long resourceview, long discussionview) {
		BoundStatement updateStatement = new BoundStatement(this.preparedStatements.get("updateClusteringusersobservationsbydate"));
		updateStatement.setLong(0, login);
		updateStatement.setLong(1, lmsuse);
		updateStatement.setLong(2, resourceview);
		updateStatement.setLong(3, discussionview);
		updateStatement.setLong(4, date);
		updateStatement.setLong(5, userid);
		try {
			ResultSet rs=this.getSession().execute(updateStatement);
		} catch (Exception ex) {
			ex.printStackTrace();
			return false;
		}
		return true;
	}
	@Override
	public boolean updateUserProfileActionsObservationCounter(Long date, Long userid, Long courseid,
															  ObservationType observationType) {
	BoundStatement updateStatement = new BoundStatement(this.preparedStatements.get("updateUserprofileactionsobservationsbydate"));
		try {
			for (int i=0;i<observationTypes.length;i++){
				ObservationType oType=observationTypes[i];
				if(oType.equals(observationType)){
					updateStatement.setLong(oType.name().toLowerCase(), 1);
				}else{
					updateStatement.setLong(oType.name().toLowerCase(),0);
				}
		}
		updateStatement.setLong("date", date);
		updateStatement.setLong("course", courseid);
		updateStatement.setLong("userid", userid);
			ResultSet rs=this.getSession().execute(updateStatement);
		} catch (Exception ex) {
			ex.printStackTrace();
			return false;
		}
		return true;
	}
	@Override
	public List<Row> findAllUsersObservationsForDate(Long date) {
		BoundStatement boundStatement = new BoundStatement(
				preparedStatements.get("findClusteringusersobservationsbydate"));
		 boundStatement.setLong(0, date);
		 List<Row> rows =null;
		 try{
				ResultSet rs = this.getSession().execute(boundStatement);
				rows = rs.all();
		 }catch(Exception ex){
			 ex.printStackTrace();
		 }
		return rows;
	}


	@Override
	public List<Row> findAllUsersProfileObservationsForDate(Long date, Long courseId) {

		BoundStatement boundStatement = new BoundStatement(
				preparedStatements.get("findUserprofileactionsobservationsbydate"));
		boundStatement.setLong(0, date);
		boundStatement.setLong(1, courseId);
		List<Row> rows =null;
		try{
			ResultSet rs = this.getSession().execute(boundStatement);
			rows = rs.all();
		}catch(Exception ex){
			ex.printStackTrace();
		}
		System.out.println("FIND ALL USER PROFILES FOR DATE:"+date+" course:"+courseId+" has results:"+rows.size());
		return rows;
	}
	@Override
	public void insertUserQuartileFeaturesByProfile(Long courseid, String profile, Long date, Long userid, String sequence) {
		BoundStatement boundStatement = new BoundStatement(
				preparedStatements
						.get("insertUserquartilefeaturesbyprofile"));
		boundStatement.setLong(0, courseid);
		boundStatement.setLong(2,date);
		boundStatement.setString(1, profile);

		boundStatement.setLong(3,userid);
		boundStatement.setString(4,sequence);

		this.getSession().execute(boundStatement);

	}

	@Override
	public void insertUserQuartileFeaturesByDate(Long courseid, Long date, Long userid, String profile, String sequence) {
		BoundStatement boundStatement = new BoundStatement(
				preparedStatements
						.get("insertUserquartilefeaturesbydate"));
		boundStatement.setLong(0, courseid);
		boundStatement.setLong(1,date);
		boundStatement.setString(3, profile);

		boundStatement.setLong(2,userid);
		boundStatement.setString(4,sequence);

		this.getSession().execute(boundStatement);

	}
	@Override
	public List<Row> findAllUserQuartileFeaturesForCourse(Long courseId) {
		BoundStatement boundStatement = new BoundStatement(
				preparedStatements.get("findUserquartilefeaturesbycourse"));
		boundStatement.setLong(0, courseId);
		List<Row> rows =null;
		try{
			ResultSet rs = this.getSession().execute(boundStatement);
			rows = rs.all();
		}catch(Exception ex){
			ex.printStackTrace();
		}
		return rows;
	}
	@Override
	public List<Row> findAllUserQuartileFeaturesForCourseAndProfile(Long courseId, String profile) {
		BoundStatement boundStatement = new BoundStatement(
				preparedStatements.get("findUserquartilefeaturesbyprofile"));
		boundStatement.setLong(0, courseId);
		boundStatement.setString(1, profile);
		List<Row> rows =null;
		try{
			ResultSet rs = this.getSession().execute(boundStatement);
			rows = rs.all();
		}catch(Exception ex){
			ex.printStackTrace();
		}
		return rows;
	}
	@Override
	public List<Row> findAllUserQuartileFeaturesForCourseDate(Long  courseId, Long  endDateSinceEpoch){
		BoundStatement boundStatement = new BoundStatement(
				preparedStatements.get("findUserquartilefeaturesbydate"));
		boundStatement.setLong(0, courseId);
		boundStatement.setLong(1, endDateSinceEpoch);
		List<Row> rows =null;
		try{
			ResultSet rs = this.getSession().execute(boundStatement);
			rows = rs.all();
		}catch(Exception ex){
			ex.printStackTrace();
		}
		return rows;
	}

	@Override
	public List<Row> findAllUserQuartileFeaturesForCourseProfileAndWeek(Long courseId, String profile, Long date) {
		BoundStatement boundStatement = new BoundStatement(
				preparedStatements.get("findUserquartilefeaturesbyprofileanddate"));
		boundStatement.setLong(0, courseId);
		boundStatement.setString(1, profile);
		boundStatement.setLong(2, date);
		List<Row> rows =null;
		try{
			ResultSet rs = this.getSession().execute(boundStatement);
			rows = rs.all();
		}catch(Exception ex){
			ex.printStackTrace();
		}
		return rows;
	}
	@Override
	public Set<Long> findAllUserCourses(Long userId) {
		BoundStatement boundStatement = new BoundStatement(
				preparedStatements.get("findUserCourses"));
		boundStatement.setLong(0, userId);
		List<Row> rows =null;
		Set<Long> courses=new HashSet<Long>();
		try{
			ResultSet rs = this.getSession().execute(boundStatement);
			Row row = rs.one();
			if(row!=null){
				 courses=row.getSet("courses",Long.class);
			}
		}catch(Exception ex){
			ex.printStackTrace();
		}
		return courses;
	}
	@Override
	public void enrollUserToCourse(Long userId, Long courseId){
		System.out.println("ENROLLING USER TO THE COURSE CALLED FOR:"+userId+" course:"+courseId);
		BoundStatement boundStatement = new BoundStatement(
				preparedStatements
						.get("addCourseToUserCourses"));
		Set<Long> course=new HashSet<>();
		course.add(courseId);
		boundStatement.setSet(0, course);
		boundStatement.setLong(1,userId);
		try{
			this.getSession().execute(boundStatement);
		}catch(Exception ex){
			ex.getStackTrace();
		}

	}
	@Override
	public void withdrawUserFromCourse(Long userId, Long courseId){
		BoundStatement boundStatement = new BoundStatement(
				preparedStatements
						.get("deleteCourseFromUserCourses"));
		Set<Long> course=new HashSet<>();
		course.add(courseId);
		boundStatement.setSet(0, course);
		boundStatement.setLong(1,userId);
		try{
			 this.getSession().execute(boundStatement);
		}catch(Exception ex){
			ex.getStackTrace();
		}
	}
}
