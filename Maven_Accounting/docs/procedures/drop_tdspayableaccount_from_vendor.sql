DROP PROCEDURE IF EXISTS dropforeignkeyconstraintsfromcolumnname;

DELIMITER &&
CREATE PROCEDURE dropforeignkeyconstraintsfromcolumnname(IN tablename varchar(100), IN columname varchar(100))
   BEGIN
	SET @s:= concat('ALTER TABLE ', tablename);
	SELECT @s:=concat(@s, ' DROP FOREIGN KEY ',CONSTRAINT_NAME, ',')
	FROM information_schema.key_column_usage
	WHERE TABLE_SCHEMA = DATABASE()
          AND TABLE_NAME = tablename
	  AND COLUMN_NAME = columname;
        SELECT @s:=concat(LEFT(@s, LENGTH(@s) - 1), ';');
	PREPARE stmt FROM @s;
	EXECUTE stmt;
	DEALLOCATE PREPARE stmt;
   END &&
DELIMITER ;

CALL dropforeignkeyconstraintsfromcolumnname('vendor','tdspayableaccount');

DROP PROCEDURE IF EXISTS dropforeignkeyconstraintsfromcolumnname;

ALTER TABLE vendor DROP COLUMN tdspayableaccount;
