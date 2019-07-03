package org.prosolo.services.nodes.data;

/**
 * @author stefanvuckovic
 * @date 2019-07-03
 * @since 1.3.2
 */
public class CompetencyBasicObjectInfo extends BasicObjectInfo {

    private final long credentialId;

    public CompetencyBasicObjectInfo(long id, String title, String description, long credentialId) {
        super(id, title, description);
        this.credentialId = credentialId;
    }

    public long getCredentialId() {
        return credentialId;
    }
}
