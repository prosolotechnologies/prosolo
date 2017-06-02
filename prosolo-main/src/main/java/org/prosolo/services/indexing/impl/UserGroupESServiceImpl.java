package org.prosolo.services.indexing.impl;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.apache.log4j.Logger;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.prosolo.bigdata.common.enums.ESIndexTypes;
import org.prosolo.common.ESIndexNames;
import org.prosolo.common.domainmodel.credential.CompetenceUserGroup;
import org.prosolo.common.domainmodel.credential.CredentialUserGroup;
import org.prosolo.common.domainmodel.user.UserGroup;
import org.prosolo.services.indexing.AbstractBaseEntityESServiceImpl;
import org.prosolo.services.indexing.UserGroupESService;
import org.prosolo.services.nodes.UserGroupManager;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service("org.prosolo.services.indexing.UserGroupESService")
public class UserGroupESServiceImpl extends AbstractBaseEntityESServiceImpl implements UserGroupESService {
	
	private static Logger logger = Logger.getLogger(UserGroupESServiceImpl.class);
	
	@Inject private UserGroupManager userGroupManager;
	
	@Override
	@Transactional
	public void saveUserGroup(UserGroup group) {
	 	try {
			XContentBuilder builder = XContentFactory.jsonBuilder().startObject();
			builder.field("id", group.getId());
			builder.field("name", group.getName());
			builder.endObject();
			System.out.println("JSON: " + builder.prettyPrint().string());
			indexNode(builder, String.valueOf(group.getId()), ESIndexNames.INDEX_USER_GROUP, 
					ESIndexTypes.USER_GROUP);
		} catch (IOException e) {
			logger.error(e);
			e.printStackTrace();
		}
	}

}
