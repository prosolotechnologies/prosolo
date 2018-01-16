package org.prosolo.services.nodes.data.assessments;

/**
 * @author stefanvuckovic
 * @date 2018-01-11
 * @since 1.2.0
 */
public class CompletionAutoGradeData extends AutomaticGradeData {

    public CompletionAutoGradeData(int grade, boolean assessed) {
        super(grade, assessed);
    }

    @Override
    public GradingMode getGradingMode() {
        return GradingMode.AUTOMATIC_BY_COMPLETION;
    }
}
