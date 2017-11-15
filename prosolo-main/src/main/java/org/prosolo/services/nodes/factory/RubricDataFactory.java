package org.prosolo.services.nodes.factory;

import org.prosolo.common.domainmodel.rubric.*;
import org.prosolo.common.domainmodel.user.User;
import org.prosolo.services.nodes.data.rubrics.ActivityRubricCriterionData;
import org.prosolo.services.nodes.data.rubrics.ActivityRubricItemData;
import org.prosolo.services.nodes.data.rubrics.ActivityRubricLevelData;
import org.prosolo.services.nodes.data.rubrics.RubricCriterionData;
import org.prosolo.services.nodes.data.rubrics.RubricData;
import org.prosolo.services.nodes.data.rubrics.RubricItemData;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Component
public class RubricDataFactory {

	public RubricData getRubricData(Rubric rubric, User creator, Set<Criterion> criteria, Set<Level> levels,
									boolean trackChanges, boolean rubricUsed) {
		if (rubric == null) {
			return null;
		}
		RubricData rd = new RubricData();
		rd.setId(rubric.getId());
		rd.setName(rubric.getTitle());
		rd.setOrganizationId(rubric.getOrganization().getId());
		rd.setReadyToUse(rubric.isReadyToUse());
		rd.setRubricUsed(rubricUsed);

		if (creator != null) {
			rd.setCreatorFullName(creator.getFullName());
		}
		rd.setCreatorId(rubric.getCreator().getId());

		if (levels != null) {
			for (Level lvl : levels) {
				RubricItemData level = new RubricItemData(lvl.getId(), lvl.getTitle(), lvl.getPoints(), lvl.getOrder());
				if (trackChanges) {
					level.startObservingChanges();
				}
				rd.addLevel(level);
			}
			rd.sortLevels();
		}

		if (criteria != null) {
			for (Criterion cat : criteria) {
				RubricCriterionData c = new RubricCriterionData(cat.getId(), cat.getTitle(), cat.getPoints(), cat.getOrder());
				if (trackChanges) {
					c.startObservingChanges();
				}
				addLevelsWithDescriptionToCriterion(rd, c, cat.getLevels());
				rd.addCriterion(c);
			}
			rd.sortCriteria();
		}
		
		if (trackChanges) {
			rd.startObservingChanges();
		}
		return rd;
	}

	private void addLevelsWithDescriptionToCriterion(RubricData rubric, RubricCriterionData criterion, Set<CriterionLevel> criterionLevelDescriptions) {
		Map<RubricItemData, String> descriptions = rubric.getLevels()
				.stream()
				.collect(Collectors.toMap(
						l -> l,
						l -> criterionLevelDescriptions
								.stream()
								.filter(cl -> cl.getLevel().getId() == l.getId()).findFirst()
								.get()
								.getDescription()));
		rubric.syncCriterionWithExistingDescriptions(criterion, descriptions);
	}

	public ActivityRubricCriterionData getActivityRubricCriterionData(Criterion crit, CriterionAssessment assessment, List<CriterionLevel> levels) {
		ActivityRubricCriterionData criterion = new ActivityRubricCriterionData();
		setItemData(criterion, crit.getId(), crit.getTitle(), crit.getOrder(), crit.getPoints());
		if (assessment != null) {
			criterion.setComment(assessment.getComment());
			criterion.setLevelId(assessment.getLevel().getId());
		}
 		for (CriterionLevel cl : levels) {
			ActivityRubricLevelData lvl = new ActivityRubricLevelData();
			setItemData(lvl, cl.getLevel().getId(), cl.getLevel().getTitle(), cl.getLevel().getOrder(), cl.getLevel().getPoints());
			lvl.setDescription(cl.getDescription());
			criterion.addLevel(lvl);
		}

		return criterion;
	}

	private <T extends ActivityRubricItemData> void setItemData(T item, long id, String title, int order, double weight) {
		item.setId(id);
		item.setName(title);
		item.setOrder(order);
		item.setWeight(weight);
	}

	public void calculatePointsForCriteriaAndLevels(List<ActivityRubricCriterionData> criteria, int maxPoints) {
		calculateCriteriaPointsBasedOnWeights(criteria, maxPoints);
		for (ActivityRubricCriterionData crit : criteria) {
			calculateLevelPointsBasedOnWeights(crit);
		}
	}

	private void calculateCriteriaPointsBasedOnWeights(List<ActivityRubricCriterionData> criteria, int maxPoints) {
		// class used for storing and sorting criteria by cut off decimal part of points when rounding
		class CriterionByPointsRemainder implements Comparable {
			double remainder;
			ActivityRubricCriterionData criterion;

			CriterionByPointsRemainder(double remainder, ActivityRubricCriterionData criterion) {
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
		for (ActivityRubricCriterionData cat : criteria) {
			double pointsDouble = (maxPoints * cat.getWeight() / 100);
			int points = (int) pointsDouble;
			sumPoints += points;
			cat.setPoints(points);
			criteriaByPointsRemainder.add(new CriterionByPointsRemainder(pointsDouble - points, cat));
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
				ActivityRubricCriterionData cat = criteriaByPointsRemainder.get(i).criterion;
				cat.setPoints(cat.getPoints() + 1);
			}
		}
	}

	private void calculateLevelPointsBasedOnWeights(ActivityRubricCriterionData criterion) {
		for (ActivityRubricLevelData lvl : criterion.getLevels()) {
			lvl.setPoints((int) Math.round(lvl.getWeight() * criterion.getPoints() / 100));
		}
	}

}
