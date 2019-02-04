UPDATE credential_assessment c
SET c.status = 'SUBMITTED' WHERE c.approved IS TRUE;

UPDATE credential_assessment c
SET c.status = 'PENDING' WHERE c.approved IS NOT TRUE;

UPDATE competence_assessment c
SET c.status = 'SUBMITTED' WHERE c.approved IS TRUE;

UPDATE competence_assessment c
SET c.status = 'PENDING' WHERE c.approved IS NOT TRUE;