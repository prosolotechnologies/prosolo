package org.prosolo.web.courses.credential;

import org.apache.log4j.Logger;
import org.prosolo.bigdata.common.exceptions.DbConnectionException;
import org.prosolo.common.domainmodel.user.UserGroupPrivilege;
import org.prosolo.common.util.string.StringUtil;
import org.prosolo.services.common.data.LazyInitData;
import org.prosolo.services.nodes.UserGroupManager;
import org.prosolo.services.nodes.data.credential.CredentialData;

import javax.inject.Inject;
import java.io.Serializable;
import java.util.List;

/**
 * @author stefanvuckovic
 * @date 2018-08-20
 * @since 1.2.0
 */
public abstract class DeliveriesBean implements Serializable {

    private static final long serialVersionUID = -8705926380482295861L;
    private static Logger logger = Logger.getLogger(DeliveriesBean.class);

    private static final int WHO_CAN_LEARN_LIMIT = 5;

    @Inject private UserGroupManager userGroupManager;

    public abstract boolean canUserNavigateToWhoCanLearnPage();

    public void initGroupsThatCanLearnData(CredentialData delivery) {
        if (!delivery.getGroupsThatCanLearn().isInitialized()) {
            try {
                delivery.getGroupsThatCanLearn().init(userGroupManager.getCredentialUserGroupsNames(delivery.getIdData().getId(), UserGroupPrivilege.Learn, WHO_CAN_LEARN_LIMIT));
            } catch (DbConnectionException e) {
                logger.error("Error", e);
            }
        }
    }

    public String getGroupsThatCanLearnString(CredentialData delivery) {
        return getCSVStringFromList(delivery.getGroupsThatCanLearn());
    }

    public void initStudentsWhoCanLearnData(CredentialData delivery) {
        if (!delivery.getStudentsWhoCanLearn().isInitialized()) {
            try {
                delivery.getStudentsWhoCanLearn().init(userGroupManager.getCredentialVisibilityUsersNames(delivery.getIdData().getId(), UserGroupPrivilege.Learn, WHO_CAN_LEARN_LIMIT));
            } catch (DbConnectionException e) {
                logger.error("Error", e);
            }
        }
    }

    public String getStudentsWhoCanLearnString(CredentialData delivery) {
        return getCSVStringFromList(delivery.getStudentsWhoCanLearn());
    }

    private String getCSVStringFromList(LazyInitData<String> data) {
        List<String> l = data.getData();
        if (l == null) {
            return "";
        }
        String csv = String.join(", ", l);
        if (l.size() < data.getCount()) {
            csv += "...";
        }
        return csv;
    }

}
