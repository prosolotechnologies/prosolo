package org.prosolo.web.rubrics;

import org.apache.log4j.Logger;
import org.prosolo.bigdata.common.exceptions.OperationForbiddenException;
import org.prosolo.services.nodes.RubricManager;
import org.prosolo.services.nodes.data.*;
import org.prosolo.services.nodes.impl.util.EditMode;
import org.prosolo.services.urlencoding.UrlIdEncoder;
import org.prosolo.web.LoggedUserBean;
import org.prosolo.web.util.ResourceBundleUtil;
import org.prosolo.web.util.page.PageUtil;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.faces.bean.ManagedBean;
import javax.inject.Inject;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

@ManagedBean(name = "rubricCategoriesBean")
@Component("rubricCategoriesBean")
@Scope("view")
public class RubricCategoriesBean implements Serializable {

	private static final long serialVersionUID = 6479781240208092217L;

	private static Logger logger = Logger.getLogger(RubricCategoriesBean.class);

	@Inject private LoggedUserBean loggedUserBean;
	@Inject private RubricManager rubricManager;
	@Inject private UrlIdEncoder idEncoder;

	private String rubricId;
	private long decodedRubricId;

	private RubricData rubric;
	private List<RubricCategoryData> categoriesToRemove;
	private List<RubricItemData> levelsToRemove;

	private EditMode editMode = EditMode.LIMITED;

	public void init() {
		decodedRubricId = idEncoder.decodeId(rubricId);
		try {
			if (decodedRubricId > 0) {
				initData();
			} else {
				PageUtil.notFound();
			}
		} catch (Exception e) {
			logger.error(e);
			PageUtil.fireErrorMessage("Error loading the page");
			rubric = new RubricData();
		}
	}

	private void initData() {
		categoriesToRemove = new ArrayList<>();
		levelsToRemove = new ArrayList<>();
		rubric = rubricManager.getRubricData(decodedRubricId, false, true, loggedUserBean.getUserId(),true);
		if (rubric == null) {
			PageUtil.notFound();
		} else {
			boolean rubricUsedInActivity = rubricManager.isRubricUsed(decodedRubricId);
			editMode = rubricUsedInActivity ? EditMode.LIMITED : EditMode.FULL;
		}
	}

	public boolean isLimitedEdit() {
		return editMode == EditMode.LIMITED;
	}

	public void moveCategoryDown(int index) {
		moveItemDown(index, rubric.getCategories());
	}

	public void moveCategoryUp(int index) {
		moveItemDown(index - 1, rubric.getCategories());
	}

	public void moveLevelDown(int index) {
		moveItemDown(index, rubric.getLevels());
	}

	public void moveLevelUp(int index) {
		moveItemDown(index - 1, rubric.getLevels());
	}

	public <T extends RubricItemData> void moveItemDown(int i, List<T> items) {
		T it1 = items.get(i);
		it1.setOrder(it1.getOrder() + 1);
		T it2 = items.get(i + 1);
		it2.setOrder(it2.getOrder() - 1);
		Collections.swap(items, i, i + 1);
	}

	public void removeCategory(int index) {
		removeItem(index, rubric.getCategories(), categoriesToRemove);
	}

	public void removeLevel(int index) {
		removeItem(index, rubric.getLevels(), levelsToRemove);
	}

	private <T extends RubricItemData> void removeItem(int index, List<T> items, List<T> itemsToRemove) {
		T item = items.remove(index);
		item.setStatus(ObjectStatusTransitions.removeTransition(item.getStatus()));
		if (item.getStatus() == ObjectStatus.REMOVED) {
			itemsToRemove.add(item);
		}
		shiftOrderOfItemsUp(index, items);
	}

	private <T extends RubricItemData> void shiftOrderOfItemsUp(int index, List<T> items) {
		int size = items.size();
		for(int i = index; i < size; i++) {
			T item = items.get(i);
			item.setOrder(item.getOrder() - 1);
		}
	}

	public void addEmptyCategory() {
		RubricCategoryData category = new RubricCategoryData(ObjectStatus.CREATED);
		addEmptyItem(category, rubric.getCategories());
		rubric.syncCategory(category);
	}

	public void addEmptyLevel() {
		RubricItemData level = new RubricItemData(ObjectStatus.CREATED);
		addEmptyItem(level, rubric.getLevels());
		rubric.syncLevel(level);
	}

	public <T extends RubricItemData> void addEmptyItem(T item, List<T> items) {
		item.setOrder(items.size() + 1);
		items.add(item);
	}

	public boolean isLastCategory(int index) {
		return isLastItem(index, rubric.getCategories());
	}

	public boolean isLastLevel(int index) {
		return isLastItem(index, rubric.getLevels());
	}


	private <T extends RubricItemData> boolean isLastItem(int index, List<T> items) {
		return items.size() == index + 1;
	}

	/*
	ACTIONS
	 */

	public void saveRubric() {
		//add removed categories and levels
		rubric.getCategories().addAll(categoriesToRemove);
		rubric.getLevels().addAll(levelsToRemove);

		//save rubric data
		try {
			rubricManager.saveRubricCategoriesAndLevels(rubric, editMode);
			PageUtil.fireSuccessfulInfoMessage(ResourceBundleUtil.getLabel("rubric") + " has been saved");
			try {
				initData();
			} catch (Exception e) {
				logger.error("Error", e);
				PageUtil.fireErrorMessage("Error reloading the " + ResourceBundleUtil.getLabel("rubric").toLowerCase() + " data. Please refresh the page.");
			}
		} catch (OperationForbiddenException e) {
			logger.error("Error", e);
			PageUtil.fireErrorMessage("This operation is not allowed. Please refresh the page to review the most recent changes and try again.");
		} catch (Exception e) {
			logger.error("Error", e);
			//remove previously added removed categories and levels to avoid errors if user tries to save again
			Iterator<RubricCategoryData> categoryIt = rubric.getCategories().iterator();
			while (categoryIt.hasNext()) {
				RubricCategoryData cat = categoryIt.next();
				if (cat.getStatus() == ObjectStatus.REMOVED) {
					categoryIt.remove();
				}
			}

			Iterator<RubricItemData> levelIt = rubric.getLevels().iterator();
			while (levelIt.hasNext()) {
				RubricItemData lvl = levelIt.next();
				if (lvl.getStatus() == ObjectStatus.REMOVED) {
					levelIt.remove();
				}
			}
			PageUtil.fireErrorMessage("Error saving the " + ResourceBundleUtil.getLabel("rubric").toLowerCase());
		}
	}

	public String getRubricId() {
		return rubricId;
	}

	public void setRubricId(String rubricId) {
		this.rubricId = rubricId;
	}

	public RubricData getRubric() {
		return rubric;
	}


}
