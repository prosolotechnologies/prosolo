package org.prosolo.web.settings;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;

import org.apache.log4j.Logger;
import org.primefaces.context.RequestContext;
import org.prosolo.common.domainmodel.feeds.FeedSource;
import org.prosolo.common.domainmodel.user.preferences.FeedsPreferences;
import org.prosolo.common.domainmodel.user.socialNetworks.SocialNetworkName;
import org.prosolo.services.feeds.FeedFinder;
import org.prosolo.services.feeds.FeedsManager;
import org.prosolo.services.htmlparser.HTMLParser;
import org.prosolo.web.LoggedUserBean;
import org.prosolo.web.portfolio.PortfolioBean;
import org.prosolo.web.settings.data.AddFeedSourceFormData;
import org.prosolo.web.settings.data.FeedSourceData;
import org.prosolo.web.util.PageUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * @author Zoran Jeremic 2013-08-15
 *
 */
@ManagedBean(name = "feedsBean")
@Component("feedsBean")
@Scope("view")
public class FeedsBean {

	@SuppressWarnings("unused")
	private static Logger logger = Logger.getLogger(FeedsBean.class);

	@Autowired
	private LoggedUserBean loggedUser;
	@Autowired
	private FeedsManager feedsManager;
	@Autowired
	private FeedFinder feedFinder;
	@Autowired
	private HTMLParser htmlParser;

	public String updatePeriod = "daily";

	public List<FeedSourceData> subscribedRssSources = new ArrayList<FeedSourceData>();
	public String personalBlogSource;
	public String personalBlogRssFeedSource;

	private FeedsPreferences feedsPreferences;
	private boolean initialisedAggregation = false;
	private String message = "";

	// used to determine whether to add parsed feeds to personal blogs or to
	// subscribed RSS feeds
	private boolean fetchingFeedsForBlog = true;

	public AddFeedSourceFormData addRssFeedSourceData = new AddFeedSourceFormData();
	public AddFeedSourceFormData addBlogSourceData = new AddFeedSourceFormData();

	@PostConstruct
	public void init() {
		feedsPreferences = feedsManager.getFeedsPreferences(loggedUser.refreshUser());

		initAllSources();
	}

	private void initAllSources() {
		if (feedsPreferences.getPersonalBlogSource() != null) {
			this.personalBlogRssFeedSource = feedsPreferences.getPersonalBlogSource().getLink();
		}

		PortfolioBean portfolio = PageUtil.getSessionScopedBean("portfolio", PortfolioBean.class);
		this.personalBlogSource = portfolio.getSocialNetworksData().getSocialNetworkAccounts()
				.get(SocialNetworkName.BLOG.toString()).getLink();

		this.subscribedRssSources = new ArrayList<FeedSourceData>();

		for (FeedSource feedSource : feedsPreferences.getSubscribedRssSources()) {
			this.subscribedRssSources.add(new FeedSourceData(feedSource));
		}
	}

	public void processRssFeedLink() {
		RequestContext context = RequestContext.getCurrentInstance();

		boolean validRssLink = feedFinder.checkIfValidRssFeedLink(addRssFeedSourceData.getLinkToAdd());

		if (validRssLink) {
			feedsPreferences = feedsManager.addSubscribedRssSource(feedsPreferences,
					addRssFeedSourceData.getLinkToAdd());

			initAllSources();

			context.addCallbackParam("close", true);
			PageUtil.fireSuccessfulInfoMessage("New RSS feed is added.");

			addRssFeedSourceData = new AddFeedSourceFormData();
		} else {
			Map<String, String> feedsUrls = feedFinder.extractFeedsFromBlog(addRssFeedSourceData.getLinkToAdd());

			this.addRssFeedSourceData.setFeedSources(new ArrayList<FeedSourceData>());

			boolean foundSources = false;

			for (Entry<String, String> feedUrl : feedsUrls.entrySet()) {
				FeedSourceData feedData = new FeedSourceData();

				String link = feedUrl.getValue();

				// check if there is already this link
				for (FeedSourceData existingRss : this.subscribedRssSources) {
					if (existingRss.getLink().equals(link)) {
						feedData.setCanNotBeAdded(true);
						addRssFeedSourceData.setSomeFeedsCanNotBeAdded(true);
						break;
					}
				}

				String title = feedUrl.getKey();

				if (title != null && title.length() > 0) {
					feedData.setTitle(title);
				} else {
					feedData.setTitle(link);
				}
				feedData.setLink(link);

				this.addRssFeedSourceData.getFeedSources().add(feedData);

				foundSources = true;
			}

			if (foundSources) {
				context.addCallbackParam("foundSources", true);
			} else {
				context.addCallbackParam("noSources", true);
			}
		}
	}

	public void addNewRSSFeedSources() {
		feedsPreferences = feedsManager.addSubscribedRssSources(feedsPreferences,
				addRssFeedSourceData.getFeedSources());

		initAllSources();

		PageUtil.fireSuccessfulInfoMessage("feedsForm:feedsFormGrowl",
				"New RSS sources for subscribed feeds are added.");
		addRssFeedSourceData = new AddFeedSourceFormData();
	}

	public void processPersonalBlogLink() {
		String newPersonalBlogLink = addBlogSourceData.getLinkToAdd();

		boolean validLink = htmlParser.checkIfValidLink(newPersonalBlogLink);

		RequestContext context = RequestContext.getCurrentInstance();

		if (validLink) {
			this.personalBlogSource = newPersonalBlogLink;
			context.addCallbackParam("validLink", true);

			Map<String, String> feedsUrls = feedFinder.extractFeedsFromBlog(newPersonalBlogLink);

			this.addBlogSourceData.setFeedSources(new ArrayList<FeedSourceData>());

			for (Entry<String, String> feedUrl : feedsUrls.entrySet()) {
				FeedSourceData feedData = new FeedSourceData();

				String link = feedUrl.getValue();
				String title = feedUrl.getKey();

				if (title != null && title.length() > 0) {
					feedData.setTitle(title);
				} else {
					feedData.setTitle(link);
				}
				feedData.setLink(link);

				this.addBlogSourceData.getFeedSources().add(feedData);
			}
		} else {
			context.addCallbackParam("invalidLink", true);
		}
	}

	public void addPersonalBlog() {
		// update personal blog rss feed
		String newBlogRssLink = null;

		if (addBlogSourceData.getFirstSelectedFeedSource() != null) {
			newBlogRssLink = addBlogSourceData.getFirstSelectedFeedSource().getLink();
		} else if (personalBlogRssFeedSource != null) {
			newBlogRssLink = personalBlogRssFeedSource;
		}

		if (newBlogRssLink != null) {
			feedsPreferences = feedsManager.addPersonalBlogRssSource(feedsPreferences, newBlogRssLink);
		}

		// update personal blog link
		PortfolioBean portfolio = PageUtil.getSessionScopedBean("portfolio", PortfolioBean.class);

		if (portfolio != null) {
			portfolio.getSocialNetworksData().getSocialNetworkAccounts().get(SocialNetworkName.BLOG.toString())
					.setLinkEdit(this.personalBlogSource);
			portfolio.saveSocialNetworks();
		}

		// reset the cache data
		initAllSources();
		addBlogSourceData = new AddFeedSourceFormData();

		PageUtil.fireSuccessfulInfoMessage("feedsForm:feedsFormGrowl", "New RSS sources for personal blog is added.");
	}

	public void removeSubscribedRssSource(FeedSourceData feedSource) {
		feedsPreferences = feedsManager.removeSubscribedRssSource(feedsPreferences, feedSource.getLink());

		this.subscribedRssSources.remove(feedSource);

		PageUtil.fireSuccessfulInfoMessage("feedsForm:feedsFormGrowl", "Feed source removed.");
	}

	public void removePersonalBlogSource() {
		feedsPreferences = feedsManager.removePersonalBlogSource(feedsPreferences);

		this.personalBlogRssFeedSource = null;

		PageUtil.fireSuccessfulInfoMessage("feedsForm:feedsFormGrowl", "Feed source removed.");
	}

	public void resetAddFeedSourceForm() {
		this.addRssFeedSourceData = new AddFeedSourceFormData();
	}

	/*
	 * Admin viewed functionalities
	 */

	// public void aggregateFeeds() {
	// if (subscribedRssSources.size() == 0) {
	// this.setMessage("You should provide at least one RSS feed you want to
	// follow.");
	// } else {
	// //User user=feedsManager.merge();
	// String systemUserEmail =
	// Settings.getInstance().config.init.defaultUser.email;
	// User systemUser = userManager.getUser(systemUserEmail);
	// List<PersonalFeed> latestOldPersonalFeeds =
	// feedsManager.getPersonalFeedsMarkedLatest(systemUser);
	// PersonalFeed latestSystemFeed = null;
	//
	// for (PersonalFeed pf : latestOldPersonalFeeds) {
	// if (latestSystemFeed == null) {
	// latestSystemFeed = pf;
	// } else {
	// if (latestSystemFeed.getDateCreated().before(pf.getDateCreated())) {
	// latestSystemFeed = pf;
	// }
	// }
	// feedsManager.saveEntity(pf);
	// }
	// feedsAgregator.directAggreagationOfPersonalFeeds(loggedUser.getUser(),
	// latestSystemFeed);
	// this.setInitialisedAggregation(true);
	//
	// String busyMessage="You have successfuly started the aggregation of your
	// RSS feeds. This process could take a while depending on the number " +
	// "of RSS feeds you and your friends are following. " +
	// "Once it is done, you will find an access link here, or in Featured News
	// service on your Home page. " +
	// "In the meantime you can continue to work on other stuff.";
	// this.setMessage(busyMessage);
	// }
	// }

	// public void generateMyPersonalFeeds() {
	// TimeFrame timeFrame = TimeFrame.DAILY;
	//
	// String systemUserEmail =
	// Settings.getInstance().config.init.defaultUser.email;
	// User systemUser = userManager.getUser(systemUserEmail);
	// PersonalFeed systemFeed =
	// feedsAgregator.aggregateSystemFeeds(systemUser.getId(),timeFrame);
	//
	// try {
	// emailSender.sendEmail(new
	// FeedsEmailGenerator(loggedUser.getUser().getName(), systemFeed),
	// loggedUser.getUser().getEmail().getAddress(), "Personal Feeds");
	// } catch (AddressException e) {
	// logger.error(e);
	// } catch (FileNotFoundException e) {
	// logger.error(e);
	// } catch (MessagingException e) {
	// logger.error(e);
	// } catch (IOException e) {
	// logger.error(e);
	// }
	// }

	// public void testBatchGenerateFeedsAndEmail(){
	// digestManager.createDailyDiggestAndSendEmails(Settings.getInstance().config.emailNotifier.activated);
	// }

	// public void removeLink(FeedSource link) {
	// if (this.subscribedRssSources.contains(link)) {
	// Iterator<FeedSource> iterator = subscribedRssSources.iterator();
	//
	// while (iterator.hasNext()) {
	// FeedSource feed = (FeedSource) iterator.next();
	//
	// if (feed.equals(link)) {
	// iterator.remove();
	// break;
	// }
	// }
	// }
	//
	// if (feedsPreferences.getSubscribedRssSources().contains(link)) {
	// Iterator<FeedSource> iterator =
	// feedsPreferences.getSubscribedRssSources().iterator();
	//
	// while (iterator.hasNext()) {
	// FeedSource feed = (FeedSource) iterator.next();
	//
	// if (feed.equals(link)) {
	// iterator.remove();
	// break;
	// }
	// }
	//
	// feedsManager.saveEntity(feedsPreferences);
	// }
	// }

	/*
	 * GETTERS / SETTERS
	 */

	public List<FeedSourceData> getSubscribedRssSources() {
		return subscribedRssSources;
	}

	public String getPersonalBlogSource() {
		return personalBlogRssFeedSource;
	}

	public void setPersonalBlogRssFeedSource(String personalBlogRssFeedSource) {
		this.personalBlogRssFeedSource = personalBlogRssFeedSource;
	}

	public String getPersonalBlogRssFeedSource() {
		return personalBlogRssFeedSource;
	}

	public String getUpdatePeriod() {
		return updatePeriod;
	}

	public void setUpdatePeriod(String updatePeriod) {
		this.updatePeriod = updatePeriod;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public boolean isInitialisedAggregation() {
		return initialisedAggregation;
	}

	public void setInitialisedAggregation(boolean initialisedAggregation) {
		this.initialisedAggregation = initialisedAggregation;
	}

	public AddFeedSourceFormData getAddRssFeedSourceData() {
		return addRssFeedSourceData;
	}

	public AddFeedSourceFormData getAddBlogSourceData() {
		return addBlogSourceData;
	}

	public boolean isFetchingFeedsForBlog() {
		return fetchingFeedsForBlog;
	}

	public void setFetchingFeedsForBlog(boolean fetchingFeedsForBlog) {
		this.fetchingFeedsForBlog = fetchingFeedsForBlog;
	}

}
