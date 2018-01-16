package org.prosolo.services.nodes.factory;

import org.prosolo.common.domainmodel.rubric.*;
import org.prosolo.common.domainmodel.rubric.visitor.CriterionVisitor;
import org.prosolo.common.domainmodel.rubric.visitor.LevelVisitor;
import org.prosolo.common.domainmodel.user.User;
import org.prosolo.services.nodes.data.rubrics.*;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
		rd.setRubricType(rubric.getRubricType());
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
				RubricLevelData level = getRubricLevelData(lvl);
				if (trackChanges) {
					level.startObservingChanges();
				}
				rd.addLevel(level);
			}
			rd.sortLevels();
		}

		if (criteria != null) {
			for (Criterion cat : criteria) {
				RubricCriterionData c = getRubricCriterionData(cat);
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

	private RubricCriterionData getRubricCriterionData(Criterion criterion) {
		return criterion.accept(new CriterionVisitor<RubricCriterionData>() {
			@Override
			public RubricCriterionData visit(Criterion criterion) {
				return new RubricCriterionData(criterion.getId(), criterion.getTitle(), criterion.getOrder());
			}

			@Override
			public RubricCriterionData visit(PointCriterion criterion) {
				return new RubricCriterionData(criterion.getId(), criterion.getTitle(), criterion.getOrder(), criterion.getPoints());
			}
		});
	}

	private RubricLevelData getRubricLevelData(Level level) {
		return level.accept(new LevelVisitor<RubricLevelData>() {
			@Override
			public RubricLevelData visit(Level level) {
				return new RubricLevelData(level.getId(), level.getTitle(), level.getOrder());
			}

			@Override
			public RubricLevelData visit(PointLevel level) {
				return new RubricLevelData(level.getId(), level.getTitle(), level.getOrder(), level.getPoints());
			}

			@Override
			public RubricLevelData visit(PointRangeLevel level) {
				//TODO implement when we introduce point range level on user interface
				return null;
			}
		});
	}

	private void addLevelsWithDescriptionToCriterion(RubricData rubric, RubricCriterionData criterion, Set<CriterionLevel> criterionLevelDescriptions) {
		Map<RubricLevelData, String> descriptions = rubric.getLevels()
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
		ActivityRubricCriterionData criterionData = crit.accept(new CriterionVisitor<ActivityRubricCriterionData>() {
			@Override
			public ActivityRubricCriterionData visit(Criterion criterion) {
				//descriptive criterion
				ActivityRubricCriterionData criterionData = new ActivityRubricCriterionData();
				setItemData(criterionData, criterion.getId(), criterion.getTitle(), criterion.getOrder());
				setCriterionAssessmentInfo(assessment, criterionData);
				return criterionData;
			}

			@Override
			public ActivityRubricCriterionData visit(PointCriterion criterion) {
				//point based criterion
				PointActivityRubricCriterionData criterionData = new PointActivityRubricCriterionData();
				setItemData(criterionData, criterion.getId(), criterion.getTitle(), criterion.getOrder());
				criterionData.setWeight(criterion.getPoints());
				setCriterionAssessmentInfo(assessment, criterionData);
				return criterionData;
			}
		});

 		for (CriterionLevel cl : levels) {
 			ActivityRubricLevelData lvl = cl.getLevel().accept(new LevelVisitor<ActivityRubricLevelData>() {

				@Override
				public ActivityRubricLevelData visit(Level level) {
					//descriptive level
					ActivityRubricLevelData lvl = new ActivityRubricLevelData();
					setItemData(lvl, level.getId(), level.getTitle(), level.getOrder());
					lvl.setDescription(cl.getDescription());
					return lvl;
				}

				@Override
				public ActivityRubricLevelData visit(PointLevel level) {
					//point based level
					PointActivityRubricLevelData lvl = new PointActivityRubricLevelData();
					setItemData(lvl, level.getId(), level.getTitle(), level.getOrder());
					lvl.setWeight(level.getPoints());
					lvl.setDescription(cl.getDescription());
					return lvl;
				}

				@Override
				public ActivityRubricLevelData visit(PointRangeLevel level) {
					//TODO implement when needed
					return null;
				}
			});

			criterionData.addLevel(lvl);
		}

		return criterionData;
	}

	private void setCriterionAssessmentInfo(CriterionAssessment assessment, ActivityRubricCriterionData criterion) {
		if (assessment != null) {
			criterion.setComment(assessment.getComment());
			criterion.setLevelId(assessment.getLevel().getId());
		}
	}

	private <T extends ActivityRubricItemData> void setItemData(T item, long id, String title, int order) {
		item.setId(id);
		item.setName(title);
		item.setOrder(order);
	}

	public RubricGradeData getRubricGradeData(RubricType rubricType, List<ActivityRubricCriterionData> criteria, int maxPoints) {
		RubricGradeData rubric;
		switch (rubricType) {
			case DESCRIPTIVE:
				return new DescriptiveRubricGradeData(criteria);
			case POINT:
				List<PointActivityRubricCriterionData> pointCriteria = new ArrayList<>();
				criteria.forEach(c -> pointCriteria.add((PointActivityRubricCriterionData) c));
				return new PointRubricGradeData(pointCriteria, maxPoints);
			default:
				//TODO implement point range case when needed
				rubric = null;
		}

		return rubric;
	}

	//get rubric entities based on rubric data

	public Level getLevel(RubricType rubricType, Rubric rubric, RubricLevelData rubricLevelData) {
		if (rubricLevelData == null) {
			return null;
		}
		return createLevelBasedOnType(rubricType, rubric, rubricLevelData);
	}

	private Level createLevelBasedOnType(RubricType rubricType, Rubric rubric, RubricLevelData rubricLevelData) {
		switch (rubricType) {
			case DESCRIPTIVE:
				Level descLevel = new Level();
				populateCommonLevelData(descLevel, rubricLevelData, rubric);
				return descLevel;
			case POINT:
				PointLevel pl = new PointLevel();
				populateCommonLevelData(pl, rubricLevelData, rubric);
				pl.setPoints(rubricLevelData.getPoints());
				return pl;
			default:
				//TODO implement POINT RANGE case when needed
				return null;
		}
	}

	private void populateCommonLevelData(Level lvl, RubricLevelData lvlData, Rubric rubric) {
		lvl.setTitle(lvlData.getName());
		lvl.setOrder(lvlData.getOrder());
		lvl.setRubric(rubric);
	}

	public Criterion getCriterion(RubricType rubricType, Rubric rubric, RubricCriterionData rubricCriterionData) {
		if (rubricCriterionData == null) {
			return null;
		}
		return createCriterionBasedOnType(rubricType, rubric, rubricCriterionData);
	}

	private Criterion createCriterionBasedOnType(RubricType rubricType, Rubric rubric, RubricCriterionData rubricCriterionData) {
		switch (rubricType) {
			case DESCRIPTIVE:
				Criterion descCriterion = new Criterion();
				populateCommonCriterionData(descCriterion, rubricCriterionData, rubric);
				return descCriterion;
			case POINT:
			case POINT_RANGE:
				PointCriterion pc  = new PointCriterion();
				populateCommonCriterionData(pc, rubricCriterionData, rubric);
				pc.setPoints(rubricCriterionData.getPoints());
				return pc;
		}
		return null;
	}

	private void populateCommonCriterionData(Criterion criterion, RubricCriterionData criterionData, Rubric rubric) {
		criterion.setTitle(criterionData.getName());
		criterion.setOrder(criterionData.getOrder());
		criterion.setRubric(rubric);
	}

	public Class<? extends Criterion> getCriterionClassForRubricType(RubricType type) {
		switch(type) {
			case DESCRIPTIVE:
				return Criterion.class;
			case POINT:
			case POINT_RANGE:
				return PointCriterion.class;
		}
		return Criterion.class;
	}

	public Class<? extends Level> getLevelClassForRubricType(RubricType type) {
		switch(type) {
			case DESCRIPTIVE:
				return Level.class;
			case POINT:
				return PointLevel.class;
			case POINT_RANGE:
				return PointRangeLevel.class;
		}
		return Level.class;
	}

}
