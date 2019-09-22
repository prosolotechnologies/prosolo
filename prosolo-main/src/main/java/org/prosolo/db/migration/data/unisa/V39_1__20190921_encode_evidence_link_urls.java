package org.prosolo.db.migration.data.unisa;

import org.flywaydb.core.api.migration.Context;
import org.prosolo.db.migration.BaseMigration;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * Migrate evidence links that should have been encoded..
 *
 * @author Nikola Milikic
 * @date 2019-09-21
 * @since 1.3.2
 */
public class V39_1__20190921_encode_evidence_link_urls extends BaseMigration {

    @Override
    protected void doMigrate(Context context) throws Exception {
        migrateEvidenceLinks(context.getConnection());
    }

    private void migrateEvidenceLinks(Connection connection) throws SQLException {
        try (PreparedStatement ps = connection.prepareStatement(
                "UPDATE learning_evidence le " +
                        "SET le.url = ? " +
                        "WHERE le.id = ?")) {

            ps.setString(1, "https://unisafiles.prosolo.ca/files/5906c435d8f7e51b2cf184405fc34e53/Journal%2522%20Summary%20Statement%20.docx");
            ps.setLong(2, 100);
            ps.addBatch();

            ps.setString(1, "https://unisafiles.prosolo.ca/files/e11723bf444038ce8c87a1e1211903da/Topic%201%20Journal%20Entry-%20Why%20I%20Want%20to%20be%20a%20Teacher%3F.docx");
            ps.setLong(2, 213);
            ps.addBatch();

            ps.setString(1, "https://unisafiles.prosolo.ca/files/ddaa7bfd6ae3cf90232a80b1e8240142/Topic%201%20Journal%20Entry-%20Why%20I%20Want%20to%20be%20a%20Teacher%3F.docx");
            ps.setLong(2, 215);
            ps.addBatch();

            ps.setString(1, "https://unisafiles.prosolo.ca/files/75b98914f0ea323a31843a85a3beaa9c/Journal%20Entry%20%231.docx");
            ps.setLong(2, 269);
            ps.addBatch();

            ps.setString(1, "https://unisafiles.prosolo.ca/files/141ff6d1cec09ea4b2c505aa9bb02940/Journal%20Entry%20-%20Why%20I%20want%20to%20be%20a%20teacher%3F.docx");
            ps.setLong(2, 65585);
            ps.addBatch();

            ps.setString(1, "https://unisafiles.prosolo.ca/files/4fd30a740b41df08c49e0211f0a20c5a/Why%20do%20I%20want%20to%20be%20a%20teacher%3F.docx");
            ps.setLong(2, 229504);
            ps.addBatch();

            ps.setString(1, "https://unisafiles.prosolo.ca/files/8480771956e2a43ee2070a613e9d028c/lzq-doorflyer%2Bstatement.pdf");
            ps.setLong(2, 229668);
            ps.addBatch();

            ps.setString(1, "https://unisafiles.prosolo.ca/files/309c1280e2171919a4152afeb6b5664e/Yoga%20lesson%2Beval.doc");
            ps.setLong(2, 262162);
            ps.addBatch();

            ps.setString(1, "https://unisafiles.prosolo.ca/files/009703635bb332be30f0782c8e69a866/block%2Bplan1%20%28evaluation%29.docx");
            ps.setLong(2, 262424);
            ps.addBatch();

            ps.setString(1, "https://unisafiles.prosolo.ca/files/e6f983d41dc69f900d14a40edfd8c85b/Maths%20lesson%20plan%20%2B%20evaluation%20of%20children%27s%20work%20samples.pdf");
            ps.setLong(2, 262478);
            ps.addBatch();

            ps.setString(1, "https://unisafiles.prosolo.ca/files/aee3b5eb0027cb2d250dad78c572679d/teacher%27s%20feed%20back%20%2B%20adjustment%20for%20the%20following%20plans.pdf");
            ps.setLong(2, 262480);
            ps.addBatch();

            ps.setString(1, "https://unisafiles.prosolo.ca/files/53530c14288c2d81323ee9356100379f/1%20How%20much%20I%20love%20you%2B%20%20eva.docx");
            ps.setLong(2, 295231);
            ps.addBatch();

            ps.setString(1, "https://unisafiles.prosolo.ca/files/b87d89529e3dc2e518e5d7e56be6e304/2%20counters%20measurement%2Beva.docx");
            ps.setLong(2, 295232);
            ps.addBatch();

            ps.setString(1, "https://unisafiles.prosolo.ca/files/6e509a8408b59baafa40c48fcb68e49e/3%20measurement%20strip%2Beva.docx");
            ps.setLong(2, 295233);
            ps.addBatch();

            ps.setString(1, "https://unisafiles.prosolo.ca/files/0a55ae9ba9f9292ae7bd514ede3fa2a7/G1%20bee%20bots%20%2B%20eva.docx");
            ps.setLong(2, 295237);
            ps.addBatch();

            ps.setString(1, "https://unisafiles.prosolo.ca/files/ec5192881d26a5f418597006dfd51710/L1%20Ratty%20%2B%20eva.docx");
            ps.setLong(2, 295240);
            ps.addBatch();

            ps.setString(1, "https://unisafiles.prosolo.ca/files/12a710d1067918f9920ee749681108ca/Year%2011%20Biology%20Feedback%20from%20Mentor%20%231%20-%2030th%20July.jpg");
            ps.setLong(2, 295463);
            ps.addBatch();

            ps.setString(1, "https://unisafiles.prosolo.ca/files/88ecfbe8462eb847e5b5455ff34cd4e6/Year%2011%20Biology%20Feedback%20from%20Mentor%20%232%20-%202nd%20August.jpg");
            ps.setLong(2, 295464);
            ps.addBatch();

            ps.setString(1, "https://unisafiles.prosolo.ca/files/77313535f1d37a8dc9d853585064133a/Year%2011%20Biology%20Feedback%20from%20Mentor%20%233%20-%2013th%20August.jpg");
            ps.setLong(2, 295465);
            ps.addBatch();

            ps.setString(1, "https://unisafiles.prosolo.ca/files/a4dd895dfb15c9b7aedfd9999d1fdcc7/Year%2010%20Oztag%20-%20survey%20question%20%237.docx");
            ps.setLong(2, 360452);
            ps.addBatch();

            ps.setString(1, "https://unisafiles.prosolo.ca/files/c54003a362e517632391396db7e50d65/Syllables%20%232.doc");
            ps.setLong(2, 360557);
            ps.addBatch();

            ps.setString(1, "https://unisafiles.prosolo.ca/files/c60b5b227465774a885c3cbaa99adf20/Syllables%20%233.doc");
            ps.setLong(2, 360559);
            ps.addBatch();

            ps.setString(1, "https://unisafiles.prosolo.ca/files/ddd082db2d10b456bfedcc82e5b8cf70/EDEH%252c%20M%20-%20PD%20Cert.pdf");
            ps.setLong(2, 458752);
            ps.addBatch();

            ps.setString(1, "https://unisafiles.prosolo.ca/files/d96b4c2584ea47892f869f4e44c4fb82/YING%252c%20B%20-%20PD%20Cert.pdf");
            ps.setLong(2, 491573);
            ps.addBatch();

            ps.setString(1, "https://unisafiles.prosolo.ca/files/9efed6161dd3ae782a7bc7c0a154283d/Chance%20%231.doc");
            ps.setLong(2, 491593);
            ps.addBatch();

            ps.setString(1, "https://unisafiles.prosolo.ca/files/7116cd3a9a30ba88b50266ca6c84de4d/Emily%20Easling_5.9.18%20final%20report%20draft%20%28Turley%25252c%20Kathy%20%28Port%20Elliot%20Primary%20School%29%29%20%281%29.pdf");
            ps.setLong(2, 720915);
            ps.addBatch();

            ps.setString(1, "https://unisafiles.prosolo.ca/files/2b469cbfab8b28a426f86233e8dff4bc/Emily%20Easling_5.9.18%20final%20report%20draft%20%28Turley%25252c%20Kathy%20%28Port%20Elliot%20Primary%20School%29%29%20%281%29.pdf");
            ps.setLong(2, 720917);
            ps.addBatch();

            ps.setString(1, "https://unisafiles.prosolo.ca/files/1d85bbc6b6417a6981835848b14eb807/Emily%20Easling_5.9.18%20final%20report%20draft%20%28Turley%25252c%20Kathy%20%28Port%20Elliot%20Primary%20School%29%29%20%281%29.pdf");
            ps.setLong(2, 720922);
            ps.addBatch();

            ps.setString(1, "https://unisafiles.prosolo.ca/files/593466b8d940cd9ac7ca63c977027a0d/Emily%20Easling_5.9.18%20final%20report%20draft%20%28Turley%25252c%20Kathy%20%28Port%20Elliot%20Primary%20School%29%29%20%281%29.pdf");
            ps.setLong(2, 720924);
            ps.addBatch();

            ps.setString(1, "https://unisafiles.prosolo.ca/files/b604fbd58f7af917679020c50a035fdc/Emily%20Easling_5.9.18%20final%20report%20draft%20%28Turley%25252c%20Kathy%20%28Port%20Elliot%20Primary%20School%29%29%20%281%29.pdf");
            ps.setLong(2, 720925);
            ps.addBatch();

            ps.setString(1, "https://unisafiles.prosolo.ca/files/552fc98d3dcb96b74dfb3550a2f08891/PROSOLO%20%2B%20Year%2010%20Drama%20-%20Verbatim%20%28Daniel%20Week%204%20Tuesday%29.docx");
            ps.setLong(2, 786619);
            ps.addBatch();

            ps.setString(1, "https://unisafiles.prosolo.ca/files/95ae225cd79d1e85a1458447ac5cd510/PROSOLO%20%2B%20Year%2010%20Drama%20-%20Verbatim%20%28Sue%20Week%204%20Wednesday%29%20%281%29.docx");
            ps.setLong(2, 786620);
            ps.addBatch();

            ps.setString(1, "https://unisafiles.prosolo.ca/files/e603e639cbd69cf7892aa2fbf45f535f/Student%20work%20edited%20%232.docx");
            ps.setLong(2, 1049205);
            ps.addBatch();

            ps.setString(1, "https://unisafiles.prosolo.ca/files/f53a4ad946d818f9ca2f5638f0f3f080/Why%20I%20Want%20To%20Be%20A%20Teacher%3F.docx");
            ps.setLong(2, 1114127);
            ps.addBatch();

            ps.setString(1, "https://unisafiles.prosolo.ca/files/235a5c4a1583725bb656e0fd49e5fbb1/Human%20Eye%20-%20%23aumsum.mp4");
            ps.setLong(2, 1409173);
            ps.addBatch();

            ps.setString(1, "https://unisafiles.prosolo.ca/files/d6f93591cff3dab696c68305900426c2/Hanball%20-%20Evaluation%20of%20teacher%20outcomes%20%232.docx");
            ps.setLong(2, 1573348);
            ps.addBatch();

            ps.setString(1, "https://unisafiles.prosolo.ca/files/5e00e039f132189308821f1aac8dfdcf/PowerPoint%20%27What%20Landscape%20am%20I%3F%22.png");
            ps.setLong(2, 1606400);
            ps.addBatch();

            ps.executeBatch();
        }
    }

}
