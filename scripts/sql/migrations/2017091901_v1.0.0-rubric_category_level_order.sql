ALTER TABLE `category`
  ADD COLUMN `category_order` int(11) NOT NULL DEFAULT 1;

ALTER TABLE `level`
  ADD COLUMN `level_order` int(11) NOT NULL DEFAULT 1;