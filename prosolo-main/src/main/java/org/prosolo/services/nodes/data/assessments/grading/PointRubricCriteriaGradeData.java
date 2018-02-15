package org.prosolo.services.nodes.data.assessments.grading;

import org.prosolo.common.domainmodel.rubric.RubricType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author stefanvuckovic
 * @date 2018-01-18
 * @since 1.2.0
 */
public class PointRubricCriteriaGradeData extends RubricCriteriaGradeData<PointRubricCriterionGradeData> {

    public PointRubricCriteriaGradeData(List<PointRubricCriterionGradeData> criteria, int maxPoints) {
        super(criteria);
        calculateCriteriaAndLevelPoints(maxPoints);
    }

    private void calculateCriteriaAndLevelPoints(int maxPoints) {
        calculateCriteriaPointsBasedOnWeights(maxPoints);
        calculateLevelPointsBasedOnWeights();
    }

    private void calculateCriteriaPointsBasedOnWeights(int maxPoints) {
        // class used for storing and sorting criteria by cut off decimal part of points when rounding
        class CriterionByPointsRemainder implements Comparable {
            double remainder;
            PointRubricCriterionGradeData criterion;

            CriterionByPointsRemainder(double remainder, PointRubricCriterionGradeData criterion) {
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
        for (PointRubricCriterionGradeData c : getCriteria()) {
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
                PointRubricCriterionGradeData c = criteriaByPointsRemainder.get(i).criterion;
                c.setPoints(c.getPoints() + 1);
            }
        }
    }

    private void calculateLevelPointsBasedOnWeights() {
        for (PointRubricCriterionGradeData crit : getCriteria()) {
            calculateLevelPointsBasedOnWeights(crit);
        }
    }

    private void calculateLevelPointsBasedOnWeights(PointRubricCriterionGradeData criterion) {
        for (PointRubricLevelGradeData lvl : criterion.getLevels()) {
            lvl.setPoints((int) Math.round(lvl.getWeight() * criterion.getPoints() / 100));
        }
    }

    @Override
    public RubricType getRubricType() {
        return RubricType.POINT;
    }
}
