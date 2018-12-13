package org.prosolo.services.user.data.profile;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.prosolo.services.common.data.SelectableData;

import java.io.Serializable;
import java.util.List;

/**
 * @author stefanvuckovic
 * @date 2018-11-20
 * @since 1.2.0
 */
@AllArgsConstructor
@Getter
public class CredentialProfileOptionsBasicData implements Serializable {

    private static final long serialVersionUID = -1041223284178573914L;

    private final long targetCredentialId;
    private final List<CompetenceProfileOptionsBasicData> competences;
    private final List<SelectableData<Long>> assessments;

}
