package org.prosolo.db.migration.data.unisa;

import org.flywaydb.core.api.migration.Context;
import org.prosolo.db.migration.BaseMigration;
import org.prosolo.services.urlencoding.UrlIdEncoder;
import org.prosolo.services.urlencoding.impl.HashidsUrlIdEncoderImpl;
import org.prosolo.services.user.impl.StudentProfileManagerImpl;
import org.prosolo.web.util.UrlDbIdEncoder;

import java.sql.*;
import java.util.LinkedList;
import java.util.List;

/**
 * @author Nikola Milikic
 * @date 2019-04-22
 * @since 1.3.2
 */
public class V24__20190422_unisa_create_profile_settings extends BaseMigration {

    private UrlIdEncoder urlIdEncoder = new HashidsUrlIdEncoderImpl();

    @Override
    protected void doMigrate(Context context) throws Exception {
        createProfileSettingsEntryForAllUsers(context.getConnection());
    }

    private void createProfileSettingsEntryForAllUsers(Connection connection) throws SQLException {
        try (Statement statement = connection.createStatement()) {

            // fetch all users
            try (ResultSet rs = statement.executeQuery(
                    "SELECT user.id, user.name, user.lastname " +
                    "FROM user AS user ");
            ) {
                // Extract all data and store to the list before making any update queries
                List<String[]> userData = new LinkedList<>();

                while (rs.next()) {
                    userData.add(new String[]{
                            String.valueOf(rs.getLong("id")),
                            rs.getString("name"),
                            rs.getString("lastname")
                    });
                }

                StringBuffer insertQueryBuffer = new StringBuffer();

                insertQueryBuffer.append(
                        "INSERT INTO profile_settings (custom_profile_url, summary_sidebar_enabled, user) VALUES ");

                boolean firstRecord = true;

                // for each user, add an entry to the profile_settings table
                for (String[] user : userData) {
                    if (!firstRecord)
                        insertQueryBuffer.append(", ");

                    long id = Long.parseLong(user[0]);

                    // supposing here that this will be a unique customProfileURL and not retrying with other variants of the URL
                    String customProfileURL = StudentProfileManagerImpl.generateCustomProfileURLPrefix(user[1], user[2]) + urlIdEncoder.encodeId(id);

                    insertQueryBuffer.append("('"+customProfileURL+"','T', "+id+")");

                    firstRecord = false;
                }
                insertQueryBuffer.append(";");

                statement.executeUpdate(insertQueryBuffer.toString());
            }
        }
    }
}
