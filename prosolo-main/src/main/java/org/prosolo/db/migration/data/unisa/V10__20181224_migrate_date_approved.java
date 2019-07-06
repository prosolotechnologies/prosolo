package org.prosolo.db.migration.data.unisa;

import org.flywaydb.core.api.migration.Context;
import org.prosolo.db.migration.BaseMigration;

import java.sql.Statement;

/**
 * @author stefanvuckovic
 * @date 2019-02-21
 * @since 1.3
 */
public class V10__20181224_migrate_date_approved extends BaseMigration {

    @Override
    protected void doMigrate(Context context) throws Exception {
        try (Statement statement = context.getConnection().createStatement()) {
            statement.executeUpdate("UPDATE credential_assessment ca\n" +
                    "    SET ca.date_approved = '2018-10-02 06:02:07'\n" +
                    "    WHERE ca.id = 295058;");
            statement.executeUpdate("UPDATE credential_assessment ca\n" +
                    "    SET ca.date_approved = '2018-08-10 01:00:11'\n" +
                    "    WHERE ca.id = 229670;");
            statement.executeUpdate("UPDATE competence_assessment ca\n" +
                    "    SET ca.date_approved = '2018-10-02 05:44:16'\n" +
                    "    WHERE ca.id = 295684;");
            statement.executeUpdate("UPDATE competence_assessment ca\n" +
                    "    SET ca.date_approved = '2018-10-02 05:49:05'\n" +
                    "    WHERE ca.id = 295685;");
            statement.executeUpdate("UPDATE competence_assessment ca\n" +
                    "    SET ca.date_approved = '2018-10-02 05:52:09'\n" +
                    "    WHERE ca.id = 295686;");
            statement.executeUpdate("UPDATE competence_assessment ca\n" +
                    "    SET ca.date_approved = '2018-10-02 05:55:59'\n" +
                    "    WHERE ca.id = 295687;");
            statement.executeUpdate("UPDATE competence_assessment ca\n" +
                    "    SET ca.date_approved = '2018-10-02 05:57:45'\n" +
                    "    WHERE ca.id = 295688;");
            statement.executeUpdate("UPDATE competence_assessment ca\n" +
                    "    SET ca.date_approved = '2018-10-02 05:59:05'\n" +
                    "    WHERE ca.id = 295689;");
            statement.executeUpdate("UPDATE competence_assessment ca\n" +
                    "    SET ca.date_approved = '2018-10-02 06:06:23'\n" +
                    "    WHERE ca.id = 756235;");
            statement.executeUpdate("UPDATE competence_assessment ca\n" +
                    "    SET ca.date_approved = '2018-10-02 06:08:19'\n" +
                    "    WHERE ca.id = 756236;");
            statement.executeUpdate("UPDATE competence_assessment ca\n" +
                    "    SET ca.date_approved = '2018-10-02 06:16:00'\n" +
                    "    WHERE ca.id = 756237;");
            statement.executeUpdate("UPDATE competence_assessment ca\n" +
                    "    SET ca.date_approved = '2018-10-02 06:18:30'\n" +
                    "    WHERE ca.id = 756238;");
            statement.executeUpdate("UPDATE competence_assessment ca\n" +
                    "    SET ca.date_approved = '2018-10-02 06:21:21'\n" +
                    "    WHERE ca.id = 756239;");
            statement.executeUpdate("UPDATE competence_assessment ca\n" +
                    "    SET ca.date_approved = '2018-08-26 23:47:25'\n" +
                    "    WHERE ca.id = 295626;");
            statement.executeUpdate("UPDATE competence_assessment ca\n" +
                    "    SET ca.date_approved = '2018-09-07 05:48:17'\n" +
                    "    WHERE ca.id = 164199;");
            statement.executeUpdate("UPDATE competence_assessment ca\n" +
                    "    SET ca.date_approved = '2018-09-07 05:49:10'\n" +
                    "    WHERE ca.id = 229592;");
            statement.executeUpdate("UPDATE competence_assessment ca\n" +
                    "    SET ca.date_approved = '2018-09-07 05:49:30'\n" +
                    "    WHERE ca.id = 229593;");
            statement.executeUpdate("UPDATE competence_assessment ca\n" +
                    "    SET ca.date_approved = '2018-09-07 05:49:46'\n" +
                    "    WHERE ca.id = 295625;");
            statement.executeUpdate("UPDATE competence_assessment ca\n" +
                    "    SET ca.date_approved = '2018-09-07 05:50:20'\n" +
                    "    WHERE ca.id = 295627;");
            statement.executeUpdate("UPDATE competence_assessment ca\n" +
                    "    SET ca.date_approved = '2018-09-07 05:50:38'\n" +
                    "    WHERE ca.id = 296079;");
            statement.executeUpdate("UPDATE competence_assessment ca\n" +
                    "    SET ca.date_approved = '2018-09-07 05:50:54'\n" +
                    "    WHERE ca.id = 296089;");
            statement.executeUpdate("UPDATE competence_assessment ca\n" +
                    "    SET ca.date_approved = '2018-09-07 05:51:07'\n" +
                    "    WHERE ca.id = 296090;");
            statement.executeUpdate("UPDATE competence_assessment ca\n" +
                    "    SET ca.date_approved = '2018-09-07 05:51:29'\n" +
                    "    WHERE ca.id = 296100;");
            statement.executeUpdate("UPDATE competence_assessment ca\n" +
                    "    SET ca.date_approved = '2018-09-11 06:04:00'\n" +
                    "    WHERE ca.id = 164195;");
            statement.executeUpdate("UPDATE competence_assessment ca\n" +
                    "    SET ca.date_approved = '2018-09-04 02:50:53'\n" +
                    "    WHERE ca.id = 297652;");
            statement.executeUpdate("UPDATE competence_assessment ca\n" +
                    "    SET ca.date_approved = '2018-08-15 03:41:03'\n" +
                    "    WHERE ca.id = 131072;");
            statement.executeUpdate("UPDATE competence_assessment ca\n" +
                    "    SET ca.date_approved = '2018-10-04 01:44:50'\n" +
                    "    WHERE ca.id = 756539;");
            statement.executeUpdate("UPDATE competence_assessment ca\n" +
                    "    SET ca.date_approved = '2018-09-03 03:08:49'\n" +
                    "    WHERE ca.id = 164533;");
            statement.executeUpdate("UPDATE competence_assessment ca\n" +
                    "    SET ca.date_approved = '2018-09-17 00:15:38'\n" +
                    "    WHERE ca.id = 755632;");
            statement.executeUpdate("UPDATE competence_assessment ca\n" +
                    "    SET ca.date_approved = '2018-08-16 00:45:24'\n" +
                    "    WHERE ca.id = 196922;");
            statement.executeUpdate("UPDATE competence_assessment ca\n" +
                    "    SET ca.date_approved = '2018-09-10 05:05:14'\n" +
                    "    WHERE ca.id = 296714;");
            statement.executeUpdate("UPDATE competence_assessment ca\n" +
                    "    SET ca.date_approved = '2018-09-10 05:06:03'\n" +
                    "    WHERE ca.id = 296715;");
            statement.executeUpdate("UPDATE competence_assessment ca\n" +
                    "    SET ca.date_approved = '2018-09-22 05:54:08'\n" +
                    "    WHERE ca.id = 756179;");
            statement.executeUpdate("UPDATE competence_assessment ca\n" +
                    "    SET ca.date_approved = '2018-11-16 02:37:21'\n" +
                    "    WHERE ca.id = 230575;");
            statement.executeUpdate("UPDATE competence_assessment ca\n" +
                    "    SET ca.date_approved = '2018-11-01 06:16:19'\n" +
                    "    WHERE ca.id = 917515;");
            statement.executeUpdate("UPDATE competence_assessment ca\n" +
                    "    SET ca.date_approved = '2018-10-22 23:17:57'\n" +
                    "    WHERE ca.id = 753756;");
            statement.executeUpdate("UPDATE competence_assessment ca\n" +
                    "    SET ca.date_approved = '2018-10-22 23:18:16'\n" +
                    "    WHERE ca.id = 753757;");
            statement.executeUpdate("UPDATE competence_assessment ca\n" +
                    "    SET ca.date_approved = '2018-10-22 23:34:45'\n" +
                    "    WHERE ca.id = 755863;");
            statement.executeUpdate("UPDATE competence_assessment ca\n" +
                    "    SET ca.date_approved = '2018-09-05 11:22:25'\n" +
                    "    WHERE ca.id = 164201;");
            statement.executeUpdate("UPDATE competence_assessment ca\n" +
                    "    SET ca.date_approved = '2018-09-12 10:03:26'\n" +
                    "    WHERE ca.id = 229587;");
            statement.executeUpdate("UPDATE competence_assessment ca\n" +
                    "    SET ca.date_approved = '2018-09-12 10:48:30'\n" +
                    "    WHERE ca.id = 295619;");
            statement.executeUpdate("UPDATE competence_assessment ca\n" +
                    "    SET ca.date_approved = '2018-09-12 10:51:36'\n" +
                    "    WHERE ca.id = 263142;");
            statement.executeUpdate("UPDATE competence_assessment ca\n" +
                    "    SET ca.date_approved = '2018-09-12 11:43:21'\n" +
                    "    WHERE ca.id = 296286;");
            statement.executeUpdate("UPDATE competence_assessment ca\n" +
                    "    SET ca.date_approved = '2018-09-12 12:00:05'\n" +
                    "    WHERE ca.id = 197684;");
            statement.executeUpdate("UPDATE competence_assessment ca\n" +
                    "    SET ca.date_approved = '2018-09-12 12:08:59'\n" +
                    "    WHERE ca.id = 164283;");
            statement.executeUpdate("UPDATE competence_assessment ca\n" +
                    "    SET ca.date_approved = '2018-09-12 12:09:13'\n" +
                    "    WHERE ca.id = 164281;");
            statement.executeUpdate("UPDATE competence_assessment ca\n" +
                    "    SET ca.date_approved = '2018-10-06 04:45:44'\n" +
                    "    WHERE ca.id = 296287;");
            statement.executeUpdate("UPDATE competence_assessment ca\n" +
                    "    SET ca.date_approved = '2018-10-06 04:51:53'\n" +
                    "    WHERE ca.id = 296288;");
            statement.executeUpdate("UPDATE competence_assessment ca\n" +
                    "    SET ca.date_approved = '2018-10-06 04:57:05'\n" +
                    "    WHERE ca.id = 295442;");
            statement.executeUpdate("UPDATE competence_assessment ca\n" +
                    "    SET ca.date_approved = '2018-10-06 04:57:06'\n" +
                    "    WHERE ca.id = 295441;");
            statement.executeUpdate("UPDATE competence_assessment ca\n" +
                    "    SET ca.date_approved = '2018-10-07 01:32:07'\n" +
                    "    WHERE ca.id = 295656;");
            statement.executeUpdate("UPDATE competence_assessment ca\n" +
                    "    SET ca.date_approved = '2018-10-07 01:32:11'\n" +
                    "    WHERE ca.id = 295655;");
            statement.executeUpdate("UPDATE competence_assessment ca\n" +
                    "    SET ca.date_approved = '2018-10-07 01:32:53'\n" +
                    "    WHERE ca.id = 295654;");
            statement.executeUpdate("UPDATE competence_assessment ca\n" +
                    "    SET ca.date_approved = '2018-10-07 05:24:48'\n" +
                    "    WHERE ca.id = 295969;");
            statement.executeUpdate("UPDATE competence_assessment ca\n" +
                    "    SET ca.date_approved = '2018-10-07 11:01:40'\n" +
                    "    WHERE ca.id = 165683;");
            statement.executeUpdate("UPDATE competence_assessment ca\n" +
                    "    SET ca.date_approved = '2018-10-07 11:01:44'\n" +
                    "    WHERE ca.id = 165684;");
            statement.executeUpdate("UPDATE competence_assessment ca\n" +
                    "    SET ca.date_approved = '2018-10-07 11:09:31'\n" +
                    "    WHERE ca.id = 655374;");
            statement.executeUpdate("UPDATE competence_assessment ca\n" +
                    "    SET ca.date_approved = '2018-10-07 11:12:33'\n" +
                    "    WHERE ca.id = 655375;");
            statement.executeUpdate("UPDATE competence_assessment ca\n" +
                    "    SET ca.date_approved = '2018-10-07 11:42:38'\n" +
                    "    WHERE ca.id = 296206;");
            statement.executeUpdate("UPDATE competence_assessment ca\n" +
                    "    SET ca.date_approved = '2018-10-07 11:45:13'\n" +
                    "    WHERE ca.id = 296207;");
            statement.executeUpdate("UPDATE competence_assessment ca\n" +
                    "    SET ca.date_approved = '2018-10-07 11:59:52'\n" +
                    "    WHERE ca.id = 165696;");
            statement.executeUpdate("UPDATE competence_assessment ca\n" +
                    "    SET ca.date_approved = '2018-10-07 11:59:53'\n" +
                    "    WHERE ca.id = 165695;");
            statement.executeUpdate("UPDATE competence_assessment ca\n" +
                    "    SET ca.date_approved = '2018-10-07 12:02:01'\n" +
                    "    WHERE ca.id = 165697;");
            statement.executeUpdate("UPDATE competence_assessment ca\n" +
                    "    SET ca.date_approved = '2018-10-08 10:44:42'\n" +
                    "    WHERE ca.id = 756379;");
            statement.executeUpdate("UPDATE competence_assessment ca\n" +
                    "    SET ca.date_approved = '2018-10-08 11:18:06'\n" +
                    "    WHERE ca.id = 786649;");
            statement.executeUpdate("UPDATE competence_assessment ca\n" +
                    "    SET ca.date_approved = '2018-10-08 11:19:38'\n" +
                    "    WHERE ca.id = 786650;");
            statement.executeUpdate("UPDATE competence_assessment ca\n" +
                    "    SET ca.date_approved = '2018-10-08 11:20:50'\n" +
                    "    WHERE ca.id = 786648;");
            statement.executeUpdate("UPDATE competence_assessment ca\n" +
                    "    SET ca.date_approved = '2018-10-08 11:49:42'\n" +
                    "    WHERE ca.id = 197652;");
            statement.executeUpdate("UPDATE competence_assessment ca\n" +
                    "    SET ca.date_approved = '2018-10-08 11:50:31'\n" +
                    "    WHERE ca.id = 197653;");
            statement.executeUpdate("UPDATE competence_assessment ca\n" +
                    "    SET ca.date_approved = '2018-11-22 10:08:02'\n" +
                    "    WHERE ca.id = 754274;");
            statement.executeUpdate("UPDATE competence_assessment ca\n" +
                    "    SET ca.date_approved = '2018-11-22 10:09:17'\n" +
                    "    WHERE ca.id = 754275;");
            statement.executeUpdate("UPDATE competence_assessment ca\n" +
                    "    SET ca.date_approved = '2018-09-20 11:35:16'\n" +
                    "    WHERE ca.id = 755793;");
            statement.executeUpdate("UPDATE competence_assessment ca\n" +
                    "    SET ca.date_approved = '2018-09-20 14:07:01'\n" +
                    "    WHERE ca.id = 426186;");
            statement.executeUpdate("UPDATE competence_assessment ca\n" +
                    "    SET ca.date_approved = '2018-10-14 11:02:45'\n" +
                    "    WHERE ca.id = 297390;");
            statement.executeUpdate("UPDATE competence_assessment ca\n" +
                    "    INNER JOIN credential_competence_assessment cca\n" +
                    "    on ca.id = cca.competence_assessment\n" +
                    "    INNER JOIN credential_assessment credA\n" +
                    "    on cca.credential_assessment = credA.id and credA.approved is true\n" +
                    "    SET ca.date_approved = credA.date_approved\n" +
                    "    WHERE ca.approved is true and ca.date_approved is NULL;");

            statement.executeUpdate("UPDATE competence_assessment ca\n" +
                    "    SET ca.date_approved = ca.last_assessment\n" +
                    "    WHERE ca.approved IS TRUE and ca.date_approved IS NULL;");

            statement.executeUpdate("UPDATE competence_assessment ca\n" +
                    "    SET ca.date_approved = ca.last_asked_for_assessment\n" +
                    "    WHERE ca.approved IS TRUE and ca.date_approved IS NULL;");

            statement.executeUpdate("UPDATE competence_assessment ca\n" +
                    "    INNER JOIN target_competence1 tc ON ca.competence = tc.competence AND ca.student = tc.user\n" +
                    "    SET ca.date_approved = tc.date_completed\n" +
                    "    WHERE ca.approved IS TRUE and ca.date_approved IS NULL;");

            statement.executeUpdate("UPDATE competence_assessment ca\n" +
                    "    SET ca.date_approved = ca.created\n" +
                    "    WHERE ca.approved IS TRUE and ca.date_approved IS NULL;");
        }
    }



}
