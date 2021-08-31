function getExtWatermarkWindow(config) {
    return new Ext.create('Ext.Watermark', {
        height: 607,
        width: 500,
        modal: true,
        resizable: false,
        pageMargin: config.pageMargin,
        pageMarginUnit: config.pageMarginUnit
    }).show();
}

Ext.define('Ext.Watermark', {
    id: 'extwatermakrwin',
    extend: 'Ext.window.Window',
    initComponent: function () {
        var me = this;
        if (pagelayoutproperty[2] != null && pagelayoutproperty[2] != undefined && pagelayoutproperty[2].watermarkProperties != null && pagelayoutproperty[2].watermarkProperties != undefined && Object.keys(pagelayoutproperty[2].watermarkProperties).length != 0) {
            me.watermarkProperties = Ext.clone(pagelayoutproperty[2].watermarkProperties);
        } else {
            me.watermarkProperties = {
                watermarkText: "",
                fontSize: 96,
                fontFamily: 'sans-serif',
                fontWeight: false,
                italicFont: false,
                opacity: 0.1,
                transform: 305
            };
        }
        me.createButtons();
        me.createComponents();
        me.callParent(arguments);
    },
    onRender: function () {
        var me = this;
        me.createCanvas(false);
        me.callParent(arguments);
    },
    createButtons: function () {
        var me = this;
        me.saveButton = Ext.create('Ext.button.Button', {
            text: 'Add',
            scope: me,
            handler: function (button, e, eOpts) {
                me.saveWatermarkProperties(button);
            }
        });

        me.closeButton = Ext.create('Ext.button.Button', {
            text: 'Close',
            scope: me,
            handler: function () {
                me.close();
            }
        });

        Ext.apply(me, {
            buttons: [me.saveButton, me.closeButton]
        })
    },
    createComponents: function () {
        var me = this;
        me.northPanel = Ext.create('Ext.panel.Panel', {
            region: 'north',
            height: 85,
            html: "<div style='float: left;'>\n\
                    <img src = ../../images/import.png style='height: 60px; width: 45px; margin: 4px;'/></div>\n\
                    <div style='float: left;'>\n\
                    <div style='font-size: 12px; font-weight: bold; margin-left: 30px;margin-top: 10px;'>Create Watermark</div>\n\
                    <div style='font-size: 10px; float: left;'><ul>\n\
                    <li><b>Text Watermark:</b> Provide a watermark text and other text related properties.</li>\n\
                    <li><b>Watermark Properties:</b> Provide a opacity and orientation to text watermark.</li>\n\
                    <li><b>Note:</b> Font size on preview page is 1/4<sup>th</sup> of actual size.</li>\n\
                    </ul></div></div>"
        });

        me.watermarkText = Ext.create('Ext.form.field.TextArea', {
            fieldLabel: 'Watermark Text',
            emptyText: 'Add watermark text here...',
            cls: 'watermarkFields',
            height: 30,
            value: me.watermarkProperties.watermarkText,
            listeners: {
                scope: this,
                change: function () {
                    this.createCanvas(false);
                }
            }
        });
        
        me.isBoldCheckbox = Ext.create('Ext.form.field.Checkbox', {
            fieldLabel: 'Bold',
            cls: 'watermarkFields',
            checked: me.watermarkProperties.fontWeight,
            style: {
                marginRight: '10px'
            },
            listeners: {
                scope: this,
                change: function () {
                    this.createCanvas(false);
                }
            }
        });

        me.fontSizeNumberField = Ext.create('Ext.form.field.Number', {
            fieldLabel: 'Font Size',
            cls: 'watermarkFields',
            value: me.watermarkProperties.fontSize,
            allowBlank: false,
            style: {
                marginLeft: '50px'
            },
            minValue: 1,
            step: 4,
            listeners: {
                scope: this,
                change: function () {
                    this.createCanvas(false);
                },
                blur: function (numberField, e, eOpts) {
                    if (!Ext.isNumber(parseInt(numberField.getValue()))) {
                        numberField.reset();
                    }
                }
            }
        });
        
        me.isItalicCheckbox = Ext.create('Ext.form.field.Checkbox', {
            fieldLabel: 'Italic',
            cls: 'watermarkFields',
            checked: me.watermarkProperties.italicFont,
            listeners: {
                scope: this,
                change: function () {
                    this.createCanvas(false);
                }
            }
        });

        var fontFamilyStore = Ext.create('Ext.data.Store', {
            fields: ['fontId', 'fontName'],
            data: [
                {"fontId": "sans-serif", "fontName": "Sans-Serif"},
                {"fontId": "arial", "fontName": "Arial"},
                {"fontId": "verdana", "fontName": "Verdana"},
                {"fontId": "times new roman", "fontName": "Times New Roman"},
                {"fontId": "tahoma", "fontName": "Tahoma"},
                {"fontId": "calibri", "fontName": "Calibri"},
                {"fontId": "courier new", "fontName": "Courier New"},
                {"fontId": "MICR Encoding", "fontName": "MICR"}
            ]
        });

        me.fontFamilyCombo = Ext.create('Ext.form.field.ComboBox', {
            fieldLabel: 'Font Family',
            store: fontFamilyStore,
            queryMode: 'local',
            displayField: 'fontName',
            valueField: 'fontId',
            cls: 'watermarkFields',
            allowBlank: false,
            forceSelection: true,
            style: {
                marginLeft: '50px'
            },
            width: 275,
            value: me.watermarkProperties.fontFamily,
            listeners: {
                scope: this,
                change: function () {
                    this.createCanvas(false);
                }
            }
        });

        

        me.textWatermarkFieldSet = Ext.create('Ext.form.FieldSet', {
            title: 'Text Watermark',
            layout: {
                type: 'table',
                columns: 2
            },
            style: {
                marginTop: '3px',
                marginRight: '5px',
                marginBottom: '3px',
                marginLeft: '5px'
            },
            items: [
                {
                    colspan: 2,
                    layout: 'fit',
                    border: false,
                    items: [me.watermarkText]
                },
                {
                    layout: 'fit',
                    border: false,
                    items: [me.isBoldCheckbox]
                },
                {
                    layout: 'fit',
                    border: false,
                    items: [me.fontSizeNumberField]
                },
                {
                    layout: 'fit',
                    border: false,
                    items: [me.isItalicCheckbox]
                },
                {
                    layout: 'fit',
                    border: false,
                    items: [me.fontFamilyCombo]
                }
            ]
        });

        me.imageUploadButton = Ext.create('Ext.form.field.File', {
            fieldLabel: 'Upload Image',
            buttonText: 'browse...',
            allowBlank: false,
            listeners: {
                scope: this,
                change: this.onWatertextImageUpload
            }
        });

        me.imageWatermarkFieldSet = Ext.create('Ext.form.FieldSet', {
            title: 'Image Watermark',
            layout: 'form',
            style: {
                marginTop: '3px',
                marginRight: '5px',
                marginBottom: '3px',
                marginLeft: '5px'
            },
            height: 55,
            items: [me.imageUploadButton],
            hidden: true
        });

        me.opacityNumberField = Ext.create('Ext.form.field.Number', {
            fieldLabel: 'Opacity',
            cls: 'watermarkFields',
            style: {
                marginRight: '10px'
            },
            minValue: 0,
            maxValue: 1,
            step: 0.01,
            value: me.watermarkProperties.opacity,
            allowDecimals: true,
            allowBlank: false,
            width: 222,
            listeners: {
                scope: this,
                change: function () {
                    this.createCanvas(false);
                },
                blur: function (numberField, e, eOpts) {
                    if (!Ext.isNumber(parseInt(numberField.getValue()))) {
                        numberField.reset();
                    }
                }
            }
        });

        me.orientationSpinnerField = Ext.create('Ext.form.field.Spinner', {
            fieldLabel: 'Orientation',
            cls: 'watermarkFields',
            minValue: 0,
            maxValue: 359,
            value: me.watermarkProperties.transform,
            step: 1,
            allowBlank: false,
            width: 222,
            onSpinUp: function () {
                var me = this;
                if (!me.readOnly) {
                    var val = Ext.isNumber(parseInt(me.getValue())) ? parseInt(me.getValue()) : me.minValue;
                    if (val == me.maxValue) {
                        me.setValue(me.minValue);
                    } else {
                        me.setValue(val + me.step);
                    }
                }
            },
            onSpinDown: function () {
                var me = this;
                if (!me.readOnly) {
                    var val = Ext.isNumber(parseInt(me.getValue())) ? parseInt(me.getValue()) : me.maxValue;
                    if (val == me.minValue) {
                        me.setValue(me.maxValue);
                    } else {
                        me.setValue(val - me.step);
                    }
                }
            },
            listeners: {
                scope: this,
                change: function () {
                    this.createCanvas(false);
                },
                blur: function (numberField, e, eOpts) {
                    if (!Ext.isNumber(parseInt(numberField.getValue()))) {
                        numberField.reset();
                    }
                }
            }
        });

//        me.RepeatCheckbox = Ext.create('Ext.form.field.Checkbox', {
//            fieldLabel: 'Repeat',
//            cls: 'watermarkFields',
//            listeners: {
//                scope: this,
//                change: function () {
//                    this.createCanvas(false);
//                }
//            }
//        });
//        
//        var positionStore = Ext.create('Ext.data.Store', {
//            fields: ['posId', 'posName'],
//            data: [
//                {"posId": "center", "posName": "Center"},
//                {"posId": "leftTop", "posName": "Left-Top"},
//                {"posId": "rightTop", "posName": "Right-Top"},
//                {"posId": "rightBottom", "posName": "Right-Bottom"},
//                {"posId": "leftBottom", "posName": "Left-Bottom"}
//            ]
//        });
        
//        me.positionCombo = Ext.create('Ext.form.field.ComboBox', {
//            fieldLabel: 'Position',
//            store: positionStore,
//            queryMode: 'local',
//            displayField: 'posName',
//            valueField: 'posId',
//            cls: 'watermarkFields',
//            value: 'center',
//            listeners: {
//                scope: this,
//                change: function () {
//                    this.createCanvas(false);
//                }
//            }
//        });

        me.watermarkPropertiesFieldSet = Ext.create('Ext.form.FieldSet', {
            title: 'Watermark Properties',
            layout: {
                type: 'table',
                columns: 2
            },
            style: {
                marginTop: '3px',
                marginRight: '5px',
                marginBottom: '3px',
                marginLeft: '5px'
            },
            items: [
                {
                    layout: 'fit',
                    border: false,
                    items: [me.opacityNumberField]
                },
                {
                    layout: 'fit',
                    border: false,
                    items: [me.orientationSpinnerField]
                },
//                {
//                    layout: 'fit',
//                    border: false,
//                    items: [me.RepeatCheckbox]
//                },
//                {
//                    layout: 'fit',
//                    border: false,
//                    items: [me.positionCombo]
//                }
            ]
        });

        me.centerPanel = Ext.create('Ext.panel.Panel', {
            region: 'center',
            items: [me.textWatermarkFieldSet, me.imageWatermarkFieldSet, me.watermarkPropertiesFieldSet]
        });

        me.southPanel = Ext.create('Ext.panel.Panel', {
            region: 'south',
            height: 295,
            bodyStyle: {
                backgroundColor: '#7A7A7A',
                textAlign: 'center'
            },
            html: "<div id = 'watermarkPreviewDiv' class = 'previewCanvas'></div>"
        });

        Ext.apply(me, {
            layout: 'border',
            items: [
                me.northPanel,
                me.centerPanel,
                me.southPanel
            ]
        })
    },
    onWatertextImageUpload: function (uploadButton, value, eOpts) {
        
    },
    mm2px: function (mm) {
        var px_per_mm = getDPI() / 25.4;
        return mm * px_per_mm;
    },
    cm2px: function (cm) {
        var px_per_cm = getDPI() / 2.54;
        return cm * px_per_cm;
    },
    in2px: function (inch) {
        var px_per_in = getDPI();
        return inch * px_per_in;
    },
    getPageMarginsConvertedToPixel: function () {
        var me = this;
        var pageMarginUnit = me.pageMarginUnit;
        var pageMarginInPixel = undefined;
        switch (pageMarginUnit) {
            case 'mm':
                pageMarginInPixel = {
                    top: Math.round(me.mm2px(me.pageMargin.top) / 4),
                    left: Math.round(me.mm2px(me.pageMargin.left) / 4),
                    bottom: Math.round(me.mm2px(me.pageMargin.bottom) / 4),
                    right: Math.round(me.mm2px(me.pageMargin.right) / 4)
                }
                break;
            case 'cm':
                pageMarginInPixel = {
                    top: Math.round(me.cm2px(me.pageMargin.top) / 4),
                    left: Math.round(me.cm2px(me.pageMargin.left) / 4),
                    bottom: Math.round(me.cm2px(me.pageMargin.bottom) / 4),
                    right: Math.round(me.cm2px(me.pageMargin.right) / 4)
                }
                break;
            case 'in':
                pageMarginInPixel = {
                    top: Math.round(me.in2px(me.pageMargin.top) / 4),
                    left: Math.round(me.in2px(me.pageMargin.left) / 4),
                    bottom: Math.round(me.in2px(me.pageMargin.bottom) / 4),
                    right: Math.round(me.in2px(me.pageMargin.right) / 4)
                }
                break;
            default:
                pageMarginInPixel = {
                    top: Math.round(me.pageMargin.top / 4),
                    left: Math.round(me.pageMargin.left / 4),
                    bottom: Math.round(me.pageMargin.bottom / 4),
                    right: Math.round(me.pageMargin.right / 4)
                }
                break;
        }
        return pageMarginInPixel;
    },
    getCanvasElement: function () {
        var me = this;
        var canvas = undefined;
        if (!document.querySelector('canvas')) {
            var watermarkPreviewDivWidth = Math.round((8.27 * getDPI()) / 4);
            var watermarkPreviewDivHeight = Math.round((11.69 * getDPI()) / 4);
            var watermarkPreviewDiv = document.getElementById('watermarkPreviewDiv');
            watermarkPreviewDiv.style.width = watermarkPreviewDivWidth + "px";
            watermarkPreviewDiv.style.height = watermarkPreviewDivHeight + "px";
            canvas = document.createElement('canvas');
            var PageMarginsInPixel = me.getPageMarginsConvertedToPixel();
            if (PageMarginsInPixel) {
                canvas.width = (watermarkPreviewDivWidth - (PageMarginsInPixel.left + PageMarginsInPixel.right));
                canvas.height = (watermarkPreviewDivHeight - (PageMarginsInPixel.top + PageMarginsInPixel.bottom));
                var cssText = 'border:1px solid rgba(0, 0, 0, 0.2);';
                cssText += 'margin-top: ' + PageMarginsInPixel.top + 'px;';
                cssText += 'margin-left: ' + PageMarginsInPixel.left + 'px;';
                cssText += 'margin-bottom: ' + PageMarginsInPixel.bottom + 'px;';
                cssText += 'margin-right: ' + PageMarginsInPixel.right + 'px;';
                canvas.style.cssText = cssText;
            }
            watermarkPreviewDiv.appendChild(canvas);
        } else {
            canvas = document.querySelector('canvas');
        }
        return canvas;
    },
    createCanvas: function () {
        var me = this;
        var canvas = me.getCanvasElement();
        if (canvas) {
            var watermarkText = me.watermarkText.getValue();
            var ctx = canvas.getContext('2d');
            if (watermarkText.length > 0) {
                var watermarkProperties = {
                    watermarkText: watermarkText,
                    fontSize: me.fontSizeNumberField.getValue() / 4,
                    fontFamily: me.fontFamilyCombo.getValue(),
                    fontWeight: me.isBoldCheckbox.getValue(),
                    italicFont: me.isItalicCheckbox.getValue(),
                    opacity: me.opacityNumberField.getValue(),
                    transform: me.orientationSpinnerField.getValue()
                };
                
                drawCanvas(canvas, watermarkProperties);
            } else {
                ctx.clearRect(0, 0, canvas.width, canvas.height);
            }
        }
        return canvas;
    },
    saveWatermarkProperties: function (button) {
        var me = this;
        var watermarkText = me.watermarkText.getValue();
        if (watermarkText.length > 0) {
            var watermarkProperties = {
                watermarkText: watermarkText,
                fontSize: Ext.isNumber(me.fontSizeNumberField.getValue()) ? me.fontSizeNumberField.getValue() : 96,
                fontFamily: me.fontFamilyCombo.getValue(),
                fontWeight: me.isBoldCheckbox.getValue(),
                italicFont: me.isItalicCheckbox.getValue(),
                opacity: Ext.isNumber(me.opacityNumberField.getValue()) ? me.opacityNumberField.getValue() : 0.1,
                transform: Ext.isNumber(parseInt(me.orientationSpinnerField.getValue())) ? parseInt(me.orientationSpinnerField.getValue()) : 305
            };
            
            pagelayoutproperty[2] = {watermarkProperties: watermarkProperties}
        } else {
            pagelayoutproperty[2] = {};
        }
        me.close();
    }
});

function addWatermarkImageToPage(pageHeight, pageWidth, pageMargin) {
    var watermarkObject = undefined;
    var height = 1103;
    if ($('#watermark').length > 0) {
        watermarkObject = JSON.parse($('#watermark').attr('properties'));
        if (watermarkObject && watermarkObject.watermarkProperties && Object.keys(watermarkObject.watermarkProperties).length != 0) {
            if (watermarkObject.watermarkProperties.watermarkText.length > 0) {
                var canvas = document.createElement('canvas');
                if (pageMargin) {
                    height = (pageHeight - (pageMargin.top + pageMargin.bottom));
                    canvas.width = pageWidth;
                    canvas.height = height;
                }

                drawCanvas(canvas, watermarkObject.watermarkProperties);

                $('#watermark').css('width', pageWidth);
                $('#watermark').css('height', height);
                $('#watermark').css('background-image', 'url(' + canvas.toDataURL() + ')');
            }
        }
    }
}

function drawCanvas(canvas, watermarkProperties) {
    if (Object.keys(watermarkProperties).length != 0 && watermarkProperties.watermarkText.length > 0) {
        var ctx = canvas.getContext('2d');
        var watermarkText = watermarkProperties.watermarkText;
        var fontSize = watermarkProperties.fontSize;
        var fontFamily = watermarkProperties.fontFamily;
        var fontWeight = watermarkProperties.fontWeight ? 'bold' : 'normal';
        var italicFont = watermarkProperties.italicFont ? 'italic' : 'normal';
        var opacity = watermarkProperties.opacity;
        var transform = watermarkProperties.transform;

        ctx.clearRect(0, 0, canvas.width, canvas.height);
        ctx.save();
        ctx.font = italicFont + ' ' + fontWeight + ' ' + fontSize + 'px' + ' ' + fontFamily;
        ctx.globalAlpha = opacity;
        ctx.textAlign = 'center';
        ctx.textBaseline = 'top';

        var numberOfLines = watermarkText.split('\n');
        var heightToAdjust = (fontSize * numberOfLines.length) / 2;
        var rotationAngleInRadian = (Math.PI / 180) * parseInt(transform);
        ctx.translate((canvas.width / 2), (canvas.height / 2));
        ctx.rotate(rotationAngleInRadian);
        for (var i = 0; i < numberOfLines.length; i++) {
            ctx.fillText(numberOfLines[i], 0, ((i * fontSize) - heightToAdjust));
        }
        ctx.restore();
    }
}
