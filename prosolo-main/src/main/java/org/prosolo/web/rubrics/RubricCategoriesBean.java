package org.prosolo.web.rubrics;

import org.apache.log4j.Logger;
import org.prosolo.services.nodes.RubricManager;
import org.prosolo.services.nodes.data.*;
import org.prosolo.services.urlencoding.UrlIdEncoder;
import org.prosolo.web.LoggedUserBean;
import org.prosolo.web.util.page.PageUtil;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.component.UIInput;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

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
	private List<RubricCategoryData> categories;
	private List<RubricItemData> levels;

	private UIInput inputCategoryPoints;

	public void init() {
		decodedRubricId = idEncoder.decodeId(rubricId);
		if (decodedRubricId > 0) {
			try {
				rubric = rubricManager.getRubricData(decodedRubricId, false, true, true);
				if (rubric == null) {
					PageUtil.notFound();
				} else {
					categories = new ArrayList<>(rubric.getCategories());
					levels = new ArrayList<>(rubric.getLevels());
				}
			} catch (Exception e) {
				logger.error(e);
				PageUtil.fireErrorMessage("Error loading the page");
			}
		} else {
			PageUtil.notFound();
		}
	}

	public void saveCategories() {
		/*
		equality check for doubles would not always work because of the way doubles are stored.
		Instead, error tolearance is used and it is 0.01 because user is allowed to enter two decimals
		so if he makes a mistake it will be by at least 0.01
		 */
		double tolerance = 0.01;
		double sum = categories
				.stream().filter(c -> c.getStatus() != ObjectStatus.REMOVED)
				.mapToDouble(c -> c.getPoints())
				.sum();
		if (Math.abs(sum - 100) > tolerance) {
			inputCategoryPoints.setValid(false);
			FacesContext.getCurrentInstance().addMessage(inputCategoryPoints.getClientId(),
					new FacesMessage("Sum of category points must always be 100"));
		} else {
			rubric.getCategories().clear();
			int order = 1;
			for (RubricCategoryData cat : categories) {
				if (cat.getStatus() != ObjectStatus.REMOVED) {
					cat.setOrder(order);
					order++;
					//sync category with levels if not already synced
					rubric.syncCategory(cat);
				}
				rubric.getCategories().add(cat);
			}
		}
	}

	public void saveLevels() {
		rubric.getLevels().clear();
		int order = 1;
		for (RubricItemData lvl : levels) {
			if (lvl.getStatus() != ObjectStatus.REMOVED) {
				lvl.setOrder(order);
				order++;
				//sync level with categories if not already synced
				rubric.syncLevel(lvl);
			}
			rubric.getLevels().add(lvl);
		}
	}

	public void moveCategoryDown(int index) {
		Collections.swap(categories, index, index + 1);
	}

	public void moveCategoryUp(int index) {
		Collections.swap(categories,index -1, index);
	}

	public void moveLevelDown(int index) {
		Collections.swap(levels, index, index + 1);
	}

	public void moveLevelUp(int index) {
		Collections.swap(levels,index -1, index);
	}

	public void removeCategory(RubricCategoryData category) {
		removeItem(category, categories);
	}

	public void removeLevel(RubricItemData level) {
		removeItem(level, levels);
	}

	private <T extends RubricItemData> void removeItem(T item, List<T> items) {
		item.setStatus(ObjectStatusTransitions.removeTransition(item.getStatus()));
		//if status is not ObjectStatus.REMOVED this item was not persisted so it can be removed from collection
		if(item.getStatus() != ObjectStatus.REMOVED) {
			items.remove(item);
		}
	}

	public void addEmptyCategory() {
		categories.add(new RubricCategoryData(ObjectStatus.CREATED));
	}

	public void addEmptyLevel() {
		levels.add(new RubricItemData(ObjectStatus.CREATED));
	}

	public boolean isFirstCategory(RubricCategoryData category) {
		return isFirstItem(category, categories);
	}

	public boolean isLastCategory(RubricCategoryData category) {
		return isLastItem(category, categories);
	}

	public boolean isFirstLevel(RubricItemData level) {
		return isFirstItem(level, levels);
	}

	public boolean isLastLevel(RubricItemData level) {
		return isLastItem(level, levels);
	}

	private <T extends RubricItemData> boolean isFirstItem(T item, List<T> items) {
		//find first not removed item
		Optional<T> it = items.stream().filter(i -> i.getStatus() != ObjectStatus.REMOVED).findFirst();
		if (it.isPresent()) {
			return it.get().equals(item);
		}
		return false;
	}

	private <T extends RubricItemData> boolean isLastItem(T item, List<T> items) {
		//find last not removed item
		for (int i = items.size() - 1; i >= 0; i--) {
			T it = items.get(i);
			if (it.getStatus() != ObjectStatus.REMOVED) {
				return it.equals(item);
			}
		}
		return false;
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

	public List<RubricCategoryData> getCategories() {
		return categories;
	}

	public List<RubricItemData> getLevels() {
		return levels;
	}

	public UIInput getInputCategoryPoints() {
		return inputCategoryPoints;
	}

	public void setInputCategoryPoints(UIInput inputCategoryPoints) {
		this.inputCategoryPoints = inputCategoryPoints;
	}
}
