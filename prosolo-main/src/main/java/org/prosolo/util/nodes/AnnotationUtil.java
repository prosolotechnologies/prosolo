package org.prosolo.util.nodes;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.prosolo.common.domainmodel.annotation.Tag;
import org.prosolo.services.nodes.data.TagCountData;
import org.prosolo.util.nodes.CreatedAscComparator;

public class AnnotationUtil {

	public static String getAnnotationsAsSortedCSV(Collection<Tag> annotations) {
		if (annotations != null && !annotations.isEmpty()) {
			List<Tag> sortedAnnotations = new ArrayList<Tag>(annotations);
			Collections.sort(sortedAnnotations, new CreatedAscComparator());

			return getCSVString(sortedAnnotations, ",");
		}
		return "";
	}

	public static String getAnnotationsAsSortedCSVForTagCountData(Collection<TagCountData> tags) {
		if (tags != null && !tags.isEmpty()) {
			List<TagCountData> sortedAnnotationsForTagCountData = new ArrayList<TagCountData>(tags);
			Collections.sort(sortedAnnotationsForTagCountData, new CreatedAscComparator());
			return getCSVStringForTagCountData(sortedAnnotationsForTagCountData, ",");
		}
		return "";
	}

	public static String getCSVString(Collection<Tag> tags, String separator) {
		StringBuffer sb = new StringBuffer();
		// for (int i = 0; i < tags.size(); i++) {
		int i = 0;
		for (Tag tag : tags) {
			sb.append(tag.getTitle());
			// sb.append(tags.get(i).getTitle());
			if (!(i == tags.size() - 1)) {
				sb.append(separator);
			}
			i++;

		}

		return sb.toString();
	}

	public static String getCSVStringForTagCountData(Collection<TagCountData> tags, String separator) {
		StringBuffer sb = new StringBuffer();
		for (TagCountData tag : tags) {
			sb.append(tag.getTitle());
			sb.append(separator);
		}

		return sb.toString();
	}

	public static List<String> getTrimmedSplitStrings(String csvString) {
		String[] splittedStrings = csvString.split(",");

		List<String> stringList = new ArrayList<String>();

		if (splittedStrings != null && splittedStrings.length > 0) {
			for (int i = 0; i < splittedStrings.length; i++) {
				String s = splittedStrings[i];

				if (s != null && s.length() > 0)
					stringList.add(s.trim());
			}
		}

		return stringList;
	}

	public static String getCSVString(Collection<String> splitStrings) {
		StringBuffer sb = new StringBuffer();

		if (splitStrings != null && !splitStrings.isEmpty()) {

			for (int i = 0; i < splitStrings.size(); i++) {
				sb.append(((List<String>) splitStrings).get(i));

				if (!(i == splitStrings.size() - 1)) {
					sb.append(",");
				}
			}
		}
		return sb.toString();
	}

	public static List<String> getAsListOfTitles(Collection<Tag> annotations) {
		if (annotations != null) {
			List<String> stringList = new LinkedList<String>();

			for (Tag ann : annotations) {
				String annTitle = ann.getTitle();
				if (annTitle != null && annTitle.length() > 0) {
					stringList.add(ann.getTitle());
				}
			}
			return stringList;
		} else {
			return new ArrayList<String>();
		}
	}

}
