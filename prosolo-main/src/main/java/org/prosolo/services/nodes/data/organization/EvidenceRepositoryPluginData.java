package org.prosolo.services.nodes.data.organization;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.prosolo.common.domainmodel.organization.settings.EvidenceRepositoryPlugin;

import java.io.Serializable;

/**
 * @author Nikola Milikic
 * @date 2019-05-30
 * @since 1.3.2
 */
@Getter
@Setter
@NoArgsConstructor
public class EvidenceRepositoryPluginData implements Serializable {

    private long pluginId;
    private boolean enabled;
    private boolean keywordsEnabled;
    private boolean fileEvidenceEnabled;
    private boolean urlEvidenceEnabled;
    private boolean textEvidenceEnabled;

    public EvidenceRepositoryPluginData(EvidenceRepositoryPlugin evidenceRepositoryPlugin) {
        this.pluginId = evidenceRepositoryPlugin.getId();
        this.enabled = evidenceRepositoryPlugin.isEnabled();
        this.keywordsEnabled = evidenceRepositoryPlugin.isKeywordsEnabled();
        this.fileEvidenceEnabled = evidenceRepositoryPlugin.isFileEvidenceEnabled();
        this.urlEvidenceEnabled = evidenceRepositoryPlugin.isUrlEvidenceEnabled();
        this.textEvidenceEnabled = evidenceRepositoryPlugin.isTextEvidenceEnabled();
    }
}

