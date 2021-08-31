/* 
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */


/* 
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
Ext.HeaderWin = function (config) {
    Ext.apply(this, {
//        frame: true,
        bodyStyle: 'padding:15px 5px 0;background-color: #FFFFFF;',
        autoHeight: true,
        border: false,
        items: [{
                //           title: 'Color properties',
                border: false,
                layout: 'border',
                width: 400,
                height: 150,
                items: [
                    this.cellcolorpicker = {
                        xtype: 'fieldset',
                        //                autoHeight:true,
                        height: 130,
                        title: 'Background color',
                        width: 170,
                        scope: this,
                        region: 'center',
                        renderTo: Ext.getBody(),
                        items: [
                            Ext.create('Ext.picker.Color', {
                                value: '', // initial selected color
                                renderTo: Ext.getBody(),
                                id: 'colorpickerbg',
                                isBackgroundColor: true,
                                handler: function (obj, rgb) {
                                }
                            })
                        ]
                    }]
            }, {
                xtype: 'tbspacer',
                height: 20
            }, this.textaligncellcombo = new Ext.form.field.ComboBox({
                fieldLabel: 'Alignments',
                store: Ext.alignStore,
                id: 'alignselectcombo',
                displayField: "name",
                valueField: 'position',
                queryMode: 'local'
            }),
            this.myDesignFieldEditorcell = new Ext.form.field.HtmlEditor({
                flex: 2,
                xtype: 'htmleditor',
                width: 350,
                id: 'myDesignFieldEditor2',
                height: 150,
                scope: this,
                style: 'background-color: white;',
                value: config.headerVal,
                margin: '10 0 30 5',
                enableLinks: false,
                enableLists: false,
                enableSourceEdit: false,
                enableAlignments: false
                        //                                hidden: true,
            })]
    }, config);
    if (config.cellparameters) {
        if (config.cellbgcolor != undefined && config.cellbgcolor != "") {
            //for setting the color of palette
            var digits = /(.*?)rgb\((\d+), (\d+), (\d+)\)/.exec(config.cellbgcolor);
            var red = parseInt(digits[2]);
            var green = parseInt(digits[3]);
            var blue = parseInt(digits[4]);
            //        var rgb = blue | (green << 8) | (red << 16);
            //        var getselectedcolor=digits[1] + rgb.toString(16);
            var getselectedcolor = rgbToHex(red, green, blue);
            this.cellcolorpicker.items[0].select(getselectedcolor.toUpperCase());
        }
        if (config.celltextalign != undefined && config.celltextalign != "") {
            //setting selected align properties
            this.textaligncellcombo.setValue(config.celltextalign);
        }

        //for setting selected htmleditor
        var htmleditorcellvalue = config.cellparameters;
        this.myDesignFieldEditorcell.setValue(htmleditorcellvalue);
    } else if (config.styleRecord) {
        if (config.styleRecord != undefined) {
            var cellcolor = Ext.getCmp('colorpickerbg');
            var headerobj = config.styleRecord;
            if (headerobj) {
                this.textaligncellcombo.setValue(headerobj.alignment);
                cellcolor.select(headerobj.backgroundcolor);
                this.myDesignFieldEditorcell.setValue(headerobj.changedlabel);
            }
        }
    }

    Ext.HeaderWin.superclass.constructor.call(this, config)
}

Ext.alignStore = Ext.create('Ext.data.Store', {
    fields: ['position', 'name'],
    data: [
//    {
//        "position":"top",
//        "name":"Top"
//    },

        {
            "position": "center",
            "name": "Center"
        },
        {
            "position": "left",
            "name": "Left"
        },
        {
            "position": "right",
            "name": "Right"
        },
    ]
});

Ext.proppanel = Ext.extend(Ext.HeaderWin, Ext.Panel, {
//    title: 'HeaderProperty',
//    closable:true,
    width: 450,
    //    autoHeight:true,
    height: 400
});
