package org.prosolo.bigdata.common.dal.pojo;

@Deprecated
public class SocialInteractionCount {
	
	private Long source;
	
	private Long target;
	
	private Long count;

	public SocialInteractionCount(Long source, Long target, Long count) {
		this.source = source;
		this.target = target;
		this.count = count;
	}

	public Long getSource() {
		return source;
	}

	public void setSource(Long source) {
		this.source = source;
	}

	public Long getTarget() {
		return target;
	}

	public void setTarget(Long target) {
		this.target = target;
	}

	public Long getCount() {
		return count;
	}

	public void setCount(Long count) {
		this.count = count;
	}
	
}
