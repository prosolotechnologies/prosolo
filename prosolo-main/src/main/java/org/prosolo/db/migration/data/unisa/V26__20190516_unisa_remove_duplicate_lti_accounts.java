package org.prosolo.db.migration.data.unisa;

import org.flywaydb.core.api.migration.Context;
import org.prosolo.db.migration.BaseMigration;
import org.prosolo.services.urlencoding.UrlIdEncoder;
import org.prosolo.services.urlencoding.impl.HashidsUrlIdEncoderImpl;
import org.prosolo.services.user.impl.StudentProfileManagerImpl;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.LinkedList;
import java.util.List;

/**
 * @author stefanvuckovic
 * @date 2019-05-16
 * @since 1.3.2
 */
public class V26__20190516_unisa_remove_duplicate_lti_accounts extends BaseMigration {

    @Override
    protected void doMigrate(Context context) throws Exception {
        try (Statement statement = context.getConnection().createStatement()) {
            String query = "DELETE u FROM lti_user u WHERE u.id NOT IN " +
                    "(SELECT * FROM " +
                        "(SELECT MIN(lu.id) " +
                        "FROM lti_user lu " +
                        "GROUP BY lu.user_id, lu.consumer) as ltiu)";
            statement.executeUpdate(query);
        }
    }

}
