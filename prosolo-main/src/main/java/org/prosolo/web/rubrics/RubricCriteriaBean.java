package org.prosolo.web.rubrics;

import org.apache.log4j.Logger;
import org.prosolo.services.nodes.RubricManager;
import org.prosolo.services.nodes.data.*;
import org.prosolo.services.nodes.data.rubrics.RubricCriterionData;
import org.prosolo.services.nodes.data.rubrics.RubricData;
import org.prosolo.services.nodes.data.rubrics.RubricItemData;
import org.prosolo.services.urlencoding.UrlIdEncoder;
import org.prosolo.web.LoggedUserBean;
import org.prosolo.web.util.page.PageUtil;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.faces.bean.ManagedBean;
import javax.inject.Inject;
import java.io.Serializable;
import java.util.*;

@ManagedBean(name = "rubricCriteriaBean")
@Component("rubricCriteriaBean")
@Scope("view")
public class RubricCriteriaBean implements Serializable {

	private static final long serialVersionUID = 6479781240208092217L;

	private static Logger logger = Logger.getLogger(RubricCriteriaBean.class);

	@Inject private LoggedUserBean loggedUserBean;
	@Inject private RubricManager rubricManager;
	@Inject private UrlIdEncoder idEncoder;

	private String rubricId;
	private long decodedRubricId;

	private RubricData rubric;
	private List<RubricCriterionData> criteriaToRemove;
	private List<RubricItemData> levelsToRemove;

	public void init() {
		decodedRubricId = idEncoder.decodeId(rubricId);
		if (decodedRubricId > 0) {
			try {
				initData();
			} catch (Exception e) {
				logger.error(e);
				PageUtil.fireErrorMessage("Error loading the page");
			}
		} else {
			PageUtil.notFound();
		}
	}

	private void initData() {
		criteriaToRemove = new ArrayList<>();
		levelsToRemove = new ArrayList<>();
		rubric = rubricManager.getRubricData(decodedRubricId, true, true, 0,true);
		if (rubric == null) {
			PageUtil.notFound();
		} else {
			//edit mode
			if (isCurrentUserCreator()) {
				//if criteria and levels are not defined add one empty criterion and level
				if (rubric.getCriteria().isEmpty()) {
					addEmptyCriterion();
				}
				if (rubric.getLevels().isEmpty()) {
					addEmptyLevel();
				}
			}
		}
	}

	public boolean isLimitedEdit() {
		//TODO when rubric is connected to activity return to this
		return false;
	}

	public boolean isCurrentUserCreator() {
		return rubric.getCreatorId() == loggedUserBean.getUserId();
	}

	public void moveCriterionDown(int index) {
		moveItemDown(index, rubric.getCriteria());
	}

	public void moveCriterionUp(int index) {
		moveItemDown(index - 1, rubric.getCriteria());
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

	public void removeCriterion(int index) {
		removeItem(index, rubric.getCriteria(), criteriaToRemove);
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

	public void addEmptyCriterion() {
		RubricCriterionData criterion = new RubricCriterionData(ObjectStatus.CREATED);
		criterion.setOrder(rubric.getCriteria().size() + 1);
		rubric.addNewCriterion(criterion);
	}

	public void addEmptyLevel() {
		RubricItemData level = new RubricItemData(ObjectStatus.CREATED);
		level.setOrder(rubric.getLevels().size() + 1);
		rubric.addNewLevel(level);
	}

	public <T extends RubricItemData> void addEmptyItem(T item, List<T> items) {
		item.setOrder(items.size() + 1);
		items.add(item);
	}

	public boolean isLastCriterion(int index) {
		return isLastItem(index, rubric.getCriteria());
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
		//add removed criteria and levels
		rubric.getCriteria().addAll(criteriaToRemove);
		rubric.getLevels().addAll(levelsToRemove);

		//save rubric data
		try {
			rubricManager.saveRubricCriteriaAndLevels(rubric);
			PageUtil.fireSuccessfulInfoMessage("Rubric saved");
			try {
				initData();
			} catch (Exception e) {
				logger.error("Error", e);
				PageUtil.fireErrorMessage("Error reloading the rubric data. Please refresh the page.");
			}
		} catch (Exception e) {
			logger.error("Error", e);
			//remove previously added removed criteria and levels to avoid errors if user tries to save again
			Iterator<RubricCriterionData> criteriaIt = rubric.getCriteria().iterator();
			while (criteriaIt.hasNext()) {
				RubricCriterionData cat = criteriaIt.next();
				if (cat.getStatus() == ObjectStatus.REMOVED) {
					criteriaIt.remove();
				}
			}

			Iterator<RubricItemData> levelIt = rubric.getLevels().iterator();
			while (levelIt.hasNext()) {
				RubricItemData lvl = levelIt.next();
				if (lvl.getStatus() == ObjectStatus.REMOVED) {
					levelIt.remove();
				}
			}
			PageUtil.fireErrorMessage("Error saving the rubric");
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
