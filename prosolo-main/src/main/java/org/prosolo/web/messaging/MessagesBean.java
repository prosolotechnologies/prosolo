package org.prosolo.web.messaging;

import org.apache.log4j.Logger;
import org.prosolo.common.event.context.data.PageContextData;
import org.prosolo.common.event.context.data.UserContextData;
import org.prosolo.common.util.Pair;
import org.prosolo.search.UserTextSearch;
import org.prosolo.search.impl.PaginatedResult;
import org.prosolo.search.util.users.UserScopeFilter;
import org.prosolo.search.util.users.UserSearchConfig;
import org.prosolo.services.interaction.MessagingManager;
import org.prosolo.services.nodes.RoleManager;
import org.prosolo.services.nodes.UnitManager;
import org.prosolo.services.nodes.data.UserData;
import org.prosolo.services.urlencoding.UrlIdEncoder;
import org.prosolo.services.util.roles.SystemRoleNames;
import org.prosolo.web.LoggedUserBean;
import org.prosolo.web.messaging.data.MessageData;
import org.prosolo.web.messaging.data.MessageThreadData;
import org.prosolo.web.notification.TopInboxBean;
import org.prosolo.web.util.page.PageUtil;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.faces.bean.ManagedBean;
import javax.inject.Inject;
import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author "Nikola Milikic"
 */
@ManagedBean(name = "messagesBean")
@Component("messagesBean")
@Scope("view")
public class MessagesBean implements Serializable {

    private static final long serialVersionUID = -7914658400194958136L;

    private static Logger logger = Logger.getLogger(MessagesBean.class);

    @Inject
    private MessagingManager messagingManager;
    @Inject
    private LoggedUserBean loggedUser;
    @Inject
    private UrlIdEncoder idEncoder;
    @Inject
    private TopInboxBean topInboxBean;
    @Inject
    private RoleManager roleManager;
    @Inject
    private UserTextSearch userTextSearch;
    @Inject
    private UnitManager unitManager;

    private List<MessageThreadData> messageThreads;
    private MessageThreadData selectedThread;
    private boolean selectedThreadUnread;
    private static final int LIMIT_THREADS = 5;
    private int pageThread = 0;
    private boolean loadMoreThreads;

    private List<MessageData> messages;
    private String threadId;
    private static final int LIMIT_MESSAGES = 7;
    private int pageMessages = 0;
    private boolean loadMoreMessages;

    private UserData messageRecipient;
    //variables used for controlling component displays
    private boolean archiveView;
    private boolean newMessageView;

    private String messageText = "";

    // search related fields
    private long loggedUserRoleId;
    private List<Long> loggedUserUnits;
    private String query;
    private List<UserData> users;
    private int userSize;
    private int userSearchLimit = 5;

    public void init() {
        long decodedThreadId = idEncoder.decodeId(threadId);
        loggedUserRoleId = roleManager.getRoleIdByName(SystemRoleNames.USER);
        loggedUserUnits = unitManager.getUserUnitIdsInRole(loggedUser.getUserId(), loggedUserRoleId);

        initThread(decodedThreadId);

        // set hasUnreadMessages flag in TopInboxBean to false
        topInboxBean.markMessageRead();
    }

    private void initThread(long messageThreadId) {
        this.messageThreads = null;
        this.selectedThread = null;
        this.pageThread = 0;
        this.newMessageView = false;
        this.messages = null;
        this.pageMessages = 0;
        this.messageRecipient = null;

        // load message threads
        loadMessageThreads();

        if (!messageThreads.isEmpty()) {
            // if URL contained a thread id, try loading it from the db.
            if (messageThreadId > 0) {
                try {
                    this.selectedThread = messagingManager.markThreadAsRead(messageThreadId, loggedUser.getUserId(), getLoggedUserContextData());

                    // if the selected message thread is among the loaded threads in the sidebar, then "selectedThread" should reference that object
                    Optional<MessageThreadData> optionalSelectedMThread = messageThreads.stream().filter(mt -> mt.getId() == messageThreadId).findAny();

                    if (optionalSelectedMThread.isPresent()) {
                        this.selectedThread = optionalSelectedMThread.get();
                    }
                } catch (Exception e) {
                    logger.debug("Could not find the message thread with id " + messageThreadId + ". The first message thread will be shown.");
                }
            }

            // if no thread id was passed in the URL, or thread id could not be loaded, open the first thread
            if (selectedThread == null) {
                this.selectedThread = messageThreads.get(0);
            }

            // if thread was unread, mark it as read now
            if (!selectedThread.isReaded()) {
                markThreadRead();
                selectedThreadUnread = true;
            } else {
                selectedThreadUnread = false;
            }

            this.messageRecipient = selectedThread.getReceiver();

            // load messages for the selected thread
            loadMessages();
        }
    }

    private void loadMessageThreads() {
        List<MessageThreadData> loadedThreads = messagingManager.getMessageThreads(
                loggedUser.getUserId(),
                pageThread, LIMIT_THREADS, archiveView);

        if (loadedThreads.size() > LIMIT_THREADS) {
            loadMoreThreads = true;
            this.messageThreads = loadedThreads.subList(0, LIMIT_THREADS);
        } else {
            loadMoreThreads = false;
            this.messageThreads = loadedThreads;
        }
    }

    private UserContextData getLoggedUserContextData() {
        String page = PageUtil.getPostParameter("page");
        String context = PageUtil.getPostParameter("context");
        page = (page != null) ? page : "messages";
        context = (context != null) ? context : "name:messages";
        return loggedUser.getUserContext(new PageContextData(page, context, null));
    }

    public void changeThread(MessageThreadData threadData) {
        // look into already loaded message threads
        Optional<MessageThreadData> optionalThread = messageThreads.stream().filter(mt -> mt.getId() == threadData.getId()).findAny();

        if (optionalThread.isPresent()) {
            this.selectedThread = optionalThread.get();
        } else {
            this.selectedThread = messagingManager.getMessageThread(threadData.getId(), loggedUser.getUserId());
        }

        this.messageRecipient = selectedThread.getReceiver();

        if (!selectedThread.isReaded()) {
            markThreadRead();
        }

        newMessageView = false;
        selectedThreadUnread = false;
        pageMessages = 0;

        loadMessages();
    }

    private void loadMessages() {
        List<MessageData> unreadMessages = messagingManager.getAllUnreadMessages(selectedThread.getId(), loggedUser.getUserId());

        List<MessageData> readMessages;

        //if number of unread messages >= LIMIT_MESSAGES, fetch only the latest two read messages in order to show unread messages in a context.
        if (unreadMessages.size() >= LIMIT_MESSAGES) {
            readMessages = messagingManager.getReadMessages(selectedThread.getId(), loggedUser.getUserId(), 0, 2);
        } else {
            //shift standard pagination for the number of unread messages (first result must be "higher" for that number, last result must be "lower")
            int numberOfMessagesToLoad = LIMIT_MESSAGES - unreadMessages.size();
            readMessages = messagingManager.getReadMessages(selectedThread.getId(), loggedUser.getUserId(), 0, numberOfMessagesToLoad);
        }

        // since getReadMessages loads an additional message, we will use this information to set the loadMoreMessages flag
        if (unreadMessages.size() >= LIMIT_MESSAGES) {
            if (readMessages.size() > 2) {
                loadMoreMessages = true;
                readMessages.remove(readMessages.size() - 1);
            } else {
                loadMoreMessages = false;
            }
        } else {
            if (unreadMessages.size() + readMessages.size() > LIMIT_MESSAGES) {
                loadMoreMessages = true;
                readMessages.remove(readMessages.size() - 1);
            } else {
                loadMoreMessages = false;
            }
        }

        this.messages = new LinkedList<>();
        this.messages.addAll(unreadMessages);
        this.messages.addAll(readMessages);

        // As we got the messages sorted by date DESC, now show them ASC
        Collections.sort(messages, Comparator.comparing(MessageData::getCreated));
    }

    private void openThreadWithParticipant(UserData messageRecipient) {
        // first search in already loaded threads
        Optional<MessageThreadData> optionalMessageThreadData = messageThreads.stream().filter(t -> t.getParticipants().stream().anyMatch(p -> p.getId() == messageRecipient.getId())).findAny();

        if (optionalMessageThreadData.isPresent()) {
            changeThread(optionalMessageThreadData.get());
        } else {
            // if not found, load from the database
            Optional<MessageThreadData> threadData = messagingManager.getMessageThreadDataForUsers(loggedUser.getUserId(), messageRecipient.getId());

            if (threadData.isPresent()) {
                this.selectedThread = threadData.get();
            }

            // if not found, then do nothing
        }
    }

    private void markThreadRead() {
        if (this.selectedThread != null && !selectedThread.isReaded()) {
            // update the data objects
            this.messageThreads.stream().filter(mt -> mt.getId() == this.selectedThread.getId()).forEach(mt -> mt.setReaded(true));
            this.selectedThread.setReaded(true);

            //save read info to database
            messagingManager.markThreadAsRead(selectedThread.getId(), loggedUser.getUserId(), getLoggedUserContextData());
        }
    }

    public void loadMoreThreads() {
        pageThread++;

        List<MessageThreadData> loadedThreads = messagingManager.getMessageThreads(
                loggedUser.getUserId(),
                pageThread, LIMIT_THREADS, archiveView);

        if (loadedThreads.size() > LIMIT_THREADS) {
            this.messageThreads.addAll(loadedThreads.subList(0, LIMIT_THREADS));
        } else {
            this.messageThreads.addAll(loadedThreads);
            loadMoreThreads = false;
        }
    }

    public void loadMoreMessages() {
        pageMessages++;

        List<MessageData> newMessages = messagingManager.getReadMessages(selectedThread.getId(), loggedUser.getUserId(), pageMessages, LIMIT_MESSAGES);

        // getReadMessages returns always one more message than requested
        if (newMessages.size() <= LIMIT_MESSAGES) {
            loadMoreMessages = false;
        } else {
            newMessages.remove(newMessages.size() - 1);
        }

        // As we sorted them by date DESC, now show them ASC (so last message will be last one created)
        Collections.sort(newMessages, Comparator.comparing(MessageData::getCreated));

        this.messages.addAll(0, newMessages);
    }

    public void sendMessage() {
        try {
            UserContextData userContext = getLoggedUserContextData();

            Pair<MessageData, MessageThreadData> result = messagingManager.sendMessageAndReturnMessageAndThread(
                    0, loggedUser.getUserId(), messageRecipient.getId(), this.messageText, userContext);

            // if the message is sent from the Archive section, then reinitialize everything as the thread has now
            // been revoked
            if (archiveView) {
                setArchiveView(false);
            } else {
                MessageThreadData thread = result.getSecond();

                // if this thread is already in the sidebar, remove it
                this.messageThreads.removeIf(t -> t.getId() == thread.getId());

                // add the thread as the first one
                this.messageThreads.add(0, thread);
                this.selectedThread = thread;
                this.messages = this.selectedThread.getMessages();
                this.archiveView = false;
                this.newMessageView = false;
            }

            logger.debug("User " + loggedUser.getUserId() + " sent a message to thread " + selectedThread.getId() + " with content: '" + this.messageText + "'");
            PageUtil.fireSuccessfulInfoMessage("messagesFormGrowl", "Your message is sent");

            this.messageText = null;

            // if selected thread is marked as unread, mark it as read
            this.selectedThreadUnread = false;
        } catch (Exception e) {
            logger.error("Exception while sending message", e);
            PageUtil.fireErrorMessage("There was an error sending the message");
        }
    }

    public void setNewMessageView(boolean newMessageView) {
        this.newMessageView = newMessageView;

        if (newMessageView) {
            resetSearch();
        }
        this.messageText = null;
        this.selectedThread = null;
        this.messageRecipient = null;
        this.loadMoreMessages = false;
        this.pageMessages = 0;
    }

    public void setArchiveView(boolean archiveView) {
        this.archiveView = archiveView;
        pageThread = 0;
        initThread(-1);
    }

    public void archiveSelectedThread() {
        messagingManager.updateArchiveStatus(selectedThread.getId(), loggedUser.getUserId(), true);
        initThread(-1);

        PageUtil.fireSuccessfulInfoMessage("Conversation is archived");
    }

    public void revokeFromArchiveSelectedThread() {
        messagingManager.updateArchiveStatus(selectedThread.getId(), loggedUser.getUserId(), false);
        this.archiveView = false;
        initThread(selectedThread.getId());

        PageUtil.fireSuccessfulInfoMessage("Conversation is revoked from the Archive");
    }

    public void deleteCurrentThread() {
        messagingManager.markThreadDeleted(selectedThread.getId(), loggedUser.getUserId());
        initThread(-1);
    }

    /*
     * Search related methods
     */
    public void search() {
        if (query.isEmpty()) {
            this.userSize = 0;
            this.users = null;
            return;
        }

        List<Long> excludeUsers = new ArrayList<>();
        excludeUsers.add(loggedUser.getUserId());

        UserSearchConfig searchConfig = UserSearchConfig.of(
                UserSearchConfig.UserScope.ORGANIZATION, UserScopeFilter.USERS_UNITS, loggedUserRoleId, loggedUserUnits);

        PaginatedResult<UserData> usersResponse = userTextSearch.searchUsersWithFollowInfo(
                loggedUser.getOrganizationId(), query, -1, userSearchLimit, loggedUser.getUserId(), searchConfig);

        if (usersResponse != null) {
            this.userSize = (int) usersResponse.getHitsNumber();
            this.users = usersResponse.getFoundNodes();
        } else {
            this.userSize = 0;
        }
    }

    public void resetSearch() {
        users = new ArrayList<>();
        query = "";
        userSize = 0;
    }


    /*
     * GETTERS / SETTERS
     */

    public String getThreadId() {
        return threadId;
    }

    public void setThreadId(String threadId) {
        this.threadId = threadId;
    }

    public MessageThreadData getSelectedThread() {
        return selectedThread;
    }

    public List<MessageThreadData> getMessageThreads() {
        return messageThreads;
    }

    public boolean isLoadMoreThreads() {
        return loadMoreThreads;
    }

    public List<MessageData> getMessages() {
        return messages;
    }

    public List<MessageData> getReadMessages() {
        return messages != null ? messages.stream().filter(msg -> msg.isReaded()).collect(Collectors.toList()) : null;
    }

    public List<MessageData> getUnreadMessages() {
        return messages != null ? messages.stream().filter(msg -> !msg.isReaded()).collect(Collectors.toList()) : null;
    }

    public boolean isLoadMoreMessages() {
        return loadMoreMessages;
    }

    public String getMessageText() {
        return messageText;
    }

    public void setMessageText(String messageText) {
        this.messageText = messageText;
    }

    public boolean isArchiveView() {
        return archiveView;
    }

    public boolean isNewMessageView() {
        return newMessageView;
    }

    public UserData getMessageRecipient() {
        return messageRecipient;
    }

    public void setMessageRecipient(UserData messageRecipient) {
        this.messageRecipient = messageRecipient;
        this.newMessageView = false;

        resetSearch();

        this.selectedThread = null;
        this.messages = null;

        openThreadWithParticipant(messageRecipient);
    }

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public List<UserData> getUsers() {
        return users;
    }

    public int getUserSize() {
        return userSize;
    }

    public boolean isSelectedThreadUnread() {
        return selectedThreadUnread;
    }
}
