package org.prosolo.common.domainmodel.studentprofile;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

/**
 * Represents credential to be displayed on student's profile
 *
 * @author stefanvuckovic
 * @date 2018-10-11
 * @since 1.2.0
 */
@Entity
@DiscriminatorValue("1")
public class CredentialProfileConfig extends StudentProfileConfig {

    private static final long serialVersionUID = 8195021345667577462L;

}
