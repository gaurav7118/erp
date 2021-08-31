/* 
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
Ext.PropertyWin= function(config){
    Ext.apply(this,{           
//        region:'center',
//        width:325,
//        height: 350, 
//        layout : 'border',
        title: 'Field/Text Property',        
        scope:this,
        //            html : "<div style='padding:10px;font-weight: bold'>Select field to delete</div>",
        items : [{
            xtype: 'fieldcontainer',
//            width: 400,
            layout: {
                type: 'column'
            },
            margin: '10 0 30 5',
            fieldLabel: '<b>Selected Field</b>',
            defaults: {
                hideLabel: true
            },
//            ,
            items: [{
                 xtype: 'displayfield',
                 value: '-',
                 itemId: 'displayfield1',
                 id: 'displayfield1',
                 width: 200
            }]
        },
        {

            xtype: 'button',
            text: 'Apply Changes',
            id : 'editformattingbtn1',
            margin: '10 0 30 5',
            scope: this,
            hidden:config.selectedFieldID==Ext.fieldID.insertField?true:(config.selectedFieldID==Ext.fieldID.insertText?true:false),
            handler: function(button) {
                if(Ext.getCmp('hidden_fieldType').getValue()=='5') {
                    var selectedfield = Ext.getCmp(Ext.getCmp('hidden_fieldID').getValue());
                    openProdWindowAndSetConfig(selectedfield, this.designerPanel.id);
                } 
                else if(isValidFieldSelected() && Ext.getCmp('hidden_allowformatting').getValue()=='true') {
                    var myView = button.up('panel');
                    var selectedfield = Ext.getCmp(Ext.getCmp('hidden_fieldID').getValue());
                    var ItemDescription = Ext.getCmp('displayfield1');
                    var myEditor = Ext.getCmp('myDesignFieldEditor1');
                    var bd = myEditor.getEditorBody();
                    var html = bd.innerHTML;
                    var wrapper= document.createElement('div');
                    wrapper.innerHTML= html;
                    ItemDescription.setValue(myEditor.getValue());
                }
            }
        },{
            flex: 1,
            xtype: 'htmleditor',
            anchor:'80%',
            id: 'myDesignFieldEditor1',
            height: 150,
            style: 'background-color: white;',
            value: Ext.getCmp('myDesignFieldEditor').getValue(),
            margin: '10 0 30 5',
            enableLinks : false,
            enableLists : false,
            enableSourceEdit : false,
            enableAlignments : false,
            hidden:(config.selectedFieldID==Ext.fieldID.insertField||config.selectedFieldID==Ext.fieldID.insertText)?true:false
        //                                hidden: true,
        },
        this.colorfieldset={
            xtype:'fieldset',
            checkboxToggle:true,
            title: 'Apply Border Color',
            autoHeight:true,
            id:'propertywindowselectfieldapplycolor', 
            disabled:(config.selectedFieldID==Ext.fieldID.insertField||config.selectedFieldID==Ext.fieldID.insertText)?false:true,
            collapsed: true,
            width:150,
            autoHeight:true,
            fieldDefaults: {
                labelAlign: 'left',
                labelWidth: 105
            },
            items:[
            this.propertywindowselectColorPalette = new Ext.ColorPalette({
                cls:'palette',
                value:(config.selectcolor!="" && config.selectcolor!=undefined &&  config.selectcolor!="B5B8C8")?config.selectcolor:"",
                id :'propertywindowfieldbordercolor',
                scope:this,
                height:70,
                colors: ["FFFFFF","CC3333", "DD4477", "994499", "6633CC", "336699", "3366CC", "22AA99", "329262", "109618", "66AA00", "AAAA11", "D6AE00", "EE8800", "DD5511", "A87070", "8C6D8C", "627487", "7083A8", "5C8D87", "898951", "B08B59","000000","008B8B","00FFFF","4B0082","6B8E23","808080","87CEEB","87CEFA","ADFF2F"]
            })]
        }
        ],
        buttons : [{
            text: 'Update',
            scope: this,
            handler: function() {
                if(Ext.getCmp('hidden_fieldType').getValue()=='5') {
                    var selectedfield = Ext.getCmp(Ext.getCmp('hidden_fieldID').getValue());
                    openProdWindowAndSetConfig(selectedfield, this.designerPanel.id);
                }else if(isValidFieldSelected() && Ext.getCmp('hidden_allowformatting').getValue()=='true') {
//                    var myView = button.up('panel');
                    var selectedfield = Ext.getCmp(Ext.getCmp('hidden_fieldID').getValue()); 
                     var myEditor = Ext.getCmp('myDesignFieldEditor1');
                    var bd = myEditor.getEditorBody();
                    var html = bd.innerHTML;
                        var checkpropertywindow= Ext.getCmp('propertywindowselectfieldapplycolor');
                        if(checkpropertywindow.collapsed==false){
                            var propertywindowselectfieldapplycolor = Ext.getCmp('propertywindowfieldbordercolor');
                            var selectedcolor="#"+propertywindowselectfieldapplycolor.getValue();
                            if(selectedcolor!=""||selectedcolor!=undefined){
                                var ss=Ext.getCmp('hidden_fieldID').getValue();
                                var divisionselectedfield = document.getElementById(ss);
                                divisionselectedfield.style.borderColor=selectedcolor;
                                selectedfield.style.borderColor=selectedcolor;
                            }
                        }else{
                            var ss=Ext.getCmp('hidden_fieldID').getValue();
                            var divisionselectedfield = document.getElementById(ss);
                            divisionselectedfield.style.borderColor='#B5B8C8';
                            selectedfield.style.borderColor='#B5B8C8';
                        }
                    var firstChild = selectedfield.el.dom.firstChild;
                    var firstChildAttributes = firstChild.attributes;
                    var nexSibling = firstChild.nextSibling;
                    selectedfield.el.dom.removeChild(firstChild);
                    var wrapper= document.createElement('div');
                    wrapper.innerHTML= html;
                    if(firstChildAttributes) {
                        for(var cnt=0; cnt<firstChildAttributes.length; cnt++) {
                            wrapper.setAttribute(firstChildAttributes[cnt]['name'], firstChildAttributes[cnt]['value']);
                        }
                    }
                    selectedfield.el.dom.insertBefore(wrapper, nexSibling);                   
                }
                this.close();
                this.destroy();
            }
        },{
            text: 'Cancel',
            scope: this,
            handler:function() {
                this.close();
                this.destroy();
            }
        }]
    },config);
    if((config.selectedFieldID==Ext.fieldID.insertField||config.selectedFieldID==Ext.fieldID.insertText) && config.selectcolor!="B5B8C8"){
        this.colorfieldset.collapsed=false;
    }
    Ext.PropertyWin.superclass.constructor.call(this, config);
}

Ext.extend(Ext.PropertyWin, Ext.Window, {
    title: 'Property',
    closable:true,
    width:350,
    height:250,
//    autoWidth:true,
    autoHeight:true,
    draggable:true,
    resizable:true,
    autoScroll:true,
//    id : 'propertywin',                   
//    plain:true,
    bodyStyle: 'background-color: #FFFFFF;padding:5px 5px 0;',
    scope:this,
    modal:true,
    onShow :function(config) {
        Ext.getCmp('displayfield1').setValue(this.fieldName);
        Ext.PropertyWin.superclass.onRender.call(this, config);
    }
})
    

