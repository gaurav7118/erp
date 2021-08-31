

-- Author Rahul A. Bhawar

-- Funstion to get destination table columns matched with sources table columns
DROP FUNCTION  IF EXISTS  getTableColumns;
delimiter //
CREATE FUNCTION getTableColumns (fromDB VARCHAR(512) ,toDB VARCHAR(512) , stb VARCHAR(512) , dtb VARCHAR(512))  RETURNS TEXT
DETERMINISTIC
BEGIN
  DECLARE var_Columns TEXT;
  SET var_Columns= "";
-- SELECT GROUP_CONCAT(t11.column_name) INTO  var_Columns FROM  (SELECT  column_name  FROM information_schema.columns WHERE table_schema=fromDB  AND table_name=stb  )  t11
-- INNER JOIN  (SELECT column_name FROM information_schema.columns  WHERE table_schema=toDB  AND table_name=dtb )  t22 ON t11.column_name = t22.column_name;
 SET @@group_concat_max_len = 999999999; --  by default this length is 1024 -- This lenth is already set on production server
-- (max lenth of table column in string = 262144(column text length=64 and column count for any table is 4096 i.e. 4096*64=262144))
SELECT GROUP_CONCAT(CONCAT('`',t11.column_name,'`')) INTO  var_Columns FROM  (SELECT  column_name  FROM information_schema.columns WHERE table_schema=fromDB  AND table_name=stb  )  t11
INNER JOIN  (SELECT column_name FROM information_schema.columns  WHERE table_schema=toDB  AND table_name=dtb )  t22 ON t11.column_name = t22.column_name;
-- SET @@group_concat_max_len = 1024; --  reset group concat lenth
RETURN var_Columns;
END//
delimiter ;
