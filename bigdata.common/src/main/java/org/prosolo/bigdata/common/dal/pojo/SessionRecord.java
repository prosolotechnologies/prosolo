package org.prosolo.bigdata.common.dal.pojo;

/**
 * @author Nikola Maric
 *
 */
public class SessionRecord {
	
	private final long userId;
	private final long sessionStart;
	private final long sessionEnd;
	private final String endReason;
	
	public SessionRecord(long userId, long sessionStart, long sessionEnd, String endReason) {
		super();
		this.userId = userId;
		this.sessionStart = sessionStart;
		this.sessionEnd = sessionEnd;
		this.endReason = endReason;
	}

	public long getUserId() {
		return userId;
	}

	public long getSessionStart() {
		return sessionStart;
	}

	public long getSessionEnd() {
		return sessionEnd;
	}

	public String getEndReason() {
		return endReason;
	}


	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((endReason == null) ? 0 : endReason.hashCode());
		result = prime * result + (int) (sessionEnd ^ (sessionEnd >>> 32));
		result = prime * result + (int) (sessionStart ^ (sessionStart >>> 32));
		result = prime * result + (int) (userId ^ (userId >>> 32));
		return result;
	}


	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		SessionRecord other = (SessionRecord) obj;
		if (endReason == null) {
			if (other.endReason != null)
				return false;
		} else if (!endReason.equals(other.endReason))
			return false;
		if (sessionEnd != other.sessionEnd)
			return false;
		if (sessionStart != other.sessionStart)
			return false;
		if (userId != other.userId)
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "SessionRecord [userId=" + userId + ", sessionStart=" + sessionStart + ", sessionEnd=" + sessionEnd
				+ ", endReason=" + endReason + "]";
	}
	
}
