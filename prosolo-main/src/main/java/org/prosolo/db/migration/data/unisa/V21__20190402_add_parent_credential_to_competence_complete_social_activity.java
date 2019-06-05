package org.prosolo.db.migration.data.unisa;

import org.flywaydb.core.api.migration.Context;
import org.prosolo.db.migration.BaseMigration;

import java.sql.*;
import java.util.LinkedList;
import java.util.List;

/**
 * @author Nikola Milikic
 * @date 2019-04-02
 * @since 1.3.2
 */
public class V21__20190402_add_parent_credential_to_competence_complete_social_activity extends BaseMigration {

    @Override
    protected void doMigrate(Context context) throws Exception {
        addParentCredentialToSocialActivity(context.getConnection());
    }

    private void addParentCredentialToSocialActivity(Connection connection) throws SQLException {
        try (Statement statement = connection.createStatement()) {

            // find all social activities of type 'CompetenceCompleteSocialActivity'
            try (ResultSet rs = statement.executeQuery(
                     "SELECT sa.id AS credId " +
                     "FROM social_activity1 AS sa " +
                     "WHERE sa.dtype = 'CompetenceCompleteSocialActivity'")
            ) {
                /**
                 * Extract all ids to the list before making any update queries.
                 *
                 * From the docs: "You must read all of the rows in the result set (or close it) before you can issue
                 * any other queries on the connection, or an exception will be thrown.
                 * (https://dev.mysql.com/doc/connector-j/5.1/en/connector-j-reference-implementation-notes.html)
                 */
                List<Long> socialActivityIds = new LinkedList<>();

                while (rs.next()) {
                    socialActivityIds.add(rs.getLong("credId"));
                }

                // update each social activity record with parent credential id its target competence references to
                for (Long socialActivityId : socialActivityIds) {
                    statement.executeUpdate(
                            "UPDATE social_activity1 sa\n" +
                                "SET sa.parent_credential = (SELECT cred.id\n" +
                                "                            FROM target_competence1 tComp\n" +
                                "                              INNER JOIN competence1 comp ON tComp.competence = comp.id\n" +
                                "                              INNER JOIN credential_competence1 credComp ON comp.id = credComp.competence\n" +
                                "                              INNER JOIN credential1 cred ON credComp.credential = cred.id\n" +
                                "                              INNER JOIN target_credential1 tCred ON tCred.credential = cred.id\n" +
                                "                            WHERE tComp.id = sa.target_competence_object\n" +
                                "                              AND tCred.user = sa.actor)\n" +
                                "WHERE sa.id = " + socialActivityId);
                }
            }
        }
    }

}
