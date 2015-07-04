package org.prosolo.web.util;

import org.junit.Test;

 

public class TwitterUtilsTest {

  @Test
  public void parseTweetText() {
	  String test="TEST 4 - Well now I finally start learning Chinese http://t.co/5SjNvBzAST  #apple #education #learning #osx #edtech http://t.co/ZNw3lEHJKo";
	  String result=TwitterUtils.parseTweetText(test);
	  System.out.println("RESULT:"+result);

  }
}
