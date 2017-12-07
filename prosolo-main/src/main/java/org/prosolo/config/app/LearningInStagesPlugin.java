package org.prosolo.config.app;

import org.simpleframework.xml.Element;

/**
 * @author stefanvuckovic
 * @date 2017-11-16
 * @since 1.2.0
 */
public class LearningInStagesPlugin extends Plugin {

    @Element(name = "max-number-of-learning-stages")
    public int maxNumberOfLearningStages;
}
