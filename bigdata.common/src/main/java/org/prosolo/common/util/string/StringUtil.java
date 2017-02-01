package org.prosolo.common.util.string;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.Jsoup;
import org.prosolo.common.domainmodel.annotation.Tag;

/**
 * @author "Nikola Milikic"
 * 
 */
public class StringUtil {

	public static String cleanHtml(String text) {
		return Jsoup.parse(text).text();
	}

	public static String splitCamelCase(String s) {
		return s.replaceAll(String.format("%s|%s|%s",
				"(?<=[A-Z])(?=[A-Z][a-z])", "(?<=[^A-Z])(?=[A-Z])",
				"(?<=[A-Za-z])(?=[^A-Za-z])"), " ");
	}

	public static Collection<String> pullLinks(String text) {
		HashSet<String> links = new HashSet<String>();
		
		String regex = "\\(?\\b(https?://|www[.])[-A-Za-z0-9+&@#/%?=~_()|!:,.;]*[-A-Za-z0-9+&@#/%=~_()|]";
		Matcher m = Pattern.compile(regex).matcher(text);
		
		while (m.find()) {
			String urlStr = m.group();
			if (urlStr.startsWith("(") && urlStr.endsWith(")")) {
				urlStr = urlStr.substring(1, urlStr.length() - 1);
			}
			links.add(urlStr);
		}
		return links;
	}
	
	public static String capitalizeFirstLetter(String input) {
		input = input.toLowerCase();
		String output = input.substring(0, 1).toUpperCase() + input.substring(1);
		return output;
	}
	
	public static long[] fromStringToLong(String arrayString) {
		if (arrayString != null && arrayString.length() > 2) {
			arrayString = arrayString.substring(1, arrayString.length() - 1);
			
			String[] idsString = arrayString.split(",");
			long[] ids = new long[idsString.length];
			
			for (int i = 0; i < idsString.length; i++) {
				ids[i] = Long.parseLong(idsString[i].trim());
			}
			return ids;
		}
		return null;
	}
	
	public static String convertToCSV(List<String> stringList) {
		String csv = "";
		for (String value : stringList) {
			csv += value + ",";
		}
		if (csv.endsWith(",")) {
			csv = csv.substring(0, csv.lastIndexOf(","));
		}
		return csv;
	}
	
	public static List<String> convertCSVToList(String csvString){
		if (csvString == null || csvString.isEmpty()) {
			return new ArrayList<String>();
		}
		
		List<String> result = Arrays.asList(csvString.split("\\s*,\\s*"));
		return result;
	}
	
	public static long[] convertToArrayOfLongs(String csvString) {
		if (csvString == null || csvString.length() < 2) {
			return new long[0];
		} else {
			String[] tokens = csvString.split("\\s*,\\s*");
			long[] array = new long[tokens.length];
			
			for (int i = 0; i < tokens.length; i++) {
				array[i] = Long.parseLong(tokens[i]);
			}
			return array;
		}
	}
	
	/**
	 * This method currently covers only space encoding. Should be revisited
	 * to cover all posible cases.
	 * @param url
	 */
	public static String encodeUrl(String url) {
		if(url != null) {
			return url.trim().replaceAll(" ", "%20");
		}
		return null;
	}
	
	public static String convertTagsToCSV(Collection<Tag> tags) {
		if(tags == null) {
			return "";
		}
		String csv = "";
		for (Tag value : tags) {
			csv += value.getTitle() + ",";
		}
		if (csv.endsWith(",")) {
			csv = csv.substring(0, csv.lastIndexOf(","));
		}
		return csv;
	}

}
