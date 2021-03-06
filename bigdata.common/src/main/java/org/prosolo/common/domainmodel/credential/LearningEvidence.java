package org.prosolo.common.domainmodel.credential;

import org.prosolo.common.domainmodel.annotation.Tag;
import org.prosolo.common.domainmodel.general.BaseEntity;
import org.prosolo.common.domainmodel.organization.Organization;
import org.prosolo.common.domainmodel.user.User;

import javax.persistence.*;
import java.util.Set;

/**
 * @author stefanvuckovic
 * @date 2017-12-04
 * @since 1.2.0
 */
@Entity
@AttributeOverride(name = "title", column = @Column(name = "title", columnDefinition="varchar(500)"))
public class LearningEvidence extends BaseEntity {

    private Organization organization;
    private User user;
    private LearningEvidenceType type;
    private String url;
    private Set<Tag> tags;
    private Set<CompetenceEvidence> competences;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    public Organization getOrganization() {
        return organization;
    }

    public void setOrganization(Organization organization) {
        this.organization = organization;
    }

    @ManyToMany
    public Set<Tag> getTags() {
        return tags;
    }

    public void setTags(Set<Tag> tags) {
        this.tags = tags;
    }

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    public LearningEvidenceType getType() {
        return type;
    }

    public void setType(LearningEvidenceType type) {
        this.type = type;
    }

    @Column(columnDefinition="varchar(1200)")
    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    @OneToMany(mappedBy = "evidence", cascade = CascadeType.REMOVE, orphanRemoval = true)
    public Set<CompetenceEvidence> getCompetences() {
        return competences;
    }

    public void setCompetences(Set<CompetenceEvidence> competences) {
        this.competences = competences;
    }

}
