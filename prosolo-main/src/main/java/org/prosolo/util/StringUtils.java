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

public class StringUtils {
	
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

	public static String shortenAndAddCommas(String str) {
		if (str != null && str.length() > 100) {
			return str.substring(0, 101) + "...";
		}
		return str;
	}

}
