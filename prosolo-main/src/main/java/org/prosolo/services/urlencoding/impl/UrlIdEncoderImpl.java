package org.prosolo.services.urlencoding.impl;

import org.hashids.Hashids;
import org.prosolo.services.urlencoding.UrlIdEncoder;
import org.springframework.stereotype.Service;

@Service("org.prosolo.services.urlencoding.UrlIdEncoder")
public class UrlIdEncoderImpl implements UrlIdEncoder {

	/* (non-Javadoc)
	 * @see org.prosolo.services.urlencoding.UrlIdEncoder#encodeId(long)
	 */
	@Override
	public String encodeId(long id){
		Hashids hashids = new Hashids("prosolo");
		return hashids.encode(id);
	}
	
	/* (non-Javadoc)
	 * @see org.prosolo.services.urlencoding.UrlIdEncoder#decodeId(java.lang.String)
	 */
	@Override
	public long decodeId(String encodedId){
		Hashids hashids = new Hashids("prosolo");
		long[] ids = hashids.decode(encodedId);
		if(ids.length == 1){
			return ids[0];
		}else{
			return 0;
		}
	}
}
