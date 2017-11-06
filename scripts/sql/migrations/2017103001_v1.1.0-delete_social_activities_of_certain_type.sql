DELETE sac FROM `social_activity_config` sac
  INNER JOIN `social_activity1` sa ON sac.social_activity = sa.id AND sa.dtype = 'ActivityCompleteSocialActivity';

DELETE ann FROM `annotation1` ann
  INNER JOIN `social_activity1` sa ON ann.annotated_resource_id = sa.id AND sa.dtype = 'ActivityCompleteSocialActivity'
WHERE ann.annotated_resource = 'SocialActivity';

DELETE ann FROM `annotation1` ann
  INNER JOIN `comment1` c ON ann.annotated_resource_id = c.id AND c.resource_type = 'SocialActivity'
  INNER JOIN `social_activity1` sa ON c.commented_resource_id = sa.id AND sa.dtype = 'ActivityCompleteSocialActivity'
WHERE ann.annotated_resource = 'Comment';

DELETE c FROM `comment1` c
  INNER JOIN `social_activity1` sa ON c.commented_resource_id = sa.id AND sa.dtype = 'ActivityCompleteSocialActivity'
WHERE c.resource_type = 'SocialActivity' AND c.parent_comment IS NOT NULL;

DELETE c FROM `comment1` c
  INNER JOIN `social_activity1` sa ON c.commented_resource_id = sa.id AND sa.dtype = 'ActivityCompleteSocialActivity'
WHERE c.resource_type = 'SocialActivity';

DELETE sa FROM `social_activity1` sa WHERE sa.dtype = 'ActivityCompleteSocialActivity';
