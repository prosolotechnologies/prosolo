package org.prosolo.services.studentProfile.observations.impl;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.hibernate.Query;
import org.prosolo.bigdata.common.exceptions.DbConnectionException;
import org.prosolo.common.domainmodel.events.EventType;
import org.prosolo.common.domainmodel.messaging.Message;
import org.prosolo.common.domainmodel.observations.Observation;
import org.prosolo.common.domainmodel.observations.Suggestion;
import org.prosolo.common.domainmodel.observations.Symptom;
import org.prosolo.common.domainmodel.user.User;
import org.prosolo.common.event.context.data.UserContextData;
import org.prosolo.services.data.Result;
import org.prosolo.services.event.EventFactory;
import org.prosolo.services.general.impl.AbstractManagerImpl;
import org.prosolo.services.interaction.MessagingManager;
import org.prosolo.services.studentProfile.observations.ObservationManager;
import org.prosolo.web.messaging.data.MessageData;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import java.util.*;

@Service("org.prosolo.services.studentProfile.observations.ObservationManager")
public class ObservationManagerImpl extends AbstractManagerImpl implements ObservationManager {

    private static final long serialVersionUID = -7710666335405883922L;

    private static Logger logger = Logger.getLogger(ObservationManagerImpl.class);

    @Inject
    private MessagingManager msgManager;
    @Inject
    private EventFactory eventFactory;
    @Inject
    private ObservationManager self;

    @Override
    @Transactional(readOnly = true)
    public Observation getLastObservationForUser(long userId) throws DbConnectionException {
        try {
            String queryString =
                    "SELECT o " +
                    "FROM Observation o " +
                    "INNER JOIN fetch o.createdFor student " +
                    "INNER JOIN fetch o.createdBy user " +
                    "LEFT JOIN fetch o.symptoms sy " +
                    "LEFT JOIN fetch o.suggestions su " +
                    "WHERE student.id = :id " +
                    "ORDER BY o.creationDate desc";

            Query query = persistence.currentManager().createQuery(queryString)
                    .setLong("id", userId)
                    .setMaxResults(1);

            return (Observation) query.uniqueResult();
        } catch (Exception e) {
            throw new DbConnectionException("Observation cannot be loaded at the moment");
        }
    }

    @Override
    public void saveObservation(long id, Date date, String message, String note, List<Long> symptomIds,
                                List<Long> suggestionIds, UserContextData context, long studentId)
            throws DbConnectionException {

        Result<Void> result = self.saveObservationAndGetEvents(id, date, message, note, symptomIds,
                suggestionIds, context, studentId);

        eventFactory.generateEvents(result.getEventQueue());
    }

    @Override
    @Transactional
    public Result<Void> saveObservationAndGetEvents(long id, Date date, String messageText, String note,
                                                    List<Long> symptomIds, List<Long> suggestionIds,
                                                    UserContextData context, long studentId)
            throws DbConnectionException {
        try {
            boolean insert = true;
            Observation observation = new Observation();

            if (id > 0) {
                insert = false;
                observation.setId(id);
                observation.setEdited(true);
            }
            observation.setCreationDate(date);
            observation.setMessage(messageText);
            observation.setNote(note);
            User creator = new User();
            creator.setId(context.getActorId());
            User student = new User();
            student.setId(studentId);
            observation.setCreatedBy(creator);
            observation.setCreatedFor(student);

            Set<Symptom> symptoms = new HashSet<>();
            for (long sid : symptomIds) {
                Symptom s = new Symptom();
                s.setId(sid);
                symptoms.add(s);
            }
            observation.setSymptoms(symptoms);

            Set<Suggestion> suggestions = new HashSet<>();
            for (long sid : suggestionIds) {
                Suggestion s = new Suggestion();
                s.setId(sid);
                suggestions.add(s);
            }
            observation.setSuggestions(suggestions);

            observation = saveEntity(observation);
            persistence.currentManager().evict(observation);

            Result<Void> res = new Result<>();

            if (insert && !StringUtils.isBlank(messageText) && context.getActorId() != studentId) {
                res.appendEvents(msgManager.sendMessageAndGetEvents(0, context.getActorId(), studentId, messageText, context).getEventQueue());
            }

            return res;
        } catch (DbConnectionException dbce) {
            dbce.printStackTrace();
            throw dbce;
        } catch (Exception e) {
            e.printStackTrace();
            throw new DbConnectionException("Error saving observation");
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    @Transactional(readOnly = true)
    public List<Observation> getObservations(long userId) throws DbConnectionException {
        try {
            String queryString =
                    "SELECT DISTINCT o " +
                            "FROM Observation o " +
                            "INNER JOIN FETCH o.createdFor student " +
                            "INNER JOIN FETCH o.createdBy user " +
                            "LEFT JOIN FETCH o.symptoms sy " +
                            "LEFT JOIN FETCH o.suggestions su " +
                            //"LEFT JOIN o.targetCredential targetCred " +
                            "WHERE student.id = :id " +
                            //"AND targetCred.id = :targetCredentialId " +
                            "ORDER BY o.creationDate desc";

            Query query = persistence.currentManager().createQuery(queryString);
            query.setLong("id", userId);
            //query.setLong("targetCredentialId", targetCredentialId);

            return query.list();
        } catch (Exception e) {
            e.printStackTrace();
            logger.error(e);
            throw new DbConnectionException("Observations cannot be loaded at the moment");
        }
    }
}
