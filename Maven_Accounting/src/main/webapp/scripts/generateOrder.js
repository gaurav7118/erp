fieldIds = {
    orderForm: "orderForm",
    docType: "docType",
    linkDocType: "linkDocType",
    linkDocNumber: "linkDocNumber",
    validateLinkDocBttn: "validateLinkDocBttn",
    productDetails: "productDetails",
    productDetailsLabel: "productDetailsLabel",
    productDetailsTable: "productDetailsTable",
    barcode: "barcode",
    scanned_barcodes: "scanned_barcodes",
    removeBarcodeBttn: "removeBarcodeBttn",
    createDocBttn: "createDocBttn"
};

function onFormLoad() {
    resetGlobalFields();
    toggleAndResetAllBarcodeFields(true);
    clearAndHideProductDetails();
}
/**
 * When we change document type from GRN to DO or vice-versa.
 * @returns {undefined}
 */
function onDocTypeChange() {
    var docTypeField = getFieldById(fieldIds.docType);
    var docTypeValue = docTypeField.value;
    updateLinkDocTypeOptions(docTypeValue);
    onLinkedDocTypeChange();
}
/**
 * While creating DO when we change document from SI to SO or vice-versa.
 * @returns {undefined}
 */
function onLinkedDocTypeChange() {
    resetLinkDocNumberField();
    toggleAndResetAllBarcodeFields(true);
    clearAndHideProductDetails();
}
/**
 * While creating DO when we change document from SI to SO or vice-versa.
 * In this case, we also change document number.So to clear recent data following methods are needed.
 * @returns {undefined}
 */
function onLinkedDocNumberChange() {
    toggleAndResetAllBarcodeFields(true);
    clearAndHideProductDetails();
}

function updateLinkDocTypeOptions(docTypeValue) {
    var linkDocTypeField = getFieldById(fieldIds.linkDocType);
    linkDocTypeField.options.length = 0;
    if (docTypeValue === "DO") {
        linkDocTypeField.add(new Option("Sales Order", "SO"));
        linkDocTypeField.add(new Option("Sales Invoice", "SI"));
    } else if (docTypeValue === "GR") {
        linkDocTypeField.add(new Option("Purchase Order", "PO"));
    }
    linkDocTypeField.disabled = (linkDocTypeField.options.length === 0);
}

function resetLinkDocNumberField() {
    getFieldById(fieldIds.linkDocNumber).value = "";
}

function resetGlobalFields() {
    var docTypeField = getFieldById(fieldIds.docType);
    var docTypeValue = docTypeField.value;
    updateLinkDocTypeOptions(docTypeValue);
    resetLinkDocNumberField();
}

function toggleAndResetAllBarcodeFields(value) {
    toggleBarcodeFields(value);
    resetBarcodeFields();
    toggleBarcodeRelatedButtons(value);

}

function toggleBarcodeFields(value) {
    getFieldById(fieldIds.barcode).disabled = value;
}

function resetBarcodeFields() {
    getFieldById(fieldIds.scanned_barcodes).value = "";
    getFieldById(fieldIds.barcode).value = "";
}

function toggleBarcodeRelatedButtons(value) {
    getFieldById(fieldIds.removeBarcodeBttn).disabled = value;
    getFieldById(fieldIds.createDocBttn).disabled = value;
}

function setFocusToBarcodeField() {
    getFieldById(fieldIds.barcode).focus();
}
/**
 * To get product details when we click on button, initial values are needed to get the product data.
 * This initial values are passed by this method.
 * @returns {undefined}
 */
function validateLinkDocNumber() {
    clearAndHideProductDetails();
    var docTypeField = getFieldById(fieldIds.docType);
    var linkDocTypeField = getFieldById(fieldIds.linkDocType);
    var linkDocNumberField = getFieldById(fieldIds.linkDocNumber);
    var docTypeValue = docTypeField.value;
    var linkDocTypeValue = linkDocTypeField.value;
    var linkDocNumberValue = linkDocNumberField.value;
    if (docTypeValue && linkDocTypeValue && linkDocNumberValue) {
        setAjaxRequestTimeout();
        getFieldById(fieldIds.validateLinkDocBttn).disabled = true;
        Wtf.Ajax.requestEx({
            url: "GenerateOrder/validateLinkDocNumber.do",
            method: "POST",
            params: {
                docTypeValue: docTypeValue,
                linkDocTypeValue: linkDocTypeValue,
                linkDocNumberValue: linkDocNumberValue
            }
        }, this, function (res, req) {
            if (res.success) {
                toggleBarcodeFields(false);
                addAndShowProductDetails(res.data);
                setFocusToBarcodeField();
                alert(res.msg);
            } else {
                toggleBarcodeFields(true);
                alert(res.msg);
            }
            getFieldById(fieldIds.validateLinkDocBttn).disabled = false;
        }, function () {
            toggleBarcodeFields(true);
            alert("Failed to make connection with web server.");
            getFieldById(fieldIds.validateLinkDocBttn).disabled = false;
        });
    } else {
        var msg = "Please provide a valid value in following field(s): ";
        if (!linkDocNumberValue) {
            msg += "Linked Document Number, ";
        }
        msg = msg.substring(0, msg.length - 2);//To remove 'comma' and 'space' characters from the end
        alert(msg);
    }
}
/**
 * When we click to save GRN/DO.
 * @returns {undefined}
 */
function validateAndSaveDoc() {
    var docTypeField = getFieldById(fieldIds.docType);
    var linkDocTypeField = getFieldById(fieldIds.linkDocType);
    var linkDocNumberField = getFieldById(fieldIds.linkDocNumber);
    var docTypeValue = docTypeField.value;
    var linkDocTypeValue = linkDocTypeField.value;
    var linkDocNumberValue = linkDocNumberField.value;
    var scannedBarcodesField = getFieldById(fieldIds.scanned_barcodes);
    var scannedBarcodesValue = scannedBarcodesField.value;
    if (scannedBarcodesValue) {
        setAjaxRequestTimeout();
        getFieldById(fieldIds.createDocBttn).disabled = true;
        Wtf.Ajax.requestEx({
            url: "GenerateOrder/validateAndSaveDoc.do",
            method: "POST",
            params: {
                docTypeValue: docTypeValue,
                linkDocTypeValue: linkDocTypeValue,
                linkDocNumberValue: linkDocNumberValue,
                scannedBarcodesValue: scannedBarcodesValue
            }
        }, this, function (res, req) {
            if (res.success) {
                onFormLoad();
                var successMsg = "Success: ";
                alert(successMsg + "\n" + res.msg);
            } else {
                var failureMsg = "Failure: ";
                alert(failureMsg + "\n" + res.msg);
            }
            getFieldById(fieldIds.createDocBttn).disabled = false;
        }, function () {
            alert("Failed to make connection with web server.");
            getFieldById(fieldIds.createDocBttn).disabled = false;
        });
    } else {
        var msg = "Please scan/enter at least one barcode";
        alert(msg);
    }
}
/**
 * To add barcodes.
 * @returns {undefined}
 */
function addBarcode() {
    var barcodeField = getFieldById(fieldIds.barcode);
    var barcodeValue = barcodeField.value;
    if (barcodeValue) {
        var scannedBarcodesField = getFieldById(fieldIds.scanned_barcodes);
        var scannedBarcodesValue = scannedBarcodesField.value;
        if (scannedBarcodesValue !== "") {
            scannedBarcodesValue += ", ";
        }
        scannedBarcodesValue += barcodeValue;
        scannedBarcodesField.value = scannedBarcodesValue;
        barcodeField.value = "";
    }
    toggleBarcodeRelatedButtons(!scannedBarcodesValue);
}

/**
 * To remove added barcode from barcode list.
 * @returns {undefined}
 */
function removeLastBarcode() {
    var scannedBarcodesField = getFieldById(fieldIds.scanned_barcodes);
    var scannedBarcodesValue = scannedBarcodesField.value;
    if (scannedBarcodesValue) {
        var lastBarcodeIndex = scannedBarcodesValue.lastIndexOf(", ");
        if (lastBarcodeIndex !== -1) {
            scannedBarcodesValue = scannedBarcodesValue.substring(0, lastBarcodeIndex);
        } else {
            scannedBarcodesValue = "";
        }
        scannedBarcodesField.value = scannedBarcodesValue;
    }
    toggleBarcodeRelatedButtons(!scannedBarcodesValue);
}

function getFieldValueById(fieldid) {
    return getFieldById(fieldid).value;
}

function getFieldById(fieldid) {
    return this.document.getElementById(fieldid);
}

function setAjaxRequestTimeout() {
    Wtf.Ajax.timeout = 300000;//set timeout to 5 minutes
}

function addAndShowProductDetails(obj) {
    addProductDetails(obj);
    showProductDetails();
}

function addProductDetails(obj) {
    if (obj.data && obj.data.detail) {
        var productDetailsArr = obj.data.detail;
        for (var i = 0; i < productDetailsArr.length; i++) {
            var productDetailsTable = getFieldById(fieldIds.productDetailsTable);
            var row = productDetailsTable.insertRow(1);
            var productNameCell = row.insertCell(0);
            var productCodeCell = row.insertCell(1);
            var quantity = row.insertCell(2);
            productNameCell.innerHTML = productDetailsArr[i].productname ? productDetailsArr[i].productname : "";
            productCodeCell.innerHTML = productDetailsArr[i].pid ? productDetailsArr[i].pid : "";
            quantity.innerHTML = productDetailsArr[i].quantity ? productDetailsArr[i].quantity : 0;
        }
    }
}

function showProductDetails() {
    getFieldById(fieldIds.productDetailsTable).style.display = "table";
    getFieldById(fieldIds.productDetails).hidden = false;
}

function clearAndHideProductDetails() {
    clearProductDetails();
    hideProductDetails();
}

function clearProductDetails() {
    var productDetailsTable = getFieldById(fieldIds.productDetailsTable);
    for (var i = productDetailsTable.rows.length - 1; i > 0; i--)
    {
        productDetailsTable.deleteRow(i);
    }
}

function hideProductDetails() {
    getFieldById(fieldIds.productDetailsTable).style.display = "none";
    getFieldById(fieldIds.productDetails).hidden = true;
}

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

