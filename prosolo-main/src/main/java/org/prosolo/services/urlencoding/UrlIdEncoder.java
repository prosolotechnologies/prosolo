package org.prosolo.services.urlencoding;

public interface UrlIdEncoder {

	String encodeId(long id);

	long decodeId(String encodedId);

}