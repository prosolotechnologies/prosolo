package org.prosolo.services.nodes.util;

import org.prosolo.domainmodel.organization.VisibilityType;
import org.prosolo.services.nodes.exceptions.VisibilityCoercionError;

public class VisibilityUtil {

	public static VisibilityType parseVisibilityType(String visibilityString) throws VisibilityCoercionError {
		VisibilityType visType = VisibilityType.valueOf(visibilityString);

		if (visType != null) {
			return visType;
		}
		throw new VisibilityCoercionError("Error converting string \""+visibilityString+
				"\" to VisibilityType enum instance");
	}
}
