package org.prosolo.common.email.generators;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;

import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheFactory;

/**
 * @author Nikola Milikic
 * @version 0.5
 *
 */
public class MoustacheUtil {
	
	public static String compileTemplate(String templateFile, String templateName, Object data) throws IOException {
		MustacheFactory mf = new DefaultMustacheFactory();
		InputStream is = Thread
				.currentThread()
				.getContextClassLoader()
				.getResourceAsStream(
						templateFile);
		BufferedReader ir = new BufferedReader(new InputStreamReader(is));
		Mustache mustache = mf.compile(ir, templateName);
		
		StringWriter outputWriter = new StringWriter();
		mustache.execute(outputWriter, data).flush();
		return outputWriter.toString();
	}
}
