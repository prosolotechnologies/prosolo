package org.prosolo.db.migration.data.unisa;

import org.junit.Assert;
import org.junit.Test;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Nikola Milikic
 * @date 2019-06-13
 * @since 1.3.2
 */
public class V33__20190613_migrate_notification_urlsTest {

    private V33__20190613_migrate_notification_urls v33Migration = new V33__20190613_migrate_notification_urls();

    @Test
    public void testIdExtractionFromCommentNotificationUrl() {
        Pattern commentPattern = v33Migration.getCommentPattern();

        String[] groups1 = v33Migration.extractMatchedGroups(commentPattern, 2,"/manage/competences/37jgAbRr?comment=4b7QxdXK");
        Assert.assertEquals("37jgAbRr", groups1[0]);
        Assert.assertEquals("4b7QxdXK", groups1[1]);

        String[] groups2 = v33Migration.extractMatchedGroups(commentPattern, 2,"/competences/37jgAbRr?comment=9Ndg5RA8");
        Assert.assertEquals("37jgAbRr", groups2[0]);
        Assert.assertEquals("9Ndg5RA8", groups2[1]);
    }

}