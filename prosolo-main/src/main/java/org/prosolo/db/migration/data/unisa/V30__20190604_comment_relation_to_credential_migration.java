package org.prosolo.db.migration.data.unisa;

import org.flywaydb.core.api.migration.Context;
import org.prosolo.db.migration.BaseMigration;

import java.sql.ResultSet;
import java.sql.Statement;

/**
 * @author stefanvuckovic
 * @date 2019-05-16
 * @since 1.3.2
 */
public class V30__20190604_comment_relation_to_credential_migration extends BaseMigration {

    @Override
    protected void doMigrate(Context context) throws Exception {
        try (Statement statement = context.getConnection().createStatement()) {
            statement.addBatch("UPDATE comment1 c SET c.credential = 32794 WHERE c.id = 294912");
            statement.addBatch("DELETE c FROM comment1 c WHERE c.id = 229376");
            statement.addBatch("UPDATE comment1 c SET c.credential = " +
                    "(SELECT tc.credential FROM `target_credential1` tc WHERE tc.user = c.user and tc.credential in " +
                            "(SELECT cc.credential FROM credential_competence1 cc INNER JOIN credential1 cred on cred.id = cc.credential WHERE cc.competence = c.commented_resource_id AND cred.type = 'Delivery')) " +
                    "WHERE c.resource_type = 'Competence' and c.manager_comment = 'F' AND c.id IN (1,2,3,4,5,6,32768,65536,163840,163841,163842,163843,196608,196609,196610,229378,262144,262145,262146,262147)");
            statement.addBatch("UPDATE comment1 c SET c.credential = " +
                    "(SELECT ci.credential FROM `credential_instructor` ci WHERE ci.user = c.user and ci.credential in " +
                            "(SELECT cc.credential FROM credential_competence1 cc INNER JOIN credential1 cred ON cred.id = cc.credential WHERE cc.competence = c.commented_resource_id AND cred.type = 'Delivery')) " +
                    "WHERE c.resource_type = 'Competence' and c.manager_comment = 'T' AND c.id IN (1,2,3,4,5,6,32768,65536,163840,163841,163842,163843,196608,196609,196610,229378,262144,262145,262146,262147)");
            statement.executeBatch();
        }
    }

}
