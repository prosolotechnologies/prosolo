package org.prosolo.services.messaging.impl;

import java.util.Map;

import org.apache.log4j.Logger;
import org.prosolo.common.messaging.MessageWrapperAdapter;
import org.prosolo.common.messaging.data.MessageWrapper;
import org.prosolo.common.messaging.data.ServiceType;
import org.prosolo.common.messaging.data.SessionMessage;
import org.prosolo.common.messaging.rabbitmq.QueueNames;
import org.prosolo.common.messaging.rabbitmq.ReliableProducer;
import org.prosolo.common.messaging.rabbitmq.impl.ReliableProducerImpl;
import org.prosolo.services.messaging.SessionMessageDistributer;
import org.prosolo.web.ApplicationBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.gson.GsonBuilder;

/**
 * @author Zoran Jeremic Sep 9, 2014
 */
@Service("org.prosolo.services.messaging.SessionMessageDistributer")
public class SessionMessageDistributerImpl implements SessionMessageDistributer {
    private static Logger logger = Logger.getLogger(SystemMessageDistributerImpl.class.getName());

    private ReliableProducer reliableProducer;
    @Autowired
    private ApplicationBean applicationBean;

    private GsonBuilder gson;

    public SessionMessageDistributerImpl() {
        gson = new GsonBuilder();
        gson.registerTypeAdapter(MessageWrapper.class, new MessageWrapperAdapter());

    }

    @Override
    public void distributeMessage(ServiceType serviceType, long receiverId, long resourceId,
                                  String resourceType, Map<String, String> parameters) {
        if (reliableProducer == null) {
            reliableProducer = new ReliableProducerImpl();
            reliableProducer.setQueue(QueueNames.SESSION.name().toLowerCase());
            reliableProducer.init();
        }
        SessionMessage message = new SessionMessage();
        message.setReceiverId(receiverId);
        message.setResourceId(resourceId);
        message.setServiceType(serviceType);
        message.setResourceType(resourceType);
        message.setParameters(parameters);
        wrapMessageAndSend(message);
    }

    @Override
    public void wrapMessageAndSend(SessionMessage message) {
        MessageWrapper wrapper = new MessageWrapper();
        wrapper.setSender(applicationBean.getServerIp());
        wrapper.setMessage(message);
        wrapper.setTimecreated(System.currentTimeMillis());
        String msg = gson.create().toJson(wrapper);

        if (message.getServiceType().equals(ServiceType.UPDATE_USER_SOCIAL_ACTIVITY_INBOX)) {
            logger.debug("Sending session message:" + msg);
        }

        reliableProducer.send(msg);
    }
}
