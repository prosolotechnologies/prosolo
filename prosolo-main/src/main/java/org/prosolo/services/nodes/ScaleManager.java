package org.prosolo.services.nodes;

import org.prosolo.common.domainmodel.workflow.Scale;
import org.prosolo.services.general.AbstractManager;

public interface ScaleManager extends AbstractManager {

	public Scale getOrCreateScale(float minValue, float maxValue);

	public Scale createScale(float minValue, float maxValue);

	public Scale getScale0To1();
	
	public Scale getScale0To5();
	
	public Scale getScale0To100();

}