package org.prosolo.services.nodes.factory;

import org.prosolo.common.domainmodel.rubric.*;
import org.prosolo.common.domainmodel.user.User;
import org.prosolo.services.nodes.data.*;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Component
public class RubricDataFactory {

	public RubricData getRubricData(Rubric rubric, User creator, Set<Category> categories, Set<Level> levels,
									boolean trackChanges) {
		if (rubric == null) {
			return null;
		}
		RubricData rd = new RubricData();
		rd.setId(rubric.getId());
		rd.setName(rubric.getTitle());
		rd.setOrganizationId(rubric.getOrganization().getId());
		rd.setReadyToUse(rubric.isReadyToUse());

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

		if (categories != null) {
			for (Category cat : categories) {
				RubricCategoryData c = new RubricCategoryData(cat.getId(), cat.getTitle(), cat.getPoints(), cat.getOrder());
				if (trackChanges) {
					c.startObservingChanges();
				}
				addLevelsWithDescriptionToCategory(rd, c, cat.getLevels());
				rd.addCategory(c);
			}
			rd.sortCategories();
		}
		
		if (trackChanges) {
			rd.startObservingChanges();
		}
		return rd;
	}

	private void addLevelsWithDescriptionToCategory(RubricData rubric, RubricCategoryData category, Set<CategoryLevel> categoryLevelDescriptions) {
		Map<RubricItemData, String> descriptions = rubric.getLevels()
				.stream()
				.collect(Collectors.toMap(
						l -> l,
						l -> categoryLevelDescriptions
								.stream()
								.filter(cl -> cl.getLevel().getId() == l.getId()).findFirst()
								.get()
								.getDescription()));
		rubric.syncCategoryWithExistingDescriptions(category, descriptions);
	}

	public ActivityRubricCategoryData getActivityRubricData(Category cat, CategoryAssessment assessment, List<CategoryLevel> levels) {
		ActivityRubricCategoryData category = new ActivityRubricCategoryData();
		setItemData(category, cat.getId(), cat.getTitle(), cat.getOrder(), cat.getPoints());
		if (assessment != null) {
			category.setCategoryAssessmentId(assessment.getId());
			category.setComment(assessment.getComment());
			category.setLevelId(assessment.getLevel().getId());
		}
 		for (CategoryLevel cl : levels) {
			ActivityRubricLevelData lvl = new ActivityRubricLevelData();
			setItemData(lvl, cl.getLevel().getId(), cl.getLevel().getTitle(), cl.getLevel().getOrder(), cl.getLevel().getPoints());
			lvl.setDescription(cl.getDescription());
			category.addLevel(lvl);
		}

		return category;
	}

	private <T extends ActivityRubricItemData> void setItemData(T item, long id, String title, int order, double weight) {
		item.setId(id);
		item.setName(title);
		item.setOrder(order);
		item.setWeight(weight);
	}

	public void calculatePointsForCategoriesAndLevels(List<ActivityRubricCategoryData> categories, int maxPoints) {
		calculateCategoryPointsBasedOnWeights(categories, maxPoints);
		for (ActivityRubricCategoryData cat : categories) {
			calculateLevelPointsBasedOnWeights(cat);
		}
	}

	private void calculateCategoryPointsBasedOnWeights(List<ActivityRubricCategoryData> categories, int maxPoints) {
		// class used for storing and sorting categories by cut off decimal part of points when rounding
		class CategoryByPointsRemainder implements Comparable {
			double remainder;
			ActivityRubricCategoryData category;

			CategoryByPointsRemainder(double remainder, ActivityRubricCategoryData category) {
				this.remainder = remainder;
				this.category = category;
			}

			@Override
			public int compareTo(Object o) {
				return Double.compare(((CategoryByPointsRemainder) o).remainder, this.remainder);
			}
		}

		List<CategoryByPointsRemainder> categoriesByPointsRemainder = new ArrayList<>();
		int sumPoints = 0;
		for (ActivityRubricCategoryData cat : categories) {
			double pointsDouble = (maxPoints * cat.getWeight() / 100);
			int points = (int) pointsDouble;
			sumPoints += points;
			cat.setPoints(points);
			categoriesByPointsRemainder.add(new CategoryByPointsRemainder(pointsDouble - points, cat));
		}
		/*
		cover the case where sum of points for all items is not equal to maxPoints

		in that case additional point is added to first diff items sorted by remainder (decimal
		part that was cut off when rounding) in descending order.
		 */
		int diff = maxPoints - sumPoints;
		if (diff > 0) {
			Collections.sort(categoriesByPointsRemainder);
			for (int i = 0; i < diff; i++) {
				ActivityRubricCategoryData cat = categoriesByPointsRemainder.get(i).category;
				cat.setPoints(cat.getPoints() + 1);
			}
		}
	}

	private void calculateLevelPointsBasedOnWeights(ActivityRubricCategoryData category) {
		for (ActivityRubricLevelData lvl : category.getLevels()) {
			lvl.setPoints((int) Math.round(lvl.getWeight() * category.getPoints() / 100));
		}
	}

}
