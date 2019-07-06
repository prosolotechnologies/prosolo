package org.prosolo.config.init;

import org.simpleframework.xml.Element;

/**
 * @author stefanvuckovic
 * @date 2019-02-21
 * @since 1.3
 */
public class DatabaseMigration {

    @Element(name = "repair", required = false)
    public boolean repair;
}
