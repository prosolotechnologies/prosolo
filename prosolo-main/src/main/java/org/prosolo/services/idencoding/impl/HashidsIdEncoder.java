package org.prosolo.services.idencoding.impl;

import org.apache.log4j.Logger;
import org.hashids.Hashids;
import org.prosolo.common.config.CommonSettings;
import org.prosolo.services.idencoding.IdEncoder;
import org.springframework.stereotype.Service;

@Service("org.prosolo.services.idencoding.IdEncoder")
public class HashidsIdEncoder implements IdEncoder {

	private static Logger logger = Logger.getLogger(HashidsIdEncoder.class);
	
	private String salt = CommonSettings.getInstance().config.appConfig.urlEncoding.salt;
	private static final String alphabet = "1234567890cfhistuCFHISTU";
	private static final int minHashLength = 3;
	
	private Hashids hashids;
	
	public HashidsIdEncoder() {
		hashids = new Hashids(salt, minHashLength, alphabet);
	}

	@Override
	public String encodeId(long id) {
		return hashids.encode(id);
	}
	
	public static void main(String[] args) {
		long id = 229377;
		System.out.println(new HashidsIdEncoder().encodeId(id));
	}
}
