package org.prosolo.bigdata.config;/**
 * Created by zoran on 17/05/16.
 */

import org.simpleframework.xml.Element;

/**
 * zoran 17/05/16
 */
public class SparkConfig {

    @Element(name = "mode", required = true)
    public String mode;

    @Element(name = "master", required = true)
    public String master;

    @Element(name = "max-cores", required = true)
    public int maxCores;

    @Element(name = "executor-memory", required = true)
    public String executorMemory;

    @Element(name = "app-name", required = true)
    public String appName;

    @Element(name = "elasticsearch-connector-port", required = true)
    public int elasticsearchConnectorPort;

    @Element(name = "one-jar", required = false)
    public String oneJar;
}
