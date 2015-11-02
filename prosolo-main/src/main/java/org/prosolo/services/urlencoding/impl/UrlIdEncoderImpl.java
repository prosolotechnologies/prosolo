package org.prosolo.services.urlencoding.impl;

import org.hashids.Hashids;
import org.prosolo.app.Settings;
import org.prosolo.core.spring.ServiceLocator;
import org.prosolo.services.urlencoding.UrlIdEncoder;
import org.springframework.stereotype.Service;

@Service("org.prosolo.services.urlencoding.UrlIdEncoder")
public class UrlIdEncoderImpl implements UrlIdEncoder {

	/* (non-Javadoc)
	 * @see org.prosolo.services.urlencoding.UrlIdEncoder#encodeId(long)
	 */
	@Override
	public String encodeId(long id){
		String salt = Settings.getInstance().config.application.urlEncoding.salt;
		Hashids hashids = new Hashids(salt);
		return hashids.encode(id);
	}
	
	/* (non-Javadoc)
	 * @see org.prosolo.services.urlencoding.UrlIdEncoder#decodeId(java.lang.String)
	 */
	@Override
	public long decodeId(String encodedId){
		String salt = Settings.getInstance().config.application.urlEncoding.salt;
		Hashids hashids = new Hashids(salt);
		long decodedId = 0;
		long [] ids = null;
		if(encodedId != null){
			ids = hashids.decode(encodedId);
		}
		if(ids != null && ids.length == 1){
			decodedId = ids[0];
		}
		
		return decodedId;
	}
}
