package org.prosolo.services.indexing;/**
 * Created by zoran on 28/09/16.
 */

import org.json.simple.JSONObject;

/**
 * zoran 28/09/16
 */
public interface LoggingESService {
    void storeEventObservedLog(JSONObject logObject);
}
