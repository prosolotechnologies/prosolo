package org.prosolo.util.nodes;

import java.util.Collection;

import org.prosolo.domainmodel.general.BaseEntity;

public class NodeUtil {

	public static String getCSVStringOfIds(Collection<? extends BaseEntity> resources) {
		StringBuffer sb = new StringBuffer();
		
		int i = 0;
		
		for (BaseEntity res : resources) {
			sb.append(res.getId());
			
			if (!(i == resources.size() - 1)) {
				sb.append(",");
			}
			i++;
		}
		
		return sb.toString();
	}
}
