INSERT INTO `capability`(`id`, `description`, `name`)
VALUES (
  (SELECT c1.id + 1
   FROM capability c1
   WHERE 1
   ORDER BY c1.id DESC
   LIMIT 1),
  "View rubrics library in manage section","manage.rubrics.view");

INSERT INTO `capability_role`(`capabilities`, `roles`)
VALUES (
  (SELECT capability.id
   FROM capability
   WHERE capability.name = "manage.rubrics.view"),
  (SELECT role.id
   FROM role
   WHERE role.title = "Manager"));

INSERT INTO `capability_role`(`capabilities`, `roles`)
VALUES (
  (SELECT capability.id
   FROM capability
   WHERE capability.name = "manage.rubrics.view"),
  (SELECT role.id
   FROM role
   WHERE role.title = "Instructor"));

INSERT INTO `capability`(`id`, `description`, `name`)
VALUES (
  (SELECT c1.id + 1
   FROM capability c1
   WHERE 1
   ORDER BY c1.id DESC
   LIMIT 1),
  "Set advanced options in the admin section that affect the whole application","admin.advanced");

INSERT INTO `capability_role`(`capabilities`, `roles`)
VALUES (
  (SELECT capability.id
   FROM capability
   WHERE capability.name = "admin.advanced"),
  (SELECT role.id
   FROM role
   WHERE role.title = "Super Admin"));