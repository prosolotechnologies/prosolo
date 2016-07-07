/**
 * 
 */
package org.prosolo.app.bc;

import org.apache.log4j.Logger;

public abstract class BusinessCase {

	public static final int BLANK = 0;
	public static final int DL_TEST = 1;
	public static final int AU_TEST = 2;
	public static final int STATISTICS = 3;
	public static final int EDX = 4;

	protected static Logger logger = Logger.getLogger(BusinessCase.class
			.getName());

}
