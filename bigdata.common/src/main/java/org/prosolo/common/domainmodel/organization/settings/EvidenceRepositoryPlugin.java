package org.prosolo.common.domainmodel.organization.settings;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Type;

import javax.persistence.Column;
import javax.persistence.Entity;

/**
 * @author Nikola Milikic
 * @date 2019-05-30
 * @since 1.3.2
 */
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EvidenceRepositoryPlugin extends OrganizationPlugin {

    private boolean keywordsEnabled;
    private boolean fileEvidenceEnabled;
    private boolean urlEvidenceEnabled;
    private boolean textEvidenceEnabled;

    @Type(type="true_false")
    @Column(columnDefinition = "char(1) DEFAULT NULL")
    public boolean isKeywordsEnabled() {
        return keywordsEnabled;
    }

    public void setKeywordsEnabled(boolean keywordsEnabled) {
        this.keywordsEnabled = keywordsEnabled;
    }

    @Type(type="true_false")
    @Column(columnDefinition = "char(1) DEFAULT NULL")
    public boolean isFileEvidenceEnabled() {
        return fileEvidenceEnabled;
    }

    public void setFileEvidenceEnabled(boolean fileEvidenceEnabled) {
        this.fileEvidenceEnabled = fileEvidenceEnabled;
    }

    @Type(type="true_false")
    @Column(columnDefinition = "char(1) DEFAULT NULL")
    public boolean isUrlEvidenceEnabled() {
        return urlEvidenceEnabled;
    }

    public void setUrlEvidenceEnabled(boolean urlEvidenceEnabled) {
        this.urlEvidenceEnabled = urlEvidenceEnabled;
    }

    @Type(type="true_false")
    @Column(columnDefinition = "char(1) DEFAULT NULL")
    public boolean isTextEvidenceEnabled() {
        return textEvidenceEnabled;
    }

    public void setTextEvidenceEnabled(boolean textEvidenceEnabled) {
        this.textEvidenceEnabled = textEvidenceEnabled;
    }
}
