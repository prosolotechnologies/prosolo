package org.prosolo.config.app;

import org.simpleframework.xml.Element;

/**
 * @author stefanvuckovic
 * @date 2017-11-14
 * @since 1.2.0
 */
public class PluginConfig {

    @Element(name = "learning-in-stages")
    public LearningInStagesPlugin learningInStagesPlugin;
}
