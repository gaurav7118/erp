DROP PROCEDURE IF EXISTS droplike;
CREATE TABLE IF NOT EXISTS IL_DUMMY (column1 INT(11));
delimiter //
CREATE PROCEDURE droplike(IN pattern varchar(100), IN databasename varchar(100))
BEGIN
  DECLARE fetchLimit INT DEFAULT 200;
  set group_concat_max_len = 6553555;
  select @drop_txt:= concat( 'drop table IF EXISTS ', group_concat('`',table_name, '`') , ';' ) from (select table_name from information_schema.tables where TABLE_SCHEMA = databasename and table_name like pattern limit fetchLimit ) as ttt ;
  prepare stmt from @drop_txt;
  execute stmt;
  DEALLOCATE PREPARE stmt;
END //
delimiter ;