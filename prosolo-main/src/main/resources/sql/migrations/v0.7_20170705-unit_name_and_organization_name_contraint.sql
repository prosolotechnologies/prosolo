ALTER TABLE `unit`
  ADD UNIQUE KEY `UK_unit_title_organization` (title(100),organization);

ALTER TABLE `organization`
  ADD UNIQUE KEY `UK_organization_title` (title(100));