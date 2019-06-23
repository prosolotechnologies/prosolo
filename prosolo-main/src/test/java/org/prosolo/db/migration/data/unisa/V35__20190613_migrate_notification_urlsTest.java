package org.prosolo.db.migration.data.unisa;

import org.junit.Test;

import java.util.regex.Pattern;

import static org.junit.Assert.*;

/**
 * @author Nikola Milikic
 * @date 2019-06-13
 * @since 1.3.2
 */
public class V35__20190613_migrate_notification_urlsTest {

    private V35__20190613_migrate_notification_urls v33Migration = new V35__20190613_migrate_notification_urls();
    private Pattern commentPattern = v33Migration.getCommentPattern();
    private Pattern assessmentRequestedPattern = v33Migration.getAssessmentRequestedPattern();
    private Pattern assessmentApprovedPattern = v33Migration.getAssessmentApprovedPattern();

    @Test
    public void testCommentUrlPattern_validUrls() {
        assertTrue(commentPattern.matcher("/manage/competences/37jgAbRr?comment=4b7QxdXK").find());
        assertTrue(commentPattern.matcher("/competences/37jgAbRr?comment=9Ndg5RA8").find());
    }

    @Test
    public void testCommentUrlPattern_invalidUrls() {
        assertFalse(commentPattern.matcher("/competences/?comment=9Ndg5RA8").find());
        assertFalse(commentPattern.matcher("/competences/37jgAbRr?comment=").find());
    }

    @Test
    public void testAssessmentRequestedUrlPattern_validUrls() {
        assertTrue(assessmentRequestedPattern.matcher("/competences/4b7QxdXK/assessments/peer/odx8Azjd").find());
        assertTrue(assessmentRequestedPattern.matcher("/competences/x4Ro8m9M/assessments/peer/Gm6K5Q1d").find());
    }

    @Test
    public void testAssessmentRequestedUrlPattern_invalidUrls() {
        assertFalse(assessmentRequestedPattern.matcher("/credentials/M0RzBROr/assessments/peer/WdVYk0jm").find());
        assertFalse(assessmentRequestedPattern.matcher("/competences/M0RzBROr/assessments/peer/").find());
    }

    @Test
    public void testAssessmentApprovedUrlPattern_validUrls() {
        assertTrue(assessmentApprovedPattern.matcher("/competences/x4Ro8m9M/assessments/instructor/LmYnGz2d").find());
        assertTrue(assessmentApprovedPattern.matcher("/competences/M0RzBROr/assessments/peer/WdVYk0jm").find());
    }

    @Test
    public void testAssessmentApprovedUrlPattern_invalidUrls() {
        assertFalse(assessmentApprovedPattern.matcher("/competences/Dlmqbdz9/assessments/self/Jm4rXKD7").find());
        assertFalse(assessmentApprovedPattern.matcher("/competences/x4Ro8m9M/assessments/instructor/").find());
    }

    @Test
    public void testExtractMatchedGroups() {
        Pattern commentPattern = v33Migration.getCommentPattern();

        String[] groups1 = v33Migration.extractMatchedGroups(commentPattern, 2,"/manage/competences/37jgAbRr?comment=4b7QxdXK");
        assertEquals("37jgAbRr", groups1[0]);
        assertEquals("4b7QxdXK", groups1[1]);

        String[] groups2 = v33Migration.extractMatchedGroups(commentPattern, 2,"/competences/37jgAbRr?comment=9Ndg5RA8");
        assertEquals("37jgAbRr", groups2[0]);
        assertEquals("9Ndg5RA8", groups2[1]);
    }

    @Test (expected = IndexOutOfBoundsException.class)
    public void testExtractMatchedGroups_wrongNumberOfGroupsLess_shouldThrowException() {
        Pattern commentPattern = v33Migration.getCommentPattern();

        String[] groups1 = v33Migration.extractMatchedGroups(commentPattern, 1,"/manage/competences/37jgAbRr?comment=4b7QxdXK");
        assertEquals("37jgAbRr", groups1[0]);
        assertEquals("4b7QxdXK", groups1[1]);
    }

    @Test (expected = IndexOutOfBoundsException.class)
    public void testExtractMatchedGroups_wrongNumberOfGroupsMore_shouldThrowException() {
        Pattern commentPattern = v33Migration.getCommentPattern();

        String[] groups2 = v33Migration.extractMatchedGroups(commentPattern, 3,"/competences/37jgAbRr?comment=9Ndg5RA8");
        assertEquals("37jgAbRr", groups2[0]);
        assertEquals("9Ndg5RA8", groups2[1]);
    }

}