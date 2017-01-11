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
			builder.startArray("credentials");
			List<CredentialUserGroup> credGroups = userGroupManager.getCredentialUserGroups(group.getId());
			for(CredentialUserGroup g : credGroups) {
				builder.startObject();
				builder.field("id", g.getCredential().getId());
				builder.endObject();
			}
			builder.endArray();
			builder.startArray("competences");
			List<CompetenceUserGroup> compGroups = userGroupManager.getCompetenceUserGroups(group.getId());
			for(CompetenceUserGroup g : compGroups) {
				builder.startObject();
				builder.field("id", g.getCompetence().getId());
				builder.endObject();
			}
			builder.endArray();
			builder.endObject();
			System.out.println("JSON: " + builder.prettyPrint().string());
			indexNode(builder, String.valueOf(group.getId()), ESIndexNames.INDEX_USER_GROUP, 
					ESIndexTypes.USER_GROUP);
		} catch (IOException e) {
			logger.error(e);
			e.printStackTrace();
		}
	}

	@Override
	public void addCredential(long groupId, long credId) {
		String script = "if (ctx._source[\"credentials\"] == null) { " +
				"ctx._source.credentials = cred " +
				"} else { " +
				"ctx._source.credentials += cred " +
				"}";
		updateCredentials(groupId, credId, script);
	}
	
	@Override
	public void removeCredential(long groupId, long credId) {
		String script = "ctx._source.credentials -= cred";
		updateCredentials(groupId, credId, script);
	}
	
	private void updateCredentials(long groupId, long credId, String script) {
		try {
			Map<String, Object> params = new HashMap<>();
			Map<String, Object> param = new HashMap<>();
			param.put("id", credId + "");
			params.put("cred", param);
			
			partialUpdateByScript(ESIndexNames.INDEX_USER_GROUP, ESIndexTypes.USER_GROUP, 
					groupId + "", script, params);
		} catch(Exception e) {
			logger.error(e);
			e.printStackTrace();
		}
	}
	
	@Override
	public void addCompetence(long groupId, long compId) {
		String script = "if (ctx._source[\"competences\"] == null) { " +
				"ctx._source.competences = comp " +
				"} else { " +
				"ctx._source.competences += comp " +
				"}";
		updateCompetences(groupId, compId, script);
	}
	
	@Override
	public void removeCompetence(long groupId, long compId) {
		String script = "ctx._source.competences -= comp";
		updateCompetences(groupId, compId, script);
	}
	
	private void updateCompetences(long groupId, long compId, String script) {
		try {
			Map<String, Object> params = new HashMap<>();
			Map<String, Object> param = new HashMap<>();
			param.put("id", compId + "");
			params.put("comp", param);
			
			partialUpdateByScript(ESIndexNames.INDEX_USER_GROUP, ESIndexTypes.USER_GROUP, 
					groupId + "", script, params);
		} catch(Exception e) {
			logger.error(e);
			e.printStackTrace();
		}
	}
}
