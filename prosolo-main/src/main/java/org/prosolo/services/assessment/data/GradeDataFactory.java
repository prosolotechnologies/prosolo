package org.prosolo.services.assessment.data;

import org.prosolo.common.domainmodel.credential.ActivityRubricVisibility;
import org.prosolo.common.domainmodel.credential.GradingMode;
import org.prosolo.common.domainmodel.rubric.*;
import org.prosolo.common.domainmodel.rubric.visitor.CriterionVisitor;
import org.prosolo.common.domainmodel.rubric.visitor.LevelVisitor;
import org.prosolo.common.util.Pair;
import org.prosolo.services.assessment.data.grading.*;

import java.util.ArrayList;
import java.util.List;

/**
 * @author stefanvuckovic
 * @date 2018-01-26
 * @since 1.2.0
 */
public class GradeDataFactory {

    enum LearningResourceType {
        ACTIVITY, OTHER
    }

    public static GradeData getGradeDataForActivity(GradingMode gradingMode,
                                         int maxPoints,
                                         int currentGrade,
                                         long rubricId,
                                         RubricType rubricType,
                                         ActivityRubricVisibility rubricVisibilityForStudent,
                                         boolean acceptGrades,
                                         AssessmentGradeSummary rubricGradeSummary) {
        GradeData gd = getGradeDataObject(LearningResourceType.ACTIVITY, gradingMode, rubricId, rubricType, acceptGrades);
        return getGradeData(LearningResourceType.ACTIVITY, gd, maxPoints, currentGrade, rubricVisibilityForStudent, rubricGradeSummary);
    }

    public static GradeData getGradeDataForLearningResource(
            GradingMode gradingMode,
            int maxPoints,
            int currentGrade,
            long rubricId,
            RubricType rubricType,
            RubricAssessmentGradeSummary rubricGradeSummary) {
        GradeData gd = getGradeDataObject(LearningResourceType.OTHER, gradingMode, rubricId, rubricType, false);
        return getGradeData(LearningResourceType.OTHER, gd, maxPoints, currentGrade, null, rubricGradeSummary);
    }

    private static GradeData getGradeDataObject(LearningResourceType resourceType, org.prosolo.common.domainmodel.credential.GradingMode gradingMode, long rubricId, RubricType rubricType, boolean acceptGrades) {
        switch (gradingMode) {
            case NONGRADED:
                return new NongradedGradeData();
            case AUTOMATIC:
                if (resourceType == LearningResourceType.ACTIVITY) {
                    if (acceptGrades) {
                        return new ExternalToolAutoGradeData();
                    }
                    return new CompletionAutoGradeData();
                }
                return new AutomaticGradeData();
            case MANUAL:
                if (rubricId > 0) {
                    switch (rubricType) {
                        case DESCRIPTIVE:
                            return new DescriptiveRubricGradeData();
                        case POINT:
                            return new PointRubricGradeData();
                        case POINT_RANGE:
                            //TODO implement when needed
                            return null;
                    }
                }
                return new ManualSimpleGradeData();
            default:
                return null;
        }
    }

    private static GradeData getGradeData(LearningResourceType resourceType, GradeData gd, int maxPoints, int currentGrade, ActivityRubricVisibility rubricVisibilityForStudent, AssessmentGradeSummary rubricGradeSummary) {
        gd.accept(new GradeDataVisitor<Void>() {
            @Override
            public Void visit(ManualSimpleGradeData gradeData) {
                gradeData.setGradeInfo(maxPoints, currentGrade);
                gradeData.setNewGrade(currentGrade > 0 ? currentGrade : 0);
                gradeData.calculateAssessmentStarData();
                return null;
            }

            @Override
            public Void visit(AutomaticGradeData gradeData) {
                gradeData.setGradeInfo(maxPoints, currentGrade);
                gradeData.calculateAssessmentStarData();
                return null;
            }

            @Override
            public Void visit(ExternalToolAutoGradeData gradeData) {
                return null;
            }

            @Override
            public Void visit(CompletionAutoGradeData gradeData) {
                return null;
            }

            @Override
            public Void visit(NongradedGradeData gradeData) {
                return null;
            }

            @Override
            public Void visit(RubricGradeData gradeData) {
                if (resourceType == LearningResourceType.ACTIVITY) {
                    gradeData.setRubricVisibilityForStudent(rubricVisibilityForStudent);
                }
                gradeData.setAssessmentStarData(rubricGradeSummary);
                return null;
            }

            @Override
            public Void visit(DescriptiveRubricGradeData gradeData) {
                gradeData.setCurrentGrade(currentGrade);
                return null;
            }

            @Override
            public Void visit(PointRubricGradeData gradeData) {
                gradeData.setGradeInfo(maxPoints, currentGrade);
                return null;
            }
        });
        return gd;
    }

    public static void updateAssessmentStarData(GradeData gd, AssessmentGradeSummary rubricGradeSummary) {
        gd.accept(new GradeDataVisitor<Void>() {
            @Override
            public Void visit(ManualSimpleGradeData gradeData) {
                gradeData.calculateAssessmentStarData();
                return null;
            }

            @Override
            public Void visit(AutomaticGradeData gradeData) {
                gradeData.calculateAssessmentStarData();
                return null;
            }

            @Override
            public Void visit(ExternalToolAutoGradeData gradeData) {
                return null;
            }

            @Override
            public Void visit(CompletionAutoGradeData gradeData) {
                return null;
            }

            @Override
            public Void visit(NongradedGradeData gradeData) {
                return null;
            }

            @Override
            public Void visit(RubricGradeData gradeData) {
                gradeData.setAssessmentStarData(rubricGradeSummary);
                return null;
            }

            @Override
            public Void visit(DescriptiveRubricGradeData gradeData) {
                return null;
            }

            @Override
            public Void visit(PointRubricGradeData gradeData) {
                return null;
            }
        });
    }

    public static RubricCriterionGradeData getRubricCriterionGradeData(Criterion crit, CriterionAssessment assessment, List<CriterionLevel> levels) {
        RubricCriterionGradeData criterionData = crit.accept(new CriterionVisitor<RubricCriterionGradeData>() {
            @Override
            public RubricCriterionGradeData visit(Criterion criterion) {
                //descriptive criterion
                RubricCriterionGradeData criterionData = new RubricCriterionGradeData();
                setItemData(criterionData, criterion.getId(), criterion.getTitle(), criterion.getOrder());
                setCriterionAssessmentInfo(assessment, criterionData);
                return criterionData;
            }

            @Override
            public RubricCriterionGradeData visit(PointCriterion criterion) {
                //point based criterion
                PointRubricCriterionGradeData criterionData = new PointRubricCriterionGradeData();
                setItemData(criterionData, criterion.getId(), criterion.getTitle(), criterion.getOrder());
                criterionData.setWeight(criterion.getPoints());
                setCriterionAssessmentInfo(assessment, criterionData);
                return criterionData;
            }
        });

        for (CriterionLevel cl : levels) {
            RubricLevelGradeData lvl = cl.getLevel().accept(new LevelVisitor<RubricLevelGradeData>() {

                @Override
                public RubricLevelGradeData visit(Level level) {
                    //descriptive level
                    RubricLevelGradeData lvl = new RubricLevelGradeData();
                    setItemData(lvl, level.getId(), level.getTitle(), level.getOrder());
                    lvl.setDescription(cl.getDescription());
                    return lvl;
                }

                @Override
                public RubricLevelGradeData visit(PointLevel level) {
                    //point based level
                    PointRubricLevelGradeData lvl = new PointRubricLevelGradeData();
                    setItemData(lvl, level.getId(), level.getTitle(), level.getOrder());
                    lvl.setWeight(level.getPoints());
                    lvl.setDescription(cl.getDescription());
                    return lvl;
                }

                @Override
                public RubricLevelGradeData visit(PointRangeLevel level) {
                    //TODO implement when needed
                    return null;
                }
            });

            criterionData.addLevel(lvl);
        }

        return criterionData;
    }

    private static void setCriterionAssessmentInfo(CriterionAssessment assessment, RubricCriterionGradeData criterion) {
        if (assessment != null) {
            criterion.setComment(assessment.getComment());
            criterion.setLevelId(assessment.getLevel().getId());
        }
    }

    private static <T extends RubricItemGradeData> void setItemData(T item, long id, String title, int order) {
        item.setId(id);
        item.setName(title);
        item.setOrder(order);
    }

    public static RubricCriteriaGradeData getRubricCriteriaGradeData(RubricType rubricType, List<RubricCriterionGradeData> criteria, int maxPoints) {
        switch (rubricType) {
            case DESCRIPTIVE:
                return new DescriptiveRubricCriteriaGradeData(criteria);
            case POINT:
                List<PointRubricCriterionGradeData> pointCriteria = new ArrayList<>();
                criteria.forEach(c -> pointCriteria.add((PointRubricCriterionGradeData) c));
                return new PointRubricCriteriaGradeData(pointCriteria, maxPoints);
            default:
                //TODO implement point range case when needed
                return null;
        }
    }
}
