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
        assertEquals(styleUtilBean.getEvidenceFileTypeIcon("http://example.com/file.txt"), "evidenceText");
        assertEquals(styleUtilBean.getEvidenceFileTypeIcon("http://example.com/file.pdf"), "evidenceText");
        assertEquals(styleUtilBean.getEvidenceFileTypeIcon("http://example.com/file.docx"), "evidenceText");
        assertEquals(styleUtilBean.getEvidenceFileTypeIcon("http://example.com/file.JPG"), "evidenceImage");
        assertEquals(styleUtilBean.getEvidenceFileTypeIcon("http://example.com/file.png"), "evidenceImage");
        assertEquals(styleUtilBean.getEvidenceFileTypeIcon("http://example.com/file.mp3"), "evidenceAudio");
        assertEquals(styleUtilBean.getEvidenceFileTypeIcon("http://example.com/file.mov"), "evidenceVideoAlt");
        assertEquals(styleUtilBean.getEvidenceFileTypeIcon("http://example.com/file.zip"), "evidenceArchive");
        assertEquals(styleUtilBean.getEvidenceFileTypeIcon("http://example.com/file.ppt"), "evidenceDoc");
        assertEquals(styleUtilBean.getEvidenceFileTypeIcon("http://example.com/file.bla"), "evidenceDoc");
        assertEquals(styleUtilBean.getEvidenceFileTypeIcon("http://example.com/file"), "evidenceDoc");
    }
}