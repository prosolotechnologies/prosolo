package org.prosolo.db.migration.data.unisa;

import lombok.Getter;
import org.flywaydb.core.api.migration.Context;
import org.prosolo.db.migration.BaseMigration;
import org.prosolo.services.urlencoding.UrlIdEncoder;
import org.prosolo.services.urlencoding.impl.HashidsUrlIdEncoderImpl;
import org.prosolo.services.user.impl.StudentProfileManagerImpl;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author stefanvuckovic
 * @date 2019-05-16
 * @since 1.3.2
 */
public class V33__20190613_migrate_notification_urls extends BaseMigration {

    @Getter
    private Pattern commentPattern = Pattern.compile(".*\\/competences\\/([a-zA-Z0-9]+)\\?comment=([a-zA-Z0-9]+)");
    @Getter
    private Pattern assessmentRequestedPattern = Pattern.compile(".*\\/competences\\/([a-zA-Z0-9]+)\\/assessments\\/peer\\/([a-zA-Z0-9]+)");
    @Getter
    private Pattern assessmentApprovedPattern = Pattern.compile(".*\\/competences\\/([a-zA-Z0-9]+)\\/assessments\\/(peer|instructor)\\/([a-zA-Z0-9]+)");
    private UrlIdEncoder idEncoder = new HashidsUrlIdEncoderImpl();

    @Override
    protected void doMigrate(Context context) throws Exception {
        migrateCommentNotifications(context);
        migrateAssessmentRequestedNotifications(context);
        migrateAssessmentApprovedNotifications(context);

    }

    private void migrateCommentNotifications(Context context) throws SQLException {
        try (Statement statement = context.getConnection().createStatement()) {

            // fetch all Comment notifications
            try (ResultSet rs = statement.executeQuery(
                    "SELECT DISTINCT notif.id AS id, notif.link AS link, cred.id AS credId\n" +
                    "FROM notification1 AS notif\n" +
                    "  INNER JOIN competence1 comp ON notif.object_id = comp.id\n" +
                    "  INNER JOIN credential_competence1 credComp ON comp.id = credComp.competence\n" +
                    "  INNER JOIN credential1 cred ON credComp.credential = cred.id\n" +
                    "  INNER JOIN target_credential1 tCred ON tCred.credential = cred.id\n" +
                    "  INNER JOIN user_user_role userReceiverRole ON notif.receiver = userReceiverRole.user\n" +
                    "  INNER JOIN user_user_role userActorRole ON notif.actor = userActorRole.user\n" +
                    "WHERE notif.type = 'Comment'\n" +
                    "  AND notif.object_type = 'Competence'\n" +
                    "  AND (\n" +
                    "    (notif.section = 'STUDENT'\n" +
                    "     AND notif.receiver = tCred.user)\n" +
                    "    OR\n" +
                    "    (notif.section = 'MANAGE'\n" +
                    "      AND notif.actor = tCred.user))")
            ) {
                // Extract all data and store to the list before making any update queries
                List<String[]> notifList = new LinkedList<>();

                while (rs.next()) {
                    notifList.add(new String[] {
                            String.valueOf(rs.getLong("id")),
                            rs.getString("link"),
                            String.valueOf(rs.getLong("credId"))
                    });
                }

                for (String[] notif : notifList) {
                    long notifId = Long.parseLong(notif[0]);
                    String oldLink = notif[1];
                    long credId = Long.parseLong(notif[2]);

                    String[] ids = extractMatchedGroups(commentPattern, 2, oldLink);

                    String newLink = "/credentials/"+idEncoder.encodeId(credId)+"/competences/"+ids[0]+"?comment="+ids[1];

                    if (oldLink.startsWith("/manage/")) {
                        newLink = "/manage" + newLink;
                    }

                    statement.executeUpdate(
                            "UPDATE notification1 notif\n" +
                            "    SET link = '" + newLink + "' \n" +
                            "    WHERE notif.id = " + notifId);
                }
            }
        }
    }

    private void migrateAssessmentRequestedNotifications(Context context) throws SQLException {
        try (Statement statement = context.getConnection().createStatement()) {

            // fetch all Assessment_Requested notifications, but only for peer Competence assessments. There is no need
            // to update Instructor Credential assessment URLs
            try (ResultSet rs = statement.executeQuery(
                    "SELECT DISTINCT notif.id AS id, notif.link AS link\n" +
                    "FROM notification1 AS notif\n" +
                    "WHERE notif.type = 'Assessment_Requested'\n" +
                    "  AND notif.object_type = 'Competence'\n" +
                    "  AND notif.link NOT REGEXP ('\\\\/manage\\\\/credentials\\\\/([a-zA-Z0-9]*)');")
            ) {
                // Extract all data and store to the list before making any update queries
                List<String[]> notifList = new LinkedList<>();

                while (rs.next()) {
                    notifList.add(new String[] {
                            String.valueOf(rs.getLong("id")),
                            rs.getString("link"),
                    });
                }

                for (String[] notif : notifList) {
                    long notifId = Long.parseLong(notif[0]);
                    String oldLink = notif[1];

                    String[] ids = extractMatchedGroups(assessmentRequestedPattern, 2, oldLink);

                    String newLink = "/assessments/my/competences/"+ids[1];

                    statement.executeUpdate(
                            "UPDATE notification1 notif\n" +
                            "SET link = '" + newLink + "' \n" +
                            "WHERE notif.id = " + notifId);
                }
            }
        }
    }

    private void migrateAssessmentApprovedNotifications(Context context) throws SQLException {
        try (Statement statement = context.getConnection().createStatement()) {

            // fetch all Assessment_Approved notifications, but only for peer Competence assessments. There is no need
            // to update Credential assessment URLs
            try (ResultSet rs = statement.executeQuery(
                    "SELECT DISTINCT notif.id AS id, notif.link AS link, cred.id AS credId\n" +
                    "FROM notification1 AS notif\n" +
                    "  INNER JOIN competence1 comp ON notif.object_id = comp.id\n" +
                    "  INNER JOIN credential_competence1 credComp ON comp.id = credComp.competence\n" +
                    "  INNER JOIN credential1 cred ON credComp.credential = cred.id\n" +
                    "  INNER JOIN target_credential1 tCred ON tCred.credential = cred.id\n" +
                    "  INNER JOIN user_user_role userReceiverRole ON notif.receiver = userReceiverRole.user\n" +
                    "  INNER JOIN user_user_role userActorRole ON notif.actor = userActorRole.user\n" +
                    "WHERE notif.type = 'Assessment_Approved'\n" +
                    "  AND notif.object_type = 'Competence';")
            ) {
                // Extract all data and store to the list before making any update queries
                List<String[]> notifList = new LinkedList<>();

                while (rs.next()) {
                    notifList.add(new String[] {
                            String.valueOf(rs.getLong("id")),
                            rs.getString("link"),
                            String.valueOf(rs.getLong("credId")),
                    });
                }

                for (String[] notif : notifList) {
                    long notifId = Long.parseLong(notif[0]);
                    String oldLink = notif[1];
                    long credId = Long.parseLong(notif[2]);

                    String[] ids = extractMatchedGroups(assessmentApprovedPattern, 3, oldLink);

                    String assessmentSection = ids[1];

                    String newLink = "/credentials/" + idEncoder.encodeId(credId) + "/competences/" + ids[0] + "/assessments/" + assessmentSection + (assessmentSection.equals("peer") ? "/"+ids[2] : "");

                    statement.executeUpdate(
                            "UPDATE notification1 notif\n" +
                            "SET link = '" + newLink + "' \n" +
                            "WHERE notif.id = " + notifId);
                }
            }
        }
    }

    public String[] extractMatchedGroups(Pattern pattern, int numberOfGroups, String input) {
        Matcher m2 = pattern.matcher(input);
        List<String> groups = new ArrayList<>();

        if (m2.find()) {
            for (int i = 1; i <= numberOfGroups; i++) {
                groups.add(m2.group(i));
            }
        }
        return groups.stream().toArray(String[]::new);
    }

}
