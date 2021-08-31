#Linux build script for ACCOUNTING project-path is as per code in /home/krawler-user/build/HQLAccounting/

echo "Minifying Process Starting............................."
echo "Merging css files............................."

#export codepath="/home/krawler-user/build/HQLAccounting"
export codepath="/home/krawler/ERP_21062014/DeskeraERPMain_ReDesign_21062014/Maven_Accounting"
cd $codepath/utils/FileMerge
php mergefilescss.php css-accounting.txt 3

echo "Merging js files............................."
php mergefilesjs.php js--index.txt 3
echo "Merging consignment js files............................."
php mergefilesjs.php js--consignment.txt 3
echo "Merging lease and assets js files............................."
php mergefilesjs.php js--assetslease.txt 3
echo "Merging inventory js files............................."
php mergefilesjs.php js--inventory.txt 3
echo "Merging loan js files............................."
php mergefilesjs.php js--loan.txt 3
echo "Merging mrp js files............................."
php mergefilesjs.php js--mrp.txt 3
echo "Merging other js files............................."
php mergefilesjs.php js--setupwizard.txt 3
php mergefilesjs.php js--accountprefsetting.txt 3

php mergefilesjs.php js--admin.txt 3

# For Maven Build /web/ ---> /src/main/webapp/
cp $codepath/src/main/webapp/scripts/index-ex.js $codepath/src/main/webapp/scripts/index-ex-temp.js
cp $codepath/src/main/webapp/scripts/admin-ex.js $codepath/src/main/webapp/scripts/admin-ex-temp.js
cp $codepath/src/main/webapp/scripts/minifiedjs/consignment-ex.js $codepath/src/main/webapp/scripts/minifiedjs/consignment-ex-temp.js
cp $codepath/src/main/webapp/scripts/minifiedjs/assetlease-ex.js $codepath/src/main/webapp/scripts/minifiedjs/assetlease-ex-temp.js
cp $codepath/src/main/webapp/scripts/minifiedjs/inventory-ex.js $codepath/src/main/webapp/scripts/minifiedjs/inventory-ex-temp.js
cp $codepath/src/main/webapp/scripts/minifiedjs/loan-ex.js $codepath/src/main/webapp/scripts/minifiedjs/loan-ex-temp.js
cp $codepath/src/main/webapp/scripts/minifiedjs/mrp-ex.js $codepath/src/main/webapp/scripts/minifiedjs/mrp-ex-temp.js
cp $codepath/src/main/webapp/scripts/minifiedjs/setupwizard-ex.js $codepath/src/main/webapp/scripts/minifiedjs/setupwizard-ex-temp.js
cp $codepath/src/main/webapp/scripts/minifiedjs/accountprefsetting-ex.js $codepath/src/main/webapp/scripts/minifiedjs/accountprefsetting-ex-temp.js
cp $codepath/src/main/webapp/style/accounting.css $codepath/src/main/webapp/style/accountingtemp.css
cp $codepath/src/main/webapp/WEB-INF/web.sso.xml $codepath/src/main/webapp/WEB-INF/web.xml
cp $codepath/src/main/webapp/admin-ex.html $codepath/src/main/webapp/admin.html
cd

#mkdir tempyui
#cd tempyui
echo "Compressing js files............................."
java -jar $codepath/utils/YUICompressor/yuicompressor-2.2.5.jar --charset UTF-8 -o $codepath/src/main/webapp/scripts/index-ex.js $codepath/src/main/webapp/scripts/index-ex-temp.js 2>&1
java -jar $codepath/utils/YUICompressor/yuicompressor-2.2.5.jar --charset UTF-8 -o $codepath/src/main/webapp/scripts/minifiedjs/consignment-ex.js $codepath/src/main/webapp/scripts/minifiedjs/consignment-ex-temp.js 2>&1
java -jar $codepath/utils/YUICompressor/yuicompressor-2.2.5.jar --charset UTF-8 -o $codepath/src/main/webapp/scripts/minifiedjs/assetlease-ex.js $codepath/src/main/webapp/scripts/minifiedjs/assetlease-ex-temp.js 2>&1
java -jar $codepath/utils/YUICompressor/yuicompressor-2.2.5.jar --charset UTF-8 -o $codepath/src/main/webapp/scripts/minifiedjs/inventory-ex.js $codepath/src/main/webapp/scripts/minifiedjs/inventory-ex-temp.js 2>&1
java -jar $codepath/utils/YUICompressor/yuicompressor-2.2.5.jar --charset UTF-8 -o $codepath/src/main/webapp/scripts/minifiedjs/loan-ex.js $codepath/src/main/webapp/scripts/minifiedjs/loan-ex-temp.js 2>&1
java -jar $codepath/utils/YUICompressor/yuicompressor-2.2.5.jar --charset UTF-8 -o $codepath/src/main/webapp/scripts/minifiedjs/mrp-ex.js $codepath/src/main/webapp/scripts/minifiedjs/mrp-ex-temp.js 2>&1
java -jar $codepath/utils/YUICompressor/yuicompressor-2.2.5.jar --charset UTF-8 -o $codepath/src/main/webapp/scripts/minifiedjs/setupwizard-ex.js $codepath/src/main/webapp/scripts/minifiedjs/setupwizard-ex-temp.js 2>&1
java -jar $codepath/utils/YUICompressor/yuicompressor-2.2.5.jar --charset UTF-8 -o $codepath/src/main/webapp/scripts/minifiedjs/accountprefsetting-ex.js $codepath/src/main/webapp/scripts/minifiedjs/accountprefsetting-ex-temp.js 2>&1
java -jar $codepath/utils/YUICompressor/yuicompressor-2.2.5.jar --charset UTF-8 -o $codepath/src/main/webapp/scripts/admin-ex.js $codepath/src/main/webapp/scripts/admin-ex-temp.js 2>&1

java -jar $codepath/utils/YUICompressor/yuicompressor-2.2.5.jar --charset UTF-8 -o $codepath/src/main/webapp/scripts/common/WtfAdvHtmlEditor.js $codepath/src/main/webapp/scripts/common/WtfAdvHtmlEditor.js
2>&1

echo "Compressing css files............................."
java -jar $codepath/utils/YUICompressor/yuicompressor-2.2.5.jar --charset UTF-8 -o $codepath/src/main/webapp/style/accounting.css $codepath/src/main/webapp/style/accountingtemp.css 2>&1


echo "Removing temporary files............................."
rm $codepath/src/main/webapp/scripts/index-ex-temp.js
rm $codepath/src/main/webapp/scripts/minifiedjs/consignment-ex-temp.js
rm $codepath/src/main/webapp/scripts/minifiedjs/assetlease-ex-temp.js
rm $codepath/src/main/webapp/scripts/minifiedjs/inventory-ex-temp.js
rm $codepath/src/main/webapp/scripts/minifiedjs/loan-ex-temp.js
rm $codepath/src/main/webapp/scripts/minifiedjs/mrp-ex-temp.js
rm $codepath/src/main/webapp/scripts/minifiedjs/setupwizard-ex-temp.js
rm $codepath/src/main/webapp/scripts/minifiedjs/accountprefsetting-ex-temp.js
rm $codepath/src/main/webapp/scripts/admin-ex-temp.js
rm $codepath/src/main/webapp/style/accountingtemp.css


echo "Compressing Report folder js files............................."
#export CURRENT_DIR=$codepath/src/main/webapp/scripts/Reports/
pathArray=($codepath/src/main/webapp/scripts/Reports/ $codepath/src/main/webapp/scripts/EntityGst/EntityGSTReports/)
export TYPE='js'
for path in "${pathArray[@]}"
do
  echo "Start Compressing JS files from path - $path"
    for file in `find $path -name "*.$TYPE"`
      do  
        # Get the current file directory
        FILE_DIRECTORY=$(dirname $file)
        # Get the basename of the current directory
        BASE_DIR_NAME=`basename $FILE_DIRECTORY`

        if test $BASE_DIR_NAME != 'minified';
        then
          # Get the current file name
          BASE_FILE_NAME=`basename $file`
          MINIFIED_FILE_NAME=${BASE_FILE_NAME%$TYPE}$TYPE
          #echo "MINIFIED_FILE_NAME = " $MINIFIED_FILE_NAME
          # Minified directory path for the current file
          MINIFIED_FILE_DIRECTORY="$FILE_DIRECTORY"
          #echo "MINIFIED_FILE_DIRECTORY =" $MINIFIED_FILE_DIRECTORY
          #create_directory_if_not_exist $MINIFIED_FILE_DIRECTORY

          MINIFIED_OUTPUT_FILE="$MINIFIED_FILE_DIRECTORY/$MINIFIED_FILE_NAME"
          java -jar $codepath/utils/YUICompressor/yuicompressor-2.2.5.jar --charset UTF-8 --type $TYPE -o $MINIFIED_OUTPUT_FILE $file
          if [ "$?" != "0" ]; then
            echo "Error Compressing $file $ERROR_DISPLAY"
          fi
          #rm $BASE_FILE_NAME   
          #mv $$MINIFIED_OUTPUT_FILE $BASE_FILE_NAME
        fi
    done
echo "Finish Compressing JS files from path - $path"
done

for file in $(find $codepath -name "customReportBuilder.jsp" -print)
do
 echo "Replacing ext-all-debug.js with ext-all.js in file .........." $file
 sed -i 's/ext-all-debug/ext-all/g' $file
done

# Vinod Gole...
current=$(date +%Y%m%d_%H%M)

# Original line content
Org_line=?v=[0-9]*_[0-9]*

# Replace line content if you change
Replace_line=?v=$current

# Source file location
source_file=$codepath/src/main/webapp/index-ex.jsp

# Command to replace content
sed -i "s/$Org_line/$Replace_line/g" $source_file
echo " "
echo -e "Given content are replaced in JS file \n"
# Below command to check replaced line content
cat $source_file | grep $Replace_line

# Source file location
source_file=$codepath/src/main/webapp/scripts/common/ModuleScripts.js

# Command to replace content
sed -i "s/$Org_line/$Replace_line/g" $source_file
echo " "
echo -e "Given content are replaced in JS file \n"
# Below command to check replaced line content
cat $source_file | grep $Replace_line


#cd
#rm -rf tempyui
logfile=$codepath/src/main/webapp/version.txt
echo "SVN Repo: /deskera/branches/Financials, Version created at : $(date +'%d-%m-%Y %H:%M:%S')" > $logfile

echo "Minifying Process Completed............................."
