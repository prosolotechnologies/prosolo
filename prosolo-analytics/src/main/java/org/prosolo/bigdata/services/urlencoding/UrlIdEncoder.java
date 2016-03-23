package org.prosolo.bigdata.services.urlencoding;

public interface UrlIdEncoder {

	String encodeId(long id);

	long decodeId(String encodedId);

}