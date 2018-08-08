package org.prosolo.services.urlencoding.impl;

import org.apache.log4j.Logger;
import org.hashids.Hashids;
import org.prosolo.common.config.CommonSettings;
import org.prosolo.services.urlencoding.UrlIdEncoder;
import org.springframework.stereotype.Service;

@Service("org.prosolo.services.urlencoding.UrlIdEncoder")
public class HashidsUrlIdEncoderImpl implements UrlIdEncoder {

	private static Logger logger = Logger.getLogger(HashidsUrlIdEncoderImpl.class);
	
	private String salt = CommonSettings.getInstance().config.appConfig.urlEncoding.salt;
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
			/*
			 * there is a bug, sometimes npe is thrown
			 */
			try {
				ids = hashids.decode(encodedId);
			} catch(Exception e) {
				logger.error(e);
			}
		}
		
		if (ids != null && ids.length == 1) {
			decodedId = ids[0];
		}
		
		return decodedId;
	}
	
	public static void main(String[] args) {
		long id = 229377;
		System.out.println(new HashidsUrlIdEncoderImpl().encodeId(id));
		String encodedId = "w71W5Qmo";
		System.out.println(new HashidsUrlIdEncoderImpl().decodeId(encodedId));
	}
}
