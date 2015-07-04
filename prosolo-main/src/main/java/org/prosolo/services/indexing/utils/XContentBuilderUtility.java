package org.prosolo.services.indexing.utils;

import static org.elasticsearch.common.xcontent.XContentFactory.jsonBuilder;

import java.io.IOException;

import org.elasticsearch.common.xcontent.XContentBuilder;

/**
 * @author Zoran Jeremic 2013-06-12
 */
public class XContentBuilderUtility {
	public static XContentBuilder getFileXContentBuilderMap() throws IOException{
		 String idxType = "attachment";
		XContentBuilder map = jsonBuilder().startObject()
	            .startObject(idxType)
	              .startObject("properties")
	                .startObject("file")
	                  .field("type", "attachment")
	                  .startObject("fields")
	                    .startObject("title")
	                      .field("store", "yes")
	                    .endObject()
	                    .startObject("file")
	                      .field("term_vector","with_positions_offsets")
	                      .field("store","yes")
	                    .endObject()
	                  .endObject()
	                .endObject()
	              .endObject()
	         .endObject();
		
		return map;
	}

}
