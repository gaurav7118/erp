/* 
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
Ext.PropertiesBoxWin= function(config){
    Ext.apply(this,{
        frame: true,
        bodyStyle: 'padding:15px 5px 0;background-color: #FFFFFF;',
        autoHeight:true, 
        border: false,
        items:[{
            title: 'Select Style',
            border : false,
            autoHeight:true, 
            items:[Ext.transparentbutton={
                xtype: 'fieldset',
                defaultType: 'radiofield',
                defaults: {
                    flex: 1
                },
                border: false,
                layout: 'hbox',
                items: [
                {
                    boxLabel  : 'Transparent',
                    name      : 'size',
                    inputValue: 'transparent',
                    id        : 'radio1',
                    scope:this,
                    checked:true,
                    listeners:{
                        change : {
                            fn: function(){ 
                                var checktransparentItem=Ext.getCmp('radio1').getValue();
                                if(checktransparentItem==true){
                                    var checktransparentItem1=Ext.getCmp('radio2');
                                    checktransparentItem1.setValue(false);
                                    this.newvalue= Ext.getCmp('hidden_fieldID').getValue();  
                                    var div2 = document.getElementById(this.newvalue);
                                    div2.style.backgroundColor="";
                                    Ext.selectColorPalette.select("");
                                }
                            }
                        }
                    }
                            
                }, Ext.opaquebutton= {
                    boxLabel  : 'Opaque',
                    name      : 'size',
                    inputValue: 'opaque',
                    id        : 'radio2',
                    checked   :false,
                    scope:this,
                    listeners:{
                        change : { 
                            fn: function(){ 
                                var checkopaqueItem=Ext.getCmp('radio2').getValue();
                                if(checkopaqueItem==true){
                                    var checktransparentItem2=Ext.getCmp('radio1');
                                    checktransparentItem2.setValue(false);
                                    this.newvalue= Ext.getCmp('hidden_fieldID').getValue();                   
                                    this.div1 = document.getElementById(this.newvalue);
                                    this.div1.style.opacity="100%";
                                    this.div1.style.backgroundColor="white";
                                    Ext.selectColorPalette.select("#FFFFFF");
                                     
                                } 
                            }
                        }
                    }
                }]

            }]
        }, {   
            xtype:'fieldset',
            autoHeight:true,  
            title:'Choose color',
            anchor : '70%',
            items:[
            Ext.selectColorPalette = new Ext.ColorPalette({
                cls:'palette',
                value:"",
                id :'selectColor',
                scope:this,
                colors: ["FFFFFF","CC3333", "DD4477", "994499", "6633CC", "336699", "3366CC", "22AA99", "329262", "109618", "66AA00", "AAAA11", "D6AE00", "EE8800", "DD5511", "A87070", "8C6D8C", "627487", "7083A8", "5C8D87", "898951", "B08B59","000000","008B8B","00FFFF","4B0082","6B8E23","808080","87CEEB","87CEFA","ADFF2F"]
            })
            ]
        }],
        buttons: [{
            text: "Close",
            scope: this,
            handler: function(){
                             
                var selectedfield = Ext.getCmp(Ext.getCmp('hidden_fieldID').getValue());
                var PageDescription = Ext.getCmp('setfieldproperty');
                var myEditor = Ext.getCmp('myDesignFieldEditor');
                var bd = myEditor.getEditorBody();                               
                var html = bd.innerHTML;
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

                PageDescription.setValue(myEditor.getValue());          
                // var myEditor = Ext.getCmp('propertiesbox');
                this.close();
            }
        }]   
    },config);
    if(config.colorPanel){  
        var digits = /(.*?)rgb\((\d+), (\d+), (\d+)\)/.exec(config.colorPanel);
        var red = parseInt(digits[2]);
        var green = parseInt(digits[3]);
        var blue = parseInt(digits[4]);
        var rgb = blue | (green << 8) | (red << 16);
        var getselectedcolor=digits[1] + rgb.toString(16);
        Ext.selectColorPalette.select(getselectedcolor.toUpperCase());
        if(getselectedcolor=="ffffff"){
            Ext.transparentbutton.items[0].checked=true;
            Ext.opaquebutton.checked=false;
        }else{
            Ext.transparentbutton.items[0].checked=false;
            Ext.opaquebutton.checked=true;
        }
    }
   
    var colors=Ext.getCmp('selectColor');
    colors.on('select', function(palette, selColor){
        this.colorInd = palette.colors.indexOf(selColor);
        var str="#"+selColor;
        this.newvalue1= Ext.getCmp('hidden_fieldID').getValue(); 
        this.myEditor1 = Ext.getCmp('myDesignFieldEditor').getValue();
        var div1 = document.getElementById(this.newvalue1);
        //        div1.style.background=str;
        var checktransparentItem=Ext.getCmp('radio1').getValue();
        var checkopaqueItem=Ext.getCmp('radio2').getValue();
        if (checktransparentItem==true && str=='#FFFFFF'){
            this.value=str;
            div1.style.backgroundColor=str;
        }else if(checkopaqueItem==false){
            Ext.Msg.alert("Only white color is applicable for transparent");
            Ext.selectColorPalette.select("#FFFFFF");
        }
        if(checkopaqueItem==true && checktransparentItem==false){
            div1.style.backgroundColor=str;
            this.backgroundColor=str;
        }
    },this); 
    
    Ext.PropertiesBoxWin.superclass.constructor.call(this, config);
        
}
   
    
Ext.extend(Ext.PropertiesBoxWin, Ext.Window, {
    title: 'Draw Box Property',
    closable:true,
    width:450,
    autoHeight:true
});
