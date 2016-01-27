package org.prosolo.services.interaction.impl;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.imageio.ImageIO;

import org.apache.log4j.Logger;
import org.hibernate.Query;
import org.hibernate.Session;
import org.prosolo.common.config.CommonSettings;
import org.prosolo.common.domainmodel.activities.ResourceActivity;
import org.prosolo.common.domainmodel.activities.TargetActivity;
import org.prosolo.common.domainmodel.activities.events.EventType;
import org.prosolo.common.domainmodel.activitywall.PostSocialActivity;
import org.prosolo.common.domainmodel.activitywall.SocialActivity;
import org.prosolo.common.domainmodel.activitywall.TwitterPostSocialActivity;
import org.prosolo.common.domainmodel.annotation.Tag;
import org.prosolo.common.domainmodel.content.ContentType;
import org.prosolo.common.domainmodel.content.GoalNote;
import org.prosolo.common.domainmodel.content.Post;
import org.prosolo.common.domainmodel.content.RichContent;
import org.prosolo.common.domainmodel.content.TwitterPost;
import org.prosolo.common.domainmodel.general.BaseEntity;
import org.prosolo.common.domainmodel.general.Node;
import org.prosolo.common.domainmodel.organization.VisibilityType;
import org.prosolo.common.domainmodel.user.LearningGoal;
import org.prosolo.common.domainmodel.user.User;
import org.prosolo.common.exceptions.ResourceCouldNotBeLoadedException;
import org.prosolo.common.util.net.HTTPSConnectionValidator;
import org.prosolo.common.util.string.StringUtil;
import org.prosolo.core.hibernate.HibernateUtil;
import org.prosolo.services.activityWall.SocialStreamObserver;
import org.prosolo.services.annotation.TagManager;
import org.prosolo.services.event.Event;
import org.prosolo.services.event.EventException;
import org.prosolo.services.event.EventFactory;
import org.prosolo.services.general.impl.AbstractManagerImpl;
import org.prosolo.services.htmlparser.HTMLParser;
import org.prosolo.services.interaction.PostManager;
import org.prosolo.services.upload.AmazonS3UploadManager;
import org.prosolo.services.upload.impl.AvatarProcessorImpl;
import org.prosolo.services.upload.impl.ImageDimensions;
import org.prosolo.util.FileUtil;
import org.prosolo.util.urigenerator.MD5HashUtility;
import org.prosolo.web.activitywall.data.AttachmentPreview;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.mortennobel.imagescaling.ResampleFilters;
import com.mortennobel.imagescaling.ResampleOp;

@Service("org.prosolo.services.interaction.PostManager")
public class PostManagerImpl extends AbstractManagerImpl implements PostManager {
	
	private static final long serialVersionUID = 6167721181566635741L;
	
	private static Logger logger = Logger.getLogger(PostManagerImpl.class);
	
	@Autowired private EventFactory eventFactory;
	@Autowired private TagManager tagManager;
	@Autowired private HTMLParser parser;
	@Autowired private AmazonS3UploadManager s3Manager;
	@Autowired @Qualifier("taskExecutor") private ThreadPoolTaskExecutor taskExecutor;

	@Override
	@SuppressWarnings("unchecked")
	@Transactional(readOnly = false)
	public PostEvent createNewPost(User user, String text, VisibilityType visibility, 
			AttachmentPreview attachmentPreview, long[] mentionedUsers, 
			boolean propagateManuallyToSocialStream, String context,
			String page, String learningContext, String service) throws EventException {
		
		// mentioned users
		Set<User> mentioned = new HashSet<User>();
		
		if (mentionedUsers != null) {
			for (long userId : mentionedUsers) {
				try {
					mentioned.add(loadResource(User.class, userId));
				} catch (ResourceCouldNotBeLoadedException e) {
					logger.error(e);
				}
			}
		}
		
		RichContent richContent = createRichContent(attachmentPreview);
		
		Post post = new Post();
		post.setDateCreated(new Date());
		post.setMaker(user);
		post.setContent(text);
		post.setRichContent(richContent);
		post.setVisibility(visibility);
		post.setMentionedUsers(mentioned);
		post = saveEntity(post);
		
		user = saveEntity(user);
		
		// generate events related to the content
		generateEventForContent(user, text, richContent);
		
		Map<String, String> parameters = new HashMap<String, String>();
		parameters.put("context", context);
		
		Event event = null;
		
		if (propagateManuallyToSocialStream) {
			//event = eventFactory.generateEvent(EventType.Post, user, post, new Class[]{SocialStreamObserver.class}, parameters);
			//changed for new context approach
			event = eventFactory.generateEvent(EventType.Post, user, post, null, null, 
					page, learningContext, service, new Class[]{SocialStreamObserver.class}, parameters);
		} else {
//			event = eventFactory.generateEvent(EventType.Post, user, post, parameters);
			//changed for new context approach
			event = eventFactory.generateEvent(EventType.Post, user, post, null, page, 
					learningContext, service, parameters);
		}
		
		return new PostEvent(post, event);
	}
	
	/**
	 * @param user
	 * @param text
	 * @param richContent
	 */
	private void generateEventForContent(final User user, final String text, final RichContent richContent) {
		String addedLink = null;

		if (richContent != null && richContent.getContentType() != null) {
			try {
				switch (richContent.getContentType()) {
				case LINK:
					eventFactory.generateEvent(EventType.LinkAdded, user,
							richContent);
					addedLink = richContent.getLink();
					break;
				case UPLOAD:
					eventFactory.generateEvent(EventType.FileUploaded, user,
							richContent);
					break;
				default:
					break;
				}
			} catch (EventException e) {
				logger.error(e);
			}
		}

		Collection<String> urls = StringUtil.pullLinks(text);
		if (urls.contains(addedLink)) {
			urls.remove(addedLink);
		}
	}

	@Override
	@Transactional(readOnly = false)
	@SuppressWarnings("unchecked")
	public PostEvent createNewPost(User user, Date created, String postLink,
			String text, VisibilityType visibility,
			boolean propagateManuallyToSocialStream) throws EventException {

		Post post = new Post();
		post.setTitle(text);
		post.setDateCreated(created);
		post.setLink(postLink);
		post.setMaker(user);
		post.setContent(text);
		post.setVisibility(visibility);
		post = saveEntity(post);

		saveEntity(user);

		Event event = null;
		if (propagateManuallyToSocialStream) {
			event = eventFactory.generateEvent(EventType.Post, user, post,
					new Class[] { SocialStreamObserver.class });
		} else {
			event = eventFactory.generateEvent(EventType.Post, user, post);
		}

		return new PostEvent(post, event);
	}

	/*
	 * TwitterPost is not Entity class and thus will not be saved in a database. This is 
	 * decided due to the performance improvements in order to lower the number of objects
	 * created when Twitter posts are created. Only an instance of TwitterPostSocialActivity
	 * will be created by the appropriate Observer.
	 */
	@Override
//	@Transactional(readOnly = false)
	public TwitterPost createNewTwitterPost(User maker, Date created, String postLink, long tweetId, String creatorName,
			String screenName, String userUrl, String profileImage, String text, VisibilityType visibility, 
			Collection<String> hashtags, boolean toSave) throws EventException {

		TwitterPost twitterPost = new TwitterPost();
//		twitterPost.setTitle(text);
		twitterPost.setDateCreated(created);
		twitterPost.setLink(postLink);
		twitterPost.setMaker(maker);
		twitterPost.setContent(text);
		twitterPost.getHashtags().addAll(tagManager.getOrCreateTags(hashtags));
		twitterPost.setVisibility(visibility);
		twitterPost.setTweetId(tweetId);
		twitterPost.setCreatorName(creatorName);
		twitterPost.setScreenName(screenName);
		twitterPost.setUserUrl(userUrl);
		twitterPost.setProfileImage(profileImage);
		
		if (toSave) {
			twitterPost = saveEntity(twitterPost);
		}
		
		return twitterPost;
	}

	@Override
	@Transactional(readOnly = false)
	@SuppressWarnings("unchecked")
	public PostEvent createNewGoalNote(User user, long goalId, String text,
			AttachmentPreview attachmentPreview,
			VisibilityType visibility,
			boolean connectNewPostWithStatus,
			boolean propagateManuallyToSocialStream, String context) throws EventException,
			ResourceCouldNotBeLoadedException {

		LearningGoal goal = loadResource(LearningGoal.class, goalId);
		
		GoalNote goalNote = new GoalNote();
		goalNote.setDateCreated(new Date());
		goalNote.setMaker(user);
		goalNote.setContent(text);
		goalNote.setVisibility(visibility);
		goalNote.setConnectWithStatus(connectNewPostWithStatus);
		
		RichContent richContent = createRichContent(attachmentPreview);
		goalNote.setRichContent(richContent);
		
		goalNote.setGoal(goal);
		goalNote = saveEntity(goalNote);
		
		// generate events related to the content
		generateEventForContent(user, text, richContent);
		
		Map<String, String> parameters = new HashMap<String, String>();
		parameters.put("context", context);
		
		Event event = null;
		if (propagateManuallyToSocialStream) {
			event = eventFactory.generateEvent(EventType.AddNote, user, goalNote, goal, new Class[]{SocialStreamObserver.class}, parameters);
		} else {
			event = eventFactory.generateEvent(EventType.AddNote, user, goalNote, goal, parameters);
		}
		
		return new PostEvent(goalNote, event);
	}

	@SuppressWarnings("unchecked")
	@Override
	public PostEvent shareResource(User user, String text,
			VisibilityType visibility, Node resource,
			boolean propagateManuallyToSocialStream,
			String context, String page,
			String learningContext, String service) throws EventException {
		
		RichContent richContent = null;
		
		if (resource instanceof ResourceActivity) {
			ResourceActivity activity = (ResourceActivity) resource;
			RichContent originalRichContent = activity.getRichContent();
			
			if (originalRichContent != null) {
				richContent = new RichContent(originalRichContent);
				richContent = saveEntity(richContent);
			}
		} else if (resource instanceof TargetActivity) {
			TargetActivity tActivity = (TargetActivity) resource;
			
			if (tActivity.getActivity() instanceof ResourceActivity) {
				ResourceActivity activity = (ResourceActivity) tActivity.getActivity();
				RichContent originalRichContent = activity.getRichContent();
				
				if (originalRichContent != null) {
					richContent = new RichContent(originalRichContent);
					richContent = saveEntity(richContent);
				}
			}
		}
		
		if (richContent == null) {
			richContent = new RichContent();
			richContent.setContentType(ContentType.RESOURCE);
			richContent.setResource(resource);
			richContent.setTitle(resource.getTitle());
			richContent.setDescription(resource.getDescription());
			richContent.setVisibility(visibility);
			richContent = saveEntity(richContent);
		}
		
		Post post = new Post();
		post.setDateCreated(new Date());
		post.setMaker(user);
		post.setContent(text);
		post.setRichContent(richContent);
		post.setVisibility(visibility);
		post = saveEntity(post);

		// generate events related to the content
		// no need for this as those links/uploaded files are already added
//		generateEventForContent(user, text, richContent);
		
		Map<String, String> parameters = new HashMap<String, String>();
		parameters.put("context", context);
		
		Event event = null;
		if (propagateManuallyToSocialStream) {
			//event = eventFactory.generateEvent(EventType.PostShare, user, post, new Class[]{SocialStreamObserver.class}, parameters);
			//changed for new context approach
			event = eventFactory.generateEvent(EventType.PostShare, user, post, null, null, 
					page, learningContext, service, new Class[]{SocialStreamObserver.class}, parameters);
		} else {
			//event = eventFactory.generateEvent(EventType.PostShare, user, post, parameters);
			//changed for new context approach
			event = eventFactory.generateEvent(EventType.PostShare, user, post, null, 
					page, learningContext, service, parameters);
		}
		
		return new PostEvent(post, event);
	}
	
	@Override
	@Transactional (readOnly = false)
	public Post resharePost(User user, Post originalPost) throws EventException {
		if (originalPost != null) {
			RichContent richContent = originalPost.getRichContent();

			Post post = new Post();
			post.setDateCreated(new Date());
			post.setMaker(user);
			post.setContent(originalPost.getContent());
			post.setRichContent(richContent);
			post.setReshareOf(originalPost);
			post = saveEntity(post);

			user = saveEntity(user);

			eventFactory.generateEvent(EventType.PostShare, user, post, originalPost.getMaker());

			return post;
		}
		return null;
	}

	@Override
	@Transactional(readOnly = false)
	@SuppressWarnings("unchecked")
	public PostEvent resharePost(User user, SocialActivity socialActivity,
			boolean propagateManuallyToSocialStream) throws EventException {

		if (socialActivity != null && user != null) {
			user = merge(user);
			BaseEntity object = socialActivity.getObject();

			if (object instanceof Post) {
				Post originalPost = (Post) object;
				RichContent richContent = originalPost.getRichContent();

				Post post = new Post();
				post.setDateCreated(new Date());
				post.setMaker(user);
				post.setContent(originalPost.getContent());
				post.setRichContent(richContent);
				post.setReshareOf(originalPost);
				post = saveEntity(post);

				user = saveEntity(user);

				// incrementing share count on the social activity
				socialActivity.setShareCount(socialActivity.getShareCount() + 1);
				saveEntity(socialActivity);

				Event event = null;
				if (propagateManuallyToSocialStream) {
					event = eventFactory.generateEvent(EventType.PostShare, user, post, originalPost.getMaker(),
							new Class[] { SocialStreamObserver.class });
				} else {
					event = eventFactory.generateEvent(EventType.PostShare, user, post, originalPost.getMaker());
				}

				return new PostEvent(post, event);
			}
		}
		return null;
	}

	//changed for new context approach
	@SuppressWarnings("unchecked")
	@Override
	public PostEvent reshareSocialActivity(User user, String text,
			VisibilityType visibility, AttachmentPreview attachmentPreview,
			SocialActivity originalSocialActivity,
			boolean propagateManuallyToSocialStream, String page,
			String learningContext, String service) throws EventException, ResourceCouldNotBeLoadedException {
		
		if (originalSocialActivity != null) {
			originalSocialActivity = merge(originalSocialActivity);
			user = merge(user);
			
			BaseEntity object = originalSocialActivity.getObject();
			
			if (object instanceof Post) {
				Post originalPost = (Post) object;

				Post post = new Post();
				post.setDateCreated(new Date());
				post.setMaker(user);
				post.setContent(text);
				post.setReshareOf(originalPost);
				post.setVisibility(visibility);
				post = saveEntity(post);

				RichContent originalRichContent = originalPost.getRichContent();

				if (originalRichContent != null) {
					RichContent richContent = new RichContent(originalRichContent);
					richContent = saveEntity(richContent);

					post.setRichContent(richContent);
					post = saveEntity(post);

					// richContent.setParent(post);
					// richContent = saveEntity(richContent);
				}

				// user.setCurrentStatus(post);
				user = saveEntity(user);

				// incrementing share count on the social activity
				originalSocialActivity.setShareCount(originalSocialActivity.getShareCount() + 1);
				originalSocialActivity = saveEntity(originalSocialActivity);

				Event event = null;
				
				Map<String, String> params = new HashMap<String, String>();
				params.put("originalSocialActivityId", String.valueOf(originalSocialActivity.getId()));
				
				if (propagateManuallyToSocialStream) {
//					event = eventFactory.generateEvent(EventType.PostShare,
//							user, post, originalPost.getMaker(),
//							new Class[] { SocialStreamObserver.class }, params);
					
					event = eventFactory.generateEvent(EventType.PostShare,
							user, post, originalPost.getMaker(), null, page, learningContext,
							service, new Class[] { SocialStreamObserver.class }, params);
				} else {
//					event = eventFactory.generateEvent(EventType.PostShare,
//							user, post, originalPost.getMaker(), params);
					event = eventFactory.generateEvent(EventType.PostShare,
							user, post, originalPost.getMaker(), page, learningContext,
							service, params);
				}

				PostEvent postEvent = new PostEvent(post, event);
				postEvent.setOriginalSocialActivity(originalSocialActivity);
				return postEvent;
			}
		}
		return null;
	}

	@Override
	@Transactional(readOnly = false)
	public RichContent createRichContent(AttachmentPreview attachmentPreview) {
		
		if (attachmentPreview != null && attachmentPreview.getLink() != null
				&& attachmentPreview.getLink().length() > 0) {
			
			String title = null;
			String description = null;
			
			if (attachmentPreview.getContentType().equals(ContentType.LINK)) {
				title = attachmentPreview.getTitle();
				description = attachmentPreview.getDescription();
			} else if (attachmentPreview.getContentType().equals(ContentType.UPLOAD)) {
				title = attachmentPreview.getUploadTitle();
				description = attachmentPreview.getUploadDescription();
			}
			
			String selectedImage = attachmentPreview.getImage();
			
			// clean parameters if there are any
			if (selectedImage != null && selectedImage.contains("?")) {
				selectedImage = selectedImage.substring(0, selectedImage.indexOf("?"));
			}
			
			RichContent richContent = new RichContent();
			richContent.setLink(attachmentPreview.getLink());
			richContent.setContentType(attachmentPreview.getContentType());

			if (title != null)
				richContent.setTitle(title);

			if (description != null)
				richContent.setDescription(description);

			if (selectedImage != null && selectedImage.length() > 0) {
				// fetch the image from the original source
				try {
					URL url = new URL(selectedImage);
					HttpURLConnection connection = (HttpURLConnection) url.openConnection();
					HttpURLConnection.setFollowRedirects(true);
					connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10.6; rv:25.0) Gecko/20100101 Firefox/25.0");
					connection.setConnectTimeout(5000);
					connection.setReadTimeout(10000);
					HTTPSConnectionValidator.checkIfHttpsConnection((HttpURLConnection) connection);
					connection.connect();
					InputStream inputStream = null;
					
					try {
						inputStream = connection.getInputStream();
					} catch (FileNotFoundException fileNotFoundException) {
						logger.error("File not found for:" + url);
					} catch (IOException ioException) {
						logger.error("IO exception for:" + url + " cause:" + ioException.getLocalizedMessage());
					}
					
					BufferedImage originalBufferedImage= ImageIO.read(inputStream);
					
					ImageDimensions imageDimensions = AvatarProcessorImpl.getScalledImageDimensions(
							originalBufferedImage.getWidth(), 
							originalBufferedImage.getHeight(), 
							100, 
							100);
					
					ResampleOp resampleOp = new ResampleOp(imageDimensions.width, imageDimensions.height);
					resampleOp.setFilter(ResampleFilters.getLanczos3Filter());
					resampleOp.setUnsharpenMask(ResampleOp.UnsharpenMask.Soft);
					BufferedImage scaledBI = resampleOp.filter(originalBufferedImage, null);

					ByteArrayOutputStream os = new ByteArrayOutputStream();
					ImageIO.write(scaledBI, selectedImage.substring(selectedImage.lastIndexOf('.') + 1), os);
					InputStream is = new ByteArrayInputStream(os.toByteArray());
					
					String imageName = selectedImage.substring(selectedImage.lastIndexOf('/') + 1);
					
					String fileType = FileUtil.getFileType(new File(url.getFile()));
					
					
					String key = "images/thumbnail/" + 
									MD5HashUtility.getMD5Hex(String.valueOf(new Date().getTime())+imageName) + "/" +
									imageName;
					
					s3Manager.storeInputStreamByKey(is, key,fileType);
					selectedImage = CommonSettings.getInstance().config.fileStore.fileStoreServiceUrl + "/" + CommonSettings.getInstance().config.fileStore.fileStoreBucketName + "/" + key;
				} catch (IOException e) {
					logger.error("IOException while storing:"+selectedImage,e);
				} catch (NoSuchAlgorithmException e) {
					logger.error(e);
				}
				
				richContent.setImageUrl(selectedImage);
			}

			return saveEntity(richContent);
		}
		return null;
	}

	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public void addFirstImage(final RichContent richContent) {
		try {
			String url = parser.getFirstImage(richContent.getLink());

			if (url != null) {
				richContent.setImageUrl(url);
				saveEntity(richContent);
			}
		} catch (IOException e) {
			logger.error(e.getMessage());
		}
	}

	@Override
	@Transactional
	public void deleteAllTwitterPostsCreatedBefore(Session session,
			Date createDate, int firstResult, int maxResults, int inThreads) {
		final ArrayBlockingQueue<Runnable> queue = new ArrayBlockingQueue<Runnable>(
				50, true);
		ThreadPoolExecutor threadPool = new ThreadPoolExecutor(inThreads,
				inThreads, 20, TimeUnit.SECONDS, queue,
				new ThreadPoolExecutor.CallerRunsPolicy());

		String query = "SELECT postSocialActivity.id, twitterPost.id, maker.id "
				+ "FROM PostSocialActivity postSocialActivity "
				+ "LEFT JOIN postSocialActivity.postObject as twitterPost "
				+ "LEFT JOIN postSocialActivity.maker as maker "
				+ "WHERE "
				+ "postSocialActivity.deleted = :deleted AND "
				+ "postSocialActivity.dateCreated <= :createDate AND "
				+ "postSocialActivity.class = PostSocialActivity AND "
				+ "postSocialActivity.action = :action "
				+ "ORDER BY postSocialActivity.dateCreated ASC";
		
		@SuppressWarnings("unchecked")
		List<Object> result = session.createQuery(query)
				.setBoolean("deleted", false)
				.setParameter("action", EventType.TwitterPost)
				.setParameter("createDate", createDate)
				.setFirstResult(firstResult).setMaxResults(maxResults).list();
		
		if (result != null) {
			Iterator<Object> resultIt = result.iterator();
			List<PostToDelete> deletingList = new ArrayList<PostToDelete>();
			int threadNo = 0;
			int threadSize = maxResults / inThreads;
			
			while (resultIt.hasNext()) {
				Object[] object = (Object[]) resultIt.next();
				Long postSocActivityId = (Long) object[0];
				Long twitterPostId = (Long) object[1];
				Long makerId = (Long) object[2];
				PostToDelete postToDelete = new PostToDelete(postSocActivityId,	twitterPostId, makerId);
				deletingList.add(postToDelete);
				
				if (threadNo < inThreads && deletingList.size() > threadSize) {
					threadNo++;
					final List<PostToDelete> destList = new ArrayList<PostToDelete>(deletingList.size());
					destList.addAll(deletingList);

					threadPool.execute(new Runnable() {
						@Override
						public void run() {
							deletePostsInCollection2(destList);
						}
					});
					deletingList = new ArrayList<PostToDelete>();
				}
			}
			
			final List<PostToDelete> destList = new ArrayList<PostToDelete>(deletingList.size());
			destList.addAll(deletingList);

			threadPool.execute(new Runnable() {
				@Override
				public void run() {
					deletePostsInCollection2(destList);
				}
			});
		}
	}

	private void deletePostsInCollection2(List<PostToDelete> postsToDelete) {
		long start = System.currentTimeMillis();
		Session session = (Session) getPersistence().openSession();
		List<Long> ids = new ArrayList<Long>();
		try{
		for (PostToDelete post : postsToDelete) {
			ids.add(post.postId);
		}
		
		String postSocialActivityHql = 
				"DELETE FROM PostSocialActivity postSocialActivity " +
				"WHERE  postSocialActivity.dateCreated <= :createDate ";
		
		Query socActQuery = session.createQuery(postSocialActivityHql);

		socActQuery.setParameter("postSocActIdList", ids);

		// Long result = (Long) session.createQuery(query)
		// .setParameter("createDate", createDate).uniqueResult();

		@SuppressWarnings("unused")
		int result1 = 0;
		try {
			result1 = socActQuery.executeUpdate();
		} catch (org.hibernate.exception.ConstraintViolationException cve) {
			// System.out
			// .println("ERROR. Constraing Violation exception for PostSocialActivity.id="
			// + post.socialActivityId);
			// PostSocialActivity socAct = (PostSocialActivity) session.get(
			// PostSocialActivity.class, post.socialActivityId);
			// socAct.setDeleted(true);
			// session.save(socAct);
		} catch (org.hibernate.exception.LockTimeoutException lte) {
			// System.out
			// .println("ERROR. LockTimeoutException. Couldn't delete:"
			// + post.socialActivityId);
		}
		// if (result1 > 0) {
		// String twitterPostHql = "DELETE FROM TwitterPost twitterPost "
		// + "WHERE  twitterPost.id = :postId ";
		// Query twitterPostQuery = session.createQuery(twitterPostHql);
		//
		// twitterPostQuery.setParameter("postId", post.postId);
		// int result2 = twitterPostQuery.executeUpdate();
		//
		// String anonNullUserHql = "DELETE FROM AnonUser user "
		// + "WHERE user.id = :makerId";
		// Query userNoDateQuery = session.createQuery(anonNullUserHql);
		// userNoDateQuery.setParameter("makerId", post.userId);
		// int result3 = userNoDateQuery.executeUpdate();
		// }
		//
		// }
		session.flush();
		logger.debug("Flushed thread deleted:" + postsToDelete.size() + " for:"
				+ (System.currentTimeMillis() - start));
	 }catch(Exception e){
			logger.error("Exception in handling message",e);
		}finally{
			HibernateUtil.close(session);
		} 
	}

	@Override
	@Transactional
	public int bulkDeleteTwitterPostSocialActivitiesCreatedBefore(
			Session session, Date createDate) {
		
		long start = System.currentTimeMillis();

		String commentedActivitiesQuery = 
			"SELECT DISTINCT socialActivity.id " +
			"FROM SocialActivityComment comment " +
			"LEFT JOIN comment.commentedSocialActivity socialActivity " +
			"WHERE socialActivity.dateCreated <= :createDate " +
				"AND socialActivity.action = :action";
		
		@SuppressWarnings("unchecked")
		List<Long> commentedActivities = persistence.currentManager().createQuery(commentedActivitiesQuery)
				.setParameter("createDate", createDate)
				.setParameter("action", EventType.TwitterPost)
				.list();
 
		String interactedActivitiesQuery = 
			"SELECT DISTINCT socialActivity.id " +
			"FROM SocialActivity socialActivity " +
			"WHERE (socialActivity.likeCount > 0 " +
					"OR socialActivity.dislikeCount > 0 " +
					"OR socialActivity.shareCount > 0 " +
					"OR socialActivity.bookmarkCount > 0) " +
				"AND socialActivity.dateCreated <= :createDate " +
				"AND socialActivity.action = :action";
			
		@SuppressWarnings("unchecked")
		List<Long> interactedActivities = persistence.currentManager().createQuery(interactedActivitiesQuery)
				.setParameter("createDate", createDate)
				.setParameter("action", EventType.TwitterPost)
				.list();
		
		commentedActivities.addAll(interactedActivities);
		
		String notificationsQuery = 
			"DELETE FROM SocialActivityNotification socialActivityNotification " +
			"WHERE socialActivityNotification.socialActivity IN ( " +
				"SELECT twitterPostSocialActivity " +
				"FROM TwitterPostSocialActivity twitterPostSocialActivity " +
				"WHERE twitterPostSocialActivity.id NOT IN (:activityIds) " +
					"AND twitterPostSocialActivity.dateCreated <= :createDate " +
					"AND twitterPostSocialActivity.action = :action ) ";
		
		int deletedNotifications = session.createQuery(notificationsQuery)
				.setParameterList("activityIds", commentedActivities)
				.setParameter("createDate", createDate)
				.setParameter("action", EventType.TwitterPost)
				.executeUpdate();
		
		
		String activitiesQuery = 
			"DELETE FROM SocialActivity socialActivity " +
			"WHERE socialActivity.id NOT IN (:activityIds) " +
				"AND socialActivity.dateCreated <= :createDate " +
				"AND socialActivity.action = :action";
		
		int deletedSocialActivities = session.createQuery(activitiesQuery)
				.setParameterList("activityIds", commentedActivities)
				.setParameter("createDate", createDate)
				.setParameter("action", EventType.TwitterPost)
				.executeUpdate();

		
		long end = System.currentTimeMillis();

		logger.debug("Deleted "+deletedNotifications+" Twitter Social Activity Notifications and "
				+deletedSocialActivities+" Twitter Social Activity in " + (end - start) + " miliseconds");
		
		return deletedNotifications + deletedSocialActivities;
	}

	@Override
	@Transactional(readOnly = true)
	public int getNumberOfTwitterPostsCreatedBefore(Session session,
			Date createDate) {
		// Session session = persistence.currentManager();
		String query = "SELECT COUNT(twitterPost) "
				+ "FROM TwitterPost twitterPost " + "WHERE "
				+ "twitterPost.dateCreated <= :createDate AND "
				+ "twitterPost.class = TwitterPost ";
		Long result = (Long) session.createQuery(query)
				.setParameter("createDate", createDate).uniqueResult();

		return (int) result.longValue();
	}
	
	@Override
	@Transactional(readOnly = true)
	public List<User> getUsersWhoSharedSocialActivity(long socialActivityId) {
		String query = 
			"SELECT DISTINCT(user) " +
			"FROM PostSocialActivity socialActivity " +
			"LEFT JOIN socialActivity.maker user " + 
			"LEFT JOIN socialActivity.postObject post " + 
			"WHERE post.reshareOf.id IN ( " +
								"SELECT post1 " +
								"FROM PostSocialActivity socialActivity1 " +
								"LEFT JOIN socialActivity1.postObject post1 " + 
								"WHERE socialActivity1.id = :socialActivityId" +
			")";
		
		@SuppressWarnings("unchecked")
		List<User> result = persistence.currentManager().createQuery(query)
				.setLong("socialActivityId", socialActivityId)
				.list();

		if (result != null) {
			return result;
		}
		return new ArrayList<User>();
	}
	
	@Override
	@Transactional(readOnly = true)
	public boolean isSharedByUser(SocialActivity socialActivity, User user) {
		if (user == null || socialActivity == null) 
			return false;
		
		String query = 
			"SELECT COUNT(user) " +
			"FROM PostSocialActivity socialActivity " +
			"LEFT JOIN socialActivity.maker user " + 
			"LEFT JOIN socialActivity.postObject post " + 
			"WHERE user = :user " + 
				"AND post.reshareOf.id IN ( " +
								"SELECT post1 " +
								"FROM PostSocialActivity socialActivity1 " +
								"LEFT JOIN socialActivity1.postObject post1 " + 
								"WHERE socialActivity1 = :socialActivity" +
			")";
		
		Long result = (Long) persistence.currentManager().createQuery(query)
				.setEntity("socialActivity", socialActivity)
				.setEntity("user", user)
				.uniqueResult();

		return result > 0;
	}
	
	@Override
	@Transactional(readOnly = true)
	public List<TwitterPostSocialActivity> getTwitterPosts(Collection<Tag> hashtags, Date date) {
		if (hashtags == null || hashtags.isEmpty()) {
			return new ArrayList<TwitterPostSocialActivity>();
		}
		
		String query = 
			"SELECT DISTINCT post " +
			"FROM TwitterPostSocialActivity post " +
			"LEFT JOIN post.hashtags hashtag " + 
			"WHERE hashtag IN (:hashtags) " +
				"AND year(post.dateCreated) = year(:date) " + 
				"AND month(post.dateCreated) = month(:date) " + 
				"AND day(post.dateCreated) = day(:date) " +
			"ORDER BY post.dateCreated DESC ";
		
		@SuppressWarnings("unchecked")
		List<TwitterPostSocialActivity> result = persistence.currentManager().createQuery(query)
				.setParameterList("hashtags", hashtags)
				.setDate("date", date)
				.list();

		return result;
	}

	@Override
	@Transactional(readOnly = false)
	public SocialActivity updatePost(User user, long socialActivityId, String updatedText, String context) throws ResourceCouldNotBeLoadedException {
		SocialActivity socialActivity = loadResource(SocialActivity.class, socialActivityId);
		
		if (socialActivity != null) {
			Date now = new Date();

			socialActivity.setText(updatedText);
			socialActivity.setLastAction(now);
			//socialActivity.setUpdated(true);
			
			if (socialActivity instanceof PostSocialActivity) {
				Post post = ((PostSocialActivity) socialActivity).getPostObject();
				post.setContent(updatedText);
				//post.setUpdated(now);
				
				saveEntity(post);
			}
			return saveEntity(socialActivity);
		}
		return null;
	}

	public class PostEvent {
		private Post post;
		private Event event;
		private SocialActivity originalSocialActivity;

		public PostEvent(Post post, Event event) {
			this.post = post;
			this.event = event;
		}

		public Post getPost() {
			return post;
		}

		public void setPost(Post post) {
			this.post = post;
		}

		public Event getEvent() {
			return event;
		}

		public void setEvent(Event event) {
			this.event = event;
		}

		public SocialActivity getOriginalSocialActivity() {
			return originalSocialActivity;
		}

		public void setOriginalSocialActivity(SocialActivity originalSocialActivity) {
			this.originalSocialActivity = originalSocialActivity;
		}
		
	}

	private class PostToDelete {
		PostToDelete(Long postSocActivityId, Long twitterPostId, Long makerId) {
			this.socialActivityId = postSocActivityId;
			this.postId = twitterPostId;
			this.userId = makerId;
		}

		@SuppressWarnings("unused")
		private Long socialActivityId;
		private Long postId;
		@SuppressWarnings("unused")
		private Long userId;
	}

}
