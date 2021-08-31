Ext.define("Ext.ux.TreePicker",{extend:"Ext.form.field.Picker",xtype:"treepicker",uses:["Ext.tree.Panel"],triggerCls:Ext.baseCSSPrefix+"form-arrow-trigger",config:{store:null,displayField:null,columns:null,selectOnTab:true,maxPickerHeight:300,minPickerHeight:100},editable:false,initComponent:function(){var A=this;A.callParent(arguments);A.mon(A.store,{scope:A,load:A.onLoad,update:A.onUpdate})},createPicker:function(){var C=this,B=new Ext.tree.Panel({baseCls:Ext.baseCSSPrefix+"boundlist",shrinkWrapDock:2,store:C.store,floating:true,displayField:C.displayField,columns:C.columns,minHeight:C.minPickerHeight,maxHeight:C.maxPickerHeight,manageHeight:false,shadow:false,listeners:{scope:C,itemclick:C.onItemClick,itemkeydown:C.onPickerKeyDown}}),A=B.getView();if(Ext.isIE9&&Ext.isStrict){A.on({scope:C,highlightitem:C.repaintPickerView,unhighlightitem:C.repaintPickerView,afteritemexpand:C.repaintPickerView,afteritemcollapse:C.repaintPickerView})}return B},repaintPickerView:function(){var A=this.picker.getView().getEl().dom.style;A.display=A.display},onItemClick:function(B,A,C,E,D){this.selectItem(A)},onPickerKeyDown:function(B,A,E,C,F){var D=F.getKey();if(D===F.ENTER||(D===F.TAB&&this.selectOnTab)){this.selectItem(A)}},selectItem:function(A){var B=this;B.setValue(A.getId());B.fireEvent("select",B,A);B.collapse()},onExpand:function(){var B=this.picker,A=B.store,D=this.value,C;if(D){C=A.getNodeById(D)}if(!C){C=A.getRoot()}B.ensureVisible(C,{select:true,focus:true})},setValue:function(C){var B=this,A;B.value=C;if(B.store.loading){return B}A=C?B.store.getNodeById(C):B.store.getRoot();if(C===undefined){A=B.store.getRoot();B.value=A.getId()}else{A=B.store.getNodeById(C)}B.setRawValue(A?A.get(B.displayField):"");return B},getSubmitValue:function(){return this.value},getValue:function(){return this.value},onLoad:function(){var A=this.value;if(A){this.setValue(A)}},onUpdate:function(A,E,B,C){var D=this.displayField;if(B==="edit"&&C&&Ext.Array.contains(C,D)&&this.value===E.getId()){this.setRawValue(E.get(D))}}})