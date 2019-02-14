package org.prosolo.services.indexing.impl;

import org.apache.log4j.Logger;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.prosolo.bigdata.common.enums.ESIndexTypes;
import org.prosolo.common.ESIndexNames;
import org.prosolo.common.domainmodel.user.UserGroup;
import org.prosolo.common.elasticsearch.impl.AbstractESIndexerImpl;
import org.prosolo.common.util.ElasticsearchUtil;
import org.prosolo.services.indexing.UserGroupESService;
import org.prosolo.services.user.UserGroupManager;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import java.io.IOException;

@Service("org.prosolo.services.indexing.UserGroupESService")
public class UserGroupESServiceImpl extends AbstractESIndexerImpl implements UserGroupESService {
	
	private static Logger logger = Logger.getLogger(UserGroupESServiceImpl.class);
	
	@Inject private UserGroupManager userGroupManager;
	
	@Override
	@Transactional
	public void saveUserGroup(long orgId, UserGroup group) {
	 	try {
			XContentBuilder builder = XContentFactory.jsonBuilder().startObject();
			builder.field("id", group.getId());
			builder.field("unit", group.getUnit().getId());
			builder.field("name", group.getName());
			builder.endObject();
			System.out.println("JSON: " + builder.prettyPrint().string());
			String fullIndexName = ElasticsearchUtil.getOrganizationIndexName(ESIndexNames.INDEX_USER_GROUP, orgId);
			indexNode(builder, String.valueOf(group.getId()), fullIndexName,
					ESIndexTypes.USER_GROUP);
		} catch (IOException e) {
			logger.error("Error", e);
		}
	}

	@Override
	public void deleteUserGroup(long orgId, long groupId) {
		try {
			String fullIndexName = ElasticsearchUtil.getOrganizationIndexName(ESIndexNames.INDEX_USER_GROUP, orgId);
			delete(groupId + "", fullIndexName, ESIndexTypes.USER_GROUP);
		} catch (Exception e) {
			logger.error("Error", e);
		}
	}

}
