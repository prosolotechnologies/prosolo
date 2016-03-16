package org.prosolo.bigdata.services.urlencoding.impl;

import org.hashids.Hashids;
import org.prosolo.bigdata.services.urlencoding.UrlIdEncoder;
import org.prosolo.common.config.CommonSettings; 

public class HashidsUrlIdEncoderImpl implements UrlIdEncoder {

	public static class HashidsUrlIdEncoderImplHolder {
		public static final HashidsUrlIdEncoderImpl INSTANCE = new HashidsUrlIdEncoderImpl();
	}
	
	public static HashidsUrlIdEncoderImpl getInstance() {
		return HashidsUrlIdEncoderImplHolder.INSTANCE;
	}
	
	String salt = CommonSettings.getInstance().config.appConfig.urlEncoding.salt;
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
