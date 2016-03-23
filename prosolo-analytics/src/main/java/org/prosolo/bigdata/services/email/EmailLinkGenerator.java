package org.prosolo.bigdata.services.email;

import java.util.LinkedHashMap;

public interface EmailLinkGenerator {

	String getLink(long userId, LinkedHashMap<String, Long> contextParams);

}