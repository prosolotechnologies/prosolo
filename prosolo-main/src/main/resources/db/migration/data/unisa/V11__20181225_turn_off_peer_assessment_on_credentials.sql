UPDATE credential_assessment_config c
SET c.enabled = 'F', c.blind_assessment_mode = 'OFF' WHERE c.assessment_type = 'PEER_ASSESSMENT';