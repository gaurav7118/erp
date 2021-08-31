DELIMITER $$

DROP TRIGGER IF EXISTS Before_userpermission_Insert;

CREATE TRIGGER Before_userpermission_Insert
BEFORE INSERT ON userpermission
FOR EACH ROW
BEGIN
  IF (EXISTS(SELECT 1 FROM userpermission WHERE (roleUserMapping = NEW.roleUserMapping or (roleUserMapping is null and New.roleUserMapping is null)) and feature=NEW.feature and role=New.role and permissioncode = NEW.permissioncode )) THEN
    SIGNAL SQLSTATE VALUE '45000' SET MESSAGE_TEXT = 'INSERT failed as the record already exists';
  END IF;
END$$
DELIMITER ;