# 1. Set lib's path variables.
# 2. Then execute this collectLibs.sh file.

export Krawler_Common_Lib_Path="/home/krawler/ERP_InvIntegration/DeskeraERPMain_ReDesign_InvAccouning_24112014/Maven_KwlCommonLibs/";
export Accounting_Libs_Folder_Path="/home/krawler/ERP_InvIntegration/DeskeraERPMain_ReDesign_InvAccouning_24112014/Maven_Accounting_Libs/";

#Set maven environment variables to execute maven commands
export M2_HOME=/usr/local/apache-maven-3.0.5
export M2=$M2_HOME/bin
MAVEN_OPTS="-Xms256m -Xmx512m"
#export PATH=$M2:$PATH
#export JAVA_HOME=/usr/java/jdk1.6.0_18


echo "Collecting Accounting Libs...."

# Build krawler Common Lib
cd $Krawler_Common_Lib_Path
mvn install


# Build all accounting Libs
cd $Accounting_Libs_Folder_Path

echo ""
echo ""
echo ""
echo "(((((((((((((((((((((((((((((((((((((((((( END ))))))))))))))))))))))))))))))))))))))))))))))))"
cd mavenAccCommonLibs/
mvn install

echo ""
echo ""
echo ""
echo "(((((((((((((((((((((((((((((((((((((((((( END ))))))))))))))))))))))))))))))))))))))))))))))))"
cd ../mavenAccCurrencyExchange/
mvn install

echo ""
echo ""
echo ""
echo "(((((((((((((((((((((((((((((((((((((((((( END ))))))))))))))))))))))))))))))))))))))))))))))))"
cd ../mavenAccTermModule/
mvn install

echo ""
echo ""
echo ""
echo "(((((((((((((((((((((((((((((((((((((((((( END ))))))))))))))))))))))))))))))))))))))))))))))))"
cd ../mavenAccUOMModule/
mvn install

echo ""
echo ""
echo ""
echo "(((((((((((((((((((((((((((((((((((((((((( END ))))))))))))))))))))))))))))))))))))))))))))))))"
cd ../mavenAccMasterItems/
mvn install

echo ""
echo ""
echo ""
echo "(((((((((((((((((((((((((((((((((((((((((( END ))))))))))))))))))))))))))))))))))))))))))))))))"
cd ../mavenAccDiscountModule/
mvn install

echo ""
echo ""
echo ""
echo "(((((((((((((((((((((((((((((((((((((((((( END ))))))))))))))))))))))))))))))))))))))))))))))))"
cd ../mavenAccAccountModule/
mvn install

echo ""
echo ""
echo ""
echo "(((((((((((((((((((((((((((((((((((((((((( END ))))))))))))))))))))))))))))))))))))))))))))))))"
cd ../mavenAccCompanyPreferences/
mvn install

echo ""
echo ""
echo ""
echo "(((((((((((((((((((((((((((((((((((((((((( END ))))))))))))))))))))))))))))))))))))))))))))))))"
cd ../mavenAccTaxModule/
mvn install

echo ""
echo ""
echo ""
echo "(((((((((((((((((((((((((((((((((((((((((( END ))))))))))))))))))))))))))))))))))))))))))))))))"
cd ../mavenAccPaymentMethods/
mvn install

echo ""
echo ""
echo ""
echo "(((((((((((((((((((((((((((((((((((((((((( END ))))))))))))))))))))))))))))))))))))))))))))))))"
cd ../mavenAccCustomerModule/
mvn install

echo ""
echo ""
echo ""
echo "(((((((((((((((((((((((((((((((((((((((((( END ))))))))))))))))))))))))))))))))))))))))))))))))"
cd ../mavenAccVendorModule/
mvn install

echo ""
echo ""
echo ""
echo "(((((((((((((((((((((((((((((((((((((((((( END ))))))))))))))))))))))))))))))))))))))))))))))))"
cd ../mavenAccJournalEntryModule/
mvn install

echo ""
echo ""
echo ""
echo "(((((((((((((((((((((((((((((((((((((((((( END ))))))))))))))))))))))))))))))))))))))))))))))))"
cd ../mavenAccDepreciation/
mvn install

echo ""
echo ""
echo ""
echo "(((((((((((((((((((((((((((((((((((((((((( END ))))))))))))))))))))))))))))))))))))))))))))))))"
cd ../mavenAccBankReconciliation/
mvn install

echo ""
echo ""
echo ""
echo "(((((((((((((((((((((((((((((((((((((((((( END ))))))))))))))))))))))))))))))))))))))))))))))))"
cd ../mavenAccProductModule/
mvn install

echo ""
echo ""
echo ""
echo "(((((((((((((((((((((((((((((((((((((((((( END ))))))))))))))))))))))))))))))))))))))))))))))))"
cd ../mavenAccSalesOrder/
mvn install

echo ""
echo ""
echo ""
echo "(((((((((((((((((((((((((((((((((((((((((( END ))))))))))))))))))))))))))))))))))))))))))))))))"
cd ../mavenAccInventoryModule/
mvn install

echo ""
echo ""
echo ""
echo "(((((((((((((((((((((((((((((((((((((((((( END ))))))))))))))))))))))))))))))))))))))))))))))))"
cd ../mavenAccInvoiceModule/
mvn install

echo ""
echo ""
echo ""
echo "(((((((((((((((((((((((((((((((((((((((((( END ))))))))))))))))))))))))))))))))))))))))))))))))"
cd ../mavenAccReceiptModule/
mvn install

echo ""
echo ""
echo ""
echo "(((((((((((((((((((((((((((((((((((((((((( END ))))))))))))))))))))))))))))))))))))))))))))))))"
cd ../mavenAccCreditNote/
mvn install

echo ""
echo ""
echo ""
echo "(((((((((((((((((((((((((((((((((((((((((( END ))))))))))))))))))))))))))))))))))))))))))))))))"
cd ../mavenAccPurchaseOrder/
mvn install

echo ""
echo ""
echo ""
echo "(((((((((((((((((((((((((((((((((((((((((( END ))))))))))))))))))))))))))))))))))))))))))))))))"
cd ../mavenAccGoodsReceipt/
mvn install

echo ""
echo ""
echo ""
echo "(((((((((((((((((((((((((((((((((((((((((( END ))))))))))))))))))))))))))))))))))))))))))))))))"
cd ../mavenAccVendorPayment/
mvn install

echo ""
echo ""
echo ""
echo "(((((((((((((((((((((((((((((((((((((((((( END ))))))))))))))))))))))))))))))))))))))))))))))))"
cd ../mavenAccDebitNote
mvn install

echo ""
echo "-----------------------------------------------------------------------------------------"
echo "---------------------- END ALL.... Now clean N build your main project ------------------"
echo "-----------------------------------------------------------------------------------------"
