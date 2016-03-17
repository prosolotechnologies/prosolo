package org.prosolo.services.email.emailLinks;

import java.util.Optional;

public class EnumUtil {

	public static <T extends Enum<T>> Optional<T> getEnumFromString(Class<T> enumClass, String enumString) {
		try {
			return Optional.of(Enum.valueOf(enumClass, enumString));
		} catch(Exception e) {
			return Optional.empty();
		}
	}
}
