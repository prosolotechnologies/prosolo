package org.prosolo.bigdata.config;

import org.simpleframework.xml.ElementMap;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by zoran on 24/04/16.
 */
public class JobProperties {
    @ElementMap(entry = "property", key = "name", attribute = true, inline = true)
    public Map<String, String> properties=new HashMap<String,String>();

    public String getProperty(String key){
        return properties.get(key);
    }
}
