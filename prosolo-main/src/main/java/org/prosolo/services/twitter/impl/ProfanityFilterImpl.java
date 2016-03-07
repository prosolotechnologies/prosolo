package org.prosolo.services.twitter.impl;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.prosolo.services.twitter.ProfanityFilter;
import org.springframework.stereotype.Service;

/**
 * @author Zoran Jeremic Sep 17, 2014
 *
 */
//@Service("org.prosolo.services.twitter.ProfanityFilter")
	@Deprecated
public class ProfanityFilterImpl implements ProfanityFilter {

	private static Logger logger = Logger.getLogger(ProfanityFilter.class);

	private Pattern pattern = null;
	private Matcher matcher = null;

	public ProfanityFilterImpl() {
		// String r= "(boogers|snot|poop|shucks|argh)";
		String regex = "\\b(?:";
		URL badwordsPath = Thread.currentThread().getContextClassLoader()
				.getResource("org/prosolo/files/badwords.sav");

		File badwordsFile = new File(badwordsPath.getPath());
		try {
			BufferedReader br = new BufferedReader(new FileReader(badwordsFile));
			String line;
			try {
				line = br.readLine();
				if (line != null) {
					regex = regex + line;
				}
				while ((line = br.readLine()) != null) {
					regex = regex + "|" + line;
				}
				regex = regex + ")\\b";
				br.close();
			} catch (IOException e) {
				logger.error(e);
			}

		} catch (FileNotFoundException e2) {
			logger.error(e2);
		}
		try {
			badwordsFile = new File(badwordsPath.toURI());
		} catch (URISyntaxException e1) {
			logger.error(e1);
		}
		pattern = Pattern.compile(regex);
	}

	@Override
	public boolean detectProfanity(String content) {
		if (matcher == null) {
			matcher = pattern.matcher(content);
		} else {
			matcher.reset(content);
		}
		if (matcher.find()) {
			return true;
		} else {
			return false;
		}

	}
}
