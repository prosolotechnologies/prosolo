package org.prosolo.common.domainmodel.user.oauth;

/**
 * @author Zoran Jeremic 2013-08-04
 *
 */
//@Entity
public class UserOauthTokens {
	
	private long id;
	private String username;
	private OauthAccessToken token;
	
	//@Id
	//@Column(name = "id", unique = true, nullable = false, insertable = false, updatable = false)
	//@GeneratedValue(strategy = GenerationType.TABLE)
	//@Type(type = "long")
	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}
 
	public String getUsername() {
		return username;
	}
	
	public void setUsername(String username) {
		this.username = username;
	}
	
	public OauthAccessToken getToken() {
		return token;
	}
	public void setToken(OauthAccessToken token) {
		this.token = token;
	}
}
	