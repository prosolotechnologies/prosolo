CREATE INDEX index_social_activity1_last_action_id ON social_activity1(last_action, id);

CREATE INDEX index_social_activity1_actor_last_action_id ON social_activity1(actor, last_action, id);

CREATE INDEX index_comment1_commented_resource_id_resource_type ON comment1(commented_resource_id, resource_type);