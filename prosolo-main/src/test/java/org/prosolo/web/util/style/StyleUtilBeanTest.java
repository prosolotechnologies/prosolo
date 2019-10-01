package org.prosolo.web.util.style;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * @author Nikola Milikic
 * @date 2018-07-20
 * @since 1.2
 */
public class StyleUtilBeanTest {

    private StyleUtilBean styleUtilBean;

    public StyleUtilBeanTest() {
        styleUtilBean = new StyleUtilBean();
    }

    @Test
    public void getEvidenceFileTypeIcon() {
        assertEquals("evidenceDoc", styleUtilBean.getEvidenceFileTypeIcon("http://example.com/file.txt"));
        assertEquals("evidenceDoc", styleUtilBean.getEvidenceFileTypeIcon("http://example.com/file.pdf"));
        assertEquals("evidenceDoc", styleUtilBean.getEvidenceFileTypeIcon("http://example.com/file.docx"));
        assertEquals("evidenceImage", styleUtilBean.getEvidenceFileTypeIcon("http://example.com/file.JPG"));
        assertEquals("evidenceImage", styleUtilBean.getEvidenceFileTypeIcon("http://example.com/file.png"));
        assertEquals("evidenceAudio", styleUtilBean.getEvidenceFileTypeIcon("http://example.com/file.mp3"));
        assertEquals("evidenceVideoAlt", styleUtilBean.getEvidenceFileTypeIcon("http://example.com/file.mov"));
        assertEquals("evidenceArchive", styleUtilBean.getEvidenceFileTypeIcon("http://example.com/file.zip"));
        assertEquals("evidenceLink", styleUtilBean.getEvidenceFileTypeIcon("http://example.com/file.ppt"));
        assertEquals("evidenceLink", styleUtilBean.getEvidenceFileTypeIcon("http://example.com/file.bla"));
        assertEquals("evidenceLink", styleUtilBean.getEvidenceFileTypeIcon("http://example.com/file"));
    }
}