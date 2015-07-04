/**
 * 
 */
package org.prosolo.domainmodel.workflow;

import javax.persistence.Column;
import javax.persistence.Entity;

import org.prosolo.domainmodel.general.BaseEntity;
import org.prosolo.domainmodel.workflow.Scale;

/**
 * @author Nikola Milikic
 *
 */

@Entity
//@Table(name="wf_Scale")
public class Scale extends BaseEntity {

	private static final long serialVersionUID = -1858409149712997317L;
	
	private float minValue;
	private float maxValue;
	
	/**
	 * @return the minValue
	 */
	@Column(name="min_value")
	public float getMinValue() {
		return minValue;
	}
	
	/**
	 * @param minValue the minValue to set
	 */
	public void setMinValue(float minValue) {
		this.minValue = minValue;
	}
	
	/**
	 * @return the maxValue
	 */
	@Column(name="max_value")
	public float getMaxValue() {
		return maxValue;
	}
	
	/**
	 * @param maxValue the maxValue to set
	 */
	public void setMaxValue(float maxValue) {
		if (0 != maxValue) {
			this.maxValue = maxValue;
		}  
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + Float.floatToIntBits(maxValue);
		result = prime * result + Float.floatToIntBits(minValue);
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		Scale other = (Scale) obj;
		if (Float.floatToIntBits(maxValue) != Float
				.floatToIntBits(other.maxValue))
			return false;
		if (Float.floatToIntBits(minValue) != Float
				.floatToIntBits(other.minValue))
			return false;
		return true;
	}
	
}
