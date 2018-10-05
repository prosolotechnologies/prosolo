package org.prosolo.config.app;

import org.simpleframework.xml.Element;

/**
 * @author Nikola Milikic
 * @date 2018-01-30
 * @since 1.2
 */
public class ManageSectionConfig {

    @Element(name = "max-rubric-levels")
    public int maxRubricLevels;
}
