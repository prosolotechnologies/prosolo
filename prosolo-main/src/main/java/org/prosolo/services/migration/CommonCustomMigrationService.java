package org.prosolo.services.migration;

import org.prosolo.bigdata.common.exceptions.DbConnectionException;
import org.prosolo.services.event.EventQueue;

/**
 * @author stefanvuckovic
 * @date 2018-01-03
 * @since 1.2.0
 */
public interface CommonCustomMigrationService {

    void migrateAssessments();

    EventQueue migrateAssessmentsAndGetEvents();

    void migrateAssessmentDiscussions();

    void migrateCompetenceAssessmentPoints() throws DbConnectionException;
}
