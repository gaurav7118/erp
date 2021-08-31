DROP PROCEDURE IF EXISTS userpermissioncheck;
delimiter //
CREATE PROCEDURE userpermissioncheck (colname varchar(50), dbschema varchar(50), tablename varchar(50))
BEGIN
SET @sql_text = '';
SET @sql_text1 = 'Optimizing the userpermission table';
SET @sql_text2 = 'userpermission table already has id column';
SET @sql_text = if((SELECT count(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE COLUMN_NAME = colname AND TABLE_SCHEMA=dbschema and TABLE_NAME=tablename >0),"1","0");

IF @sql_text = 0 THEN
 BEGIN 
	select @sql_text1;


create table userperm_temp as
select feature, role, permissioncode, roleUserMapping, count(*) as count
from userpermission
group by feature,role,permissioncode,roleUserMapping having count > 1;

insert into userperm_temp
select feature, role, permissioncode, roleUserMapping, count(*) as count
from userpermission
group by feature,role,permissioncode,roleUserMapping having count = 1;

select feature, role, permissioncode, roleUserMapping, count(*) as count
from userperm_temp
group by feature,role,permissioncode,roleUserMapping having count = 1;

 create table userpermission_backup_29062017 as select * from userpermission;
   delete from userpermission;

SET foreign_key_checks = 0;
   insert into userpermission select feature, role, permissioncode, roleUserMapping from userperm_temp;
   SET foreign_key_checks = 1;

DROP TABLE userperm_temp;
 




insert into tmp_kg select 'a' ,feature,role,permissioncode,roleusermapping from userpermission;

drop table userpermission;

alter table tmp_kg rename to userpermission;

DROP TRIGGER Before_tmp_kg_Insert;

UPDATE userpermission set id='ef2a7b2b-a365-11e7-8c03-eca86bfcd416' where feature='01156a6e8f6649cfb885d78b7a1ab6d1' and role='1' and permissioncode='1' and roleusermapping is null;
UPDATE userpermission set id='ef2a7b2b-a365-11e7-8c03-eca86bfcd415' where feature='01156a6e8f6649cfb885d78b7a1ab6d1' and role='2' and permissioncode='1' and roleusermapping is null;
UPDATE userpermission set id='ef2a7e91-a365-11e7-8c03-eca86bfcd415' where feature='1a702a484c9c11e6beb89e71128cae77' and role='1' and permissioncode='31' and roleusermapping is null;
UPDATE userpermission set id='ef2a814f-a365-11e7-8c03-eca86bfcd415' where feature='1b1570e29bb411e4b5cbeca86bff8e7d' and role='1' and permissioncode='536870911' and roleusermapping is null;
UPDATE userpermission set id='ef2a84da-a365-11e7-8c03-eca86bfcd415' where feature='1b3e04c4a2f011e4adaceca86bff8e7d' and role='1' and permissioncode='15' and roleusermapping is null;
UPDATE userpermission set id='ef2a8762-a365-11e7-8c03-eca86bfcd415' where feature='233cc888aa3011e6ab4c14dda9792840' and role='1' and permissioncode='3' and roleusermapping is null;
UPDATE userpermission set id='ef2a89e2-a365-11e7-8c03-eca86bfcd415' where feature='25178b7fbdc144c3ad53fc476f7f8eb8' and role='1' and permissioncode='3' and roleusermapping is null;
UPDATE userpermission set id='ef2a8c5a-a365-11e7-8c03-eca86bfcd415' where feature='2a0cb967a6c14c78603391cce1712905' and role='1' and permissioncode='7' and roleusermapping is null;
UPDATE userpermission set id='ef2a8edc-a365-11e7-8c03-eca86bfcd415' where feature='2d428a08f31f11e48635eca86bff9ae1' and role='1' and permissioncode='536870911' and roleusermapping is null;
UPDATE userpermission set id='ef2a9246-a365-11e7-8c03-eca86bfcd415' where feature='2d428a08f31f11e48635eca86bff9ae2' and role='1' and permissioncode='16777215' and roleusermapping is null;
UPDATE userpermission set id='ef2a94bd-a365-11e7-8c03-eca86bfcd415' where feature='394ca219569d412e92317a164be46321' and role='1' and permissioncode='1' and roleusermapping is null;
UPDATE userpermission set id='ef2a9788-a365-11e7-8c03-eca86bfcd415' where feature='394ca219569d412e92317a164be46321' and role='2' and permissioncode='1' and roleusermapping is null;
UPDATE userpermission set id='ef2a9a06-a365-11e7-8c03-eca86bfcd415' where feature='3a0cb967a6c14c65903391cce5627f03' and role='1' and permissioncode='3' and roleusermapping is null;
UPDATE userpermission set id='ef2a9c7d-a365-11e7-8c03-eca86bfcd415' where feature='3a0cb967a6c14c65903391cce5627f03' and role='2' and permissioncode='3' and roleusermapping is null;
UPDATE userpermission set id='ef2a9ef0-a365-11e7-8c03-eca86bfcd415' where feature='3a0cb967a6c14c78603391cce2512f05' and role='1' and permissioncode='7' and roleusermapping is null;
UPDATE userpermission set id='ef2aa157-a365-11e7-8c03-eca86bfcd415' where feature='485b1f96a2f511e4adaceca86bff8e7d' and role='1' and permissioncode='255' and roleusermapping is null;
UPDATE userpermission set id='ef2aa3c9-a365-11e7-8c03-eca86bfcd415' where feature='4d5cb414b35411e3abe3001cc066e9f0' and role='1' and permissioncode='255' and roleusermapping is null;
UPDATE userpermission set id='ef2aa644-a365-11e7-8c03-eca86bfcd415' where feature='4f52f9615e344a50a7e1c8c6a51ded39' and role='1' and permissioncode='63' and roleusermapping is null;
UPDATE userpermission set id='ef2aa8ab-a365-11e7-8c03-eca86bfcd415' where feature='4f52f9615e344a50a7e1c8c6a51ded39' and role='2' and permissioncode='63' and roleusermapping is null;
UPDATE userpermission set id='ef2aab11-a365-11e7-8c03-eca86bfcd415' where feature='4f52f9615e344a50a7e1c8c6a51def99' and role='1' and permissioncode='511' and roleusermapping is null;
UPDATE userpermission set id='ef2aad82-a365-11e7-8c03-eca86bfcd415' where feature='4f52f9615e344a50a7e1c8c6a51deg99' and role='1' and permissioncode='511' and roleusermapping is null;
UPDATE userpermission set id='ef2aafec-a365-11e7-8c03-eca86bfcd415' where feature='5f52f9615e344a50a7e1c8c6a51def99' and role='1' and permissioncode='4095' and roleusermapping is null;
UPDATE userpermission set id='ef2ab252-a365-11e7-8c03-eca86bfcd415' where feature='5f52f9615e374a50a7e1c8c6a51deg99' and role='1' and permissioncode='2147483647' and roleusermapping is null;
UPDATE userpermission set id='ef2ab4bd-a365-11e7-8c03-eca86bfcd415' where feature='5f52f9615e374a50a7e1c8c6a61deg99' and role='1' and permissioncode='1073741823' and roleusermapping is null;
UPDATE userpermission set id='ef2ab720-a365-11e7-8c03-eca86bfcd415' where feature='5f52f9615e384a50a7e1c8c6a61deg99' and role='1' and permissioncode='4095' and roleusermapping is null;
UPDATE userpermission set id='ef2ab988-a365-11e7-8c03-eca86bfcd415' where feature='5f92f9615e374a50a7e1c8c6a51deg99' and role='1' and permissioncode='16383' and roleusermapping is null;
UPDATE userpermission set id='ef2abbee-a365-11e7-8c03-eca86bfcd415' where feature='5f92f9615e374a50a7e1c8c6a51teg33' and role='1' and permissioncode='2097151' and roleusermapping is null;
UPDATE userpermission set id='ef2abfbf-a365-11e7-8c03-eca86bfcd415' where feature='5f92f9615e374a50a7g1c8c6a51yeg22' and role='1' and permissioncode='3' and roleusermapping is null;
UPDATE userpermission set id='ef2ac230-a365-11e7-8c03-eca86bfcd415' where feature='721350624d6f11e6beb89e71128cae77' and role='1' and permissioncode='31' and roleusermapping is null;
UPDATE userpermission set id='ef2ac4a1-a365-11e7-8c03-eca86bfcd415' where feature='888795fc83ad4c90875768a661ea2384' and role='1' and permissioncode='31' and roleusermapping is null;
UPDATE userpermission set id='ef2ac708-a365-11e7-8c03-eca86bfcd415' where feature='8aa257b02afc4f04875d1a54810257fd' and role='1' and permissioncode='63' and roleusermapping is null;
UPDATE userpermission set id='ef2ac96c-a365-11e7-8c03-eca86bfcd415' where feature='8aa257b02afc4f04875d1a54810257fd' and role='2' and permissioncode='63' and roleusermapping is null;
UPDATE userpermission set id='ef2acbdc-a365-11e7-8c03-eca86bfcd415' where feature='98c6d3ec64fb4dbb97dfa269e599ffb6' and role='1' and permissioncode='3' and roleusermapping is null;
UPDATE userpermission set id='ef2ace38-a365-11e7-8c03-eca86bfcd415' where feature='98c6d3ec64fb4dbb97dfa269e599ffb6' and role='2' and permissioncode='3' and roleusermapping is null;
UPDATE userpermission set id='ef2ad09c-a365-11e7-8c03-eca86bfcd415' where feature='a528df089bd711e4bbfeeca86bff8e7d' and role='1' and permissioncode='255' and roleusermapping is null;
UPDATE userpermission set id='ef2ad304-a365-11e7-8c03-eca86bfcd415' where feature='a528df089bd711e4bbfeeca86hg8e7d' and role='1' and permissioncode='63' and roleusermapping is null;
UPDATE userpermission set id='ef2ad56e-a365-11e7-8c03-eca86bfcd415' where feature='ac4d34fea2f411e4adaceca86bff8e7d' and role='1' and permissioncode='255' and roleusermapping is null;
UPDATE userpermission set id='ef2ad7d1-a365-11e7-8c03-eca86bfcd415' where feature='b66459ce4cce11e6beb89e71128cae77' and role='1' and permissioncode='31' and roleusermapping is null;
UPDATE userpermission set id='ef2ada3a-a365-11e7-8c03-eca86bfcd415' where feature='b6c966e0114a456da40d4fbffa3806ab' and role='1' and permissioncode='127' and roleusermapping is null;
UPDATE userpermission set id='ef2adca1-a365-11e7-8c03-eca86bfcd415' where feature='bd7ef07de36f443aaeb8f56c3550fa85' and role='1' and permissioncode='7' and roleusermapping is null;
UPDATE userpermission set id='ef2adf0a-a365-11e7-8c03-eca86bfcd415' where feature='bd7ef07de36f443aaeb8f56c3550fa85' and role='2' and permissioncode='7' and roleusermapping is null;
UPDATE userpermission set id='ef2ae16e-a365-11e7-8c03-eca86bfcd415' where feature='bf7d01fd440f11e6b39514dda97927d6' and role='1' and permissioncode='2047' and roleusermapping is null;
UPDATE userpermission set id='ef2ae3d3-a365-11e7-8c03-eca86bfcd415' where feature='d577b5b0412c11e7897014dda9792823' and role='c340667e24c444b40124c7bfa271019d' and permissioncode='3' and roleusermapping is null;
UPDATE userpermission set id='ef2ae7a1-a365-11e7-8c03-eca86bfcd415' where feature='e0729e3ae07847cba2ee04d3823100dc' and role='1' and permissioncode='31' and roleusermapping is null;
UPDATE userpermission set id='ef2aea17-a365-11e7-8c03-eca86bfcd415' where feature='e0729e3ae07847cba2ee04d3823100dc' and role='2' and permissioncode='31' and roleusermapping is null;
UPDATE userpermission set id='ef2aec7d-a365-11e7-8c03-eca86bfcd415' where feature='e092295a9bd711e4bbfeeca86bff8e7d' and role='1' and permissioncode='255' and roleusermapping is null;
UPDATE userpermission set id='ef2aeede-a365-11e7-8c03-eca86bfcd415' where feature='e092295a9bd711e4trfeoca86bff8e7d' and role='1' and permissioncode='63' and roleusermapping is null;
UPDATE userpermission set id='ef2af145-a365-11e7-8c03-eca86bfcd415' where feature='e71503d6a2f011e4adaceca86bff8e7d' and role='1' and permissioncode='15' and roleusermapping is null;
UPDATE userpermission set id='ef2af3a9-a365-11e7-8c03-eca86bfcd415' where feature='f487fea4b35b11e39a00001cc066e9f0' and role='1' and permissioncode='255' and roleusermapping is null;
UPDATE userpermission set id='ef2af608-a365-11e7-8c03-eca86bfcd415' where feature='f8989a5ee8f11e59471eca86bff9ae1' and role='1' and permissioncode='127' and roleusermapping is null;
UPDATE userpermission set id='ef2af871-a365-11e7-8c03-eca86bfcd415' where feature='f8989a5ee8f11e59471eca86bff9ae1' and role='2' and permissioncode='127' and roleusermapping is null;
UPDATE userpermission set id='ef2afad7-a365-11e7-8c03-eca86bfcd415' where feature='fc31be704cd711e6beb89e71128cae77' and role='1' and permissioncode='63' and roleusermapping is null;
UPDATE userpermission set id='ef2afd43-a365-11e7-8c03-eca86bfcd415' where feature='ff80808122f3cb640122f449ca47000b' and role='1' and permissioncode='3' and roleusermapping is null;
UPDATE userpermission set id='ef2affab-a365-11e7-8c03-eca86bfcd415' where feature='ff80808122f3cb640122f449ca47000b' and role='2' and permissioncode='1' and roleusermapping is null;
UPDATE userpermission set id='ef2b020f-a365-11e7-8c03-eca86bfcd415' where feature='ff80808122f3cb640122f449ca47000b' and role='c340667e24a4841d0124b534504c0171' and permissioncode='3' and roleusermapping is null;
UPDATE userpermission set id='ef2b0474-a365-11e7-8c03-eca86bfcd415' where feature='ff80808122f3cb640122f449ca47000b' and role='ff80808122787f070122787f13620002' and permissioncode='3' and roleusermapping is null;
UPDATE userpermission set id='ef2b06e2-a365-11e7-8c03-eca86bfcd415' where feature='ff80808122f3cb640122f44a6cca000c' and role='1' and permissioncode='15' and roleusermapping is null;
UPDATE userpermission set id='ef2b0943-a365-11e7-8c03-eca86bfcd415' where feature='ff80808122f3cb640122f44a6cca000c' and role='c340667e24a4841d0124b534504c0171' and permissioncode='3' and roleusermapping is null;
UPDATE userpermission set id='ef2b0c80-a365-11e7-8c03-eca86bfcd415' where feature='ff80808122f3cb640122f44a6cca000c' and role='c340667e24c444b40124c7bfa271019d' and permissioncode='1' and roleusermapping is null;
UPDATE userpermission set id='ef2b0f73-a365-11e7-8c03-eca86bfcd415' where feature='ff80808122f3cb640122f44bd982000f' and role='1' and permissioncode='511' and roleusermapping is null;
UPDATE userpermission set id='ef2b11e2-a365-11e7-8c03-eca86bfcd415' where feature='ff80808122f3cb640122f44bd982000f' and role='2' and permissioncode='1' and roleusermapping is null;
UPDATE userpermission set id='ef2b144e-a365-11e7-8c03-eca86bfcd415' where feature='ff80808122f3cb640122f44bd982000f' and role='c340667e24a4841d0124b534504c0171' and permissioncode='255' and roleusermapping is null;
UPDATE userpermission set id='ef2b16b4-a365-11e7-8c03-eca86bfcd415' where feature='ff80808122f3cb640122f44bd982000f' and role='ff80808122787f070122787f13620002' and permissioncode='255' and roleusermapping is null;
UPDATE userpermission set id='ef2b1919-a365-11e7-8c03-eca86bfcd415' where feature='ff80808122f3cb640122f44c10f80010' and role='1' and permissioncode='511' and roleusermapping is null;
UPDATE userpermission set id='ef2b1b8a-a365-11e7-8c03-eca86bfcd415' where feature='ff80808122f3cb640122f44c10f80010' and role='2' and permissioncode='1' and roleusermapping is null;
UPDATE userpermission set id='ef2b1ded-a365-11e7-8c03-eca86bfcd415' where feature='ff80808122f3cb640122f44c10f80010' and role='c340667e24a4841d0124b534504c0171' and permissioncode='255' and roleusermapping is null;
UPDATE userpermission set id='ef2b2054-a365-11e7-8c03-eca86bfcd415' where feature='ff80808122f3cb640122f44c528e0011' and role='1' and permissioncode='32767' and roleusermapping is null;
UPDATE userpermission set id='ef2b22b9-a365-11e7-8c03-eca86bfcd415' where feature='ff80808122f3cb640122f44c528e0011' and role='2' and permissioncode='1' and roleusermapping is null;
UPDATE userpermission set id='ef2b2523-a365-11e7-8c03-eca86bfcd415' where feature='ff80808122f3cb640122f44c528e0011' and role='c340667e24a4841d0124b534504c0171' and permissioncode='4095' and roleusermapping is null;
UPDATE userpermission set id='ef2b278a-a365-11e7-8c03-eca86bfcd415' where feature='ff80808122f3cb640122f44c9db10012' and role='1' and permissioncode='2147483647' and roleusermapping is null;
UPDATE userpermission set id='ef2b29eb-a365-11e7-8c03-eca86bfcd415' where feature='ff80808122f3cb640122f44c9db10012' and role='c340667e24a4841d0124b534504c0171' and permissioncode='17179869183' and roleusermapping is null;
UPDATE userpermission set id='ef2b2c54-a365-11e7-8c03-eca86bfcd415' where feature='ff80808122f3cb640122f4521014001a' and role='1' and permissioncode='262143' and roleusermapping is null;
UPDATE userpermission set id='ef2b2ec3-a365-11e7-8c03-eca86bfcd415' where feature='ff80808122f3cb640122f4521014001a' and role='c340667e24a4841d0124b534504c0171' and permissioncode='262143' and roleusermapping is null;
UPDATE userpermission set id='ef2b3127-a365-11e7-8c03-eca86bfcd415' where feature='ff80808122f3cb640122f452dbc9001d' and role='1' and permissioncode='1' and roleusermapping is null;
UPDATE userpermission set id='ef2b34ec-a365-11e7-8c03-eca86bfcd415' where feature='ff80808122f3cb640122f452dbc9001d' and role='c340667e24c444b40124c7bfa271019d' and permissioncode='1' and roleusermapping is null;
UPDATE userpermission set id='ef2b3760-a365-11e7-8c03-eca86bfcd415' where feature='ff80808122f9dba90122fa2bcbcf0021' and role='1' and permissioncode='7' and roleusermapping is null;
UPDATE userpermission set id='ef2b39d3-a365-11e7-8c03-eca86bfcd415' where feature='ff80808122f9dba90122fa2bcbcf0021' and role='c340667e24a4841d0124b534504c0171' and permissioncode='7' and roleusermapping is null;
UPDATE userpermission set id='ef2b3c40-a365-11e7-8c03-eca86bfcd415' where feature='ff80808122f9dba90122fa2bcbcf0021' and role='c340667e24c444b40124c7bfa271019d' and permissioncode='7' and roleusermapping is null;
UPDATE userpermission set id='ef2b3ea1-a365-11e7-8c03-eca86bfcd415' where feature='ff80808122f9dba90122fa4704a5002e' and role='1' and permissioncode='15' and roleusermapping is null;
UPDATE userpermission set id='ef2b4105-a365-11e7-8c03-eca86bfcd415' where feature='ff80808122f9dba90122fa4704a5002e' and role='c340667e24a4841d0124b534504c0171' and permissioncode='3' and roleusermapping is null;
UPDATE userpermission set id='ef2b4377-a365-11e7-8c03-eca86bfcd415' where feature='ff80808122f9dba90122fa47e6e0002f' and role='1' and permissioncode='3' and roleusermapping is null;
UPDATE userpermission set id='ef2b45d6-a365-11e7-8c03-eca86bfcd415' where feature='ff80808122f9dba90122fa47e6e0002f' and role='c340667e24a4841d0124b534504c0171' and permissioncode='3' and roleusermapping is null;
UPDATE userpermission set id='ef2b4842-a365-11e7-8c03-eca86bfcd415' where feature='ff80808122f9dba90122fa47e6e0002f' and role='c340667e24c444b40124c7bfa271019d' and permissioncode='3' and roleusermapping is null;
UPDATE userpermission set id='ef2b4aa3-a365-11e7-8c03-eca86bfcd415' where feature='ff80808122f9dba90122fa4827210030' and role='1' and permissioncode='4194303' and roleusermapping is null;
UPDATE userpermission set id='ef2b4d0c-a365-11e7-8c03-eca86bfcd415' where feature='ff80808122f9dba90122fa4827210030' and role='c340667e24a4841d0124b534504c0171' and permissioncode='65535' and roleusermapping is null;
UPDATE userpermission set id='ef2b4f7b-a365-11e7-8c03-eca86bfcd415' where feature='ff80808122f9dba90122fa4888cf0031' and role='1' and permissioncode='7' and roleusermapping is null;
UPDATE userpermission set id='ef2b51e5-a365-11e7-8c03-eca86bfcd415' where feature='ff80808122f9dba90122fa4888cf0031' and role='c340667e24a4841d0124b534504c0171' and permissioncode='3' and roleusermapping is null;
UPDATE userpermission set id='ef2b544f-a365-11e7-8c03-eca86bfcd415' where feature='ff80808122f9dba90122fa4888cf0047' and role='1' and permissioncode='1' and roleusermapping is null;
UPDATE userpermission set id='ef2b5735-a365-11e7-8c03-eca86bfcd415' where feature='ff80808122f9dba90122fa4888cf0047' and role='c340667e24a4841d0124b534504c0171' and permissioncode='1' and roleusermapping is null;
UPDATE userpermission set id='ef2b59b9-a365-11e7-8c03-eca86bfcd415' where feature='ff80808122f9dba90122fa4888cf0048' and role='1' and permissioncode='3' and roleusermapping is null;
UPDATE userpermission set id='ef2b5cbf-a365-11e7-8c03-eca86bfcd415' where feature='ff80808122f9dba90122fa4888cf0048' and role='c340667e24a4841d0124b534504c0171' and permissioncode='3' and roleusermapping is null;
UPDATE userpermission set id='ef2b5f2a-a365-11e7-8c03-eca86bfcd415' where feature='ff80808122f9dba90122fa4888cf0049' and role='1' and permissioncode='1' and roleusermapping is null;
UPDATE userpermission set id='ef2b6199-a365-11e7-8c03-eca86bfcd415' where feature='ff80808122f9dba90122fa4888cf0049' and role='c340667e24a4841d0124b534504c0171' and permissioncode='1' and roleusermapping is null;
UPDATE userpermission set id='ef2b63f7-a365-11e7-8c03-eca86bfcd415' where feature='ff80808122f9dba90122fa4888cf0054' and role='1' and permissioncode='268435455' and roleusermapping is null;
UPDATE userpermission set id='ef2b6657-a365-11e7-8c03-eca86bfcd415' where feature='ff80808122f9dba90122fa4888cf0054' and role='2' and permissioncode='8589934591' and roleusermapping is null;
UPDATE userpermission set id='ef2b68bc-a365-11e7-8c03-eca86bfcd415' where feature='ff80808122f9dba90122fa4888cf0054' and role='c340667e24a4841d0124b534504c0171' and permissioncode='8589934591' and roleusermapping is null;
UPDATE userpermission set id='ef2b6b27-a365-11e7-8c03-eca86bfcd415' where feature='ff80808122f9dba90122fa4888cf0054' and role='ff80808122787f070122787f13620002' and permissioncode='8589934591' and roleusermapping is null;
UPDATE userpermission set id='ef2b6d8a-a365-11e7-8c03-eca86bfcd415' where feature='ff80808122f9dba90122fa4888cf0057' and role='1' and permissioncode='7' and roleusermapping is null;
UPDATE userpermission set id='ef2b6ff6-a365-11e7-8c03-eca86bfcd415' where feature='ff80808122f9dba90122fa4888cf0058' and role='1' and permissioncode='1023' and roleusermapping is null;
UPDATE userpermission set id='ef2b7256-a365-11e7-8c03-eca86bfcd415' where feature='ff80808122f9dba90122fa4888cf0058' and role='c340667e24a4841d0124b534504c0171' and permissioncode='1023' and roleusermapping is null;
UPDATE userpermission set id='ef2b74c0-a365-11e7-8c03-eca86bfcd415' where feature='ff80808122f9dba90122fa4888cf0060' and role='1' and permissioncode='511' and roleusermapping is null;
UPDATE userpermission set id='ef2b7722-a365-11e7-8c03-eca86bfcd415' where feature='ff80808122f9dba90122fa4888cf0060' and role='c340667e24c444b40124c7bfa271019d' and permissioncode='15' and roleusermapping is null;
UPDATE userpermission set id='ef2b7987-a365-11e7-8c03-eca86bfcd415' where feature='ff80808122f9dba90122fa4888cf0061' and role='1' and permissioncode='1' and roleusermapping is null;
UPDATE userpermission set id='ef2b7bef-a365-11e7-8c03-eca86bfcd415' where feature='ff80808122f9dba90122fa4888cf0061' and role='c340667e24a4841d0124b534504c0171' and permissioncode='1' and roleusermapping is null;
UPDATE userpermission set id='ef2b7e5c-a365-11e7-8c03-eca86bfcd415' where feature='ff80808122f9dba90122fa4888cf0061' and role='c340667e24c444b40124c7bfa271019d' and permissioncode='1' and roleusermapping is null;
UPDATE userpermission set id='ef2b8106-a365-11e7-8c03-eca86bfcd415' where feature='ff80808122f9dba90122fa4888cf0062' and role='1' and permissioncode='15' and roleusermapping is null;
UPDATE userpermission set id='ef2b848c-a365-11e7-8c03-eca86bfcd415' where feature='ff80808122f9dba90122fa4888cf0062' and role='c340667e24a4841d0124b534504c0171' and permissioncode='3' and roleusermapping is null;
UPDATE userpermission set id='ef2b86f5-a365-11e7-8c03-eca86bfcd415' where feature='ff80808122f9dba90122fa4888cf0063' and role='1' and permissioncode='1' and roleusermapping is null;
UPDATE userpermission set id='ef2b8965-a365-11e7-8c03-eca86bfcd415' where feature='ff80808122f9dba90122fa4888cf0064' and role='1' and permissioncode='3' and roleusermapping is null;
UPDATE userpermission set id='ef2b8bcb-a365-11e7-8c03-eca86bfcd415' where feature='ff80808122f9dba90122fa4888cf0065' and role='1' and permissioncode='7' and roleusermapping is null;
UPDATE userpermission set id='ef2b8e2f-a365-11e7-8c03-eca86bfcd415' where feature='ff80808122f9dba90122fa4888cf0066' and role='1' and permissioncode='7' and roleusermapping is null;
UPDATE userpermission set id='ef2b9097-a365-11e7-8c03-eca86bfcd415' where feature='ff80808122f9dba90122fa4888cf0067' and role='1' and permissioncode='1023' and roleusermapping is null;
UPDATE userpermission set id='ef2b92fe-a365-11e7-8c03-eca86bfcd415' where feature='ff80808122f9dba90122fa4888cf0068' and role='1' and permissioncode='16383' and roleusermapping is null;
UPDATE userpermission set id='ef2b955d-a365-11e7-8c03-eca86bfcd415' where feature='ff80808122f9dba90122fa4888cf0069' and role='1' and permissioncode='1' and roleusermapping is null;
UPDATE userpermission set id='ef7a8617-a365-11e7-8c03-eca86bfcd415' where feature='69cef2f2519211e7a2b0708bcdaa138a' and role='1' and permissioncode='3' and roleusermapping is null;
UPDATE userpermission set id='ef9a2a97-a365-11e7-8c03-eca86bfcd415' where feature='d577b5b0412c11e7897014dda9792823' and role='1' and permissioncode='3' and roleusermapping is null;
UPDATE userpermission set id='efd73044-a365-11e7-8c03-eca86bfcd415' where feature='ec85493ed63711e6acd414dda97926e5' and role='1' and permissioncode='3' and roleusermapping is null;	
END;
ELSE
BEGIN
drop table tmp_kg;
DROP TRIGGER IF EXISTS Before_tmp_kg_Insert;
   select @sql_text2;
END;
    END IF;

END//

delimiter ;