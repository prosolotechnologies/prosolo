package org.prosolo.services.nodes.data.rubrics;

import org.prosolo.common.domainmodel.rubric.RubricType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author stefanvuckovic
 * @date 2018-01-11
 * @since 1.2.0
 */
public class PointRubricGradeData extends RubricGradeData<PointActivityRubricCriterionData> {

    private int maxPoints;

    public PointRubricGradeData(List<PointActivityRubricCriterionData> criteria, int maxPoints) {
        super(criteria);
        this.maxPoints = maxPoints;
        calculateCriteriaAndLevelPoints();
    }

    private void calculateCriteriaAndLevelPoints() {
        calculateCriteriaPointsBasedOnWeights();
        calculateLevelPointsBasedOnWeights();
    }

    private void calculateCriteriaPointsBasedOnWeights() {
        // class used for storing and sorting criteria by cut off decimal part of points when rounding
        class CriterionByPointsRemainder implements Comparable {
            double remainder;
            PointActivityRubricCriterionData criterion;

            CriterionByPointsRemainder(double remainder, PointActivityRubricCriterionData criterion) {
                this.remainder = remainder;
                this.criterion = criterion;
            }

            @Override
            public int compareTo(Object o) {
                return Double.compare(((CriterionByPointsRemainder) o).remainder, this.remainder);
            }
        }

        List<CriterionByPointsRemainder> criteriaByPointsRemainder = new ArrayList<>();
        int sumPoints = 0;
        for (PointActivityRubricCriterionData c : getCriteria()) {
            double pointsDouble = (maxPoints * c.getWeight() / 100);
            int points = (int) pointsDouble;
            sumPoints += points;
            c.setPoints(points);
            criteriaByPointsRemainder.add(new CriterionByPointsRemainder(pointsDouble - points, c));
        }
		/*
		cover the case where sum of points for all items is not equal to maxPoints

		in that case additional point is added to first diff items sorted by remainder (decimal
		part that was cut off when rounding) in descending order.
		 */
        int diff = maxPoints - sumPoints;
        if (diff > 0) {
            Collections.sort(criteriaByPointsRemainder);
            for (int i = 0; i < diff; i++) {
                PointActivityRubricCriterionData c = criteriaByPointsRemainder.get(i).criterion;
                c.setPoints(c.getPoints() + 1);
            }
        }
    }

    private void calculateLevelPointsBasedOnWeights() {
        for (PointActivityRubricCriterionData crit : getCriteria()) {
            calculateLevelPointsBasedOnWeights(crit);
        }
    }

    private void calculateLevelPointsBasedOnWeights(PointActivityRubricCriterionData criterion) {
        for (PointActivityRubricLevelData lvl : criterion.getLevels()) {
            lvl.setPoints((int) Math.round(lvl.getWeight() * criterion.getPoints() / 100));
        }
    }

//    @Override
//    public int getGivenGrade() {
//        return isAssessed() ? calculateGradeByRubric() : -1;
//    }

    private int calculateGradeByRubric() {
        return getCriteria()
                .stream()
                .mapToInt(c -> c.getLevels().stream()
                        .filter(lvl -> lvl.getId() == c.getLevelId())
                        .findFirst().get().getPoints()).sum();
    }

    @Override
    public RubricType getRubricType() {
        return RubricType.POINT;
    }
}
