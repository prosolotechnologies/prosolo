package org.prosolo.services.logging;

import java.io.Serializable;
import java.net.InetAddress;
import java.net.UnknownHostException;

import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Service;

/**
 *
 * @author Zoran Jeremic, Aug 25, 2014
 *
 */
@Service("org.prosolo.services.logging.AccessResolver")
public class AccessResolverImpl implements AccessResolver, Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4707453367891780464L;

	@Override
	public String findRemoteIPAddress() {
		FacesContext context = FacesContext.getCurrentInstance();
		HttpServletRequest request = (HttpServletRequest) context.getExternalContext().getRequest();
		return findRemoteIPAddress(request);
	}

	@Override
	public String findServerIPAddress() {
		InetAddress ip = null;
		try {
			ip = InetAddress.getLocalHost();
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
		return ip.toString();
	}

	@Override
	public String findRemoteIPAddress(HttpServletRequest request) {
		String ipAddress = request.getHeader("X-FORWARDED-FOR");

		if (ipAddress == null) {
			ipAddress = request.getRemoteAddr();
		}
		return ipAddress;
	}

}
