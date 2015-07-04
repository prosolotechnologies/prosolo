package org.prosolo.services.email;

import java.io.FileReader;
import java.io.IOException;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheFactory;

/**
 * @author Nikola Milikic
 * @version 0.5
 *
 */
public class TestEmailGenerator {
	
	List<Item> items() {
		return Arrays.asList(new Item("Item 1"), new Item("Item 2"));
	}
	
	static class Item {
		Item (String name) {
			this.name = name;
		}
		
		String name;
	}
	
	
	private static final String templatePath = "src/test/resources/services/email/";
	
	@Test
	public void testTemplateLoading() throws IOException  {
		MustacheFactory mf = new DefaultMustacheFactory();
		Mustache mustache = mf.compile(new FileReader(templatePath + "template.html"), "template.html");
		
		StringWriter outputWriter = new StringWriter();
		mustache.execute(outputWriter, new TestEmailGenerator()).flush();
		
		Assert.assertEquals("Name: Item 1\nName: Item 2\n", outputWriter.toString());
	}
	
}
