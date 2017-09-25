package org.prosolo.services.nodes.factory;

import org.prosolo.common.domainmodel.rubric.Category;
import org.prosolo.common.domainmodel.rubric.CategoryLevel;
import org.prosolo.common.domainmodel.rubric.Level;
import org.prosolo.common.domainmodel.rubric.Rubric;
import org.prosolo.common.domainmodel.user.User;
import org.prosolo.services.nodes.data.RubricCategoryData;
import org.prosolo.services.nodes.data.RubricData;
import org.prosolo.services.nodes.data.RubricItemData;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Set;
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
}
