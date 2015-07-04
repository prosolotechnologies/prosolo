package org.prosolo.util;

import java.util.Iterator;
import java.util.List;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.DOMBuilder;
import org.jdom2.xpath.XPath;
import org.jdom2.filter.ElementFilter;




/**
 * @author Zoran Jeremic Dec 21, 2014
 *
 */

public class XMLUtils {

	public static Document convertW3CDocument(org.w3c.dom.Document w3cDoc) {
		DOMBuilder domBuilder = new DOMBuilder();
		Document doc = domBuilder.build(w3cDoc);
		return doc;
	}

	// ---------------------------------------------------
	// Function to get a named XML child value from a parent element
	public static String getXmlChildValue(Element root, String name) {
		String value = null;
		Element child = getXmlChild(root, name);
		if (child != null) {
			value = child.getText();
		}
		return value;
	}

	// ---------------------------------------------------
	// Function to get a named XML child element from a parent element
	public static Element getXmlChild(Element root, String name) {
		Element child = null;
		List<Element> elements = null;
		if (name != null) {
			ElementFilter elementFilter = new ElementFilter(name);
			Iterator<Element> iter = (Iterator<Element>) root
					.getDescendants(elementFilter);
			if (iter.hasNext()) {
				child = iter.next();
			}
		} else {
			elements = (List<Element>) root.getChildren();
			if (elements.size() >= 1) {
				child = elements.get(0);
			}
		}
		return child;
	}
	public static Element getXMLElementByPath(Element root, String path) throws JDOMException{
		XPath xPath=XPath.newInstance(path);
		Element el=(Element) xPath.selectSingleNode(root);
		return el;
	}

}
