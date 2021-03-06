package org.prosolo.services.messaging.impl;

import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.prosolo.app.Settings;
import org.prosolo.common.config.CommonSettings;
import org.prosolo.common.messaging.data.SystemMessage;
import org.prosolo.common.messaging.rabbitmq.WorkerException;
import org.prosolo.common.util.string.StringUtil;
import org.prosolo.config.Config;
import org.prosolo.services.messaging.MessageHandler;
import org.springframework.stereotype.Service;

/**
 * @author Zoran Jeremic Oct 17, 2014
 *
 */
@Deprecated
@Service("org.prosolo.services.messaging.SystemMessageHandler")
public class SystemMessageHandlerImpl implements MessageHandler<SystemMessage> {
	//@Autowired
	//private TwitterStreamsManager twitterStreamsManager;
	private static Logger logger = Logger
			.getLogger(SystemMessageHandlerImpl.class.getName());

	@Override
	public void handle(SystemMessage message)  throws WorkerException {
		logger.info("Handling system message:" + message.getServiceType());
		try {


			Map<String, String> parameters = message.getParameters();
			Config config = Settings.getInstance().config;
			if (!CommonSettings.getInstance().config.rabbitMQConfig.distributed || CommonSettings.getInstance().config.rabbitMQConfig.masterNode) {
				switch (message.getServiceType()) {
					case UPDATEHASHTAGSANDRESTARTSTREAM:
						String addedString = parameters.get("added");
						List<String> addedTags = StringUtil
								.convertCSVToList(addedString);
						String removedString = parameters.get("removed");
						List<String> removedTags = StringUtil
								.convertCSVToList(removedString);
						long userId = Long.valueOf(parameters.get("userId"));
						long lGoalId = Long.valueOf(parameters.get("goalId"));
						//twitterStreamsManager.updateHashTagsStringsAndRestartStream(
						//	removedTags, addedTags, lGoalId, userId);
						break;
					case ADDNEWTWITTERUSERANDRESTARTSTREAM:
						long twitterId = Long.valueOf(parameters.get("twitterId"));
						//twitterStreamsManager.addNewTwitterUserAndRestartStream(twitterId);
						break;
					default:
						break;
				}
			}

		} catch (Exception e) {
			logger.error("Exception in handling message", e);
			throw new WorkerException();
		}
	}
}
