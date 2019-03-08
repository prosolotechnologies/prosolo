package org.prosolo.db.migration.data.common;

import org.flywaydb.core.api.migration.Context;
import org.prosolo.db.migration.BaseMigration;

import java.sql.Statement;

/**
 * @author stefanvuckovic
 * @date 2019-02-21
 * @since 1.3
 */
public class V14__20190124_migrate_assessment_status extends BaseMigration {

    @Override
    protected void doMigrate(Context context) throws Exception {
        try (Statement statement = context.getConnection().createStatement()) {
            statement.executeUpdate("UPDATE credential_assessment c\n" +
                    "            SET c.status = 'SUBMITTED' WHERE c.approved IS TRUE;");
            statement.executeUpdate("UPDATE credential_assessment c\n" +
                    "            SET c.status = 'PENDING' WHERE c.approved IS NOT TRUE;");
            statement.executeUpdate("UPDATE competence_assessment c\n" +
                    "            SET c.status = 'SUBMITTED' WHERE c.approved IS TRUE;");
            statement.executeUpdate("UPDATE competence_assessment c\n" +
                    "            SET c.status = 'PENDING' WHERE c.approved IS NOT TRUE;");
        }

    }
}
