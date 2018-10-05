package org.prosolo.common.domainmodel.activitywall;

import org.prosolo.common.domainmodel.organization.Unit;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.OneToOne;

/**
 * @author stefanvuckovic
 * @date 2018-03-29
 * @since 1.2.0
 */
@Entity
public class UnitWelcomePostSocialActivity extends SocialActivity1 {

    private static final long serialVersionUID = -2670338796652550439L;

    private Unit unit;

    @OneToOne(fetch = FetchType.LAZY)
    public Unit getUnit() {
        return unit;
    }

    public void setUnit(Unit unit) {
        this.unit = unit;
    }
}
