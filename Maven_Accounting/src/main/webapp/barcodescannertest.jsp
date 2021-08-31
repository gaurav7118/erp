
<%@ page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
    <head>
        <title>Generate Order</title>
        <meta charset="UTF-8">
    </head>
    <body class="body">
        <div class="form-style-10">
            <h1 class="logo">Barcode Scanner Test<span><br>Please move cursor to Barcode field and scan a barcode</span></h1>
            <form method="post" name="orderForm" id="orderForm" onsubmit="return false">
                <div class="inner-wrap">
                    <label>Document Type: <select size="1" name="docType" id="docType">
                            <option value ="DO">Delivery Order</option>
                            <option value ="GR">Goods Receipt Note</option>
                        </select>
                    </label>
                    <label>Document Number: <input type="text" name="linkDocNumber" id="linkDocNumber"/></label>
                    <label>Barcode: <input type="text" name="barcode" id="barcode" onchange="addBarcode1()"/></label>
                    <label>Scanned Barcodes: <textarea name="scanned_barcodes" id="scanned_barcodes" rows="6" disabled></textarea></label>
                </div>
                <div class="button-section">
                    <input type="button" name="removeBarcodeBttn" id="removeBarcodeBttn" value="Remove Last Barcode" onclick="removeLastBarcode()"/>
                    <input type="reset" name="reset" value="Reset"/>
                </div>
            </form>
        </div>
        <script type="text/javascript">
            function addBarcode1() {
                var barcodeField = this.document.getElementById("barcode");
                var barcodeValue = barcodeField.value;
                if (barcodeValue) {
                    var scannedBarcodesField = this.document.getElementById("scanned_barcodes");
                    var scannedBarcodesValue = scannedBarcodesField.value;
                    if (scannedBarcodesValue !== "") {
                        scannedBarcodesValue += ", ";
                    }
                    scannedBarcodesValue += barcodeValue;
                    scannedBarcodesField.value = scannedBarcodesValue;
                    barcodeField.value = "";
                }
            }
            function removeLastBarcode() {
                var scannedBarcodesField = this.document.getElementById("scanned_barcodes");
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
            }
        </script>
        <style type="text/css">
            .body {
                overflow: hidden;
            }
            .form-style-10{
                width:800px;
                padding:15px 50px 25px 50px;
                margin:40px auto;
                background: #FFF;
                border-radius: 10px;
                -webkit-border-radius:10px;
                -moz-border-radius: 10px;
                box-shadow: 0px 0px 10px rgba(0, 0, 0, 0.13);
                -moz-box-shadow: 0px 0px 10px rgba(0, 0, 0, 0.13);
                -webkit-box-shadow: 0px 0px 10px rgba(0, 0, 0, 0.13);
            }
            .form-style-10 .logo{
                margin: auto -10px 15px -10px;
            }
            .form-style-10 .inner-wrap{
                padding: 20px 20px 20px 20px;
                margin: auto -10px 15px -10px;
                background: #F8F8F8;
                border-radius: 10px;
            }
            .form-style-10 h1{
                background: #2A88AD;
                padding: 10px 30px 15px 20px;
                margin: -30px -30px 30px -30px;
                border-radius: 10px 10px 10px 10px;
                -webkit-border-radius: 10px 10px 10px 10px;
                -moz-border-radius: 10px 10px 10px 10px;
                color: #fff;
                text-shadow: 1px 1px 3px rgba(0, 0, 0, 0.12);
                font: normal 30px serif;
                -moz-box-shadow: inset 0px 2px 2px 0px rgba(255, 255, 255, 0.17);
                -webkit-box-shadow: inset 0px 2px 2px 0px rgba(255, 255, 255, 0.17);
                box-shadow: inset 0px 2px 2px 0px rgba(255, 255, 255, 0.17);
                border: 1px solid #257C9E;
            }
            .form-style-10 h1 > span{
                display: block;
                margin-top: 0px;
                font: 13px Arial, Helvetica, sans-serif;
            }
            .form-style-10 label{
                display: block;
                font: 15px Arial, Helvetica, sans-serif;
                color: #888;
                margin-bottom: 15px;
            }
            .form-style-10 input[type="text"],
            .form-style-10 input[type="date"],
            .form-style-10 input[type="datetime"],
            .form-style-10 input[type="email"],
            .form-style-10 input[type="number"],
            .form-style-10 input[type="search"],
            .form-style-10 input[type="time"],
            .form-style-10 input[type="url"],
            .form-style-10 input[type="password"],
            .form-style-10 textarea,
            .form-style-10 select {
                display: block;
                box-sizing: border-box;
                -webkit-box-sizing: border-box;
                -moz-box-sizing: border-box;
                width: 100%;
                padding: 8px;
                border-radius: 6px;
                -webkit-border-radius:6px;
                -moz-border-radius:6px;
                border: 2px solid #fff;
                box-shadow: inset 0px 1px 1px rgba(0, 0, 0, 0.33);
                -moz-box-shadow: inset 0px 1px 1px rgba(0, 0, 0, 0.33);
                -webkit-box-shadow: inset 0px 1px 1px rgba(0, 0, 0, 0.33);
            }

            .form-style-10 .section{
                font: normal 20px serif;
                color: #2A88AD;
                margin-bottom: 5px;
            }
            .form-style-10 .section span {
                background: #2A88AD;
                padding: 5px 10px 5px 10px;
                position: absolute;
                border-radius: 50%;
                -webkit-border-radius: 50%;
                -moz-border-radius: 50%;
                border: 4px solid #fff;
                font-size: 14px;
                margin-left: -45px;
                color: #fff;
                margin-top: -3px;
            }

            .form-style-10 input[type="submit"]{
                background: #2A88AD;
                padding: 8px 20px 8px 20px;
                margin: 0px 0px 0px 390px;
                border-radius: 5px;
                -webkit-border-radius: 5px;
                -moz-border-radius: 5px;
                color: #fff;
                text-shadow: 1px 1px 3px rgba(0, 0, 0, 0.12);
                font: normal 30px serif;
                -moz-box-shadow: inset 0px 2px 2px 0px rgba(255, 255, 255, 0.17);
                -webkit-box-shadow: inset 0px 2px 2px 0px rgba(255, 255, 255, 0.17);
                box-shadow: inset 0px 2px 2px 0px rgba(255, 255, 255, 0.17);
                border: 1px solid #257C9E;
                font-size: 15px;
            }

            .form-style-10 input[type="reset"] {
                background: #2A88AD;
                padding: 8px 20px 8px 20px;
                margin: 0px -10px 0px 20px;
                border-radius: 5px;
                -webkit-border-radius: 5px;
                -moz-border-radius: 5px;
                color: #fff;
                text-shadow: 1px 1px 3px rgba(0, 0, 0, 0.12);
                font: normal 30px serif;
                -moz-box-shadow: inset 0px 2px 2px 0px rgba(255, 255, 255, 0.17);
                -webkit-box-shadow: inset 0px 2px 2px 0px rgba(255, 255, 255, 0.17);
                box-shadow: inset 0px 2px 2px 0px rgba(255, 255, 255, 0.17);
                border: 1px solid #257C9E;
                font-size: 15px;
            }
            
            .form-style-10 input[type="button"] {
                background: #2A88AD;
                padding: 8px 20px 8px 20px;
                margin: 0px -10px 0px 20px;
                border-radius: 5px;
                -webkit-border-radius: 5px;
                -moz-border-radius: 5px;
                color: #fff;
                text-shadow: 1px 1px 3px rgba(0, 0, 0, 0.12);
                font: normal 30px serif;
                -moz-box-shadow: inset 0px 2px 2px 0px rgba(255, 255, 255, 0.17);
                -webkit-box-shadow: inset 0px 2px 2px 0px rgba(255, 255, 255, 0.17);
                box-shadow: inset 0px 2px 2px 0px rgba(255, 255, 255, 0.17);
                border: 1px solid #257C9E;
                font-size: 15px;
            }

            .form-style-10 input[type="button"]:hover,
            .form-style-10 input[type="reset"]:hover,
            .form-style-10 input[type="submit"]:hover{
                background: #2A6881;
                -moz-box-shadow: inset 0px 2px 2px 0px rgba(255, 255, 255, 0.28);
                -webkit-box-shadow: inset 0px 2px 2px 0px rgba(255, 255, 255, 0.28);
                box-shadow: inset 0px 2px 2px 0px rgba(255, 255, 255, 0.28);
            }
        </style>
    </body>
</html>

