package org.prosolo.services.nodes.factory;

import org.prosolo.common.domainmodel.rubric.Criterion;
import org.prosolo.common.domainmodel.rubric.CriterionLevel;
import org.prosolo.common.domainmodel.rubric.Level;
import org.prosolo.common.domainmodel.rubric.Rubric;
import org.prosolo.common.domainmodel.user.User;
import org.prosolo.services.nodes.data.rubrics.RubricCriterionData;
import org.prosolo.services.nodes.data.rubrics.RubricData;
import org.prosolo.services.nodes.data.rubrics.RubricItemData;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class RubricDataFactory {

	public RubricData getRubricData(Rubric rubric, User creator, Set<Criterion> criteria, Set<Level> levels,
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
}
