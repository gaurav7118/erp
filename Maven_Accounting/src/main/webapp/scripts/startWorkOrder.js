/**
 * Ticket : ERM-334
 * @type type
 * This startWorkOrder.js file is used to validate startworkorder.jsp page.
 * Here Validation is applied by following methods :
 * 1-> onFormLoad_WO()
 * 2-> resetGlobalFields_WO()
 * 3-> toggleAndResetAllBarcodeFields_WO()
 * 4-> clearAndHideProductDetails_WO().
 * startWorkOrder.js file is not used in Accounting so,no need to change this file.
 */
fieldIDs_WO = {
    linkDocNumber_WO: "linkDocNumber_WO",
    barcode_WO: "barcode_WO",
    scanned_barcodes_WO: "scanned_barcodes_WO",
    removeBarcodeBttn_WO: "removeBarcodeBttn_WO",
    createDocBttn_WO: "createDocBttn_WO",
    productDetailsTable_WO: "productDetailsTable_WO",
    productDetails_WO: "productDetails_WO"
};

function getFieldById(fieldid) {
    return this.document.getElementById(fieldid);
}

function setAjaxRequestTimeout() {
    Wtf.Ajax.timeout = 300000;//set timeout to 5 minutes
}

function onFormLoad_WO()
{
    resetGlobalFields_WO();
    toggleAndResetAllBarcodeFields_WO(true);
    clearAndHideProductDetails_WO();
}

/**
 * Function to reset All Global Fields
 * @returns {undefined}
 */
function resetGlobalFields_WO()
{
    resetLinkDocNumberField_WO();
}


function resetLinkDocNumberField_WO()
{
    getFieldById(fieldIDs_WO.linkDocNumber_WO).value = "";
}

/**
 * Function to toggle and reset all Barcode Fields 
 * @param {type} value
 * @returns {undefined}
 */
function toggleAndResetAllBarcodeFields_WO(value)
{
    toggleBarcodeFields_WO(value);
    resetBarcodeFields_WO();
    toggleBarcodeRelatedButtons_WO(value);
}

function toggleBarcodeFields_WO(value)
{
    getFieldById(fieldIDs_WO.barcode_WO).disabled = value;
}

function resetBarcodeFields_WO()
{
    getFieldById(fieldIDs_WO.scanned_barcodes_WO).value = "";
    getFieldById(fieldIDs_WO.barcode_WO).value = "";
}

function toggleBarcodeRelatedButtons_WO(value)
{
    getFieldById(fieldIDs_WO.removeBarcodeBttn_WO).disabled = value;
    getFieldById(fieldIDs_WO.createDocBttn_WO).disabled = value;
}

function setFocusToBarcodeField_WO() {
    getFieldById(fieldIDs_WO.barcode_WO).focus();
}
/**
 * This Function by default Hide Product Details Table When Form Loads. 
 * @returns {undefined}
 */
function clearAndHideProductDetails_WO()
{
    clearProductDetails_WO();
    hideProductDetails_WO();
}

function clearProductDetails_WO()
{
    var productDetailsTable_WO = getFieldById(fieldIDs_WO.productDetailsTable_WO);

    for (var i = productDetailsTable_WO.rows.length - 1; i > 0; i--)
    {
        productDetailsTable_WO.deleteRow(i);
    }
}

function hideProductDetails_WO()
{
    getFieldById(fieldIDs_WO.productDetailsTable_WO).style.display = "none";
    getFieldById(fieldIDs_WO.productDetails_WO).hidden = true;
}

/**
 * Validating Document Based on Work Order Number Entered by User
 * @returns {undefined}
 */
function validateLinkDocNumber_WO()
{
    clearAndHideProductDetails_WO();
    var linkDocNumber_WO = getFieldById(fieldIDs_WO.linkDocNumber_WO);
    var linkDocNumber_WO_value = linkDocNumber_WO.value;

    if (linkDocNumber_WO_value)
    {
        setAjaxRequestTimeout();
        Wtf.Ajax.requestEx({
            url: "GenerateOrder/validateLinkDocNumber.do",
            method: "POST",
            params: {
                linkDocTypeValue: "WO",
                linkDocNumberValue: linkDocNumber_WO_value
            }
        }, this, function (res, req) {
            if (res.success) {
                toggleBarcodeFields_WO(false);
                addAndShowProductDetails_WO(res.data);
                setFocusToBarcodeField_WO();
                alert(res.msg);
            } else {
                toggleBarcodeFields_WO(true);
                alert(res.msg);
            }
        }, function () {
            toggleBarcodeFields_WO(true);
            alert("Failed to make connection with web server.");
        });
    } else {
        var msg = "Please provide a valid value in following field(s): ";
        if (!linkDocNumber_WO_value)
        {
            msg += "Linked Work Order Number, ";
        }
        msg = msg.substring(0, msg.length - 2); //to remove comma and space characters from the end
        alert(msg);
    }
}

/**
 * Used to add[addProductDetails_WO()] and show[showProductDetails_WO()] Product Details After 
 * 'Validate Word Order Button' Clicked.
 * @param {type} obj
 * @returns {undefined}
 */
function addAndShowProductDetails_WO(obj)
{
    addProductDetails_WO(obj);
    showProductDetails_WO();
}

function addProductDetails_WO(obj)
{
    if (obj.data && obj.data.detail)
    {
        var productDetailsArr = obj.data.detail;
        for (var i = 0; i < productDetailsArr.length; i++)
        {
            var productDetailsTable_WO = getFieldById(fieldIDs_WO.productDetailsTable_WO);
            var row = productDetailsTable_WO.insertRow(1);
            var productNameCell = row.insertCell(0);
            var productCodeCell = row.insertCell(1);
            var quantity = row.insertCell(2);
            var producttype = row.insertCell(3);
            productNameCell.innerHTML = productDetailsArr[i].productname ? productDetailsArr[i].productname : "";
            productCodeCell.innerHTML = productDetailsArr[i].pid ? productDetailsArr[i].pid : "";
            quantity.innerHTML = productDetailsArr[i].quantity ? productDetailsArr[i].quantity : 0;
            producttype.innerHTML = productDetailsArr[i].producttype ? productDetailsArr[i].producttype : "";
        }
    }
}

function showProductDetails_WO()
{
    getFieldById(fieldIDs_WO.productDetailsTable_WO).style.display = "table";
    getFieldById(fieldIDs_WO.productDetails_WO).hidden = false;
}


function onLinkedDocNumberChange_WO()
{
    toggleAndResetAllBarcodeFields_WO(true);
    clearAndHideProductDetails_WO();
}

/**
 * This Function is used to add bar code to Scanned Barcode Fields.
 * @returns {undefined}
 */
function addBarcode_WO()
{
    var barcodeField = getFieldById(fieldIDs_WO.barcode_WO);
    var barcodeValue = barcodeField.value;
    if (barcodeValue)
    {
        var scannedbarcodesField = getFieldById(fieldIDs_WO.scanned_barcodes_WO);
        var scannedbarcodesFieldValue = scannedbarcodesField.value;
        if (scannedbarcodesFieldValue !== "")
        {
            scannedbarcodesFieldValue += ", ";
        }
        scannedbarcodesFieldValue += barcodeValue;
        scannedbarcodesField.value = scannedbarcodesFieldValue;
        barcodeField.value = "";
    }
    toggleBarcodeRelatedButtons_WO(!scannedbarcodesFieldValue);
}

/**
 * This function is used to remove last barcode from Scanned Barcode Fields
 * @returns {undefined}
 */
function removeLastBarcode_WO()
{
    var scannedBarcodeField = getFieldById(fieldIDs_WO.scanned_barcodes_WO);
    var scannedBarcodeValue = scannedBarcodeField.value;
    if (scannedBarcodeValue)
    {
        var lastBarcodeindex = scannedBarcodeValue.lastIndexOf(", ");
        if (lastBarcodeindex !== -1)
        {
            scannedBarcodeValue = scannedBarcodeValue.substring(0, lastBarcodeindex);
        } else {
            scannedBarcodeValue = "";
        }
        scannedBarcodeField.value = scannedBarcodeValue;
    }
    toggleBarcodeRelatedButtons_WO(!scannedBarcodeValue);
}

/**
 * This function is used to Validate and Save document
 * @returns {undefined}
 */
function validateAndSaveDoc_WO()
{
    var linkDocNumber_WO = getFieldById(fieldIDs_WO.linkDocNumber_WO);
    var linkDocNumberValue = linkDocNumber_WO.value;
    var scannedBarcodesField_WO = getFieldById(fieldIDs_WO.scanned_barcodes_WO);
    var scannedBarcodeValue = scannedBarcodesField_WO.value;
    if (scannedBarcodeValue)
    {
        setAjaxRequestTimeout();
        Wtf.Ajax.requestEx({
            url: "GenerateOrder/validateAndSaveDoc.do",
            method: "POST",
            params: {
                linkDocTypeValue: "WO",
                linkDocNumberValue: linkDocNumberValue,
                scannedBarcodesValue: scannedBarcodeValue
            }
        }, this, function (res, req) {
            if (res.success)
            {
                onFormLoad_WO();
                var successMsg = "Success: ";
                alert(successMsg + "\n" + res.msg);
            } else {
                var failureMsg = "Work Order could not be started due to following errors : \n";
                alert(failureMsg + "\n" + res.msg);
            }
        }, function () {
            alert("Failed to make Connection with web server");
        });
    } else {
        var msg = "Please scan/enter at least one barcode";
        alert(msg);
    }
}


/**
 * This function call when user click on Sign Out button and redirect ro sign-in page 
 * @param {type} type
 * @returns {undefined}
 */
function signOut(type) {
    var _out = "";
    if (type !== undefined && typeof type != "object")
        _out = "?type=" + type;
    _dC('lastlogin');
    _dC('featureaccess');
    _dC('username');
    _dC('lid');
    _dC('companyid');
    var domainPatt = /[ab]\/([^\/]*)\/(.*)/;
    var m = domainPatt.exec(window.location);
    var _u = '../../error.do';
    if (type == "noaccess" || type == "alreadyloggedin") {
        _u += '?e=' + type;
        if (m && m[1]) {
            _u += '&n=' + m[1];
        }
    }
    else {
        if (m && m[1]) {
            _u = '../../b/' + m[1] + '/signOut.do' + _out;
        }
    }
    _r(_u);
}

function _dC(n) {
    document.cookie = n + "=" + ";path=/;expires=Thu, 01-Jan-1970 00:00:01 GMT";
}

function _r(url) {
    window.top.location.href = url;
}