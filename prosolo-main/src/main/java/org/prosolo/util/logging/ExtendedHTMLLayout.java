/**
 * 
 */
package org.prosolo.util.logging;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.HTMLLayout;
import org.apache.log4j.spi.LoggingEvent;

/**
 * @author "Nikola Milikic"
 * 
 */
public class ExtendedHTMLLayout extends HTMLLayout {
	// RegEx pattern looks for <tr> <td> nnn...nnn </td> (all whitespace ignored)

	private static final String rxTimestamp = "\\s*<\\s*tr\\s*>\\s*<\\s*td\\s*>\\s*(\\d*)\\s*<\\s*/td\\s*>";

	private String timestampFormat = "yyyy-MM-dd-HH:mm:ss.SZ";
	private SimpleDateFormat sdf = new SimpleDateFormat(timestampFormat);

	public ExtendedHTMLLayout() {
		super();
	}

	/** Override HTMLLayout's format() method */
	public String format(LoggingEvent event) {
		String record = super.format(event); // Get the log record in the default HTMLLayout format.

		Pattern pattern = Pattern.compile(rxTimestamp); // RegEx to find the default timestamp
		Matcher matcher = pattern.matcher(record);

		// If default timestamp cannot be found,
		if (!matcher.find()) {
			return record; // Just return the unmodified log record.
		}

		StringBuffer buffer = new StringBuffer(record);

		// Replace the default timestamp with one formatted as desired.
		buffer.replace(matcher.start(1), 
				matcher.end(1), 
				sdf.format(new Date(event.timeStamp)));

		// Return the log record with the desired timestamp format.
		return buffer.toString(); 
	}

	/**
	 * Setter for timestamp format. Called if
	 * log4j.appender.<category>.layout.TimestampFormat property is specfied
	 */
	public void setTimestampFormat(String format) {
		this.timestampFormat = format;
		// Use the format specified by the TimestampFormat property
		this.sdf = new SimpleDateFormat(format); 
	}

	/** Getter for timestamp format being used. */
	public String getTimestampFormat() {
		return this.timestampFormat;
	}
}
