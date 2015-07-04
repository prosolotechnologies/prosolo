package org.prosolo.services.rest.clients;

import org.prosolo.bigdata.common.rest.exceptions.ConnectException;
import org.prosolo.bigdata.common.rest.pojo.RequestObject;

/**
@author Zoran Jeremic Apr 20, 2015
 *
 */

public interface RecommendationServicesRest {

	String sendPostRequest(String serviceLocalPath, RequestObject paramObject) throws ConnectException;

}

