package org.prosolo.bigdata.twitter;
/**
@author Zoran Jeremic Jun 20, 2015
 *
 */
import java.util.ArrayList;
import java.util.List;

import org.apache.spark.api.java.function.Function;
 

import twitter4j.Status;

public class LoadTwitterFunction implements Function<Status, List<Object>> {
	private static final long serialVersionUID = 1L;
	private List<TwitterParameter> twitterParameters;

	public LoadTwitterFunction(List<TwitterParameter> twitterParameters) {
		this.twitterParameters = twitterParameters;
	}

	public List<Object> call(Status status) throws Exception {
		List<Object> list = new ArrayList<Object>();
		for (TwitterParameter parameter : twitterParameters) {
			System.out.println("Load twitter function:"+status.getText());
			list.add(TwitterUtil.parse(parameter, status));
		}

		return list;
	}
}

