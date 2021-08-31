CREATE TABLE `tmp_kg` (
  `id` varchar(255) NOT NULL DEFAULT '',
  `feature` varchar(255) NOT NULL,
  `role` varchar(255) NOT NULL,
  `permissioncode` bigint(20) NOT NULL,
  `roleUserMapping` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 ;

DELIMITER $$

DROP TRIGGER IF EXISTS Before_tmp_kg_Insert;

CREATE TRIGGER Before_tmp_kg_Insert
BEFORE INSERT ON tmp_kg
FOR EACH ROW
BEGIN
  IF (EXISTS(SELECT 1 FROM tmp_kg WHERE New.id='a')) THEN
   SET NEW.id = (select UUID());
  END IF;

END$$
DELIMITER ;

call userpermissioncheck('id',(select database()),'userpermission');


