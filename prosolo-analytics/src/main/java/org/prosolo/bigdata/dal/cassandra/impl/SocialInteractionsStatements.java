package org.prosolo.bigdata.dal.cassandra.impl;

/**
 * Created by zoran on 01/04/16.
 */
public enum SocialInteractionsStatements {
    FIND_SOCIAL_INTERACTION_COUNTS,
    FIND_STUDENT_SOCIAL_INTERACTION_COUNTS,
    UPDATE_CURRENT_TIMESTAMPS,
    FIND_CURRENT_TIMESTAMPS,
    INSERT_INSIDE_CLUSTERS_INTERACTIONS,
    INSERT_OUTSIDE_CLUSTERS_INTERACTIONS,
    FIND_OUTSIDE_CLUSTER_INTERACTIONS,
    FIND_INSIDE_CLUSTER_INTERACTIONS,
    INSERT_STUDENT_CLUSTER,
    FIND_STUDENT_CLUSTER,
    UPDATE_FROMINTERACTION,
    UPDATE_TOINTERACTION,
    SELECT_INTERACTIONSBYTYPE,

    INSERT_STUDENT_INTERACTIONS_BY_PEER, INSERT_STUDENT_INTERACTIONS_BY_TYPE, SELECT_INTERACTIONSBYPEERSOVERVIEW, SELECT_INTERACTIONSBYTYPEOVERVIEW, UPDATE_SOCIALINTERACTIONCOUNT
}
