package org.prosolo.bigdata.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.Test;

/**
@author Zoran Jeremic Apr 14, 2015
 *
 */

public class TestStringPatterns {
	@Test
	public void testInitializeStreaming() {
		String str = "UPDATE activityinteraction  SET count=count+1 WHERE competenceid=? AND activityid=?;";
		//Pattern pattern = Pattern.compile("'\\s(.*?)=\\?");
		String whereclause=str.substring(str.lastIndexOf("WHERE")+6,str.lastIndexOf(";"));
		whereclause=whereclause.replaceAll("AND ", "");
		whereclause=whereclause.replaceAll("=\\?", "");
		System.out.println("WHERE:"+whereclause);
		String[] words = whereclause.split("\\s+");
		for(int i=0;i<words.length;i++){
			System.out.println("Param:"+words[i]);
		}
		Pattern pattern = Pattern.compile("\\?");
		Matcher matcher = pattern.matcher(str);
		System.out.println("checking:"+str);
		if (matcher.find()) {
			System.out.println("FOUND:"+matcher.group());
			
		    //System.out.println(matcher.group(1));
		}
	}

}

