DELETE cr.*
FROM capability_role cr
  LEFT JOIN capability ON capability.id = cr.capabilities
  LEFT JOIN role ON role.id = cr.roles
WHERE capability.name = "basic.user.access"
      AND role.title IN ("Manager", "Instructor", "Admin", "Super Admin");

DELETE cr.*
FROM capability_role cr
  LEFT JOIN capability ON capability.id = cr.capabilities
  LEFT JOIN role ON role.id = cr.roles
WHERE capability.name = "users.view"
      AND role.title IN ("Admin", "Super Admin");

DELETE FROM `capability` WHERE name = "users.view";

DELETE cr.*
FROM capability_role cr
  LEFT JOIN capability ON capability.id = cr.capabilities
  LEFT JOIN role ON role.id = cr.roles
WHERE capability.name = "roles.view"
      AND role.title IN ("Admin");

DELETE cr.*
FROM capability_role cr
  LEFT JOIN capability ON capability.id = cr.capabilities
  LEFT JOIN role ON role.id = cr.roles
WHERE capability.name = "adminDashboard.view"
      AND role.title IN ("Admin");