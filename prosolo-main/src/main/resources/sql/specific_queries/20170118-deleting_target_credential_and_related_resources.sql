SET @target_credential_to_delete = 65545;   

-- deleting from competence_assessment -- 
DELETE ca
FROM competence_assessment ca
INNER JOIN credential_assessment ON ca.credential_assessment = credential_assessment.id
INNER JOIN target_credential1 ON credential_assessment.target_credential = target_credential1.id
WHERE target_credential1.id = @target_credential_to_delete;


-- deleting from credential_assessment -- 
DELETE ca
FROM credential_assessment ca
WHERE ca.target_credential = @target_credential_to_delete;


-- deleting from social_activity1 --
DELETE sa 
FROM social_activity1 sa 
INNER JOIN target_activity1 ON sa.target_activity_object = target_activity1.id
INNER JOIN target_competence1 ON target_activity1.target_competence = target_competence1.id
INNER JOIN target_credential1 ON target_competence1.target_credential = @target_credential_to_delete;

-- deleting from social_activity1 --
DELETE sa 
FROM social_activity1 sa 
INNER JOIN target_competence1 ON sa.target_competence_object = target_competence1.id
INNER JOIN target_credential1 ON target_competence1.target_credential = @target_credential_to_delete;

-- deleting from social_activity1 --
DELETE sa 
FROM social_activity1 sa 
INNER JOIN target_activity1 ON sa.target_activity_object = target_activity1.id
INNER JOIN target_competence1 ON target_activity1.target_competence = target_competence1.id
INNER JOIN target_credential1 ON target_competence1.target_credential = @target_credential_to_delete;


-- deleting from target_activity1_files --
DELETE taf 
FROM target_activity1_files taf 
INNER JOIN target_activity1 ON taf.target_activity1 = target_activity1.id
INNER JOIN target_competence1 ON target_activity1.target_competence = target_competence1.id
INNER JOIN target_credential1 ON target_competence1.target_credential = @target_credential_to_delete;


-- deleting from target_activity1_links --
DELETE tal 
FROM target_activity1_links tal 
INNER JOIN target_activity1 ON tal.target_activity1 = target_activity1.id
INNER JOIN target_competence1 ON target_activity1.target_competence = target_competence1.id
INNER JOIN target_credential1 ON target_competence1.target_credential = @target_credential_to_delete;


-- deleting from target_activity1_captions --
DELETE tac 
FROM target_activity1_captions tac 
INNER JOIN target_activity1 ON tac.target_activity1 = target_activity1.id 
INNER JOIN target_competence1 ON target_activity1.target_competence = target_competence1.id 
INNER JOIN target_credential1 ON target_competence1.target_credential = @target_credential_to_delete;



-- deleting from resource_link --
DELETE rl 
FROM resource_link rl 
INNER JOIN target_activity1_files ON target_activity1_files.files = rl.id 
INNER JOIN target_activity1 ON target_activity1_files.target_activity1 = target_activity1.id 
INNER JOIN target_competence1 ON target_activity1.target_competence = target_competence1.id 
INNER JOIN target_credential1 ON target_competence1.target_credential = @target_credential_to_delete;


-- deleting from resource_link --
DELETE rl 
FROM resource_link rl 
INNER JOIN target_activity1_links ON target_activity1_links.links = rl.id
INNER JOIN target_activity1 ON target_activity1_links.target_activity1 = target_activity1.id
INNER JOIN target_competence1 ON target_activity1.target_competence = target_competence1.id
INNER JOIN target_credential1 ON target_competence1.target_credential = @target_credential_to_delete;


-- deleting from resource_link --
DELETE rl 
FROM resource_link rl 
INNER JOIN target_activity1_captions ON target_activity1_captions.captions = rl.id
INNER JOIN target_activity1 ON target_activity1_captions.target_activity1 = target_activity1.id
INNER JOIN target_competence1 ON target_activity1.target_competence = target_competence1.id
INNER JOIN target_credential1 ON target_competence1.target_credential = @target_credential_to_delete;


-- deleting from target_activity1 --
DELETE ta 
FROM target_activity1 ta 
INNER JOIN target_competence1 ON ta.target_competence = target_competence1.id
INNER JOIN target_credential1 ON target_competence1.target_credential = @target_credential_to_delete;


-- deleting from target_competence1 --
DELETE tc 
FROM target_competence1 tc
INNER JOIN target_credential1 ON tc.target_credential = @target_credential_to_delete;

-- delete from target_credential1_hashtags --
DELETE tch 
FROM target_credential1_hashtags tch 
WHERE tch.target_credential1 = @target_credential_to_delete;

-- delete from target_credential1_tags --
DELETE tct 
FROM target_credential1_tags tct 
WHERE tct.target_credential1 = @target_credential_to_delete;

-- deleting from target_credential --
DELETE tc 
FROM target_credential1 tc 
WHERE tc.id = @target_credential_to_delete;
