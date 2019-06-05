package org.prosolo.db.migration.data.unisa;

import org.flywaydb.core.api.migration.Context;
import org.prosolo.db.migration.BaseMigration;

import java.sql.Statement;

/**
 * @author stefanvuckovic
 * @date 2019-02-21
 * @since 1.3
 */
public class V11__20181225_turn_off_peer_assessment_on_credentials extends BaseMigration {

    @Override
    protected void doMigrate(Context context) throws Exception {
        try (Statement statement = context.getConnection().createStatement()) {
            statement.executeUpdate("UPDATE credential_assessment_config c\n" +
                    "        SET c.enabled = 'F', c.blind_assessment_mode = 'OFF' WHERE c.assessment_type = 'PEER_ASSESSMENT';");
        }

    }
}
