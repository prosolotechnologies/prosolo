package org.prosolo.services.nodes.data.organization;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.prosolo.services.user.data.UserData;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * @author stefanvuckovic
 * @date 2019-03-19
 * @since 1.3
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class OrganizationBasicData implements Serializable {

    private String title;
    private List<UserData> admins = new ArrayList<>();

}
