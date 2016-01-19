package org.prosolo.services.urlencoding.impl;

import org.hashids.Hashids;
import org.prosolo.app.Settings;
import org.prosolo.services.urlencoding.UrlIdEncoder;
import org.springframework.stereotype.Service;

@Service("org.prosolo.services.urlencoding.UrlIdEncoder")
public class HashidsUrlIdEncoderImpl implements UrlIdEncoder {

	private String salt = Settings.getInstance().config.application.urlEncoding.salt;
	private static final String alphabet = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890";
	private static final int minHashLength = 8;
	
	private Hashids hashids;
	
	public HashidsUrlIdEncoderImpl() {
		hashids = new Hashids(salt, minHashLength, alphabet);
	}

	@Override
	public String encodeId(long id){
		return hashids.encode(id);
	}
	
	@Override
	public long decodeId(String encodedId){
		long decodedId = 0;
		long[] ids = null;
		
		if (encodedId != null) {
			ids = hashids.decode(encodedId);
		}
		
		if (ids != null && ids.length == 1) {
			decodedId = ids[0];
		}
		
		return decodedId;
	}
}