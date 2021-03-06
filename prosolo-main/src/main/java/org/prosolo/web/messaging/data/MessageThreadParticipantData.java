package org.prosolo.web.messaging.data;

import org.prosolo.common.domainmodel.messaging.ThreadParticipant;
import org.prosolo.common.domainmodel.user.User;
import org.prosolo.common.util.ImageFormat;
import org.prosolo.services.user.data.UserData;
import org.prosolo.web.util.AvatarUtils;

/**
 * @author Nikola Milikic
 * @date 2018-01-12
 * @since 1.2
 */
public class MessageThreadParticipantData extends UserData {


    private static final long serialVersionUID = 1645000395726643265L;

    private long participantId;
    private boolean deleted;

    public MessageThreadParticipantData(ThreadParticipant participant) {
        User user = participant.getUser();

        setId(user.getId());
        setName(user.getName());
        setLastName(user.getLastname());
        setName(user.getName());
        setFullName(user.getFullName());
        setAvatarUrl(AvatarUtils.getAvatarUrlInFormat(user.getAvatarUrl(), ImageFormat.size120x120));
        this.participantId = participant.getId();
        this.deleted = participant.isDeleted();
    }

    public long getParticipantId() {
        return participantId;
    }

    public void setParticipantId(long participantId) {
        this.participantId = participantId;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }
}
