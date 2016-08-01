package org.prosolo.services.indexing.impl;

import java.io.IOException;

import org.apache.log4j.Logger;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.prosolo.bigdata.common.enums.ESIndexTypes;
import org.prosolo.common.domainmodel.annotation.Tag;
import org.prosolo.services.indexing.AbstractBaseEntityESServiceImpl;
import org.prosolo.common.ESIndexNames;
import org.prosolo.services.indexing.TagEntityESService;
import org.springframework.stereotype.Service;

/**
 * @author Zoran Jeremic 2013-08-21
 *
 */
@Service("org.prosolo.services.indexing.TagEntityESService")
public class TagEntityESServiceImpl extends AbstractBaseEntityESServiceImpl implements TagEntityESService {
	private static Logger logger = Logger.getLogger(TagEntityESServiceImpl.class.getName());
	 
	/* (non-Javadoc)
	 * @see org.prosolo.services.indexing.impl.AnnotationsEntityESService#saveAnnotationToES(org.prosolo.common.domainmodel.annotation.Annotation)
	 */
	@Override
	public void saveTagToES(Tag tag) {
		
		try {
			XContentBuilder builder = XContentFactory.jsonBuilder().startObject();
			builder.field("id", tag.getId());
		//	builder.field("url", annotation.getUri());
			builder.field("title", tag.getTitle());
			builder.endObject();
			indexNode(builder, String.valueOf(tag.getId()), ESIndexNames.INDEX_NODES,ESIndexTypes.TAGS);
		} catch (IOException e) {
			logger.error(e);
		}
	}
}
