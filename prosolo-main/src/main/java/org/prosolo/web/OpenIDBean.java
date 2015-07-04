package org.prosolo.web;

import java.io.IOException;
import java.io.Serializable;
import java.util.List;

import javax.faces.bean.ManagedBean;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.openid4java.OpenIDException;
import org.openid4java.consumer.ConsumerManager;
import org.openid4java.consumer.VerificationResult;
import org.openid4java.discovery.DiscoveryInformation;
import org.openid4java.discovery.Identifier;
import org.openid4java.message.AuthRequest;
import org.openid4java.message.AuthSuccess;
import org.openid4java.message.ParameterList;
import org.openid4java.message.ax.AxMessage;
import org.openid4java.message.ax.FetchRequest;
import org.openid4java.message.ax.FetchResponse;
import org.prosolo.domainmodel.user.OpenIDAccount;
import org.prosolo.domainmodel.user.OpenIDProvider;
import org.prosolo.domainmodel.user.User;
import org.prosolo.services.authentication.RegistrationManager;
import org.prosolo.services.nodes.UserManager;
import org.prosolo.web.unauthorized.SelfRegistrationBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
/**
 *
 * @author Zoran Jeremic, Aug 5, 2014
 *
 */
@ManagedBean(name="openid")
@Component("openid")
@Scope("session")
public class OpenIDBean implements Serializable{

	@Autowired private RegistrationManager registrationManager;
	@Autowired private UserManager userManager;
	@Autowired private SelfRegistrationBean selfRegistration;
	@Autowired private LoggedUserBean loggedUserBean;
	/**
	 * 
	 */
	private static final long serialVersionUID = 3821655231973768917L;
	private static Logger logger = Logger.getLogger(LoggedUserBean.class);
	 private final static String GOOGLE_ENDPOINT = "https://www.google.com/accounts/o8/id";
	 private final static String EDX_ENDPOINT ="https://courses.edx.org/openid/provider/login/";
	// private final static String AUTH_SIGNUP_SUCCEED = "/auth/afterSignup.xhtml";
	 //   private final static String AUTH_SIGNUP_FAILED = "/auth/loginError.xhtml";
	    private final static String AUTH_SUCCEED = "/openid.xhtml";
	  //  private final static String AUTH_FAILED = "/auth/loginError.xhtml";
	 
 
	   private String userSuppliedId; //Users OpenID URL
	    private String validatedId;
	    private String openIdEmail;
	   
		private String openIdFirstName;
	    private String openIdLastName;
	    private String openIdCountry;
	    private String openIdLanguage;
	    private OpenIDProvider openIDProvider;
	    private String version;
	    public OpenIDProvider getOpenIDProvider() {
			return openIDProvider;
		}
		public void setOpenIDProvider(OpenIDProvider openIDProvider) {
			this.openIDProvider = openIDProvider;
		}

		//private Boolean signup = Boolean.TRUE;
	    private ConsumerManager manager;
	    private DiscoveryInformation discovered;
	
	    public String getOnLoad() {
	    	System.out.println("get on load");
	        verify();
	        System.out.println("Verification finished...");
	        authenticateUser();
	        return "pageLoaded";
	    }
	    public void authenticateUser(){
	    	System.out.println("authenticate user:"+validatedId);
	   	OpenIDAccount openIDAccount=registrationManager.findOpenIDAccount(validatedId);
	     	if(openIDAccount==null){
	    		//signup new openid account
	    		boolean isEmailExists = registrationManager.isEmailAlreadyExists(openIdEmail);
	    		openIDAccount=new OpenIDAccount();
	    		openIDAccount.setValidatedId(validatedId);
	    		openIDAccount.setOpenIDProvider(openIDProvider);
	    		User user=null;
				if (isEmailExists) {
					//Connect openid with existing account
					 user=userManager.getUser(openIdEmail);
					}else{
					//Create new user
					logger.info("create new user :"+openIdFirstName+" : "+openIdLastName+" : "+openIdEmail);
					 user= selfRegistration.registerUserOpenId(openIdFirstName, openIdLastName, openIdEmail);
					 registrationManager.setEmailAsVerified(openIdEmail,true);
					}
				openIDAccount.setUser(user);
				userManager.saveEntity(openIDAccount);
		    		
	    	}else{
	    		//openid account already registered
	    	}
	    	loggedUserBean.loginOpenId(openIdEmail);
	      }
	    
	    private void login() throws IOException {
	        manager = new ConsumerManager();
	        validatedId = null;
	        String returnToUrl = returnToUrl("/openid.xhtml");
	        authRequest(returnToUrl);
//  	        if (url != null) {
//	            FacesContext.getCurrentInstance().getExternalContext().redirect(url);
//	        } 
	    }
	    public void signinOpenidGoogle(){
	    	this.signinOpenid(GOOGLE_ENDPOINT);
	    }
	    public void signinOpenidEdx(){
	    	this.signinOpenid(EDX_ENDPOINT);
	    }
	 
	    private void signinOpenid(String endpoint) {
	    	userSuppliedId =endpoint;
	    	try {
				login();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}

		/**
	* Create the current url and add another url path fragment on it.
	* Obtain from the current context the url and add another url path fragment at
	* the end
	* @param urlExtension f.e. /nextside.xhtml
	* @return the hole url including the new fragment
	*/
	    private String returnToUrl(String urlExtension) {
	        FacesContext context = FacesContext.getCurrentInstance();
	        HttpServletRequest request = (HttpServletRequest) context.getExternalContext().getRequest();
	        String portExt="";
	        if(request.getServerPort()!=80){
	        	portExt=":"+request.getServerPort();
	        }
	        String returnToUrl = "http://" + request.getServerName() + portExt
	                + context.getApplication().getViewHandler().getActionURL(context, urlExtension);
	        return returnToUrl;
	    }
	 
	    /**
	* Create an authentication request.
	* It performs a discovery on the user-supplied identifier. Attempt it to
	* associate with the OpenID provider and retrieve one service endpoint
	* for authentication. It adds some attributes for exchange on the AuthRequest.
	* A List of all possible attributes can be found on @see http://www.axschema.org/types/
	* @param returnToUrl
	* @return the URL where the message should be sent
	* @throws IOException
	*/
	    private void authRequest(String returnToUrl) throws IOException {
	        try {
	            List discoveries = manager.discover(userSuppliedId);
	            discovered = manager.associate(discoveries);
	            AuthRequest authReq = manager.authenticate(discovered, returnToUrl);
	 
	            FetchRequest fetch = FetchRequest.createFetchRequest();
	            version=discovered.getVersion();

	            if(userSuppliedId.contains("edx.org")){
	            	fetch.addAttribute("fullname", "http://axschema.org/namePerson", true);
	            	fetch.addAttribute("email", "http://axschema.org/contact/email", true);
	            }else if (userSuppliedId.contains("myopenid")){
		             fetch.addAttribute("email", "http://schema.openid.net/contact/email", true);
	             fetch.addAttribute("firstname", "http://schema.openid.net/firstname", true);
	             } else {
	            	 fetch.addAttribute("email", "http://axschema.org/contact/email", true);
	            	 fetch.addAttribute("fullname", "http://axschema.org/namePerson", true);
	            	 fetch.addAttribute("firstname", "http://axschema.org/namePerson/first", true);
	            	 fetch.addAttribute("lastname", "http://axschema.org/namePerson/last", true);
	            	 fetch.addAttribute("country", "http://axschema.org/contact/country/home", true);
	            	 fetch.addAttribute("language", "http://axschema.org/pref/language", true);
	            
	             /* ... */
	             }
	           authReq.addExtension(fetch);
	           String url= authReq.getDestinationUrl(true);
	            if (url != null) {
	            	if(!discovered.isVersion2()){
			            FacesContext.getCurrentInstance().getExternalContext().redirect(url);
	            	}else{
	            		ExternalContext context = FacesContext.getCurrentInstance().getExternalContext();
            			 context.redirect(url);
	            	    FacesContext.getCurrentInstance().responseComplete();
	            	}
		        } 
        } catch (OpenIDException e) {

	        	e.printStackTrace();
	        }
	         
	    }
	 
	    public void verify() {
	        ExternalContext context = javax.faces.context.FacesContext
	                .getCurrentInstance().getExternalContext();
	       HttpServletRequest request = (HttpServletRequest) context.getRequest();
	        validatedId = verifyResponse(request);
	    }
	 
	    /**
	* Set the class members with date from the authentication response.
	* Extract the parameters from the authentication response (which comes
	* in as a HTTP request from the OpenID provider). Verify the response,
	* examine the verification result and extract the verified identifier.
	* @param httpReq httpRequest
	* @return users identifier.
	*/
	    private String verifyResponse(HttpServletRequest httpReq) {
	        try {
	            ParameterList response =
	                    new ParameterList(httpReq.getParameterMap());
	 
	            StringBuffer receivingURL = httpReq.getRequestURL();
	            String queryString = httpReq.getQueryString();
	            if (queryString != null && queryString.length() >0) {
	            	String param=httpReq.getQueryString();
	            	receivingURL.append("?").append(param);
	            }
	            logger.info("ReceivingURL:"+receivingURL.toString());
	            logger.info("Response:"+response.toString());
	            logger.info("Discovered:"+discovered.toString());
	            VerificationResult verification = manager.verify(
	                    receivingURL.toString(),
	                    response, discovered);
	           
	            Identifier verified = verification.getVerifiedId();
	            if (verified != null) {
	                AuthSuccess authSuccess =
	                        (AuthSuccess) verification.getAuthResponse();
	                if(userSuppliedId.contains("edx.org")){
	                	 FetchResponse fetchResp = (FetchResponse) authSuccess.getExtension(AxMessage.OPENID_NS_AX);
	                	 String fullname=(String) fetchResp.getAttributeValues("ext0").get(0);
		                    String[] names=fullname.split(" ");
		                    if(names.length>0)
		                    openIdFirstName=names[0];
		                    if(names.length>1)
		                    openIdLastName=names[1];
		                    openIdEmail=(String) fetchResp.getAttributeValues("ext1").get(0);
	                }else if (authSuccess.hasExtension(AxMessage.OPENID_NS_AX)) {
	                    FetchResponse fetchResp = (FetchResponse) authSuccess.getExtension(AxMessage.OPENID_NS_AX);
	                    List emails = fetchResp.getAttributeValues("email");
	                    openIdEmail = (String) emails.get(0);
	                    List firstname = fetchResp.getAttributeValues("firstname");
	                    openIdFirstName = (String) firstname.get(0);
	                    List lastname = fetchResp.getAttributeValues("lastname");
	                    openIdLastName = (String) lastname.get(0);
	                    List country = fetchResp.getAttributeValues("country");
	                    openIdCountry = (String) country.get(0);
	                    List language = fetchResp.getAttributeValues("language");
	                    openIdLanguage = (String) language.get(0);
	                     /* Some other attributes ... */
	  	                }
	                return verified.getIdentifier();
	            }else{
	            	logger.info("NOT VERIFIED USER:ReceivingURL:"+receivingURL.toString());
		            logger.info("NOT VERIFIED USER:Response:"+response.toString());
		            logger.info("NOT VERIFIED USER:Discovered:"+discovered.toString());
	            }
	        } catch (OpenIDException e) {
	            logger.error("OpenIDException in OpenIDBean",e);
	        	//e.printStackTrace();
	        }
	        return null;
	    }
	 
	    public String guestLogin(){
	     openIdEmail = "Guest";
	     openIdFirstName = "Guest";
	     return "guest";
	    }
	    
    /**
	* Getter and Setter Method
	*/
	    public String getUserSuppliedId() {
	        return userSuppliedId;
	    }
	    public void setUserSuppliedId(String userSuppliedId) {
	        this.userSuppliedId = userSuppliedId;
	    }
	    public String getValidatedId() {
	        return validatedId;
	    }
	    public String getOpenIdEmail() {
	        return openIdEmail;
	    }
	    public String getOpenIdFirstName() {
			return openIdFirstName;
		}

		public void setOpenIdFirstName(String openIdFirstName) {
			this.openIdFirstName = openIdFirstName;
		}

		public String getOpenIdLastName() {
			return openIdLastName;
		}

		public void setOpenIdLastName(String openIdLastName) {
			this.openIdLastName = openIdLastName;
		}

		public String getOpenIdCountry() {
			return openIdCountry;
		}

		public void setOpenIdCountry(String openIdCountry) {
			this.openIdCountry = openIdCountry;
		}

		public String getOpenIdLanguage() {
			return openIdLanguage;
		}

		public void setOpenIdLanguage(String openIdLanguage) {
			this.openIdLanguage = openIdLanguage;
		}

		public void setValidatedId(String validatedId) {
			this.validatedId = validatedId;
		}

		public void setOpenIdEmail(String openIdEmail) {
			this.openIdEmail = openIdEmail;
		}

}
