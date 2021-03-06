package org.prosolo.common.util;/**
 * Created by zoran on 27/06/16.
 */


import com.google.common.base.Charsets;
import org.apache.log4j.Logger;
import org.elasticsearch.common.io.Streams;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

/**
 * zoran 27/06/16
 */
public class ElasticsearchUtil {
    private static Logger logger = Logger.getLogger(ElasticsearchUtil.class);

    public static final String DEFAULT_SCRIPT_LANG = "painless";

    public static String copyToStringFromClasspath(String path) throws IOException {
        InputStream is = Streams.class.getResourceAsStream(path);
        if (is == null) {
            throw new FileNotFoundException("Resource [" + path + "] not found in classpath");
        }
        return Streams.copyToString(new InputStreamReader(is, Charsets.UTF_8));
    }

    private static String getOrganizationIndexSuffix(long organizationId) {
        return "_" + organizationId;
    }

    /**
     * Returns exact index name based on base index name and organization id.
     *
     * If {@code orgId} is not greater than zero IllegalArgumentException is thrown
     *
     * @param base
     * @param organizationId
     * @return
     * @throws IllegalArgumentException
     */
    public static String getOrganizationIndexName(String base, long organizationId) {
        if (organizationId <= 0) {
            logger.error("Organization id passed (" + organizationId + ") must be greater than zero");
            throw new IllegalArgumentException("organizationId must be greater than zero");
        }
        return base + getOrganizationIndexSuffix(organizationId);
    }

    /**
     *
     * @param date
     * @throws {@link NullPointerException} if date is null
     */
    public static String getDateStringRepresentation(Date date) {
        if(date == null) {
            throw new NullPointerException();
        }
        return getDateFormatter().format(date);
    }

    public static Date parseDate(String date) {
        try {
            return date == null ? null : getDateFormatter().parse(date);
        } catch (ParseException e) {
            logger.error(e);
            return null;
        }
    }

    private static DateFormat getDateFormatter() {
        DateFormat df = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        df.setTimeZone(TimeZone.getTimeZone("UTC"));
        return df;
    }

    public static String escapeSpecialChars(String query) {
        //special characters lucene uses so they need to be escaped: + - && || ! ( ) { } [ ] ^ " ~ * ? : \ /
        String specialChars = "\\+|-|&|\\||!|\\(|\\)|\\{|}|\\[|]|\\^|\"|~|\\*|\\?|:|\\\\|/";
        String escapedSearchTerm = query.replaceAll("(" + specialChars + ")", "\\\\$1");
        return escapedSearchTerm;
    }

}
