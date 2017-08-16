package org.prosolo.util;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.codec.binary.Hex;

public class
StringUtils {
	
	private static String indent = "";

	public static String toStringByReflection(final Object instance) {
		indent += "\t";

		StringBuffer buffer = new StringBuffer(instance.getClass() + ":\r\n");

		Class<?> factory = instance.getClass();

		Field[] fields = factory.getFields();
		for (Field field : fields) {
			try {
				field.setAccessible(true);

				String name = field.getName();
				Object value = field.get(instance);

				buffer.append(indent + name + " = " + value + "\r\n");
			} catch (Exception e) { }
		}
		indent = indent.substring(0, indent.length() - 1);

		return buffer.toString();
	}

	public static double roundThreeDecimals(double d) {
    	int ix = (int)(d * 1000.0); // scale it
    	double dbl2 = ((double)ix)/1000.0;
       	return dbl2;
	}

	public static String camelCase(String text) {
		if (text.length() == 0 || !text.contains("_")) {
			return text.toLowerCase();
		} else {
			String[] words = text.split("_");
			
			StringBuffer toReturn = new StringBuffer();
			
			for (int i = 0; i < words.length; i++) {
				if (i == 0) {
					toReturn.append(words[i].toLowerCase());
				} else {
					toReturn.append(capitalizeWord(words[i]));
				}
			}
			
			return toReturn.toString();
		}
	}
	
	public static String capitalizeWord(String name) {
		return name.substring(0, 1).toUpperCase() + name.substring(1).toLowerCase();
	}
	
	public static String getHashValue(String string) {
		MessageDigest messageDigest;
		try {
			messageDigest = MessageDigest.getInstance("MD5");
			messageDigest.reset();
			messageDigest.update(string.getBytes(Charset.forName("UTF8")));
			final byte[] resultByte = messageDigest.digest();
			return new String(Hex.encodeHex(resultByte));
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		return null;
	}

	public static String getUTF(String inputString) {
		String utf8tweet = "";

		try {
			byte[] utf8Bytes = inputString.getBytes("UTF-8");
			utf8tweet = new String(utf8Bytes, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		Pattern unicodeOutliers = Pattern.compile("[^\\x00-\\x7F]",
				Pattern.UNICODE_CASE | Pattern.CANON_EQ
						| Pattern.CASE_INSENSITIVE);
		
		Matcher unicodeOutlierMatcher = unicodeOutliers.matcher(utf8tweet);
		utf8tweet = unicodeOutlierMatcher.replaceAll(" ");
		return utf8tweet;
	}
	
	public static String toCSV(List<Long> idList) {
		StringBuffer buffer = new StringBuffer();
		
		if (idList != null & !idList.isEmpty()) {
			for (int i = 0; i < idList.size(); i++) {
				buffer.append(idList.get(i));
				
				if (i < idList.size()) {
					buffer.append(",");
				}
			}
		}
		String commaSeparatedValues=buffer.toString();
		if (commaSeparatedValues.endsWith(",")) {
            commaSeparatedValues = commaSeparatedValues.substring(0,
                    commaSeparatedValues.lastIndexOf(","));
        }
		return commaSeparatedValues;
	}
	
	public static List<Long> fromCSV(String csvIds) {
		List<Long> ids = new ArrayList<Long>();
		
		if (csvIds != null && csvIds.length() > 0) {
			String[] split = csvIds.split(",");
			
			for (int i = 0; i < split.length; i++) {
				ids.add(Long.valueOf(split[i]));
			}
		}
		return ids;
	}
	
	public static String longToString(Long num) {
		return String.valueOf(num);
	}
	
}
