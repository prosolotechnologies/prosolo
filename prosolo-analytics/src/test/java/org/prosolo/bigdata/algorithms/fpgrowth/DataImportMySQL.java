package org.prosolo.bigdata.algorithms.fpgrowth;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Random;

import org.junit.Test;
import org.prosolo.bigdata.common.events.pojo.DataName;
import org.prosolo.bigdata.common.events.pojo.DataType;
import org.prosolo.bigdata.dal.cassandra.AnalyticalEventDBManager;
import org.prosolo.bigdata.dal.cassandra.impl.AnalyticalEventDBManagerImpl;
import org.prosolo.bigdata.events.pojo.AnalyticsEvent;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

/**
 * @author Zoran Jeremic May 3, 2015
 *
 */

public class DataImportMySQL {
	// JDBC driver name and database URL
	static final String JDBC_DRIVER = "com.mysql.jdbc.Driver";
	static final String MOOC_DB_URL = "jdbc:mysql://localhost/prosolo_temp";
	static final String DB_URL = "jdbc:mysql://localhost/prosolo";

	// Database credentials
	static final String USER = "root";
	static final String PASS = "root";

	/**
	 * This method imports data related to the use of target activities in MOOC
	 * course
	 */
	@Test
	public void importMOOCData() {
		Connection conn = null;
		Statement stmt = null;
		AnalyticalEventDBManager dbManager = AnalyticalEventDBManagerImpl.getInstance();
		try {
			// STEP 2: Register JDBC driver
			Class.forName("com.mysql.jdbc.Driver");

			// STEP 3: Open a connection
			System.out.println("Connecting to database...");
			conn = DriverManager.getConnection(MOOC_DB_URL, USER, PASS);

			// STEP 4: Execute a query
			System.out.println("Creating statement...");
			stmt = conn.createStatement();
			String sql;
			sql = "SELECT id,competence competence FROM node where dtype='TargetCompetence'";
			ResultSet rs = stmt.executeQuery(sql);
			// STEP 5: Extract data from result set
			while (rs.next()) {
				AnalyticsEvent event = new AnalyticsEvent();
				event.setDataName(DataName.TARGETCOMPETENCEACTIVITIES);
				event.setDataType(DataType.RECORD);
				JsonObject data = new JsonObject();
				int tcid = rs.getInt("id");
				data.add("targetcompetenceid", new JsonPrimitive(tcid));
				int cid = rs.getInt("competence");
				data.add("competenceid", new JsonPrimitive(cid));
				System.out.println("TC:" + tcid + " comp:" + cid);
				String tactSQL = "SELECT target_activities FROM competence_target_competence_target_activity where node="
						+ tcid;
				Statement stmt2 = conn.createStatement();
				ResultSet rs2 = stmt2.executeQuery(tactSQL);
				JsonArray activitieslist = new JsonArray();
				while (rs2.next()) {
					long tact = rs2.getLong("target_activities");
					Statement stmt3 = conn.createStatement();
					String sqlAct = "SELECT activity FROM node where id="
							+ tact;
					ResultSet rs3 = stmt3.executeQuery(sqlAct);
					rs3.next();
					System.out.println("T ACT:"
							+ rs2.getLong("target_activities") + " act:"
							+ rs3.getLong("activity"));
					activitieslist.add(new JsonPrimitive(rs3
							.getLong("activity")));
					rs3.close();
					stmt3.close();
				}
				data.add("activities", activitieslist);

				rs2.close();
				stmt2.close();
				event.setData(data);
				dbManager.insertAnalyticsEventRecord(event);
			}
			// STEP 6: Clean-up environment
			rs.close();
			stmt.close();
			conn.close();

		} catch (SQLException se) {
			// Handle errors for JDBC
			se.printStackTrace();
		} catch (Exception e) {
			// Handle errors for Class.forName
			e.printStackTrace();
		} finally {
			// finally block used to close resources
			try {
				if (stmt != null)
					stmt.close();
			} catch (SQLException se2) {
			}// nothing we can do
			try {
				if (conn != null)
					conn.close();
			} catch (SQLException se) {
				se.printStackTrace();
			}// end finally try
		}// end try
		System.out.println("Goodbye!");
	}

	/**
	 * Imports data from Prosolo current DB. This method is used for testing
	 * only.
	 */
	@Test
	public void importData() {
		Connection conn = null;
		Statement stmt = null;
		AnalyticalEventDBManager dbManager = AnalyticalEventDBManagerImpl.getInstance();
		try {
			// STEP 2: Register JDBC driver
			Class.forName("com.mysql.jdbc.Driver");

			// STEP 3: Open a connection
			System.out.println("Connecting to database...");
			conn = DriverManager.getConnection(DB_URL, USER, PASS);

			// STEP 4: Execute a query
			System.out.println("Creating statement...");
			stmt = conn.createStatement();
			String sql;
			sql = "SELECT id,competence competence FROM node where dtype='TargetCompetence'";
			ResultSet rs = stmt.executeQuery(sql);
			// STEP 5: Extract data from result set
			while (rs.next()) {
				AnalyticsEvent event = new AnalyticsEvent();
				event.setDataName(DataName.TARGETCOMPETENCEACTIVITIES);
				event.setDataType(DataType.RECORD);
				JsonObject data = new JsonObject();
				int tcid = rs.getInt("id");
				data.add("targetcompetenceid", new JsonPrimitive(tcid));
				int cid = rs.getInt("competence");
				data.add("competenceid", new JsonPrimitive(cid));
				System.out.println("TC:" + tcid + " comp:" + cid);
				String tactSQL = "SELECT target_activities FROM node_target_activities where node="
						+ tcid;
				Statement stmt2 = conn.createStatement();
				ResultSet rs2 = stmt2.executeQuery(tactSQL);
				JsonArray activitieslist = new JsonArray();
				while (rs2.next()) {
					long tact = rs2.getLong("target_activities");
					Statement stmt3 = conn.createStatement();
					String sqlAct = "SELECT activity FROM node where id="
							+ tact;
					ResultSet rs3 = stmt3.executeQuery(sqlAct);
					rs3.next();
					System.out.println("T ACT:"
							+ rs2.getLong("target_activities") + " act:"
							+ rs3.getLong("activity"));
					activitieslist.add(new JsonPrimitive(rs3
							.getLong("activity")));
					rs3.close();
					stmt3.close();
				}
				data.add("activities", activitieslist);

				rs2.close();
				stmt2.close();
				event.setData(data);
				dbManager.insertAnalyticsEventRecord(event);
			}
			// STEP 6: Clean-up environment
			rs.close();
			stmt.close();
			conn.close();

		} catch (SQLException se) {
			// Handle errors for JDBC
			se.printStackTrace();
		} catch (Exception e) {
			// Handle errors for Class.forName
			e.printStackTrace();
		} finally {
			// finally block used to close resources
			try {
				if (stmt != null)
					stmt.close();
			} catch (SQLException se2) {
			}// nothing we can do
			try {
				if (conn != null)
					conn.close();
			} catch (SQLException se) {
				se.printStackTrace();
			}// end finally try
		}// end try
		System.out.println("Goodbye!");
	}

	@Test
	public void testGenerateRandomData() {
		AnalyticalEventDBManager dbManager =  AnalyticalEventDBManagerImpl.getInstance();
		try {

			Random r = new Random();
			for (int i = 0; i < 10000; i++) {

				AnalyticsEvent event = new AnalyticsEvent();
				event.setDataName(DataName.TARGETCOMPETENCEACTIVITIES);
				event.setDataType(DataType.RECORD);
				JsonObject data = new JsonObject();
				int tcid = r.nextInt(5000);

				data.add("targetcompetenceid", new JsonPrimitive(tcid));
				int cid = r.nextInt(100);
				data.add("competenceid", new JsonPrimitive(cid));
				System.out.println("TC:" + tcid + " comp:" + cid);

				JsonArray activitieslist = new JsonArray();
				int numberOfAct = r.nextInt(8);
				for (int x = 0; x < numberOfAct; x++) {
					// while(rs2.next()){
					// long tact=r.nextInt(10000);
					long act = r.nextInt(100);

					activitieslist.add(new JsonPrimitive(act));
					// rs3.close();
					// stmt3.close();
				}
				data.add("activities", activitieslist);

				// rs2.close();
				// stmt2.close();
				event.setData(data);
				dbManager.insertAnalyticsEventRecord(event);
			}

		} catch (Exception e) {
			// Handle errors for Class.forName
			e.printStackTrace();

			System.out.println("Goodbye!");
		}
	}
}
