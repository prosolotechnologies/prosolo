package org.prosolo.services.nodes.data.assessments;

import org.prosolo.services.nodes.data.ActivityRubricCategoryData;
import org.prosolo.services.nodes.data.assessments.GradingMode;

import java.io.Serializable;
import java.util.List;

public class GradeData implements Serializable {

	private static final long serialVersionUID = -5566090807276070094L;

	private long id;
	private int minGrade;
	private int maxGrade;
	private Integer value;
	//assessment mode
	private GradingMode gradingMode;
	private List<ActivityRubricCategoryData> rubricCategories;
	private boolean rubricInitialized;
	private boolean assessed;
	
	public int getMinGrade() {
		return minGrade;
	}

	public void setMinGrade(int minGrade) {
		this.minGrade = minGrade;
	}

	public int getMaxGrade() {
		return maxGrade;
	}

	public void setMaxGrade(int maxGrade) {
		this.maxGrade = maxGrade;
	}

	public Integer getValue() {
		return value;
	}

	public void setValue(Integer value) {
		this.value = value;
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public boolean isAssessed() {
		return assessed;
	}

	public void setAssessed(boolean assessed) {
		this.assessed = assessed;
	}

	public GradingMode getGradingMode() {
		return gradingMode;
	}

	public void setGradingMode(GradingMode gradingMode) {
		this.gradingMode = gradingMode;
	}

	public List<ActivityRubricCategoryData> getRubricCategories() {
		return rubricCategories;
	}

	public void setRubricCategories(List<ActivityRubricCategoryData> rubricCategories) {
		this.rubricCategories = rubricCategories;
	}

	public boolean isRubricInitialized() {
		return rubricInitialized;
	}

	public void setRubricInitialized(boolean rubricInitialized) {
		this.rubricInitialized = rubricInitialized;
	}
	
}
