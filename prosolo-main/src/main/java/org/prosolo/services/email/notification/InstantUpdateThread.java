package org.prosolo.services.email.notification;


public class InstantUpdateThread  extends Thread{
/*
	private static Logger logger = Logger.getLogger(InstantUpdateThread.class.getName());
	private Event event;
	
	public InstantUpdateThread(Event ev){
		this.event=ev;
	}
	@Override
	public void run() {
		try {
		 
			processEventForInstantUpdates(event);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	private void processEventForInstantUpdates(Event event2) throws Exception {
		logger.debug("process event for instant updates");
		@SuppressWarnings("unused")
		User creator=(User) event.getActor();
		Collection<User> usersToNotify=new ArrayList<User>();
		
//		if (event.checkEventType(EventType.SharingEvent)) {
//
//			Collection<Node> sharedWithUsers = ((SharingEvent) event)
//					.getSharedWith();
//			for (BaseEntity sharRes : sharedWithUsers) {
//				if (sharRes instanceof User) {
//					usersToNotify.add((User) sharRes);
//				}
//			}
//		} else
		if(event.checkAction(EventType.CreateRecommendation)){
		 
		Recommendation recommendation=(Recommendation)  event.getObject();
		usersToNotify.add(recommendation.getRecommendedTo());
	
//	}else if(event.checkEventType(EventType.HelpRequestEvent)){
//		 event.getResource();
//		//usersToNotify=SocialStreamQueriesImpl.getUsersFollowingSpecificUser(creator.getUri().toString());
//		usersToNotify = ServiceLocator.getInstance().getService(SocialStreamUserQueries.class).getUsersFollowingSpecificUser(creator.getUri().toString());
//		 
	}
		for(User user:usersToNotify){
		createUpdates(user,event);
		}
	}
	protected void createUpdates(User user,Event event) throws Exception{
		//checkData(user);
		 String html="";
		 
		HTMLDocument htmlDoc=new HTMLDocument();
		 HTMLDocumentFooter htmlFooter=new HTMLDocumentFooter();
		 HTMLDocumentHeader htmlHeader=new HTMLDocumentHeader();
		 htmlHeader.setTitle("Learning Organisation message");
		 
		 Calendar cal = Calendar.getInstance();
		   DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd");
		   cal.add(Calendar.DATE, -1);
		    String yesterdayDate=dateFormat.format(cal.getTime());
		   
		   
		  
		 htmlHeader.setPeriod(yesterdayDate);
		 HTMLDocumentMainBody htmlMainBody=new HTMLDocumentMainBody();
		 htmlMainBody.setBodyTitle("Message for "+user.getName());
		 addHTMLMainBodyContent(htmlMainBody);
		
		 htmlDoc.setDocFooter(htmlFooter);
		 htmlDoc.setDocHeader(htmlHeader);
		 htmlDoc.setDocMainBody(htmlMainBody);
		 html=htmlDoc.produceHTMLDocument();
		 String subject="Message for "+user.getName()+" - "+yesterdayDate;
		 SendEmail sEmail=new SendEmail();
		// if(!eventsByTypes.isEmpty()){
		 sEmail.sendEmailToUser(user,html,subject);
		// }
	}
	
	private void addHTMLMainBodyContent(HTMLDocumentMainBody htmlMainBody){
		 
	//	Iterator<Entry<String, ArrayList<Event>>> it=eventsByTypes.entrySet().iterator();
	//	while(it.hasNext()){
			//Map.Entry<String, ArrayList<Event>>eventsPairs=it.next();
			
		 //	ArrayList<Event> events=eventsPairs.getValue();
			 HTMLDocumentBodyElement bElement=new HTMLDocumentBodyElement();
		//	 bElement.setTitle("Updates for ..." );
		//	for(Event event:events){
				
				 String message=NotificationMessageUtility.getEventMessage(event,"instant");
				 message="<br>"+message+"<br><br><br><br>";
				//String userName= eventgetMaker().getName();
				//message=event.getUri().toString();
				 bElement.addNotification(message);
			// }
			 htmlMainBody.addBodyElement(bElement);
			
		//}
	}
	*/
}
